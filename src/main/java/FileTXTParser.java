import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: FileTXTParser
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-5
 */
public class FileTXTParser {
    private String time;   // e.g. 2001-11
    private String start;  // e.g. the first transaction date of Oct 2001 is 2nd
    private String end;  // e.g. the last transaction data of Oct 2001 is 31st

    private static String marketReturn = "";  // market return is the same during the same period of time

    private String companyName;
    private String inputFilePath;
    private List<String> attributes;
    private List<String> values;

    public FileTXTParser(String time, String companyName, String inputFilePath) {
        this.time = time;
        getDate();  // assign start and end date

        this.companyName = companyName;
        this.inputFilePath = inputFilePath;

        this.attributes = new ArrayList<String>(Arrays.asList("Start Price", "End Price", "Company Return", "Market Return", "Delta"));

        this.values = new ArrayList<String>();

        if(marketReturn.equals("")) {
            File inputFile = new File(Run.class.getClassLoader().getResource("GSPC").getPath());
            String s1 = readFromGSPC(inputFile, time + "-" + start);
            String s2 = readFromGSPC(inputFile, time + "-" + end);
            marketReturn = calculateReturnRatio(s1, s2);
        }
    }

    public void parseFile() throws Exception {
        File inputFile1 = new File(inputFilePath + "/" + start + "/close");
        String price1 = readFromFile(inputFile1, companyName);

        File inputFile2 = new File(inputFilePath + "/" + end + "/close");
        String price2 = readFromFile(inputFile2, companyName);

        values.add(price1);
        values.add(price2);
        values.add(calculateReturnRatio(price1, price2));
        values.add(marketReturn);
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public List<String> getValues() {
        return values;
    }

    private void getDate() {
        if (time.equals("2001-10")) {
            start = "02";
            end = "31";
        }
        else if (time.equals("2001-11")) {
            start = "01";
            end = "30";
        }
        else if (time.equals("2001-12")) {
            start = "03";
            end = "31";
        }
        else if (time.equals("2006-10")) {
            start = "02";
            end = "31";
        }
        else if (time.equals("2006-11")) {
            start = "01";
            end = "30";
        }
        else if (time.equals("2006-12")) {
            start = "01";
            end = "29";
        }
        else if (time.equals("2011-10")) {
            start = "03";
            end = "31";
        }else if (time.equals("2011-11")) {
            start = "01";
            end = "30";
        }
        else if (time.equals("2011-12")) {
            start = "01";
            end = "30";
        }
        else {
            System.out.println("You shouldn't be here!");
        }
    }

    private String readFromFile(File file, String companyName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = bufferedReader.readLine())!= null) {
            String[] fields = line.trim().split("\\s+");
            if (fields[0].toLowerCase().equals(companyName.toLowerCase()))
                return fields[2];  // close price
        }
        return "";
    }

    private String readFromGSPC(File file, String date){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.trim().split("\\s+");
                if (fields[0].equals(date))
                    return fields[fields.length - 1];  // close price
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String calculateReturnRatio(String s1, String s2) {
        if(s1.equals("") || s2.equals(""))
            return "0";
        double num1 = Double.parseDouble(s1);
        double num2 = Double.parseDouble(s2);
        double res = 0;
        if (num1 > 0) // i.e. num1 != 0
            res = (num2 - num1) / num1 * 100;
        return String.format("%.2f", res) + "%";
    }

}
