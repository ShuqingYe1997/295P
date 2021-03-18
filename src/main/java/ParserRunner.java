import java.io.File;

/**
 * @ClassName: ParserRunner
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-3
 */


/*
 * parser year [month_path] [out_directory]
 */

public class ParserRunner {
    public static void process_profile_file(String filename, String profile_path, 
    String writeDirectory, String outFilePath, String time, String inputFolderPath)
    {
        try {
            if(!filename.contains(".html") || filename.equals("index.html")) return;
            
            String companyName = filename.substring(0, filename.indexOf(".html"));
            // filter out those companies whose name containing dots
            if (companyName.contains("."))
            {
                return;
            }
            FileHTMLParser htmlParser = new FileHTMLParser(time, companyName, profile_path + "/" + filename);
            htmlParser.parseFile();

            FileTXTParser txtParser = new FileTXTParser(time, companyName, inputFolderPath);
            txtParser.parseFile();

            FileCSVWriter writer = new FileCSVWriter(time,
                    htmlParser.getAttributes(), htmlParser.getValues(),
                    txtParser.getAttributes(), txtParser.getValues());
            writer.saveFile(writeDirectory, outFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void process_profile_folder(String dir, String profile_path,
    String writeDirectory, String outFilePath, String time, String inputFolderPath)
    {
        File profile_folder = new File(profile_path + "/" + dir);
        // profile_folder now is [path]\2006-11\2006\11\profiles\Yahoo\US\01\p\a

        if (!profile_folder.exists()) {
            dir = dir.toUpperCase();
            profile_folder = new File(profile_path + "/" + dir);
        }
        String[] profile_files = profile_folder.list();

        // sort the list
        for(int i = 0; i < profile_files.length; i++) {
            String filename =  profile_files[i];
            process_profile_file(filename, profile_path + "/" + dir, writeDirectory, outFilePath, time, inputFolderPath);
        }
    }

    public static void main(String[] args) {
        String year = args[0];
        String filePath = args[1];
        filePath = filePath.replace("\\", "/");
        String writeDirectory = args[2] + "/";

        String month = filePath.substring(filePath.lastIndexOf("/") + 1);

        String time  = year + "-" + month;
        String outFilePath = writeDirectory + time + ".csv";

        String profile_path = filePath + "/profiles/Yahoo/US/01/p/";

        // ATTENTION! We only need to read dir a to z (or A to Z)
        for (char dir = 'a'; dir <= 'z'; dir++)
            process_profile_folder(dir + "", profile_path, writeDirectory, outFilePath, time, filePath);

        // for (int i = 10; i <= 12; i++) {
        //     File file = new File(writeDirectory + time.substring(0, 5) + i + ".csv");   // e.g. 2001- + month
        //     if (!file.exists()) {
        //         return;  // can't combine because some months are missing
        //     }
        // }
        // try {
        //     FileCSVCombiner combiner = new FileCSVCombiner(time.substring(0, 4), writeDirectory);
        //     combiner.saveFile();
        // }catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

}
