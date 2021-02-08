import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: FileHTMLParser2
 * @Description:
 * @Author: SQ
 * @Date: 2021-2-5
 */
public class FileHTMLParser2 {

    private String inputFilename;
    private String inputFilePath;
    private List<String> attributes;
    private List<String> values;

    FileHTMLParser2(String inputFilename, String inputFilePath) {
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
        Document document = Jsoup.parse(inputFile, "UTF-8", "");

        for (int i = 6; i <= 20; i += 2) {
            Elements trs = document.select("#yfncsumtab > tbody > tr:nth-child(2) > td:nth-child(1) > " +
                    "table:nth-child(" + i + ") > tbody > tr > td > table > tbody");
            for (Element tr: trs) {
//                String name = tr.child(0).ownText();
//                String value = tr.child(1).ownText();
//                System.out.printf("%s:\t%s\n", name, value);
                System.out.println(tr.text());
            }
        }

        for (int i = 6; i <= 10; i += 2) {
            Elements trs = document.select("#yfncsumtab > tbody > tr:nth-child(2) > td:nth-child(3) > " +
                    "table:nth-child(" + i + ") > tbody > tr > td > table > tbody");
            for (Element tr: trs) {
//                String name = tr.child(0).ownText();
//                String value = tr.child(1).text();
//                System.out.printf("%s:\t%s\n", name, value);
                System.out.println(tr.text());
            }
        }

    }

    public List<String> getAttributes() {
        return attributes;
    }

    public List<String> getValues() {
        return values;
    }
}
