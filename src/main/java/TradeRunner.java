import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.*;
import java.util.*;

/**
 * @ClassName: AdversaryTrades
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-27
 */

class Trades {
    private String inputFilename;
    private double cash;
    private String time;
    private String start;
    private String end;

    private List<Stock> portfolio;
    private List<Integer> shares;

    public Trades(String time, String inputFilename, double cash) {
        this.inputFilename = inputFilename;
        this.cash = cash;
        this.time = time;
        getDate();  // set start and end trading date

        portfolio = new ArrayList<Stock>();
        shares = new ArrayList<Integer>();
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

    /**
     * 1. read csv, get companies
     * 2. get their price at the start trading day (using company name and date and file directory)
     * 3. output trades in the given format
     **/
    public void saveFile(String filePath)  {
        try {
            readPortfolio();
            readPrices();
//            System.out.println(portfolio);
            calculateShares();

            File outputFile = new File(filePath + time + "-trades.txt");
            FileWriter writer  = new FileWriter(outputFile, true);

            // buy
            // e.g. 03 15:59 buy 10 shares of SMSI
            for (int i = 0; i < portfolio.size(); i++) {
                writer.write(start + " 15:59 buy " +  shares.get(i) + " shares of " + portfolio.get(i).symbol + "\n");
            }

            //sell
            for (int i = 0; i < portfolio.size(); i++) {
                writer.write(end + " 15:59 sell " + shares.get(i) + " shares of " + portfolio.get(i).symbol + "\n");
            }
            writer.close();
            System.out.println("=================== portfolio saved ===================");

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void readPortfolio() throws Exception {
        CSVReaderBuilder builder = new CSVReaderBuilder(new FileReader("input/" + inputFilename));
        CSVReader reader = builder.withSkipLines(1).build();  // skip header
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            portfolio.add(new Stock(nextLine[0]));
        }
        reader.close();
    }

    private void readPrices() throws Exception {
        CSVReaderBuilder builder = new CSVReaderBuilder(new FileReader("output/" + time + ".csv"));
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
            if (nextLine[0].equals(portfolio.get(i).symbol)) {
                portfolio.get(i).setStartingPrice(Double.parseDouble(nextLine[40]));  // start price
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

public class TradeRunner {
    public static void main(String[] args) {
        Trades trade = new Trades("2011-10", "2011-10-portfolio.csv", 100000);
        trade.saveFile("output/");
    }
}


