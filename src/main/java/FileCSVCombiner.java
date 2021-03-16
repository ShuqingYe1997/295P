import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


/**
 * @ClassName: FileCSVCombiner
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-10
 */
public class FileCSVCombiner {
    private String time;
    private String filePath;

    public FileCSVCombiner(String time, String filePath) {
        this.time = time;
        this.filePath = filePath;
    }

    public void saveFile() throws Exception {
        File dir = new File(filePath);
        if (!dir.exists())
            dir.mkdir();

        File outputFile = new File(filePath + "/" + time + ".csv");
        CSVWriter writer = new CSVWriter(new FileWriter(outputFile, true));

        CSVReaderBuilder builder = null;
        CSVReader reader = null;
        for (int month = 10; month <= 12; month++) {
            builder = new CSVReaderBuilder(new FileReader(filePath + "/" + time + "-" + month + ".csv"));
            if (month != 10)
                reader = builder.withSkipLines(1).build();  // skip header
            else
                reader = builder.build();

            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String[] strings = new String[nextLine.length - 4];    // skip unnecessary fields
                for (int i = 0; i < nextLine.length; i++) {
                    if (i < 40)
                        strings[i] = nextLine[i];
                    else if (i >= 44)
                        strings[i - 4] = nextLine[i];
                }
                writer.writeNext(strings);
            }
            reader.close();  // close after reading one file
        }
        writer.close();  // close after writing all 3 files
        //System.out.println("********************" + time + ".csv saved." + "********************");
    }
}
