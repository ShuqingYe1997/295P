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
    String day;
    String time;
    String action;
    int shares;
    Stock stock;

    public Transaction() {}

    public Transaction(String day, String time, String action, int shares, Stock stock) {
        this.day = day;
        this.time = time;
        this.action = action;
        this.shares = shares;
        this.stock = stock;
    }

    // make a copy
    public Transaction(Transaction t) {
        this.day = t.day;
        this.time = t.time;
        this.action = t.action;
        this.shares = t.shares;
        this.stock = new Stock(t.stock.symbol, t.stock.price);
    }

    // 就为了这个写多态也太麻烦了吧！
    public String toString() {
        if (action.equals("buy"))  // 31 15:59 1461 AMSC sold at $4.37
            return day + " " + time + " " + shares + " " + stock.symbol + " "
                + "bought" + " at $" + stock.price;
        else if (action.equals("sell"))
            return day + " " + time + " " + shares + " " + stock.symbol + " "
                    + "sold" + " at $" + stock.price;
        else  // 1326 shares of AUO [$3.78] valued at $5012.28
            return shares + " shares of " + stock.symbol + " "
                    + "[$" + stock.price + "] valued at $" + String.format("%.2f", shares * stock.price);
    }
}

class PortFolio {
    private List<Transaction> tradeList;  // 持仓
    private double totalValue;

    public PortFolio() {
        tradeList = new ArrayList<Transaction>();
        totalValue = 0;
    }

    // buy
    public boolean update(Transaction t, double cashBalance) {
        if (t.shares * t.stock.price > cashBalance) {
            System.out.println("TRADING WARNING (not fatal): cash balance $" + String.format("%.2f", cashBalance) +
                    " too small to purchase " + t.shares + " shares of " + t.stock.symbol);
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
                    return false;
                } else {
                    p.shares -= t.shares;
                    if (p.shares == 0)
                        tradeList.remove(p);
                    return true;
                }
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
            t.action = "display";
            totalValue += t.shares * t.stock.price;  // todo: price should be...?
            System.out.println(t.toString());
        }
        System.out.println("Total stocks' value: $" + String.format("%.2f", totalValue));
    }

    public double getTotalValue() {
        return totalValue;
    }
}

public class Adversary {
    private String date;
    private String inputFilename;
    private double cashBalance;

    private List<Transaction> inputTradeList;  // 委托
    private PortFolio portfolio;

    public final int TRASACTION_FEE = 10;

    public Adversary(String date, String inputFilename, double cash) {
        this.date = date;
        this.inputFilename = inputFilename;
        this.cashBalance = cash;

        this.inputTradeList = new ArrayList<Transaction>();
        this.portfolio = new PortFolio();
    }

    public void execute() {
        try {
            readTrades();
            for (Transaction t: inputTradeList) {
                readStream(t);  // t's time might be changed, price will be set
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

    // TODO: what to do when short of money
    // TODO: 1% DCV
    private void buy(Transaction t) {
        if (!portfolio.update(t, cashBalance))
            return;

        double cashSpent = t.shares * t.stock.price;
        cashBalance -= cashSpent + TRASACTION_FEE;

        // 03 15:59 1461 AMSC bought at $3.45; cash spent $5040.45; cash balance $94949.55
        System.out.print(t.toString() + "; ");
        System.out.print("cash spent $" + String.format("%.2f", cashSpent) + "; ");
        System.out.println("cash balance $" + String.format("%.2f", cashBalance));
    }

    private void sell(Transaction t) {
       if (!portfolio.update(t))
           return;

        double cashAcquired = t.shares * t.stock.price;
        cashBalance += cashAcquired - TRASACTION_FEE;

        // 31 15:59 1461 AMSC sold at $4.37; cash acquired $6384.57; cash balance $10258.96
        System.out.print(t.toString() + "; ");
        System.out.print("cash acquired $" + String.format("%.2f", cashAcquired) + "; ");
        System.out.println("cash balance $" + String.format("%.2f", cashBalance));
    }

    public void displayAccountStatus() {
        System.out.println("Account status: \nCash balance is $" + String.format("%.2f", cashBalance));
        portfolio.display();
        System.out.println("TOTAL: $" + String.format("%.2f", cashBalance + portfolio.getTotalValue()));
    }

    private void readTrades() throws Exception{
        FileReader reader = new FileReader(new File("output/" + inputFilename));
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


    private void readStream(Transaction t) throws Exception{
        String filePath = "D:/下载/streaming-tsv/" + date.substring(0,4) + "/" + date.substring(5) + "/" + t.day + "/streaming.tsv";
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
//                    System.out.println(line);
                    setTradingPrice(tokens, t);
                    return;
                }
            }
        }

        // if no transaction at the given time, then go get at the closest time before that
        setTradingPrice(transactions.get(transactions.size() - 1), t);
    }

    private void setTradingPrice(String[] tokens, Transaction t) {
        double price;
        if (t.action.equals("buy")) {
            if (tokens[tokens.length - 1].equals("N/A"))
                price = Double.parseDouble(tokens[2]); // TODO: what to do with N/A?
            else
                price = Double.parseDouble(tokens[tokens.length - 1]);  // asking price
        }
        else if (t.action.equals("sell")){
            if (tokens[tokens.length - 2].equals("N/A"))
                price = Double.parseDouble(tokens[2]); // TODO: what to do with N/A?
            else
                price = Double.parseDouble(tokens[tokens.length - 2]);  // bidding price
        }
        else {  // display
            price = Double.parseDouble(tokens[2]);
        }
        t.time = tokens[1];
        t.stock.setPrice(price);
    }

    public static void main(String[] args) {
        Adversary adversary = new Adversary("2011/10", "2011-10-buy.txt", 100000);
        adversary.execute();
    }
}
