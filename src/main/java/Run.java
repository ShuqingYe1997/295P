/**
 * @ClassName: Run
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-3
 */

public class Run {
    public static void main(String[] args) {
        for (char i = 'a'; i <= 'z'; i++) {
            String dir = i + "";
            FileHTMLParser companies = new FileHTMLParser(dir);
            try {
                companies.readFile();
                companies.parseFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
