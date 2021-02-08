import java.io.File;
import java.util.Scanner;

/**
 * @ClassName: Run
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-3
 */

public class Run {
    public static void main(String[] args) {
        System.out.println("Input time (e.g. 2001-10): ");
        Scanner scanner = new Scanner(System.in);
        String time  = scanner.nextLine();
        while (!isValid(time)) {
            System.out.println("Input time (e.g. 2001-10): ");
            time  = scanner.nextLine();
        }

//        String filePath = Run.class.getClassLoader().getResource().getPath();
        String filePath = "D:\\下载\\2001-12\\2001\\12";

        for (char i = 'a'; i <= 'z'; i++) {
            String profilePath = filePath + "\\profiles\\Yahoo\\US\\15\\p\\" + i;
            File dir = new File(profilePath);
            if (dir.exists()) {
                String[] filenames = dir.list();
                if (filenames == null || filenames.length == 0)
                    continue;
                for (String filename: filenames) {
                    try {
                        String companyName = filename.substring(0, filename.indexOf(".html"));

                        FileHTMLParser htmlParser = new FileHTMLParser(companyName, profilePath + "/" + filename);
                        htmlParser.parseFile();

                        FileTXTParser txtParser = new FileTXTParser(time, companyName, filePath);
                        txtParser.parseFile();

                        FileCSVWriter writer = new FileCSVWriter(time,
                                htmlParser.getAttributes(), htmlParser.getValues(),
                                txtParser.getAttributes(), txtParser.getValues());
                        writer.saveFile();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
 
    // tentative func
    public static boolean isValid(String time) {
        return
                (time.equals("2001-10") || time.equals("2001-11") || time.equals("2001-12")
                || time.equals("2006-10") || time.equals("2006-11") || time.equals("2006-12")
                || time.equals("2011-10") || time.equals("2011-11") || time.equals("2011-12"));
    }
}
