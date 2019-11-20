package edu.hacettepe;

import com.jeejava.server.MyServer;
import com.jeejava.server.ShutDown;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends Application {


    private static String downloadFileLocation = "crawledpages/";
    private MyServer server;

    Label logger;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(grid, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Site Downloader");

        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label urls = new Label("Site Urls");
        grid.add(urls, 0, 1);

        TextArea websiteUrls = new TextArea();
        grid.add(websiteUrls, 1, 1);


        GridPane gridInner = new GridPane();
        gridInner.setAlignment(Pos.TOP_LEFT);
        gridInner.setHgap(10);
        gridInner.setVgap(10);
        gridInner.setPadding(new Insets(5, 5, 5, 5));

        grid.add(gridInner,1,2);

        Button btnDownloadHtml = new Button();
        btnDownloadHtml.setText("Download as Html");
        btnDownloadHtml.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    for(String url : parseUrls(websiteUrls.getText())){
                        writeScreenLog("Html crawling started for " + url);
                        HtmlCrawler(url);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnDownloadImages = new Button();
        btnDownloadImages.setText("Download Site Images");
        btnDownloadImages.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    for(String url : parseUrls(websiteUrls.getText())){
                        writeScreenLog("Image crawling started for " + url);
                        imageCrawler(url);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnScreenShot = new Button();
        btnScreenShot.setText("Take Screenshot");
        btnScreenShot.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {

                    for(String url : parseUrls(websiteUrls.getText())){
                        Snapshotter snapshotter = new Snapshotter(url,downloadFileLocation);
                        snapshotter.getWebsiteSnapshot();
                        writeScreenLog("Screenshot saved for " + url);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        gridInner.add(btnDownloadHtml, 0,1);
        gridInner.add(btnDownloadImages, 1, 1);
        gridInner.add(btnScreenShot, 2, 1);

        // create a File chooser
        DirectoryChooser downloadLocationPicker = new DirectoryChooser();

        // create a Label
        Label filePickerLabel = new Label("no location specified");

        // create a Button
        Button btnSelectFile = new Button("Select Download Location");

        btnSelectFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // get the file selected
                File file = downloadLocationPicker.showDialog(primaryStage);

                if (file != null) {

                    filePickerLabel.setText(file.getAbsolutePath() + File.separator
                            + "  selected");
                    downloadFileLocation = file.getAbsolutePath() + File.separator;
                    writeScreenLog("Download location will be: " + downloadFileLocation);
                }
            }
        });


        gridInner.add(filePickerLabel,0,2);
        gridInner.add(btnSelectFile,1,2);


        Button btnServerStart = new Button();
        btnServerStart.setText("Start Server");
        btnServerStart.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    createWebServer(downloadFileLocation,9000);
                    writeScreenLog("Web server started at " + "http://localhost" + ":" +9000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        gridInner.add(btnServerStart,0,3);

        Button btnServerStop = new Button();
        btnServerStop.setText("Stop Server");
        btnServerStop.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                try {
                    new ShutDown(server).start();
                    writeScreenLog("Server stopped");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final HBox hbServer = new HBox();
        hbServer.setSpacing(5);
        hbServer.setAlignment(Pos.CENTER);
        hbServer.getChildren().addAll(btnServerStart,btnServerStop);

        gridInner.add(hbServer,1,3);

        logger = new Label();

        //Create a scroll pane, setting scrlLabel as the content.
        ScrollPane scrlPane = new ScrollPane(logger);
        scrlPane.setPrefViewportWidth(130);
        scrlPane.setPrefViewportHeight(80);
        //Enable panning.
        scrlPane.setPannable(true);

        grid.add(scrlPane,1,3);

        primaryStage.show();




    }

    public void createWebServer(String serverHome,int port){

        server = new MyServer(serverHome,port);

        Thread thread = new Thread(server);
        thread.start();
        /*try {
            thread.join();
        } catch (Exception e) {
        }*/
    }

    public static void HtmlCrawler(String url) throws Exception  {

        String crawlStorageFolder = downloadFileLocation + "crawmeta";
        int numberOfCrawlers = 4;

        CrawlConfig config = new CrawlConfig();
        config.setIncludeHttpsPages(true);
        //config.setIncludeBinaryContentInCrawling(true);

        config.setMaxDepthOfCrawling(1);

        config.setCrawlStorageFolder(crawlStorageFolder);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // Where should the downloaded images be stored?
        File storageFolder = new File(downloadFileLocation + File.separator + url.replace("http://","").replace("https://","") + "-html" +File.separator);

        // Since images are binary content, we need to set this parameter to
        // true to make sure they are included in the crawl.
        config.setIncludeBinaryContentInCrawling(true);

        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        List<String> crawlDomains = Arrays.asList(url);


        // For each crawl, you need to add some seed urls. These are the first
        // URLs that are fetched and then the crawler starts following links
        // which are found in these pages

        for (String domain : crawlDomains) {
            controller.addSeed(domain);
        }

        // The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<HtmlCrawler> factory = () -> new HtmlCrawler(storageFolder,crawlDomains);

        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(factory, numberOfCrawlers);
    }

    public static void imageCrawler(String url) throws Exception {
        CrawlConfig config = new CrawlConfig();

        // Set the folder where intermediate crawl data is stored (e.g. list of urls that are extracted from previously
        // fetched pages and need to be crawled later).
        config.setCrawlStorageFolder(downloadFileLocation + "crawlmetaimg");
        config.setIncludeHttpsPages(true);

        // Number of threads to use during crawling. Increasing this typically makes crawling faster. But crawling
        // speed depends on many other factors as well. You can experiment with this to figure out what number of
        // threads works best for you.
        int numberOfCrawlers = 4;

        // Where should the downloaded images be stored?
        File storageFolder = new File(downloadFileLocation + File.separator + url.replace("http://","").replace("https://","") + "-images" +File.separator);

        // Since images are binary content, we need to set this parameter to
        // true to make sure they are included in the crawl.
        config.setIncludeBinaryContentInCrawling(true);

        List<String> crawlDomains = Arrays.asList(url);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        for (String domain : crawlDomains) {
            controller.addSeed(domain);
        }

        if (!storageFolder.exists()) {
            storageFolder.mkdirs();
        }

        CrawlController.WebCrawlerFactory<ImageCrawler> factory = () -> new ImageCrawler(storageFolder, crawlDomains);
        controller.start(factory, numberOfCrawlers);
    }

    public List<String> parseUrls(String urls){
        List<String> parsed = new ArrayList();
        if(urls != null && !urls.isEmpty()){
            if(urls.contains(",")){
                parsed =  Arrays.asList(urls.split(","));
            }
            if(urls.contains(";")){
                parsed=  Arrays.asList(urls.split(";"));
            }
            if(urls.contains(System.getProperty("line.separator"))){
                parsed =  Arrays.asList(urls.split(System.getProperty("line.separator")));
            }
            else
                parsed.add(urls);

        }
        for(int i = 0; i < parsed.size(); i++){
            if(!parsed.get(i).startsWith("http")){
                parsed.set(i,"http://".concat(parsed.get(i)));
                writeScreenLog(parsed.get(i).concat(" used for crawling (http schema not exist)") );
            }
        }

        return parsed;
    }

    public void writeScreenLog(String text){
        logger.setText(logger.getText().concat(text.concat(System.getProperty("line.separator"))));
    }
}
