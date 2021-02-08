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
    private String time;
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

    FileHTMLParser(String time, String inputFilename, String inputFilePath) {
        this.time = time;
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
        if (time.startsWith("2001"))
            parseFile1();
        else
            parseFile2();
    }


    /*
     * @Description: A parser for 2001
     * @Param:
     * @Return:
     * @Author: SQ
     * @Date: 2021-2-8
     **/
    private void parseFile1() throws IOException {
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

    /*
     * @Description: A parser for 2006 and 2011
     * @Param: []
     * @Return: void
     * @Author: SQ
     * @Date: 2021-2-8
     **/
    private void parseFile2() throws IOException {
        File inputFile = new File(inputFilePath);
        Document document = Jsoup.parse(inputFile, "UTF-8", "");

        for (int i = 6; i <= 20; i += 2) {
            // skip "table:nth-child(8) > tbody > tr > td > table > tbody"
            if (i == 8)
                continue;
            Element trs = document.select("#yfncsumtab > tbody > tr:nth-child(2) > td:nth-child(1) > " +
                    "table:nth-child(" + i + ") > tbody > tr > td > table > tbody").first();
            if (trs == null)
                return;


            for (Element tr: trs.select("tr")) {
                if (tr.childrenSize() < 2)
                    continue;
                String name = tr.child(0).ownText();
                String value = tr.child(1).ownText();
//                System.out.printf("%s\t%s\n", name, value);
                attributes.add(name);
                values.add(value);
            }
        }

        for (int i = 6; i <= 10; i += 2) {
            Element trs = document.select("#yfncsumtab > tbody > tr:nth-child(2) > td:nth-child(3) > " +
                    "table:nth-child(" + i + ") > tbody > tr > td > table > tbody").first();
            // handle table "Share Statistics"
            if (i == 8)
                trs = document.select("#yfncsumtab > tbody > tr:nth-child(2) > td:nth-child(3) > " +
                        "table:nth-child(" + i + ") > tbody > tr > td > table > tbody").last();

            if (trs == null)
                return;

            for (Element tr: trs.select("tr")) {
                if (tr.childrenSize() < 2)
                    continue;
                String name = tr.child(0).ownText();
                String value = tr.child(1).text();
//                System.out.printf("%s\t%s\n", name, value);
                attributes.add(name);
                values.add(value);
            }
        }
    }

    public List<String> getAttributes() {
        if (!time.startsWith("2001"))
            return attributes;
        return HEADER;
    }

    public List<String> getValues() {
        if (!time.startsWith("2001"))
            return values;
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
