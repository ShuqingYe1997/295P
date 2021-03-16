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

    public static void process_profile_folder(String filename, String profile_path, 
    String writeDirectory, String outFilePath, String time, String inputFolderPath)
    {
        String newProfilePath = profile_path + "/" + filename;
        String[] profile_files;
        File profile_folder = new File(newProfilePath);
        // Populates the array with names of files and directories
        profile_files = profile_folder.list();

        // sort the list
        for(int i = 0; i < profile_files.length; i++) {
            String subfilename =  profile_files[i];
            // skip current folder and uplevel folder
            if(subfilename.equals(".") || subfilename.equals("..")) continue;
            String processing_file = newProfilePath + "/" +subfilename;
            //System.out.println("profile path: " + profilePath);
            File file = new File(processing_file);
            if(file.isFile())
            {
                process_profile_file(subfilename, newProfilePath, writeDirectory, outFilePath, time, inputFolderPath);
            }
            else{
                process_profile_folder(subfilename, newProfilePath, writeDirectory, outFilePath, time, inputFolderPath);
            }
        } // end of traversal file dir
    }

    public static void main(String[] args) {
        String year = args[0];
        String filePath = args[1];
        filePath = filePath.replace("\\", "/");
        String writeDirectory = args[2] + "/";


        String month = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());

        String time  = year + "-" + month;
        String outFilePath = writeDirectory + time + ".csv";

        String profile_path = filePath + "/profiles/Yahoo/US/01/";

        process_profile_folder("p", profile_path, writeDirectory, outFilePath, time, filePath);
 

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
