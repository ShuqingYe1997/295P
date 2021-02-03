import java.io.File;

/**
 * @ClassName: Run
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-3
 */

public class Run {
    public static void main(String[] args) {
        String path = Run.class.getClassLoader().getResource("p").getPath();

        for (char i = 'a'; i <= 'z'; i++) {
            String filePath = path + "/" + i;
            File dir = new File(filePath);
            if (dir.exists()) {
                String[] filenames = dir.list();
                if (filenames == null || filenames.length == 0)
                    continue;
                for (String filename: filenames) {
                    try {
                        String companyName = filename.substring(0, filename.indexOf(".html"));

                        FileHTMLParser parser = new FileHTMLParser(companyName, filePath + "/" + filename);
                        parser.parseFile();

                        FileCSVWriter writer = new FileCSVWriter(companyName,
                                parser.getAttributes(), parser.getValues());
                        writer.saveFile();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
