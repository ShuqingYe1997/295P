import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.*;
import java.time.Year;
import java.util.*;

/**
 * @ClassName: AdversaryTrades
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-27
 */

public class Trades {
    private String inputFilePath;
    private String outTradeFilePath;
    private String monthlyDataPath;

    private double cash;
    private String time;  // 2001-10
    private String start; // date string
    private String end;   // date string

    private List<Stock> portfolio;
    private List<Integer> shares;

    public Trades(String time, String streamDirectory, String outputFolder, double cash) {
        this.cash = cash;
        this.time = time;
        this.outTradeFilePath = outputFolder + "/" + time + "-trades.txt";
        this.monthlyDataPath = outputFolder + "/" + this.time + ".csv";
        this.inputFilePath = outputFolder + "/" + time + "-portfolio.csv";

        getDate(streamDirectory);  // set start and end trading date

        portfolio = new ArrayList<Stock>();
        shares = new ArrayList<Integer>();
    }

    private void getDate(String streamDirectory) {
        start = "0";
        end = "0";

        String[] dayList;
        File month_folder = new File(streamDirectory);

        // Populates the array with names of files and directories
        dayList = month_folder.list();

        // sort the list
        for(int i = 0; i< dayList.length-1; i++) {
            for (int j = i+1; j< dayList.length; j++) {
               if(dayList[i].compareTo(dayList[j])>0) {
                  String temp = dayList[i];
                  dayList[i] = dayList[j];
                  dayList[j] = temp;
               }
            }
         }
            
        if(dayList.length < 1)
        {
            //System.out.println("Error： The profile should have at least two trading dates.");
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

    /**
     * 1. read csv, get companies
     * 2. get their price at the start trading day (using company name and date and file directory)
     * 3. output trades in the given format
     **/
    public void saveFile()  {
        try {
            readPortfolio();
            readPrices();
//            //System.out.println(portfolio);
            calculateShares();

            File outputFile = new File(outTradeFilePath);
            FileWriter writer  = new FileWriter(outputFile, false);
            writer.flush();
            // buy
            // e.g. 03 15:59 buy 10 shares of SMSI
            // NOTE: change 15:59 to 15:55 to avoid weird price
            for (int i = 0; i < portfolio.size(); i++) {
                writer.write(time + "-" + start + " 15:55 buy " +  shares.get(i) + " shares of " + portfolio.get(i).symbol + "\n");
            }

            //sell
            for (int i = 0; i < portfolio.size(); i++) {
                writer.write(time + "-" + end + " 15:55 sell " + shares.get(i) + " shares of " + portfolio.get(i).symbol + "\n");
            }
            writer.close();
            //System.out.println("=================== Trading file of " + time + " saved ===================");

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void readPortfolio() throws Exception {
        CSVReaderBuilder builder = new CSVReaderBuilder(new FileReader(this.inputFilePath));
        CSVReader reader = builder.withSkipLines(1).build();  // skip header
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            portfolio.add(new Stock(nextLine[0]));
        }
        reader.close();
    }

    private void readPrices() throws Exception {
        CSVReaderBuilder builder = new CSVReaderBuilder(new FileReader(this.monthlyDataPath));
        CSVReader reader = builder.withSkipLines(1).build();  // skip header

        // reduce time complexity
        // otherwise, T(n) = # of companies in portfolio * # of companies in monthly csv
        Collections.sort(portfolio, new Comparator<Stock>() {
            public int compare(Stock o1, Stock o2) {
                return o1.symbol.compareTo(o2.symbol);
            }
        });

        String[] nextLine;
        int i = 0;
        while ((nextLine = reader.readNext()) != null && i < portfolio.size()) {
            if (nextLine[0].toUpperCase().equals(portfolio.get(i).symbol)) {
                portfolio.get(i).setPrice(Double.parseDouble(nextLine[40]));  // start price
                i++;
            }
        }
        reader.close();
    }

    private void calculateShares() {
        for (int i = 0; i < portfolio.size(); i++) {
            shares.add((int)(this.cash / portfolio.size() / portfolio.get(i).price));
        }
    }
}

