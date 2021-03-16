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
    private String outputFilename;

    private List<String> attributes1;
    private List<String> values1;
    private List<String> attributes2;
    private List<String> values2;

    private static int cnt = 0;

    FileCSVWriter(String time,
                  List<String> attributes1, List<String> values1,
                  List<String> attributes2, List<String> values2) {
        this.outputFilename = time;
        this.attributes1 = attributes1;
        this.values1 = values1;
        this.attributes2 = attributes2;
        this.values2 = values2;

        cnt++;
    }

    public void saveFile(String folder, String filePath) throws Exception {
        File dir = new File(folder);
        if (!dir.exists())
            dir.mkdir();

        File outputFile = new File(filePath);
        CSVWriter writer = new CSVWriter(new FileWriter(outputFile, true));

        List<String[]> data = new ArrayList<String[]>();

        // The first write, header needed
        if (cnt == 1) {
            attributes1.addAll(attributes2); // combine header
            String[] header = new String[attributes1.size()];
            attributes1.toArray(header);
            data.add(header);
        }
        values1.addAll(values2);  // combine data
        String[] values = new String[values1.size()];
        values1.toArray(values);
        data.add(values);

        writer.writeAll(data);

        writer.close();
        //if (cnt % 100 == 0)
            //System.out.println("============" + cnt + " companies saved." + "============");
    }
}
