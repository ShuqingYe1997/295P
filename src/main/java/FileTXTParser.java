import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

    private static double marketReturn = -1.0;  // market return is the same during the same period of time

    private String companyName;
    private String inputFilePath;
    private List<String> attributes;
    private List<String> values;

    public FileTXTParser(String time, String companyName, String inputFilePath) {
        this.time = time;
        getDate();  // assign start and end date

        this.companyName = companyName;
        this.inputFilePath = inputFilePath;

        this.attributes = new ArrayList<String>(Arrays.asList("Start Price", "End Price", "Company Return",
                                                "Market Return", "Avg DCV", "Delta Return"));

        this.values = new ArrayList<String>();

        if(marketReturn == -1) {
            File inputFile = new File(Run.class.getClassLoader().getResource("GSPC").getPath());
            String s1 = readFromGSPC(inputFile, time + "-" + start);
            String s2 = readFromGSPC(inputFile, time + "-" + end);
            marketReturn = calculateReturnRatio(s1, s2);
        }
    }

    public void parseFile() throws Exception {
        File inputFile1 = new File(inputFilePath + "/" + start + "/close");
        String price1 = readFromFile(inputFile1, companyName, "ClosePrice");
        String volume1 = readFromFile(inputFile1, companyName, "Volume");

        File inputFile2 = new File(inputFilePath + "/" + end + "/close");
        String price2 = readFromFile(inputFile2, companyName, "ClosePrice");

        double companyReturn = calculateReturnRatio(price1, price2);
        double avgDCV = calculateDCV(price1, volume1);

        values.add(price1);
        values.add(price2);
        values.add(String.format("%.2f", companyReturn));
        values.add(String.format("%.2f", marketReturn));
        values.add(String.format("%.2f", avgDCV));
        values.add(String.format("%.2f", companyReturn - marketReturn));
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

    private String readFromFile(File file, String companyName, String label) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = bufferedReader.readLine())!= null) {
            String[] fields = line.trim().split("\\s+");
            if (fields[0].toLowerCase().equals(companyName.toLowerCase())) {
                if (label.equals("ClosePrice"))
                    return fields[2];  // close price
                else if (label.equals("Volume"))
                    return fields[5];  // daily volume (share)
            }
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
        return "0";
    }

    private double calculateReturnRatio(String s1, String s2) {
        if(s1.equals("") || s2.equals(""))
            return 0;
        double num1 = Double.parseDouble(s1);
        double num2 = Double.parseDouble(s2);
        double res = 0;
        if (num1 > 0) // i.e. num1 != 0
            res = (num2 - num1) / num1 * 100;
        return res;
    }

    private double calculateDCV(String price1, String volume1) {
        if(price1.equals("") || volume1.equals("") || price1.equals("N/A") || volume1.equals("N/A"))
            return 0;
        double p1 = Double.parseDouble(price1);
        double v1 = Double.parseDouble(volume1);

        return p1 * v1;

    }

}
