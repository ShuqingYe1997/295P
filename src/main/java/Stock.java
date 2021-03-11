/**
 * @ClassName: Stock
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-28
 */
public class Stock {
    String symbol;
    double price;

    public Stock(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }

    public Stock(String symbol) {
        this.symbol = symbol.toUpperCase();
    }

    @Override
    public String toString() {
        return symbol + " " + price;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
