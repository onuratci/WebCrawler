package edu.hacettepe;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Marker;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class HtmlCrawler extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");

    private final File storageFolder;
    private final List<String> crawlDomains;

    public HtmlCrawler(File storageFolder, List<String> crawlDomains) {
        this.storageFolder = storageFolder;
        this.crawlDomains = ImmutableList.copyOf(crawlDomains);
    }

    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "https://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
                /*&& href.startsWith("https://www.ics.uci.edu/")*/;
    }

    /**
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        System.out.println("URL: " + url);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();
            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            // Get a unique name for storing this image
            String pageName = FilenameUtils.getName(page.getWebURL().getURL());



            String realPath = page.getWebURL().getPath().replace(page.getWebURL().getPath().substring(page.getWebURL().getPath().lastIndexOf("/")),"");


            if(pageName== null || pageName.isEmpty() || pageName.equals("")){
                pageName = "index.htm";
            }
            if(!pageName.contains(".")){
                pageName = pageName.concat(".htm");
            }
            String filename =  storageFolder.getAbsolutePath() +  realPath + File.separator +  pageName;


            try {
                Files.createParentDirs(new File(filename));

                Files.write(((HtmlParseData) page.getParseData()).getHtml().getBytes(), new File(filename));
                WebCrawler.logger.info("Stored: {}", url);
            } catch (IOException iox) {
                WebCrawler.logger.error("Failed to write file: {}", filename, iox);
            }
        }
        else {
            System.out.println("not a html data.");
        }
    }
}
