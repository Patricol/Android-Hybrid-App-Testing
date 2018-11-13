package gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import logger.LogFileGenerator;
import serverDriver.SelendroidServerDriver;
import testers.AutomatedTester;
import testers.DepthFirstTester;
import testers.LogReplayTester;
import testers.RandomTester;
import testers.BreadthFirstTester;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

//"appId":"com.google.zxing.client.android:4.7.3"
//"appId":"com.netflix.mediaclient:3.8.2 build 3903"
//"appID":"com.evernote:6.0.1"	
//"appID": "com.untappdllc.app:3.0.7"

 
public class OldMainGUI extends Application{
	public static void main(String[] args) {
	     Application.launch(OldMainGUI.class, args);
	 }

    private static String logFileMessageEndOfTesting = "--- Testing terminated ---\n";
	
	private Stage startWindow, popupWindow;
	private Scene sceneSelendroidLoaded, loadingScene;
	private TextArea console, hierarchy, recentInput;
	
	private SelendroidServerDriver newDriver;
	private AutomatedTester automatedTester;
	private LogFileGenerator logFileGenerator;
	private RandomTester randomTester;
    private DepthFirstTester depthTester;
    private BreadthFirstTester breadthTester;
	private LogReplayTester logTester;
	
    private int newExtraction = 0;
    private int interval = 1000;
    private String apkPath = "src/employee-directory.apk";
    private String directoryPath = ""; // the string of the path for log file directory
    private String prefPath = ""; // the string of the input preference file
    private String inputFilePath = "";
    private ArrayList<String> testInput = new ArrayList<>();
	private final Label logDirectory = new Label();
    private final Label inputChooserLabel = new Label();
 
    
    @Override
    public void start(final Stage primaryStage) {
    	startWindow = primaryStage;
        primaryStage.setTitle("Input Generator Application - Alpha");
        primaryStage.getIcons().add(new Image("icon.png"));
        
        
///////////////////////////////////////// Setting up the elements of GUI /////////////////////////////////////////
        
        // Elements on the start screen
        // APK file chooser
        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("APK files (*.apk)", "*.apk");
        fileChooser.getExtensionFilters().add(extFilter);
        // Input file chooser
        final FileChooser inputFileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter2 = new FileChooser.ExtensionFilter("Input Text File (*.txt)", "*.txt");
        inputFileChooser.getExtensionFilters().add(extFilter2);
        
        // Log file directory chooser
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        // buttons
        final Button inputChooserBtn = new Button("Select User Test Input");
        final Button fileChooserBtn = new Button("Select APK");
        final Button directoryChooserBtn = new Button("Select Log Directory");
        final Button btn = new Button("Start Selendroid");

        // Capabilities text field
        final TextField capabilities = new TextField("io.selendroid.directory:0.0.1");
        // Labels
        final Label caps = new Label("Selendroid Capabilities:");
        final Label currentApk = new Label(apkPath);
        
        // Elements on the pop-up menu
        // For time-between-input selection
        final Label inputRate = new Label("Time Between Inputs (ms) ");
        final Label currentPref = new Label("  None");
        final Button inputPref = new Button("Select Input Preference File");
        final Button confirm = new Button("Save");
        final ComboBox time = new ComboBox();
        time.getItems().addAll("1000","2000","3000");
        time.getSelectionModel().selectFirst();
        // File chooser for text file only
        final FileChooser prefChooser = new FileChooser();
        FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
        prefChooser.getExtensionFilters().add(textFilter);
        // Pop-up pane
        GridPane popupPane = new GridPane();
        popupPane.setVgap(5); // Vertical gap in pixels
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(20);
        RowConstraints r1 = new RowConstraints();
        r1.setPercentHeight(30);
        popupPane.getColumnConstraints().add(c1);
        popupPane.getRowConstraints().add(r1);
        popupPane.add(inputRate, 1, 1);
        popupPane.add(time, 2, 1);
        popupPane.add(inputPref, 1, 2);
        popupPane.add(currentPref, 2, 2);
        GridPane.setMargin(confirm, new Insets(30, 0, 0, 0));
        popupPane.add(confirm, 1, 3);
        // Make another stage for pop-up scene
        popupWindow = new Stage();
        popupWindow.setScene(new Scene(popupPane, 400, 250));
        popupWindow.initModality(Modality.APPLICATION_MODAL); // Tell the stage it is meant to pop-up
        popupWindow.setTitle("Settings");
        
        // Loading label on the loading screen
        final Label loading = new Label("Loading...");
        
        
        // Elements on the main screen
        // First menu bar item
        Menu fileMenu = new Menu ("_File"); // When you hold alt, you can hotkey "F" to access this menu
        //First dropdown tab items
        fileMenu.getItems().add(new MenuItem("Example1"));
        fileMenu.getItems().add(new MenuItem("Example2"));
        fileMenu.getItems().add(new MenuItem("Example3"));
        fileMenu.getItems().add(new SeparatorMenuItem()); // Add a horizontal line in the dropdown menu
        MenuItem settings = new MenuItem("Settings");
        fileMenu.getItems().add(settings);
        fileMenu.getItems().add(new SeparatorMenuItem());
        MenuItem menuExit = new MenuItem("Exit");
        fileMenu.getItems().add(menuExit);
        
        // Second menu bar item
        Menu testingMenu = new Menu ("_Testing"); // When you hold alt, you can hotkey "T" to access this menu
        // Second dropdown tab items
        final MenuItem menuGenerateRandomInput = new MenuItem("Random Single Input");
        testingMenu.getItems().add(menuGenerateRandomInput);
        testingMenu.getItems().add(new SeparatorMenuItem());
        final MenuItem menuReplayLogFile = new MenuItem("Replay Log File");
        testingMenu.getItems().add(menuReplayLogFile);
        
        // Log file chooser
        final FileChooser logFileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter3 = new FileChooser.ExtensionFilter(String.format("Choose log file to replay (*%s)", LogFileGenerator.logFileDefaultExtension), String.format("*%s", LogFileGenerator.logFileDefaultExtension));
        logFileChooser.getExtensionFilters().add(extFilter3);
        
        inputFileChooser.getExtensionFilters().add(extFilter2);
        testingMenu.getItems().add(new SeparatorMenuItem());
        final MenuItem menuBeginRandom = new MenuItem("Begin random testing");
        testingMenu.getItems().add(menuBeginRandom);
        testingMenu.getItems().add(new SeparatorMenuItem());
        final MenuItem menuBeginDepth = new MenuItem("Begin depth first testing");
        testingMenu.getItems().add(menuBeginDepth);
        testingMenu.getItems().add(new SeparatorMenuItem());
        final MenuItem menuBeginBreadth = new MenuItem("Begin breadth first testing");
        testingMenu.getItems().add(menuBeginBreadth);
        testingMenu.getItems().add(new SeparatorMenuItem());
        final MenuItem menuStop = new MenuItem("Stop");
        menuStop.setDisable(true);
        testingMenu.getItems().add(menuStop);
        final MenuItem menuPause = new MenuItem("Pause");
        menuPause.setDisable(true);
        testingMenu.getItems().add(menuPause);
        final MenuItem menuResume = new MenuItem("Resume");
        menuResume.setDisable(true);
        testingMenu.getItems().add(menuResume);
        
        // Third menu bar item
        Menu viewMenu = new Menu ("_View");
        // Third dropdown tab items
        MenuItem clearConsole = new MenuItem("Clear Console");
        MenuItem clearLoggerView = new MenuItem("Clear Logger View");
        MenuItem resumeApp = new MenuItem("Resume App");
        MenuItem navigateBack = new MenuItem("Navigate Back"); 
        viewMenu.getItems().add(clearConsole);
        viewMenu.getItems().add(clearLoggerView);
        viewMenu.getItems().add(resumeApp);
        viewMenu.getItems().add(navigateBack); 
        
        
        // Fourth menu bar item. Temporary location for extraction functionality
        Menu extractMenu = new Menu("_Extraction");
        // Fourth dropdown tab items. 
        MenuItem entireSource = new MenuItem("Extract page source");
        extractMenu.getItems().add(entireSource);
        MenuItem htmlSource = new MenuItem("Extract HTML source (WebView only)");
        extractMenu.getItems().add(htmlSource);
        MenuItem webViewElements = new MenuItem("Extract all elements");
        extractMenu.getItems().add(webViewElements);
        MenuItem elementsByKeyword = new MenuItem("Elements by keyword (defaults to 'input')");
        extractMenu.getItems().add(elementsByKeyword);
        MenuItem extractHierarchy = new MenuItem("Extract hierarchy");
        extractMenu.getItems().add(extractHierarchy);
        
        //Menu bar
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, testingMenu, viewMenu, extractMenu);
        
        
        // Starting scene
        GridPane grid = new GridPane();
        grid.setVgap(5);
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(35);
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(40);
        grid.getColumnConstraints().add(column1);
        grid.getRowConstraints().add(row1);
        grid.add(menuBar,0,1);
        grid.add(btn, 2, 1);
        grid.add(directoryChooserBtn, 2, 4);
        grid.add(logDirectory, 3, 4);
        grid.add(inputChooserBtn, 2, 5);
        grid.add(inputChooserLabel, 3, 5);
        grid.add(fileChooserBtn, 2,2);
        grid.add(currentApk, 3, 2);
        grid.add(caps, 2, 3);
        grid.add(capabilities, 3, 3);
        primaryStage.setScene(new Scene(grid, 1280, 720));
        primaryStage.show();

        
        
        // Loading screen
        BorderPane loadingPane = new BorderPane();
        loadingPane.setCenter(loading);
        loadingScene = new Scene(loadingPane, 1280, 720);
        
        
        // Main scene after Selendroid loads
        BorderPane selendroidLoadedPane = new BorderPane();
        selendroidLoadedPane.setTop(menuBar);
        selendroidLoadedPane.setCenter(addConsoleSplitPane());
        sceneSelendroidLoaded = new Scene(selendroidLoadedPane, 1280, 720); // Set dimensions of main scene
        
        
///////////////////////////////////////// Menu items input handling /////////////////////////////////////////
        
        // What happens when you presses the button on the starting screen
        btn.setOnAction(event -> {
            btn.setDisable(true);
            startWindow.setScene(loadingScene);
            new Thread(() -> {
                newDriver = new SelendroidServerDriver(apkPath, capabilities.getText());
                logFileGenerator = new LogFileGenerator(directoryPath);
                String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456890!@#$%^&*()_+,./<>?;':";
                setGui();

                //scan the input file if it exists
                if(!inputFilePath.isEmpty()){
                    File inputFile = new File(inputFilePath);
                    try{
                        Scanner newScan = new Scanner(inputFile);
                        while(newScan.hasNextLine()){
                             testInput.add(newScan.nextLine());
                             System.out.print(testInput.size()+"");

                        }
                        newScan.close();
                    }
                    catch(FileNotFoundException e){
                        System.out.println("Input file read failed, using default");
                        testInput.add("a");
                    }

                }
                else{
                    char[] alphaArr = alpha.toCharArray();
                    for(char c: alphaArr){
                        testInput.add(""+c);
                    }
                }

                randomTester = new RandomTester(logFileGenerator, testInput, newDriver);
                depthTester = new DepthFirstTester(logFileGenerator, testInput, newDriver);
                breadthTester = new BreadthFirstTester(logFileGenerator, testInput, newDriver);
                automatedTester = new AutomatedTester(newDriver, randomTester);
                Platform.runLater(() -> {
                    startWindow.setScene(sceneSelendroidLoaded);//TODO : set this only on successful load
                });
            }).start();
        });
        

        
        // What happens when you press the button on the starting screen to select APK
        fileChooserBtn.setOnAction(event -> {
            File apk = fileChooser.showOpenDialog(primaryStage);
            if(apk != null){
                apkPath = apk.getPath();
                currentApk.setText(apkPath);
            }
        });
        
        // What happens when you press the button on the starting screen to select the test input
        inputChooserBtn.setOnAction(event -> {
            File inputFile = inputFileChooser.showOpenDialog(primaryStage);
            if(inputFile != null){
                inputFilePath = inputFile.getPath();
                inputChooserLabel.setText(inputFilePath);
            }
        });
        
        // What happens when you press the button on the starting screen to select log file directory
        directoryChooserBtn.setOnAction(event -> {
            File directory = directoryChooser.showDialog(primaryStage);
            if(directory != null){
               logDirectory.setText(directoryPath = directory.getPath());
            }
        });
         
       // What happens when you hit generate random input on menubar
         menuGenerateRandomInput.setOnAction(event -> {
             if(newExtraction == 1){
                 try {
                     randomTester.test(newDriver.possibleTargets);
                     newExtraction = 0;
                 } catch (Exception e) {
                     console.appendText(e.getMessage());
                 }
             }
             else{
                 console.appendText("Please extract elements! \n");
             }
         });
         
         // What happens when you hit Replay Log File on menubar
         menuReplayLogFile.setOnAction(event -> {
              File logFile = logFileChooser.showOpenDialog(primaryStage);
              logTester = new LogReplayTester(newDriver, logFile, recentInput);
             menuBeginDepth.setDisable(true);
             menuBeginBreadth.setDisable(true);
              menuBeginRandom.setDisable(true);
              menuStop.setDisable(false);
              menuPause.setDisable(false);
              menuGenerateRandomInput.setDisable(true);
             console.appendText("Testing has begun...\n");
             automatedTester.setTester(logTester);
             automatedTester.startTesting();
         });
         
        // What happens when you hit exit on menubar
        menuExit.setOnAction(event -> {
            logFileGenerator.close();
            newDriver.end();
            primaryStage.close();
            System.exit(0);
        });
        menuBeginDepth.setOnAction(event -> {
            menuBeginDepth.setDisable(true);
            menuBeginBreadth.setDisable(true);
            menuBeginRandom.setDisable(true);
            menuStop.setDisable(false);
            menuPause.setDisable(false);
            menuGenerateRandomInput.setDisable(true);
            console.appendText("Testing has begun...\n");
            automatedTester.setTester(depthTester);
            automatedTester.startTesting();
        });
        menuBeginBreadth.setOnAction(event -> {
            menuBeginDepth.setDisable(true);
            menuBeginBreadth.setDisable(true);
            menuBeginRandom.setDisable(true);
            menuStop.setDisable(false);
            menuPause.setDisable(false);
            menuGenerateRandomInput.setDisable(true);
            console.appendText("Testing has begun...\n");
            automatedTester.setTester(breadthTester);
            automatedTester.startTesting();
        });
        // What happens when you hit begin on menubar: testingMenu.Begin
        menuBeginRandom.setOnAction(event -> {
            menuBeginDepth.setDisable(true);
        menuBeginBreadth.setDisable(true);
            menuBeginRandom.setDisable(true);
            menuStop.setDisable(false);
            menuPause.setDisable(false);
            menuGenerateRandomInput.setDisable(true);
            console.appendText("Testing has begun...\n");
            automatedTester.setTester(randomTester);
            automatedTester.startTesting();
        });
        
        // What happens when you hit stop on menubar: testingMenu.Stop
        menuStop.setOnAction(event -> {
            menuBeginDepth.setDisable(false);
            menuBeginBreadth.setDisable(false);
            menuBeginRandom.setDisable(false);
            menuStop.setDisable(true);
            menuPause.setDisable(true);
            menuResume.setDisable(true);
            menuGenerateRandomInput.setDisable(false);
            automatedTester.stopTesting();
            console.appendText(logFileMessageEndOfTesting);
            logFileGenerator.log(logFileMessageEndOfTesting);
//            	logFileGenerator.close();
        });
        
        // What happens when you hit pause on menubar: testingMenu.Pause
        menuPause.setOnAction(event -> {
            menuStop.setDisable(false);
            menuPause.setDisable(true);
            menuResume.setDisable(false);
            console.appendText("Testing paused...\n");
            automatedTester.pauseInput();
        });
        
        // What happens when you hit resume on menubar: testingMenu.Resume
        menuResume.setOnAction(event -> {
            menuStop.setDisable(false);
            menuPause.setDisable(false);
            menuResume.setDisable(true);
            console.appendText("Testing resumed...\n");
            automatedTester.resumeInput();
        });
        
        // What happens when you hit clear console
    	clearConsole.setOnAction(event -> console.clear());
    	
    	// Clears the logger view screen
    	clearLoggerView.setOnAction(event -> recentInput.clear());
    	// What happens when you hit resume app on menubar
    	// Resumes the app.... IF IT WAS ACTUALLY IMPLEMENTED AHHHH 
    	resumeApp.setOnAction(event -> newDriver.resumeApp());
        
        // Presses the android Back button 
        navigateBack.setOnAction(event -> newDriver.webviewBack());
        
        // What happens when you click settings menu item
        settings.setOnAction(event -> popupWindow.showAndWait());
        
        // What happens when you press the button on the pop-up screen to select input preference file
        inputPref.setOnAction(event -> {
            File pref = prefChooser.showOpenDialog(popupWindow);
            if(pref != null){
                prefPath = pref.getPath();
                currentPref.setText(prefPath);
            }
        });
        
        // What happens when you click confirm on pop-up menu
        confirm.setOnAction(event -> {
            if (time.getValue() != null) {
                interval = Integer.parseInt((String) time.getValue());
            }
            popupWindow.close();
        });
        
///////////////////////////////////////// Extraction menu input handling /////////////////////////////////////////
        
    	// What happens when you hit extract all info
        entireSource.setOnAction(event -> {
            // Get the GUI info by elements, print it to console (for now)
            console.appendText("Extracting source of entire activity\n");
            console.appendText(newDriver.extractPageSource());
            newExtraction = 1;
        });
    	
        // What happens when you hit extract HTML
        htmlSource.setOnAction(event -> {
            // Get the GUI info by HTML, print it to console (for now)
            console.appendText("Extracting WebView as HTML source \n");
            console.appendText(newDriver.extractWebViewHTML());
        });
        
        // What happens when you hit extract hierarchy on menubar
        extractHierarchy.setOnAction(event -> {
            hierarchy.clear();
            String unformattedHierarchy = newDriver.extractHierarchy();
            StringBuilder str = new StringBuilder(unformattedHierarchy);

            int index = unformattedHierarchy.indexOf("</WebView>");
            if (index == -1) {
                index = unformattedHierarchy.indexOf("</CordovaWebView>");
            }
            str.insert(index, newDriver.extractElementNames());
            String formattedHierarchy = prettyFormat(str.toString(), 4);
            hierarchy.appendText(formattedHierarchy);

            newDriver.switchToWebView();
        });
        
        // What happens when you hit extract all GUI elements
        webViewElements.setOnAction(event -> {
            newExtraction = 1;
            String extraction;
            extraction = newDriver.extractElements();
            console.appendText(extraction);
        });
        
        // What happens when you hit extract elements by keyword
        elementsByKeyword.setOnAction(event -> {
            // Get the GUI info by elements, print it to console (for now)
            console.appendText("Extracting elements by keyword: Input \n");
            console.appendText(newDriver.extractElementsByKeyword("input"));
        });
    }

    
////////////////////////////////////////////////// Other methods //////////////////////////////////////////////////
    
    // Give LogFileGenerator the GUI it needs. 
    private void setGui(){
//    	logFileGenerator.setGUI(this);
    }
    
    /**Accept input from the logger*/
 	public void displayNewInput(String input) {
 		recentInput.appendText(input);
 	}
    // Set up nested Splitpane
    private SplitPane addViewSplitPane(){
    	SplitPane splitPane = new SplitPane();

    	final StackPane spleft = new StackPane();
    	recentInput = new TextArea();
    	recentInput.setEditable(false);
    	spleft.getChildren().add(recentInput);
    	
    	final StackPane spright = new StackPane();
    	hierarchy = new TextArea();
    	hierarchy.setEditable(false);
    	spright.getChildren().add(hierarchy);
    	
    	splitPane.getItems().addAll(spleft, spright);
    	splitPane.setDividerPositions(0.5f);
		return splitPane;
    }
   
    private SplitPane addConsoleSplitPane(){
    	SplitPane horizontalSplitPane = new SplitPane();
    	horizontalSplitPane.setOrientation(Orientation.VERTICAL);
    	
    	final StackPane sptop = new StackPane();
    	sptop.getChildren().add(addViewSplitPane()); // Set up nested Splitpane on top
    	
    	final StackPane spbottom = new StackPane();
    	console = new TextArea();
        console.appendText("Device found, loading Interface...\n");
        console.setEditable(false);
    	spbottom.getChildren().add(console);
    	
    	horizontalSplitPane.getItems().addAll(sptop, spbottom);
    	horizontalSplitPane.setDividerPositions(0.7f);
		return horizontalSplitPane;
    }
    
    /**Allows to add text to the console from elsewhere.*/
    public void appendToConsole(String text){
    	console.appendText(text + "\n");
    }
    
    /**
     * Transforms a raw XML string into indented XML string.
     * @param input: raw XML string
     * @param indent: indentation required
     * @return formatted XML string
     */
    private static String prettyFormat(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer(); 
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }

}