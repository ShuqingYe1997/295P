import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: Adversary
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-28
 */

class Transaction {
    String date;
    String time;
    String action;
    int shares;
    Stock stock;

    public Transaction() {
    }

    public Transaction(String date, String time, String action, int shares, Stock stock) {
        this.date = date;
        this.time = time;
        this.action = action;
        this.shares = shares;
        this.stock = stock;
    }

    // make a copy
    public Transaction(Transaction t) {
        this.date = t.date;
        this.time = t.time;
        this.action = t.action;
        this.shares = t.shares;
        this.stock = new Stock(t.stock.symbol, t.stock.price);
    }

    // 就为了这个写多态也太麻烦了吧！
    public String toString() {
        if (action.equals("buy"))  // 2011-10-31 15:59 1461 AMSC sold at $4.37
            return date + " " + time + " " + shares + " " + stock.symbol + " "
                + "bought" + " at $" + String.format("%.2f", stock.price);
        else if (action.equals("sell"))
            return date + " " + time + " " + shares + " " + stock.symbol + " "
                    + "sold" + " at $" + String.format("%.2f", stock.price);
        else  // 1326 shares of AUO [$3.78] valued at $5012.28
            return shares + " shares of " + stock.symbol + " "
                    + "[$" + stock.price + "] valued at $" + String.format("%.2f", shares * stock.price);
    }
}

class PortFolio {
    private String year;
    private String month;
    private String streamingFolder;

    private List<Transaction> tradeList;  // 持仓
    private double totalValue;

    public PortFolio(String streamingFolder, String year, String month) {
        this.year = year;
        this.month = month;
        this.streamingFolder = streamingFolder;
        tradeList = new ArrayList<Transaction>();
        totalValue = 0;
    }

    // buy
    public boolean update(Transaction t, double cashBalance) {
        if (t.shares * t.stock.price > cashBalance) {
            System.out.println("TRADING WARNING (not fatal): cash balance $" + String.format("%.2f", cashBalance) +
                    " too small to purchase " + t.shares + " shares of " + t.stock.symbol);
            if(Utils.Force_Trade)
            {
                // Trading fee has to be included
                if (cashBalance - Utils.TRANSACTION_FEE <= 0) {
                    return false;
                }
                t.shares = (int)((cashBalance - Utils.TRANSACTION_FEE) / t.stock.price);
                System.out.println("TRADING WARNING (not fatal): Can ONLY Buy " + t.shares + " shares of " + t.stock.symbol);
            }
            else
                return false;
        }

        for (Transaction p : tradeList) {
            if (p.stock.symbol.equals(t.stock.symbol)) {
                p.shares += t.shares;
                return true;
            }
        }
        // not holding this stock
        tradeList.add(new Transaction(t));
        return true;
    }

    // sell
    public boolean update(Transaction t) {
        for (Transaction p : tradeList) {
            if (p.stock.symbol.equals(t.stock.symbol)) {
                if (p.shares < t.shares) {
                    System.out.println("TRADING WARNING (not fatal): you only have " + p.shares +
                            " shares of " + t.stock.symbol + "; you cannot sell " + t.shares + " shares");
                    if (Utils.Force_Trade) {
                        t.shares = p.shares;
                        System.out.println("TRADING WARNING (not fatal): Sell " + t.shares +
                                " shares of " + t.stock.symbol +
                                ". That's all the shares you have.");
                    } else
                        return false;
                }
                p.shares -= t.shares;
                if (p.shares == 0)
                    tradeList.remove(p);
                return true;
            }
        }
        // not holding
        System.out.println("TRADING WARNING (not fatal): you only have 0" +
                " shares of " + t.stock.symbol + "; you cannot sell " + t.shares + " shares");
        return false;
    }

    // 2958 shares of GSS [$1.71] valued at $5043.39
    public void display() {
        for (Transaction t: tradeList) {
            Transaction dis = new Transaction(t);  // make a copy
            dis.date = tradeList.get(tradeList.size() - 1).date;  // get the  date of the latest transaction
            dis.time = "15:59";
            dis.action = "display";
            try {
                Utils.readStream(this.streamingFolder, dis, year, month);
            }catch (Exception e) {
                e.printStackTrace();
            }
            totalValue += dis.shares * dis.stock.price;
            System.out.println(dis.toString());
        }
        System.out.println("Total stocks' value: $" + String.format("%.2f", totalValue));
    }

    public double getTotalValue() {
        return totalValue;
    }
}

class Utils {
    static boolean Force_Trade = true;
    static final int TRANSACTION_FEE = 10;

    public static void readStream(String directory_date_path, Transaction t, String year, String month) throws Exception{
        String filePath = directory_date_path + "/" + t.date.substring(8) + "/streaming.tsv";;
        FileReader reader = new FileReader(new File(filePath));
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;

        List<String[]> transactions = new ArrayList<String[]>();  // easier to retrieve a former time
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith(t.stock.symbol + "\t")) {  // some symbols may have the same prefix
                String[] tokens = line.split("\t");
                transactions.add(tokens);

                if(tokens[1].compareTo(t.time) >= 0) {
                    if (tokens[1].compareTo(t.time) > 0) {
                        System.out.println("TRADING ERROR: Trade of " + t.stock.symbol +
                                " ordered at " + t.time + " but first quote is " + tokens[1] + "; " +
                                "trade will occur at " + tokens[1]);
                    }
                    setTradingPrice(tokens, t);
                    bufferedReader.close();
                    return;
                }
            }
        }

        if(transactions.size() == 0 )
        {
            System.out.print("Could not find transaction:" + t.stock.symbol);
        }
        else
           // if no transaction at the given time, then go get at the closest time before that
           setTradingPrice(transactions.get(transactions.size() - 1), t);
    }

    private static void setTradingPrice(String[] tokens, Transaction t) {
        double price;
        if (t.action.equals("buy")) {
            price = getBuyingPrice(tokens, t);
        }

        else if (t.action.equals("sell")){
            price = getSellingPrice(tokens, t);
        }
        else {  // display
            price = Double.parseDouble(tokens[2]);
        }
        t.time = tokens[1];
        t.stock.setPrice(price);
    }

    // print out one line saying the average price of the whole set of trades
    private static double getBuyingPrice(String[] tokens, Transaction t) {
        double price = 0;
        int volume = Integer.parseInt(tokens[5]);
        double currentPrice = Double.parseDouble(tokens[2]);


        // bid-ask spread
        double spread = 0;
        if (tokens[tokens.length - 1].equals("N/A")) {
            spread = Math.exp(-0.2 * Math.log(volume)) / 2;
            price = currentPrice + spread;
        }
        else {
            double bidding = Double.parseDouble(tokens[tokens.length - 2]);
            double asking = Double.parseDouble(tokens[tokens.length - 1]);
            price = asking;
            spread = (asking - bidding) / 2;
        }

        // exceeding 1% DCV
        if (t.shares > volume * 0.01) {
            int shares = t.shares;
            double sum = 0;
            int cnt = 1;
            while (shares > 0) {
                sum += volume * 0.01 * (currentPrice + cnt * spread);
                cnt++;
                shares -= volume * 0.01;
            }
            // the average price of the whole set of trades
            price = sum / t.shares;
        }

        return price;
    }

    private static double getSellingPrice(String[] tokens, Transaction t) {
        double price = 0;
        int volume = Integer.parseInt(tokens[5]);
        double currentPrice = Double.parseDouble(tokens[2]);

        // bid-ask spread
        double spread = 0;
        if (tokens[tokens.length - 1].equals("N/A")) {
            spread = Math.exp(-0.2 * Math.log(volume)) / 2;
            price = currentPrice - spread;
        }
        else {
            double bidding = Double.parseDouble(tokens[tokens.length - 2]);
            double asking = Double.parseDouble(tokens[tokens.length - 1]);
            price = asking;
            spread = (asking - bidding) / 2;
        }

        // exceeding 1% DCV
        if (t.shares > volume * 0.01) {
            int shares = t.shares;
            double sum = 0;
            int cnt = 1;
            while (shares > 0) {
                sum += volume * 0.01 * (currentPrice - cnt * spread);
                cnt++;
                shares -= volume * 0.01;
            }
            // the average price of the whole set of trades
            price = sum / t.shares;
        }

        return price;
    }

}

public class Adversary {
    private String year;
    private String month;
    private String inputFilename;
    private double cashBalance;
    private String streamingMonthFolder;

    private List<Transaction> inputTradeList;  // 委托
    private PortFolio portfolio;

    public Adversary(String streamingMonthFolder, String year, String month, String inputFilename, double cash) {
        this.year = year;
        this.month = month;
        this.inputFilename = inputFilename;
        this.cashBalance = cash;
        this.streamingMonthFolder = streamingMonthFolder;

        this.inputTradeList = new ArrayList<Transaction>();
        this.portfolio = new PortFolio(streamingMonthFolder, year, month);
    }

    public void execute() {
        try {
            readTrades();
            for (Transaction t: inputTradeList) {
                Utils.readStream(this.streamingMonthFolder,t, year, month);  // t's time might be changed, price will be set
                if (t.action.equals("buy")) {
                    buy(t);
                }
                else if (t.action.equals("sell"))
                    sell(t);
                else
                System.out.println("Syntax error in " + inputFilename + "!!!");
            }
            displayAccountStatus();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void buy(Transaction t) {
        if (!portfolio.update(t, cashBalance))
            return;

        double cashSpent = t.shares * t.stock.price;
        cashBalance -= cashSpent + Utils.TRANSACTION_FEE;

        // 03 15:59 1461 AMSC bought at $3.45; cash spent $5040.45; cash balance $94949.55
        System.out.print(t.toString() + "; ");
        System.out.print("cash spent $" + String.format("%.2f", cashSpent) + "; ");
        System.out.println("cash balance $" + String.format("%.2f", cashBalance));
    }

    private void sell(Transaction t) {
       if (!portfolio.update(t))
           return;

        double cashAcquired = t.shares * t.stock.price;
        cashBalance += cashAcquired - Utils.TRANSACTION_FEE;

        // 31 15:59 1461 AMSC sold at $4.37; cash acquired $6384.57; cash balance $10258.96
        System.out.print(t.toString() + "; ");
        System.out.print("cash acquired $" + String.format("%.2f", cashAcquired) + "; ");
        System.out.println("cash balance $" + String.format("%.2f", cashBalance));
    }

    private void readTrades() throws Exception{
        FileReader reader = new FileReader(new File("FinalExe/output/" + inputFilename));
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            // 03 15:59 buy 2500 shares of RDN
            String[] tokens = line.split("\\s+");
            inputTradeList.add(new Transaction(tokens[0], tokens[1], tokens[2],
                    Integer.parseInt(tokens[3]), new Stock(tokens[tokens.length - 1])));
        }

        bufferedReader.close();
        reader.close();
    }

    public void displayAccountStatus() {
        System.out.println("Account status: \nCash balance is $" + String.format("%.2f", cashBalance));
        portfolio.display();
        System.out.println("TOTAL: $" + String.format("%.2f", cashBalance + portfolio.getTotalValue()));
    }


/*
 * Adversary [mode] [year] [month] [steamDirectory_MONTH] [writeDirectory]
 * [mode] : 0 - generate trade file only
 *          1 - generate trade file and calculate Adversary
 */

    public static void main(String[] args) {
        // create-trades YEAR-FORMAT MONTH_1 MONTH_2 write-directory
        // MONTH1 can be ignored
        // write-directory is output/ (currently)
        if(args.length != 5) 
        {
            //System.out.println("Wrong Arguments. Adversary [mode] [year-month] [writeDirectory].");
            return;
        }

        final int CASH = 100000;

        int mode = Integer.parseInt(args[0]);
        String streamDirectory = args[3];
        String writeDirectory = args[4];

        String year = args[1];
        String month = args[2];
        String year_month = year+"-"+month;

        Trades trade = new Trades(year_month, streamDirectory, 
            writeDirectory, CASH);
        trade.saveFile();

        if(mode == 1)
        {
            Adversary adversary = new Adversary(streamDirectory, year, month, year_month + "-trades.txt", CASH);
            adversary.execute();
        }
    }
}
