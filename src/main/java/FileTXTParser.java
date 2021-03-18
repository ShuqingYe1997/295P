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
    private String start;  // e.g. the first trading date of Oct 2001 is 2nd
    private String end;  // e.g. the last trading data of Oct 2001 is 31st

    private static double marketReturn = -1.0;  // market return is the same during the same period of time

    private String companyName;
    private String inputFilePath;
    private List<String> attributes;
    private List<String> values;

    public FileTXTParser(String time, String companyName, String inputFilePath) {
        this.time = time;
        getDate(inputFilePath);  // assign start and end date

        this.companyName = companyName;
        this.inputFilePath = inputFilePath;

        this.attributes = new ArrayList<String>(Arrays.asList("Start Price", "End Price", "Company Return",
                                                "Market Return", "DCV", "Delta Return"));

        this.values = new ArrayList<String>();

        if(marketReturn == -1) {
            File inputFile = new File("GSPC");
            String s1 = readFromGSPC(inputFile, time + "-" + start);
            String s2 = readFromGSPC(inputFile, time + "-" + end);
            marketReturn = calculateReturnRatio(s1, s2);
        }
    }

    public void parseFile() throws Exception {
        try
        {
            File inputFile1 = new File(inputFilePath + "/" + start + "/close");
            String price1 = readFromFile(inputFile1, companyName, "ClosePrice");
            String volume1 = readFromFile(inputFile1, companyName, "Volume");
    
            File inputFile2 = new File(inputFilePath + "/" + end + "/close");
            String price2 = readFromFile(inputFile2, companyName, "ClosePrice");
    
            double companyReturn = calculateReturnRatio(price1, price2);
            double DCV = calculateDCV(price1, volume1);
    
            values.add(price1);
            values.add(price2);
            values.add(String.format("%.2f", companyReturn));
            values.add(String.format("%.2f", marketReturn));
            values.add(String.format("%.2f", DCV));
            values.add(String.format("%.2f", companyReturn - marketReturn));
        }
        catch(Exception e)
        {
            
        }

    }

    public List<String> getAttributes() {
        return attributes;
    }

    public List<String> getValues() {
        return values;
    }

    public void getDate(String filepath) {
        start = "00";
        end = "00";

        // 2001-12-03 is a public holiday
        if (time.equals("2001-12")) {
            start = "04";
            end = "31";
            return;
        }

        // Populates the array with names of files and directories
        File month_folder = new File(filepath);
        String[] dayList = month_folder.list();
        Arrays.sort(dayList);
            
        if(dayList.length < 1)
        {
            //System.out.println("Error�� The profile should have at least two trading dates.");
            return;
        }

        int i = 0;
        while(i < dayList.length)
        {
            String folderName = dayList[i];
            if(folderName.length() == 2
             && folderName.matches("-?\\d+"))
            {
                start = folderName;
                break;
            }
            i++;
        }
        
        i = 0;
        
        while(i < dayList.length)
        {
            String folderName = dayList[dayList.length-i-1];
            if(folderName.length() == 2
             && folderName.matches("-?\\d+"))
            {
                end = folderName;
                break;
            }
            i++;
        }        

        if(start.equals("00") ||
        end.equals("00"))
        {
            //System.out.println("Erorr: Wrong date folder");
        }

    }

    private String readFromFile(File file, String companyName, String label) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line = null;
        while ((line = bufferedReader.readLine())!= null) {
            String[] fields = line.trim().split("\\s+");
            if (fields[0].toLowerCase().equals(companyName.toLowerCase())) {
                if (label.equals("ClosePrice"))
                {
                    bufferedReader.close();
                    return fields[2];  // close price
                }
                else if (label.equals("Volume"))
                {
                    bufferedReader.close();
                    return fields[5];  // daily volume (share)
                }            }
        }
        bufferedReader.close();
        return "";
    }

    private String readFromGSPC(File file, String date){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.trim().split("\\s+");
                if (fields[0].equals(date))
                {
                    bufferedReader.close();
                    return fields[4];  // close
//                    return fields[fields.length - 1];  // close price
                }
            }
            bufferedReader.close();
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
