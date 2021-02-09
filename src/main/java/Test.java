import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @ClassName: Test
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-8
 */
public class Test {
    public static void main(String[] args) {
        try {
            File file = new File("D:\\UCI_MCS\\295P\\StockPrediction\\src\\main\\resources\\script.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lines = line.split("\\t+");
                if(lines.length > 1) {
                    if (lines[1].endsWith("last"))
                        System.out.println("else if (HEADER.get(i).equals("  +lines[0]+ "))\n" +
                                "element = document.select(\"" + lines[1] +"\").last();");
                    else
                        System.out.println("else if (HEADER.get(i).equals("  +lines[0]+ "))\n" +
                                "element = document.selectFirst(\"" + lines[1] +"\");");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
