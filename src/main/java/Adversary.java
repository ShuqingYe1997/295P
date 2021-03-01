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

class Trade {
    String day;
    String time;
    String action;
    int shares;
    Stock stock;

    public Trade() {}

    public Trade(String day, String time, String action, int shares, Stock stock) {
        this.day = day;
        this.time = time;
        this.action = action;
        this.shares = shares;
        this.stock = stock;
    }

    // 31 15:59 1461 AMSC sold at $4.37
    public String toString() {
        return day + " " + time + " " + shares + " " + stock.symbol + " "
                + (this.action.equals("buy") ? "bought" : "sold")+ " at $" + stock.price;
    }
}

public class Adversary {
    private String date;
    private String inputFilename;
    private double cashBalance;

    private List<Trade> inputTradeList;  // 委托
    private List<Trade> actualTradeList;  // 成交
//TODO    private List<Hold> portfolio;  // 持仓

    public final int TRASACTION_FEE = 10;

    public Adversary(String date, String inputFilename, double cash) {
        this.date = date;
        this.inputFilename = inputFilename;
        this.cashBalance = cash;
        this.inputTradeList = new ArrayList<Trade>();
        this.actualTradeList = new ArrayList<Trade>();
    }

    public void execute() {
        try {
            readTrades();
            for (Trade t: inputTradeList) {
                readStream(t);
                if (t.action.equals("buy")) {
                    buy(t);
                }
                else if (t.action.equals("sell"))
                    sell(t);
                else
                    System.out.println("Syntax error in " + inputFilename + "!!!");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: what to do when short of money
    // TODO: 1% DCV
    private void buy(Trade t) {
        double cashSpent = t.shares * t.stock.price;
        if (cashSpent > cashBalance) {
            System.out.println("TRADING WARNING (not fatal): cash balance " + String.format("%.2f", cashBalance) +
                    " too small to purchase " + t.shares+ " shares of " + t.stock.symbol);
            return;
        }
        actualTradeList.add(t);

        cashBalance -= cashSpent + TRASACTION_FEE;
        // 03 15:59 1461 AMSC bought at $3.45; cash spent $5040.45; cash balance $94949.55
        System.out.print(t.toString() + "; ");
        System.out.print("cash spent $" + String.format("%.2f", cashSpent) + "; ");
        System.out.println("cash balance $" + String.format("%.2f", cashBalance));
    }

    private void sell(Trade t) {
        int totalShares = 0;
        for (Trade actualT: actualTradeList) {
            if (actualT.stock.symbol.equals(t.stock.symbol))
                totalShares += actualT.shares;
        }
        if (totalShares < t.shares) {
            System.out.println("TRADING WARNING (not fatal): you only have " + totalShares +
                    " shares of "+ t.stock.symbol +"; you cannot sell " + t.shares + " shares");
            return;
        }

        actualTradeList.add(t);

        double cashAcquired = t.shares * t.stock.price;
        cashBalance += cashAcquired - TRASACTION_FEE;

        // 31 15:59 1461 AMSC sold at $4.37; cash acquired $6384.57; cash balance $10258.96
        System.out.print(t.toString() + "; ");
        System.out.print("cash acquired $" + String.format("%.2f", cashAcquired) + "; ");
        System.out.println("cash balance $" + String.format("%.2f", cashBalance));
    }

    private void readTrades() throws Exception{
        FileReader reader = new FileReader(new File("output/" + inputFilename));
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            // 03 15:59 buy 2500 shares of RDN
            String[] tokens = line.split("\\s+");
            inputTradeList.add(new Trade(tokens[0], tokens[1], tokens[2],
                    Integer.parseInt(tokens[3]), new Stock(tokens[tokens.length - 1])));
        }
    }


    private void readStream(Trade t) throws Exception{
        // input/2011/10/03/streaming.tsv
        String filePath = "input/" + date.substring(0,4) + "/" + date.substring(5) + "/" + t.day + "/streaming.tsv";
        FileReader reader = new FileReader(new File(filePath));
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith(t.stock.symbol + "\t" + t.time)) {
//                System.out.println(line);
                String[] tokens = line.split("\t");
                double askingPrice, biddingPrice;
                if (t.action.equals("buy")) {
                    if (tokens[tokens.length - 1].equals("N/A"))
                        askingPrice = Double.parseDouble(tokens[2]); // TODO: what to do with N/A?
                    else
                        askingPrice = Double.parseDouble(tokens[tokens.length - 1]);
                    t.stock.setStartingPrice(askingPrice);  // buy
                }
                else  {
                    if (tokens[tokens.length - 2].equals("N/A"))
                        biddingPrice = Double.parseDouble(tokens[2]); // TODO: what to do with N/A?
                    else
                        biddingPrice = Double.parseDouble(tokens[tokens.length - 2]);
                    t.stock.setEndPrice(biddingPrice);  // sell
                }
                return;  // hit
            }
        }

        //TODO if not find at the given time, then go get at the closest time
    }

    public static void main(String[] args) {
        Adversary adversary = new Adversary("2011/10", "2011-10-trades.txt", 100000);
        adversary.execute();
    }
}
