import edu.service.FileConvertService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 测试 FFmpeg
 */
@RunWith(SpringRunner.class)
@ContextConfiguration({"classpath:config/application.xml"})
public class FFmpegTest {
    @Autowired
    private FileConvertService convertService;

    @Test
    public void convertOnWin() throws Exception {
        /* ffmpegService.convertFileToMp4(new File("C:/ffmpeg/input/test.3gp"));
        ffmpegService.convertFileToMp4(new File("C:/ffmpeg/input/test.avi"));
        ffmpegService.convertFileToMp4(new File("C:/ffmpeg/input/test.flv"));
        ffmpegService.convertFileToMp4(new File("C:/ffmpeg/input/test.mkv"));
        ffmpegService.convertFileToMp4(new File("C:/ffmpeg/input/test.mov"));
        ffmpegService.convertFileToMp4(new File("C:/ffmpeg/input/test.mp4"));
        ffmpegService.convertFileToMp4(new File("C:/ffmpeg/input/test.mpg"));
        ffmpegService.convertFileToMp4(new File("C:/ffmpeg/input/test.rmvb4"));
        ffmpegService.convertFileToMp4(new File("C:/ffmpeg/input/test.swf"));
        ffmpegService.convertFileToMp4(new File("C:/ffmpeg/input/test.wmv")); */
    }

    /**
     * 在 Mac 下进行测试
     */
    @Test
    public void convertOnMac() throws Exception {
        // convertService.convertToMp4(new File("/Users/Biao/Download/test.flv"), new File("/ebag/temp/preview/test-flv.mp4"));
    }
}
