import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: FileCSVWriter
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-3
 */
public class FileCSVWriter {

    private String outputDirName;
    private String outputFilename;
//    private List<String> attributes;
//    private List<String> values;
    private String[] attributes;
    private String[] values;

    FileCSVWriter(String time, String outputFilename, String[] attributes, String[] values) {
        this.outputDirName = "output/" + time;
        this.outputFilename = outputFilename;
        this.attributes = attributes;
        this.values = values;
    }

    public void saveFile() throws Exception {
        File dir = new File(outputDirName);
        if (!dir.exists())
            dir.mkdir();
        String filePath = this.outputDirName + "/"+ outputFilename + ".csv";
        File outputFile = new File(filePath);
        CSVWriter writer = new CSVWriter(new FileWriter(outputFile, true));

        List<String[]> data = new ArrayList<String[]>();
        data.add(this.attributes);
        data.add(this.values);
        writer.writeAll(data);

        writer.close();
        System.out.println("============" + this.outputFilename + " saved" + "============");
    }
}
