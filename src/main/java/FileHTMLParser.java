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
    private File inputFile;

    public final Set<String> DUPLICATED_ATTRIBUTE_SET = new HashSet<String>(Arrays.asList(
            "Daily Volume", "52-Week Change", "Earnings",
            "Most recent quarter", "Shares Short"));

    FileHTMLParser(String inputFilename) {
        this.inputFilename = inputFilename;
        this.inputFile = null;
    }

    public void readFile() throws NullPointerException {
        String filePath = FileHTMLParser.class.getClassLoader().getResource(inputFilename).getPath();
        this.inputFile = new File(filePath);
    }

    public void parseFile() throws IOException {
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
                    System.out.printf("%s:\t%s\n", handleAttributeName(name), handleAttributeValue(value));
                }
            }
        }
    }

    public void saveFile() {

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
