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

class Stock {
    String symbol;
    double price;

    public Stock(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }

    @Override
    public String toString() {
        return symbol + " " + price;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}

class Trades {
    private String time;
    private String start;
    private String end;

    private List<Stock> portfolio;
    private List<Integer> shares;

    public static final int MONEY_IN_TOTAL = 100000; // 100K cash

    public Trades(String time) {
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
    public void saveTrades(String filePath)  {
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
                writer.write(start + " 15:59 buy " +  shares.get(i) + " shares of " + portfolio.get(i).getSymbol() + "\n");
            }

            //sell
            for (int i = 0; i < portfolio.size(); i++) {
                writer.write(end + " 15:59 sell " + shares.get(i) + " shares of " + portfolio.get(i).getSymbol() + "\n");
            }
            writer.close();

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void readPortfolio() throws Exception {
        CSVReaderBuilder builder = new CSVReaderBuilder(new FileReader("input/" + "2011-10-portfolio.csv"));
        CSVReader reader = builder.withSkipLines(1).build();  // skip header
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            portfolio.add(new Stock(nextLine[0], 0));
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
                return o1.getSymbol().compareTo(o2.getSymbol());
            }
        });

        String[] nextLine;
        int i = 0;
        while ((nextLine = reader.readNext()) != null && i < portfolio.size()) {
            if (nextLine[0].equals(portfolio.get(i).getSymbol())) {
                portfolio.get(i).setPrice(Double.parseDouble(nextLine[40]));  // start price
                i++;
            }
        }
        reader.close();
    }

    private void calculateShares() {
        for (int i = 0; i < portfolio.size(); i++) {
            shares.add((int)(MONEY_IN_TOTAL / portfolio.size() / portfolio.get(i).getPrice()));
        }
    }
}

public class AdversaryTrades {
    public static void main(String[] args) {
        Trades trade = new Trades("2011-10");
        trade.saveTrades("output/");
    }
}


