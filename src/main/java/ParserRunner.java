import java.io.File;
import java.util.Scanner;

/**
 * @ClassName: ParserRunner
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-3
 */

public class ParserRunner {
    public static void main(String[] args) {
        // TODO:
        System.out.println("Input file path:");
        Scanner scanner = new Scanner(System.in);
        String filePath = scanner.nextLine();

        System.out.println("Input time (e.g. 2001-10): ");
        String time  = scanner.nextLine();

        while (!isValid(time)) {
            System.out.println("Input file path:");
            filePath = scanner.nextLine();
            System.out.println("Input time (e.g. 2001-10): ");
            time  = scanner.nextLine();
        }

        for (char i = 'a'; i <= 'z'; i++) {
            String profilePath = filePath + "\\profiles\\Yahoo\\US\\01\\p\\" + i;
            File dir = new File(profilePath);
            if (dir.exists()) {
                String[] filenames = dir.list();
                if (filenames == null || filenames.length == 0)
                    continue;
                for (String filename: filenames) {
                    try {
                        String companyName = filename.substring(0, filename.indexOf(".html"));
                        // filter out those companies whose name containing dots
                        if (companyName.contains("."))
                            continue;
                        FileHTMLParser htmlParser = new FileHTMLParser(time, companyName, profilePath + "/" + filename);
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
        } // end of traversal file dir

        String writeDirectory = "output/";
        for (int i = 10; i <= 12; i++) {
            File file = new File(writeDirectory + time.substring(0, 5) + i + ".csv");   // e.g. 2001- + month
            if (!file.exists()) {
                return;  // can't combine because some months are missing
            }
        }
        try {
            FileCSVCombiner combiner = new FileCSVCombiner(time.substring(0, 4), "output/");
            combiner.saveFile();
        }catch (Exception e) {
            e.printStackTrace();
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
