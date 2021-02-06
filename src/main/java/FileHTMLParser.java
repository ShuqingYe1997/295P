import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * @ClassName: FileHTMLParser
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-3
 */
public class FileHTMLParser {
    private String inputFilename;
    private String inputFilePath;
    private List<String> attributes;
    private List<String> values;

    // handle duplicated labels
    private final Set<String> DUPLICATED_ATTRIBUTE_SET = new HashSet<String>(Arrays.asList(
            "Daily Volume", "52-Week Change", "Earnings", "Shares Short"));

    // uniform header
    private final List<String> HEADER = new ArrayList<String>(Arrays.asList("Company","52-Week Low",
            "Recent Price","52-Week High","Beta","Daily Volume (3-month avg)","Daily Volume (10-day avg)",
            "52-Week Change","52-Week Change relative to S&P500","Market Capitalization","Shares Outstanding",
            "Float","Annual Dividend","Dividend Yield","Last Split","Book Value","Earnings (ttm)","Earnings (mrq)","Sales (per share)",
            "Cash","Price/Book","Price/Earnings","Price/Sales","Sales (ttm)","EBITDA","Income","Profit Margin",
            "Operating Margin","Fiscal Year Ends","Most recent quarter",
            "Return Assets","Return Equity","Current Ratio","Debt/Equity","Total Cash","Shares Short","Percent of Float",
            "Shares Short (Prior Month)","Short Ratio","Daily Volume"));

    FileHTMLParser(String inputFilename, String inputFilePath) {
        this.inputFilename = inputFilename;
        this.inputFilePath = inputFilePath;

        // Add an attribute
        this.attributes = new ArrayList<String>();
        this.attributes.add("Company");

        // Value == company name (without .html)
        this.values = new ArrayList<String>();
        this.values.add(inputFilename);
    }


    public void parseFile() throws IOException {
        File inputFile = new File(inputFilePath);
        Document document = Jsoup.parse(inputFile, "UTF-8", "http://biz.yahoo.com/");

        // The middle section of the table
        Elements table = document.select("body > table:nth-child(7) > tbody > tr:nth-child(1)");
        Elements subTables = table.select("tbody");
        // 3 subTables
        for (Element subTable: subTables) {
            // remove sub-headers
            Elements trs = subTable.select("tr").not("[align=\"center\"]");
            for (Element tr: trs) {
                if (tr.childrenSize() >= 2) {
                    Element name = tr.child(0);
                    Element value = tr.child(1);
//                    System.out.printf("%s:\t%s\n", handleAttributeName(name), handleAttributeValue(value));
                    attributes.add(handleAttributeName(name));
                    values.add(handleAttributeValue(value));
                }
            }
        }
    }

    public List<String> getAttributes() {
        return HEADER;
    }

    public List<String> getValues() {
        List<String> res = new ArrayList<String>();
        for (int j = 0; j < HEADER.size(); j++)
            res.add("none");

        for (int j = 0, i = 0; i < attributes.size() && j < HEADER.size(); ) {
            if (attributes.get(i).equals(HEADER.get(j))) {
                res.set(j, values.get(i));
                j++;
                i++;
            } else {
                res.set(j, "none");
                j++;
            }
        }
        return res;
    }

    // remove <small> from distinguishable attributes
    private String handleAttributeName(Element name) {
        // ownText! Thank you!!!
        String text = name.ownText();

        if (!DUPLICATED_ATTRIBUTE_SET.contains(text)) {
            if (text.equals("Sales")) { // Deal with the first "Sales" in Per-Share Data
                DUPLICATED_ATTRIBUTE_SET.add("Sales");
                text += " (per share)";
            }
            if (text.equals("")) {  // handle "Last Split"
                text = "Last Split";
            }
        }
        else {
            text = name.text(); // attach <small> to duplicated attributes
        }
        return text;
    }

    // remove "$" and set date to "none"
    private String handleAttributeValue(Element value) {
        String text = value.ownText();  // get "-"
        String figure = value.select("tt").text();  // get absolute value

        String res = text + figure;
        if (res.equals(""))
            res = "none";
        return res;
    }
}
