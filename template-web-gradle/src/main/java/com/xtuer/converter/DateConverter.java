package com.xtuer.converter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 把日期字符串转换为 Date 对象。
 */
public class DateConverter implements Converter<String, Date> {
    private static Logger logger = LoggerFactory.getLogger(DateConverter.class);

    /**
     * 把日期字符串转换为 Date 对象，接收两种日期格式: yyyy-MM-dd 或者 yyyy-MM-dd HH:mm:ss。
     * 如果日期的格式不对，则返回 null。
     *
     * @param source 字符串格式的日期
     * @return 返回日期 Date 的对象，如果日期的格式不对，则返回 null。
     */
    @Override
    public Date convert(String source) {
        String pattern = source.length()==10 ? "yyyy-MM-dd" : "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(pattern);

        try {
            return format.parse(source);
        } catch (ParseException ex) {
            logger.warn(ExceptionUtils.getStackTrace(ex));
        }

        return null;
    }
}
