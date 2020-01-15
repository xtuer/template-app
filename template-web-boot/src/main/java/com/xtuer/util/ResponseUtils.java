package com.xtuer.util;

import com.xtuer.bean.Mime;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HttpServletResponse 响应访问文件的工具类，支持普通访问，同时支持 range。
 *
 * 参考: How to Implement HTTP byte-range requests in Spring MVC
 * 网址: https://stackoverflow.com/questions/28427339/how-to-implement-http-byte-range-requests-in-spring-mvc
 */
@Slf4j
public final class ResponseUtils {
    private static final int DEFAULT_BUFFER_SIZE   = 2097152; // bytes = 2MB
    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

    /**
     * 读取文件到 response
     *
     * @param path     // 文件的路径
     * @param filename // 文件名，因为文件的路径中的文件名是编码过的，而真实的文件名保存在数据库，所以 path 中的文件名很可能不是真正的文件名
     * @param request  // HttpServletRequest 对象
     * @param response // HttpServletResponse 对象
     * @throws IOException 访问文件发生异常时抛出
     */
    public static void readFileToResponse(String path, String filename, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. 如果文件不存在则返回 404 页面
        // 2. 如果 header 中有 Range，则校验和处理 Range
        //    2.1 Range 头的格式为 "bytes=n-n,n-n,n-n...". 如果不匹配，则返回 416
        //    2.2 处理 If-Range 头
        //    2.3 如果没有有效的 If-Range 头, 则处理 Range 中的每一部分，使用逗号分隔
        // 3. 设置响应头
        //    3.1 使用文件名获取 content type 和 content disposition
        //    3.2 初始化 response，设置响应头
        // 4. 根据 ranges 读取文件到 response
        //    4.1 ranges 为空或者只有一个元素并为 fullRange 时，则读取整个文件 (fullRange 为处理 If-Range 头得到的)
        //    4.2 ranges 只有一个元素并不为 fullRange 时，读取文件的部分，范围由 ranges.get(0) 指定
        //    4.3 ranges 有多个元素时，读取文件的多个部分，范围由 ranges 指定

        // [1] 如果文件不存在则返回 404 页面
        if (!Files.exists(Paths.get(path))) {
            log.warn("文件 {} 不存在", path);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        filename = StringUtils.isBlank(filename) ? FilenameUtils.getName(path) : filename; // 传入的文件名为空时取 path 中的文件名
        long        length    = Files.size(Paths.get(path));      // 文件的长度
        Range       fullRange = new Range(0, length - 1, length); // 整个文件的 range
        String      range     = request.getHeader("Range");
        List<Range> ranges    = new ArrayList<>();

        // [2] 如果 header 中有 Range，则校验和处理 Range
        if (range != null) {
            // [2.1] Range 头的格式为 "bytes=n-n,n-n,n-n...". 如果不匹配，则返回 416
            //       Safari: Range: bytes=0-1
            //               Range: bytes=0-17611749
            //       Chrome: Range: bytes=0-
            //               Range: bytes=458752-
            if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
                response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return;
            }

            // [2.2] 处理 If-Range 头
            String ifRange = request.getHeader("If-Range");
            if (ifRange != null && !ifRange.equals(FilenameUtils.getName(path))) {
                try {
                    long ifRangeTime = request.getDateHeader("If-Range"); // Throws IAE if invalid.
                    if (ifRangeTime != -1) {
                        ranges.add(fullRange);
                    }
                } catch (IllegalArgumentException ignore) {
                    ranges.add(fullRange);
                }
            }

            // [2.3] 如果没有有效的 If-Range 头, 则处理 Range 中的每一部分，使用逗号分隔
            if (ranges.isEmpty()) {
                for (String part : range.substring(6).split(",")) {
                    // Assuming a file with length of 100, the following examples returns bytes at:
                    // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).

                    // Chrome always starts its first video request with the following: Range: bytes=0-
                    // Safari 的第一个请求为 Range: bytes=0-1
                    // 参考: https://stackoverflow.com/questions/3303029/http-range-header

                    if ("0-".equals(part)) {
                        ranges.add(new Range(0, 1, length));
                    } else {
                        long start = Range.sublong(part, 0, part.indexOf("-"));
                        long end   = Range.sublong(part, part.indexOf("-") + 1, part.length());

                        if (start == -1) {
                            start = length - end;
                            end = length - 1;
                        } else if (end == -1 || end > length - 1) {
                            end = length - 1;
                        }

                        // Check if Range is syntactically valid. If not, then return 416.
                        if (start > end) {
                            response.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                            response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                            return;
                        }

                        // Add range.
                        ranges.add(new Range(start, end, length));
                    }
                }
            }

            // Range 调试信息
            log.debug("Range: {}", range);
            log.debug(ranges.toString());
        }

        // [3] 设置响应头
        // [3.1] 使用文件名获取 content type 和 content disposition
        String contentType = Mime.getContentType(filename);
        String disposition = "inline";
        if (!contentType.startsWith("image")) {
            // Expect for images, determine content disposition. If content type is supported by
            // the browser, then set to inline, else attachment which will pop a 'save as' dialogue.
            String accept = request.getHeader("Accept");
            disposition = (accept != null && HttpUtils.accepts(accept, contentType)) ? "inline" : "attachment";
        }

        filename = new String(filename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1); // 解决文件名乱码问题

        // [3.2] 初始化 response，设置响应头
        response.reset(); // Initialize response.
        response.setBufferSize(DEFAULT_BUFFER_SIZE);
        response.setHeader("Content-Type", contentType);
        response.setHeader("Content-Disposition", disposition + ";filename=\"" + filename + "\"");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("ETag", filename);

        // [4] 根据 ranges 读取文件到 response
        try (RandomAccessFile input = new RandomAccessFile(path, "r"); ServletOutputStream output = response.getOutputStream()) {
            if (ranges.isEmpty() || ranges.get(0) == fullRange) {
                // [4.1] ranges 为空或者只有一个元素并为 fullRange 时，则读取整个文件 (fullRange 为处理 If-Range 头得到的)
                log.debug("返回整个文件: {}", path);

                response.setHeader("Content-Range", "bytes " + fullRange.start + "-" + fullRange.end + "/" + fullRange.total);
                response.setHeader("Content-Length", String.valueOf(fullRange.length));
                Range.copy(input, output, length, fullRange.start, fullRange.length);
            } else if (ranges.size() == 1) {
                // [4.2] ranges 只有一个元素并不为 fullRange 时，读取文件的部分，范围由 ranges.get(0) 指定
                Range r = ranges.get(0);
                log.debug("返回文件的一个部分 : from ({}) to ({}), Path: {}", r.start, r.end, path);

                response.setHeader("Content-Range", "bytes " + r.start + "-" + r.end + "/" + r.total); // Content-Range: bytes 0-17611749/17611750
                response.setHeader("Content-Length", String.valueOf(r.length));
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.
                Range.copy(input, output, length, r.start, r.length); // Copy single part range.
            } else {
                // [4.3] ranges 有多个元素时，读取文件的多个部分，范围由 ranges 指定
                response.setContentType("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                // Copy multi part range.
                for (Range r : ranges) {
                    log.debug("返回文件的多个部分: from ({}) to ({}), Path: {}", r.start, r.end, path);

                    // Add multipart boundary and header fields for every range.
                    output.println();
                    output.println("--" + MULTIPART_BOUNDARY);
                    output.println("Content-Type: " + contentType);
                    output.println("Content-Range: bytes " + r.start + "-" + r.end + "/" + r.total);
                    Range.copy(input, output, length, r.start, r.length); // Copy single part range of multi part range.
                }

                // End with multipart boundary.
                output.println();
                output.println("--" + MULTIPART_BOUNDARY + "--");
            }
        }
    }

    private static class Range {
        long start;  // 开始位置
        long end;    // 结束位置
        long length; // 读取长度
        long total;  // 总的长度

        /**
         * Construct a byte range.
         *
         * @param start Start of the byte range.
         * @param end   End of the byte range.
         * @param total Total length of the byte source.
         */
        public Range(long start, long end, long total) {
            this.start  = start;
            this.end    = end;
            this.length = end - start + 1; // 从 0 开始，所以长度需要加 1
            this.total  = total;
        }

        @Override
        public String toString() {
            return String.format("Range{ start=%d, end=%d, length=%d, total=%d }", start, end, length, total);
        }

        /**
         * 把指定范围内字符串转换为数字
         *
         * @param text       // 字符串
         * @param beginIndex // 开始下标
         * @param endIndex   // 结束下标
         * @return 返回数字，如果范围无效则返回 -1
         */
        public static long sublong(String text, int beginIndex, int endIndex) {
            String substring = text.substring(beginIndex, endIndex);
            return (substring.length() > 0) ? Long.parseLong(substring) : -1;
        }

        private static void copy(RandomAccessFile input, OutputStream output, long inputSize, long start, long length) throws IOException {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int read;

            if (inputSize == length) {
                // Write full range.
                while ((read = input.read(buffer)) > 0) {
                    output.write(buffer, 0, read);
                    output.flush();
                }
            } else {
                input.seek(start);
                long toRead = length;

                while ((read = input.read(buffer)) > 0) {
                    if ((toRead -= read) > 0) {
                        output.write(buffer, 0, read);
                        output.flush();
                    } else {
                        output.write(buffer, 0, (int) toRead + read);
                        output.flush();
                        break;
                    }
                }
            }
        }
    }

    private static class HttpUtils {
        /**
         * Returns true if the given accept header accepts the given value.
         *
         * @param acceptHeader The accept header.
         * @param toAccept The value to be accepted.
         * @return True if the given accept header accepts the given value.
         */
        public static boolean accepts(String acceptHeader, String toAccept) {
            String[] acceptValues = acceptHeader.split("\\s*([,;])\\s*");
            Arrays.sort(acceptValues);

            return Arrays.binarySearch(acceptValues, toAccept) > -1
                    || Arrays.binarySearch(acceptValues, toAccept.replaceAll("/.*$", "/*")) > -1
                    || Arrays.binarySearch(acceptValues, "*/*") > -1;
        }

        /**
         * Returns true if the given match header matches the given value.
         *
         * @param matchHeader The match header.
         * @param toMatch The value to be matched.
         * @return True if the given match header matches the given value.
         */
        public static boolean matches(String matchHeader, String toMatch) {
            String[] matchValues = matchHeader.split("\\s*,\\s*");
            Arrays.sort(matchValues);
            return Arrays.binarySearch(matchValues, toMatch) > -1
                    || Arrays.binarySearch(matchValues, "*") > -1;
        }
    }
}
