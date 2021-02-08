import java.io.File;
import java.util.Scanner;

/**
 * @ClassName: Test
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-7
 */
public class Test {

    public static void main(String[] args) {
        String time = "2006-10";
        String filePath = Test.class.getClassLoader().getResource("aapl.html").getPath();
        try {
            FileHTMLParser2 htmlParser = new FileHTMLParser2("aapl", filePath);
            htmlParser.parseFile();

//            FileTXTParser txtParser = new FileTXTParser(time, "aapl", filePath);
//            txtParser.parseFile();

//            FileCSVWriter writer = new FileCSVWriter(time,
//                    htmlParser.getAttributes(), htmlParser.getValues(),
//                    txtParser.getAttributes(), txtParser.getValues());
//            writer.saveFile();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}