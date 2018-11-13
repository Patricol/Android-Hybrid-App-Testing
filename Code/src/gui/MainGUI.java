package gui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import logger.LogFileGenerator;
import serverDriver.SelendroidServerDriver;
import testers.AutomatedTester;
import testers.BreadthFirstTester;
import testers.DepthFirstTester;
import testers.LogReplayTester;
import testers.RandomTester;

/**
 * MainGUI provides a graphical interface for this testing suite of API calls. 
 * As such it gives users a centralized hub for extracting WebView elements, controlling
 * their APKs with various testers, and logging the coverage results of these tests. 
 */
public class MainGUI extends Application {
	private Stage mainStage;
	private Scene mainScene;
	
	private SelendroidServerDriver newDriver;
	private AutomatedTester automatedTester;
	private LogFileGenerator logFileGenerator;

	private TextArea console;
	private TextArea logger;
	private TextArea extraction;
	
	private Text testStatus;
	private Text selendroidStatus; 
	private Text apkLocStatus;
	
	private String apkPath;
	private String inputPath;
	private String logDirectory;
	
	private ArrayList<String> testInput = new ArrayList<>();
	
	private int speed = 0;

	private String statusSelRunning = "Selendroid: Running";
    private String statusSelStarting = "Selenroid: Starting...";
    private String statusSelStopped = "Selendroid: Stopped";
    private String statusTestRunning = "Tester: Running";
    private String statusTestStopped = "Tester: Stopped";
	private String capabilities;
	private String keyword;
	private String pauseText = "Pause";
	private String currentTesterString = "Depth-First Tester";

	/**
	 * The main application to be run. 
	 * 
	 * Note: in its current state MainGUI does not receive or interpret any invocation arguments.
	 * 
	 * @param args - simply passes any arguments given to the program on launch to the GUI
	 */
	public static void main(String[] args) {
		Application.launch(MainGUI.class, args);
	}

	/* (non-Javadoc)
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) {
		// Set up default values and create the main stage to run the GUI on
        apkPath = "";
        inputPath = "";
        logDirectory = "";

		mainStage = primaryStage;
		mainStage.setTitle("Android Hybrid App Tester | CSCI435 F17");
		mainStage.getIcons().add(new Image("icon.png"));
		mainScene = buildMainScene();
		
		logFileGenerator = new LogFileGenerator(logDirectory);
		
		mainStage.setScene(mainScene);
		mainStage.show();
	}
	
	/**
	 * Constructs the main scene upon which the GUI will be drawn.
	 * 
	 * @return the newly-created scene
	 */
	private Scene buildMainScene() {
		// Components for mainScene
		BorderPane mainPane = new BorderPane();
		
		mainPane.setBottom(buildStatusBar());
		mainPane.setCenter(buildMainSplitPane());
		mainPane.setTop(new VBox(buildMenuBar(), buildToolBar()));
		testStatus.setText(statusTestStopped);
		selendroidStatus.setText(statusSelStopped);

		return new Scene(mainPane);
	}

	/**
	 * Constructs the main menu bar to be used in the application 
	 * and all of the necessary handlers for the related components.
	 * 
	 * @return the newly-created menu bar
	 */
	private MenuBar buildMenuBar() {		
		// Components for menuBar
		// Create file menu items
		Menu fileMenu = new Menu("File");
		MenuItem openApk = new MenuItem("Open APK File...");
		MenuItem openInput = new MenuItem("Open Input File...");
		MenuItem saveExtractSummary = new MenuItem("Save Extraction/Summary...");
		MenuItem exit = new MenuItem("Exit");
		fileMenu.getItems().addAll(openApk, openInput, saveExtractSummary, new SeparatorMenuItem(), exit);
		
		// Create edit menu items
		Menu editMenu = new Menu("Edit");
		MenuItem logDir = new MenuItem("Log File Directory...");
		MenuItem navBack = new MenuItem("Navigate Back");
		MenuItem resumeApp = new MenuItem("Resume App");
		editMenu.getItems().addAll(logDir, navBack, resumeApp);
		
		// Create view menu items
		Menu viewMenu = new Menu("View");
		MenuItem clearConsole = new MenuItem("Clear Console");
		MenuItem clearLogger = new MenuItem("Clear Logger");
		MenuItem clearExtraction = new MenuItem("Clear Extraction");
		MenuItem clearAll = new MenuItem("Clear All");
		viewMenu.getItems().addAll(clearConsole, clearLogger, clearExtraction, clearAll);
		
		// Create tester menu items
		// Testers are in a radio menu so only one can be selected at a time. Default is Depth-First.
		Menu testMenu = new Menu("Test");
		ToggleGroup testToggle = new ToggleGroup();
		RadioMenuItem depthRadio = new RadioMenuItem("Depth-First Coverage Tester");
		depthRadio.setToggleGroup(testToggle);
		depthRadio.setSelected(true);
		RadioMenuItem breadthRadio = new RadioMenuItem("Breadth-First Coverage Tester");
		breadthRadio.setToggleGroup(testToggle);
		RadioMenuItem randomRadio = new RadioMenuItem("Randomized Coverage Tester");
		randomRadio.setToggleGroup(testToggle);
		RadioMenuItem replayRadio = new RadioMenuItem("Replay Previous Log File");
		replayRadio.setToggleGroup(testToggle);
		MenuItem randomSingle = new MenuItem("Random Single Input");
		testMenu.getItems().addAll(depthRadio, breadthRadio, randomRadio, replayRadio, new SeparatorMenuItem(), randomSingle);
		
		// Create extract menu items
		Menu extractMenu = new Menu("Extract");
//		MenuItem extractName = new MenuItem("Extract by Name");
		MenuItem extractSource = new MenuItem("Extract Page Source");
//		MenuItem extractHierarchy = new MenuItem("Extract Hierarchy");
		MenuItem extractWebView = new MenuItem("Extract WebView");
		MenuItem extractAll = new MenuItem("Extract All");
		extractMenu.getItems().addAll(extractSource, extractWebView, extractAll);
		
		// Create help menu items
		Menu helpMenu = new Menu("Help");
		MenuItem helpAbout = new MenuItem("About");
		MenuItem helpGUI = new MenuItem("Open GUI Getting Started Page");
		MenuItem referenceGUI = new MenuItem("Open GUI Reference Page");
		MenuItem helpAPI = new MenuItem("Open API General Help Page & Reference");
		helpMenu.getItems().addAll(helpAbout, helpGUI, referenceGUI, new SeparatorMenuItem(), helpAPI);
		
		// Event handlers for menuBar
		// File menu handlers
		openApk.setOnAction(e -> setApkFile(chooseApkFile()));
		
		openInput.setOnAction(e -> {
            inputPath = chooseTxtFile();
            toConsole("Opened input file: " + inputPath);
        });
		
		saveExtractSummary.setOnAction(e -> {
			chooseSaveFile(extraction.getText());
		});
				
		exit.setOnAction(e -> { // TODO: only close when these entities are running
            logFileGenerator.close();
            newDriver.end();
            mainStage.close();
            System.exit(0);
        });
		
		// Edit menu handlers
		logDir.setOnAction(e -> {
            logDirectory = chooseDirectory();
            logFileGenerator = new LogFileGenerator(logDirectory);
            toConsole("Logging to: " + logDirectory);
        });
				
		navBack.setOnAction(e -> {
            newDriver.webviewBack();
            toConsole("Sent navigate back.");
        });
		
		resumeApp.setOnAction(e -> {
            newDriver.resumeApp();
            toConsole("Resumed app after leaving.");
        });
		
		// View Menu handlers
		clearConsole.setOnAction(e -> console.clear());
		
		clearLogger.setOnAction(e -> logger.clear());
		
		clearExtraction.setOnAction(e -> extraction.clear());
		
		clearAll.setOnAction(e -> {
            console.clear();
            logger.clear();
            extraction.clear();
        });
		
		// Tester menu handlers
		depthRadio.setOnAction(e -> {
            automatedTester.setTester(new DepthFirstTester(logFileGenerator, testInput, newDriver));
            currentTesterString = "Depth-First Tester";
            toConsole("Using depth-first coverage testing.");
        });
		
		breadthRadio.setOnAction(e -> {
            automatedTester.setTester(new BreadthFirstTester(logFileGenerator, testInput, newDriver));
            currentTesterString = "Breadth-First Tester";
            toConsole("Using breadth-first coverage testing.");
        });
		
		randomRadio.setOnAction(e -> {
            automatedTester.setTester(new RandomTester(logFileGenerator, testInput, newDriver));
            currentTesterString = "Random Coverage Tester";
            toConsole("Using randomized coverage testing.");
        });
		
		replayRadio.setOnAction(e -> {
            File logToReplay = new File(chooseLogFile());
            automatedTester.setTester(new LogReplayTester(newDriver, logToReplay, logger));
            toConsole("Tester will replay log file at: " + logToReplay.getPath());
        });
		
		randomSingle.setOnAction(e -> {
            // TODO: extract elements automatically!
            RandomTester randomTesterForSingleInput = new RandomTester(logFileGenerator, testInput, newDriver);
            automatedTester.setTester(randomTesterForSingleInput);
            newDriver.extractElements();

            try {
                randomTesterForSingleInput.test(newDriver.possibleTargets);
                toConsole("Sent random single input.");
            } catch (Exception ex) {
                toConsole("Could not extract elements and touch a target.");
            }
        });
		
		// Extract menu handlers
//		extractName.setOnAction(e -> {
//            chooseKeyword();
//            extraction.clear();
//            toExtraction("=== Keyword Extraction ===\n");
//            toExtraction(newDriver.extractElementsByKeyword(keyword));
//            toConsole("Extracted based on keyword: ");
//        });
		
		extractSource.setOnAction(e -> {
            extraction.clear();
            toExtraction("=== Source Extraction ===\n");
            toExtraction(newDriver.extractPageSource());
            toConsole("Extracted source.");
        });
		
//		extractHierarchy.setOnAction(e -> {
//            extraction.clear();
//            toExtraction("=== Hierarchy Extraction ===\n");
//            toExtraction(newDriver.extractHierarchy());
//            toExtraction(newDriver.extractElementNames());
//            toConsole("Extracted hierarchy.");
//        });
		
		extractWebView.setOnAction(e -> {
            // Get all information about the GUI from the HTML code and print it to the console(?)
            extraction.clear();
            toExtraction("=== WebView Extraction ===\n");
            toExtraction(newDriver.extractWebViewHTML());
            toConsole("Extracted WebView.");
        });
		
		extractAll.setOnAction(e -> {
            extraction.clear();
            toExtraction("=== All Elements Extraction ===\n");
            toExtraction(newDriver.extractElements());
            toConsole("Extracted all elements.");
        });
		
		// Help menu handlers
		// TODO: replace GitLab Wiki URLs with JavaDoc when necessary
		helpAbout.setOnAction(e -> {
			openURL("https://gitlab.com/WM-CSCI435-F17/Android-Hybrid-App-Testing/wikis/home");
		});
		
		helpGUI.setOnAction(e -> {
			openURL("https://gitlab.com/WM-CSCI435-F17/Android-Hybrid-App-Testing/wikis/GUI-Overview-and-How-To");
		});
		
		referenceGUI.setOnAction(e -> {
			openURL("https://gitlab.com/WM-CSCI435-F17/Android-Hybrid-App-Testing/wikis/GUI-Reference");
		});
		
		helpAPI.setOnAction(e -> {
			openURL("https://gitlab.com/WM-CSCI435-F17/Android-Hybrid-App-Testing/wikis/API-Overview-and-How-To");
		});
		
		// Listener based on status of Selendroid driver for the purpose of enabling/disabling elements
		selendroidStatus.textProperty().addListener((os, old_string, new_string) -> {
            if (new_string.equals(statusSelRunning)) {
                testMenu.setDisable(false);
                extractMenu.setDisable(false);
                navBack.setDisable(false);
                resumeApp.setDisable(false);
            } else if (new_string.equals(statusSelStarting)) {
                testMenu.setDisable(true);
                extractMenu.setDisable(true);
                navBack.setDisable(true);
                resumeApp.setDisable(true);
            } else if (new_string.equals(statusSelStopped)) {
                testMenu.setDisable(true);
                extractMenu.setDisable(true);
                navBack.setDisable(true);
                resumeApp.setDisable(true);
                depthRadio.setSelected(true);
            }
        });

		// Listener based on status of testing for the purpose of enabling/disabling elements
		testStatus.textProperty().addListener((os, old_string, new_string) -> {
            if (new_string.equals(statusTestRunning)) {testMenu.setDisable(true);}
            else if (new_string.equals(statusTestStopped)) {testMenu.setDisable(false);}
        });
				
		// Create and return menuBar
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, testMenu, extractMenu, helpMenu);
		return menuBar;
	}

	/**
	 * Creates and returns the tool bar from which Selendroid and the testers are controlled
	 * 
	 * @return mainToolBar - the newly-created tool bar
	 */
	private ToolBar buildToolBar() {
			// Components for mainToolBar
			Text testText = new Text("Tester:");
			Button runButton = new Button("Run");
			Button stopButton = new Button("Stop");
			Button pauseResumeButton = new Button(pauseText);
			
			// Creation of a slider that allows for selection of testing speed intervals between 0.5 and 3 seconds
			Text speedText = new Text("Speed: 0.5s");
			Slider speedSlider = new Slider();
			speedSlider.setMin(0.5);
			speedSlider.setMax(3);
			speedSlider.setValue(SelendroidServerDriver.defaultWaitTimeSeconds);
			speedSlider.setShowTickLabels(false);
			speedSlider.setShowTickMarks(true);
			speedSlider.setMajorTickUnit(1);
			speedSlider.setMinorTickCount(4);
			speedSlider.setBlockIncrement(0.1);
			
			Text coverageText = new Text("Coverage: ");
	        ProgressIndicator pi = new ProgressIndicator(0);
			
			Separator bar1 = new Separator();
			bar1.setOrientation(Orientation.VERTICAL);
			HBox gap = new HBox();
			HBox.setHgrow(gap, Priority.ALWAYS);
			Separator bar2 = new Separator();
			bar2.setOrientation(Orientation.VERTICAL);
			
			Text selendroidText = new Text("Selendroid:");
			Button startSelButton = new Button("Start");
			Button stopSelButton = new Button("Stop");
			Button restartSelButton = new Button("Restart");
			
			// Event handlers for mainToolBar
			runButton.setOnAction(event -> {
                automatedTester.startTesting();
                testStatus.setText(statusTestRunning);
                toConsole("Tester started.");
                pi.progressProperty().bind(coverageTask.progressProperty());
                new Thread(coverageTask).start();
            });
			
			stopButton.setOnAction(event -> {
                automatedTester.stopTesting();
                logFileGenerator.log("Testing terminated.");
                logFileGenerator.logSummaryStatistics(currentTesterString, 
                		newDriver.getPagesVisited(), newDriver.getLinksClicked(), 
                		newDriver.getLinksFound(), newDriver.getCoverage());
                testStatus.setText(statusTestStopped);
                toConsole("Tester stopped.");
                toConsole("Summary statistics written to log file.");
            });
			
			pauseResumeButton.setOnAction(event -> {
                if (pauseResumeButton.getText().equals(pauseText)) {
                    pauseResumeButton.setText("Resume");
                    automatedTester.pauseInput();
                    toConsole("Tester paused.");
                } else {
                    pauseResumeButton.setText(pauseText);
                    automatedTester.resumeInput();
                    toConsole("Tester resumed.");
                }

            });
			
			// Interval is changed in realtime as the slider is moved.
			speedSlider.valueProperty().addListener((ov, old_val, new_val) -> {
                float newSpeed = new_val.floatValue();

                newSpeed = newSpeed * 1000;
                speed = Math.round(newSpeed);
                speedText.setText(String.format("Speed: %.1fs", new_val));
                automatedTester.setWaitTime(speed);
            });
			
			startSelButton.setOnAction(event -> handleSelendroidStartClick());
			
			stopSelButton.setOnAction(event -> handleSelendroidStopClick());
			
			restartSelButton.setOnAction(event -> {
                handleSelendroidStopClick();
                handleSelendroidStartClick();
            });
			
			// Listener based on status of selendroid driver for the purpose of enabling/disabling elements
			selendroidStatus.textProperty().addListener((os, old_string, new_string) -> {
                if (new_string.equals(statusSelRunning)) {
                    runButton.setDisable(false);
                    stopButton.setDisable(true);
                    pauseResumeButton.setDisable(true);
                    speedSlider.setDisable(false);
                    startSelButton.setDisable(true);
                    stopSelButton.setDisable(false);
                    restartSelButton.setDisable(false);
                } else if (new_string.equals(statusSelStarting)) {
                    runButton.setDisable(true);
                    stopButton.setDisable(true);
                    pauseResumeButton.setDisable(true);
                    speedSlider.setDisable(true);
                    startSelButton.setDisable(true);
                    stopSelButton.setDisable(true);
                    restartSelButton.setDisable(true);
                } else if (new_string.equals(statusSelStopped)) {
                    runButton.setDisable(true);
                    stopButton.setDisable(true);
                    pauseResumeButton.setDisable(true);
                    speedSlider.setDisable(true);
                    startSelButton.setDisable(false);
                    stopSelButton.setDisable(true);
                    restartSelButton.setDisable(true);
                }
            });
			
			// Listener based on status of testing for the purpose of enabling/disabling elements
			testStatus.textProperty().addListener((os, old_string, new_string) -> {
                if (new_string.equals(statusTestRunning)) {
                    runButton.setDisable(true);
                    stopButton.setDisable(false);
                    pauseResumeButton.setDisable(false);
                    speedSlider.setDisable(true);
                    startSelButton.setDisable(true);
                    stopSelButton.setDisable(true);
                    restartSelButton.setDisable(true);
                } else if (new_string.equals(statusTestStopped)) {
                    runButton.setDisable(false);
                    stopButton.setDisable(true);
                    pauseResumeButton.setDisable(true);
                    speedSlider.setDisable(false);
                    startSelButton.setDisable(true);
                    stopSelButton.setDisable(false);
                    restartSelButton.setDisable(false);
                }
            });

			return new ToolBar(testText, runButton, stopButton, pauseResumeButton, speedText, speedSlider, bar1,
                    coverageText, pi, gap, bar2, selendroidText, startSelButton, stopSelButton, restartSelButton);
		}

    /**
	 * Creates and returns the main status bar that monitors the status of the Selendroid 
	 * driver, the tester in use, and the APK file location.
	 * 
	 * @return statusBar - the newly-created status bar as a tool bar. 
	 */
	private ToolBar buildStatusBar() {
		testStatus = new Text();
		selendroidStatus = new Text();
		apkLocStatus = new Text("APK File: (None)");
		HBox gap = new HBox();
		HBox.setHgrow(gap, Priority.ALWAYS);
		Separator bar1 = new Separator();
		bar1.setOrientation(Orientation.VERTICAL);
		Separator bar2 = new Separator();
		bar2.setOrientation(Orientation.VERTICAL);

		return new ToolBar(apkLocStatus, gap, bar1, testStatus, bar2, selendroidStatus);
	}

	/**
	 * Creates and returns the top split panes for the logger area and the extraction area.
	 * 
	 * @return topSplitPane - the newly-created upper split panes
	 */
	private SplitPane buildTopPanes() {
		// Create left pane
		StackPane leftStackPane = new StackPane();
		logger = new TextArea();
		logger.setEditable(false);
		leftStackPane.getChildren().add(logger);
		
		// Create right pane
		StackPane rightStackPane = new StackPane();
		extraction = new TextArea();
		extraction.setEditable(false);
		rightStackPane.getChildren().add(extraction);
		
		// Create and return top pane out of left and right panes
		SplitPane topSplitPane = new SplitPane();
		topSplitPane.getItems().addAll(leftStackPane, rightStackPane);
		topSplitPane.setDividerPositions(0.5f);
		return topSplitPane;
	}

	/**
	 * Creates and returns the main split pane containing the top split panes and the bottom console area.
	 * 
	 * @return mainSplitPane - the newly-created main split pane
	 */
	private SplitPane buildMainSplitPane() {
		// Create top half out of left and right top panes
		StackPane topStackPane = new StackPane();
		topStackPane.getChildren().add(buildTopPanes());
		
		// Create bottom half out of console pane
		StackPane bottomStackPane = new StackPane();
		console = new TextArea();
		console.setEditable(false);
		toConsole("Please select an APK first (File->Open APK File...), then start Selendroid.");
		bottomStackPane.getChildren().add(console);
		
		// Create main pane out of top and bottom halves
		SplitPane mainSplitPane = new SplitPane();
		mainSplitPane.setOrientation(Orientation.VERTICAL);
		mainSplitPane.getItems().addAll(topStackPane, bottomStackPane);
		mainSplitPane.setDividerPositions(0.7f);
		return mainSplitPane;
	}

	/**
	 * Sets the APK file to be tested.
	 * 
	 * @param apkFilepath - the path of the APK file to be tested
	 */
	private void setApkFile(String apkFilepath) {
		apkPath = apkFilepath;
		toConsole("Opened APK file: " + apkFilepath);
		apkLocStatus.setText("APK File: " + apkFilepath);
	}

	/**
	 * Handles starting the Selendroid driver based on whether an APK has been selected.
	 */
	private void handleSelendroidStartClick() {
	    if (apkPath.equals("")) {
	        setApkFile("..\\Artifacts\\employee-directory.apk"); //For use while developing.
	        capabilities = "io.selendroid.directory:0.0.1";
	        //setApkFile(chooseApkFile());
	    }
	    selendroidStatus.setText(statusSelStarting);
	    toConsole("Selendroid starting...");
	    startSelendroidDriver();
	}

	/**
	 * Handles stopping the Selendroid driver.
	 */
	private void handleSelendroidStopClick() {
	    toConsole("Stopping Selendroid...");
	    newDriver.end();
	    selendroidStatus.setText(statusSelStopped);
	}

	/**
	 * Opens a file chooser window and returns the path of the selected .apk file.
	 * 
	 * @return apkFile.getPath() or "" if none selected/found
	 */
	private String chooseApkFile() {
		// Create file chooser for APKs
		FileChooser apkChooser = new FileChooser();
		apkChooser.getExtensionFilters().add(new ExtensionFilter("Android APK files (*.apk)", "*.apk"));
		apkChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

		// Import file
		File apkFile = apkChooser.showOpenDialog(mainStage);

		// Return file path if not null
		if (apkFile != null) {
			chooseCapabilities();
			return apkFile.getPath();
		} else {return "";}
		
	}
	
	/**
	 * Opens a file chooser window and returns the path of the selected .txt file.
	 * 
	 * @return txtFile.getPath() or "" if none selected/found
	 */
	private String chooseTxtFile() {
		// Create file chooser for Text Files
		FileChooser txtChooser = new FileChooser();
		txtChooser.getExtensionFilters().add(new ExtensionFilter("Plain Text Input Files (*.txt)", "*.txt"));
		txtChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		
		// Import file
		File txtFile = txtChooser.showOpenDialog(mainStage);
		
		// Return file path if not null
		if (txtFile != null) {return txtFile.getPath();}
		else {return "";}
		
	}
	
	/**
	 * Opens a file chooser window and returns the path of the selected .log file.
	 * 
	 * @return logFile.getPath() or "" if none selected/found
	 */
	private String chooseLogFile() {
		// Create file chooser for Log Files
		FileChooser logChooser = new FileChooser();
		logChooser.getExtensionFilters().add(new ExtensionFilter("Plain Text Log Files (*.log)", "*.log"));
		logChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		
		// Import file
		File logFile = logChooser.showOpenDialog(mainStage);
		
		// Return file path if not null
		if (logFile != null) {return logFile.getPath();}
		else {return "";}
	}
	
	/**
	 * Opens a directory chooser window and returns the path of the selected directory.
	 * 
	 * @return directory.getPath() or "" if none selected/found
	 */
	private String chooseDirectory() {
		// Create directory chooser
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		
		// Import directory
		File directory = directoryChooser.showDialog(mainStage);
		
		// Return directory path if not null
		if (directory != null) {return directory.getPath();}
		else {return "";}
	}
	
	/**
	 * Saves the contents text param to a text file of 
	 * the user's choice. This may be used at any time and will write
	 * out the empty pane if the user so chooses. 
	 * 
	 * @param text - the text to be written
	 */
	private void chooseSaveFile(String text) {
		// Create file chooser for saving the summary or extraction
		FileChooser saveChooser = new FileChooser();
		saveChooser.getExtensionFilters().add(new ExtensionFilter("Plain Text Files (*.txt)", "*.txt"));
		saveChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
		
		// Write out file if path not null
		try {
			File saveFile = saveChooser.showSaveDialog(mainStage);
			FileWriter fileWriter = new FileWriter(saveFile);
			fileWriter.write(text);
			fileWriter.close();
			toConsole("File written successfully.");
		}
		catch (Exception e) {
			toConsole("Error: couldn't write to file!");
		}
	}

	/**
	 * Opens a popup window for selecting the capabilities with which Selendroid should run the given APK.
	 * 
	 * TODO: deprecate and extract appID and feed it to Selendroid driver automatically. 
	 */
	private void chooseCapabilities() {
		Stage capPopup = new Stage();
		capPopup.setTitle("Enter Selendroid's capabilities for your APK:");
		capPopup.getIcons().add(new Image("icon.png"));

		TextArea capEntry = new TextArea(capabilities);
		VBox popupBoxes = new VBox(capEntry);
		
		capPopup.setScene(new Scene(popupBoxes));
		
		capEntry.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER ) {
                capabilities = capEntry.getText();
                toConsole("Set Selendroid capabilities: " + capabilities);
                capPopup.close();
            }
        });
		
		capPopup.showAndWait();
	}
	
	/**
	 * Opens a pop-up window for selecting a keyword to do a name-based page extraction on.
	 */
	private void chooseKeyword() {
		Stage keyPopup = new Stage();
		keyPopup.setTitle("Enter the keyword to be extracted:");
		keyPopup.getIcons().add(new Image("icon.png"));

		TextArea keyEntry = new TextArea();
		VBox popupBoxes = new VBox(keyEntry);

		keyPopup.setScene(new Scene(popupBoxes));

		keyEntry.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER ) {
                keyword = keyEntry.getText();
                keyPopup.close();
            }
        });

		keyPopup.showAndWait();
	}

	/**
	 * Sends given string to the console text area (formatted with carriage return).
	 * 
	 * @param text - the text to be written to the console text area
	 */
	private void toConsole(String text) {
		console.appendText(String.format("%s\n", text));
	}

	/**
	 * Sends given string to the logger text area.
	 * 
	 * @param input - the string to be written to the logger text area
	 */
	public void toLogger(String input) {
		Platform.runLater(() ->logger.appendText(input));
	}
	
	/**
	 * Sends given string to the extraction text area.
	 * 
	 * @param text - the string to be written to the extraction text area
	 */
	public void toExtraction(String text) {
		extraction.appendText(text);
	}
	
	/**
	 * Launches the Selendroid driver on its own  thread with the previously-set APK file and capabilities.
	 */
	private void startSelendroidDriver() {
		new Thread(() -> {
            newDriver = new SelendroidServerDriver(apkPath, capabilities);
            setGui();
            readInputFile();
            automatedTester = new AutomatedTester(newDriver, new DepthFirstTester(logFileGenerator, testInput, newDriver));

            Platform.runLater(() -> {
                selendroidStatus.setText(statusSelRunning); //TODO : set this only on successful load
                toConsole("Selendroid started.");
            });

        }).start();
	}

	/**
	 * Creates a task so that the coverage being recorded in the testing threads may be 
	 * displayed inside the MainGUI as it is updated. 
	 */
	private Task coverageTask = new Task<Void>() {
	    @Override public Void call() {
	    	while(newDriver != null && !isCancelled()) {
		    	updateProgress(newDriver.getCoverage(), 1);
	    	}
	        return null;
	    }
	};	
	
	/**
	 * Reads the given input file into testInput to be used during testing. If
	 * no file is found then a character string is used instead containing all
	 * alphanumerics and some special keyboard characters. 
	 * 
	 * Note: we recommend that users provide their own dictionary file for testing with 
	 * more useful input representative of their application being tested.
	 */
	private void readInputFile() {
		String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456890!@#$%^&*()_+,./<>?;':";
	
		if (!inputPath.isEmpty()) {
			// If an input file was specified, read its contents into testInput
			File inputFile = new File(inputPath);
			Scanner inputScan;
            try {inputScan = new Scanner(inputFile);}
            catch (java.io.FileNotFoundException e){
                toConsole("Failed to read input file.");
                testInput.add("a"); // Is this necessary??
                return;
            }
            while (inputScan.hasNextLine()) {testInput.add(inputScan.nextLine());}
            inputScan.close();
            toConsole("Successfully read input file.");
		}
		else {
			// Read alphanumeric string to Input
			char[] alphaArr = alpha.toCharArray();
			
			for (char c: alphaArr) {
				testInput.add("" + c);
			}
		}
	}

	/**
	 * Converts the given string into a URL (and subsequently a URI) and 
	 * opens it in the system's default web browser. 
	 * 
	 * @param urlString - the string to be converted into a URI and opened.
	 */
	private void openURL(String urlString) {
		try {
			Desktop.getDesktop().browse(new URL(urlString).toURI());
		}
		catch (Exception e) {
			toConsole("Failed to open page.");
		}
	}

	/**
	 * Specifies to the logFileGenerator which GUI it should sent its output to.
	 */
	private void setGui() {
		logFileGenerator.setGUI(this);
	}

	/**
     * Transforms a raw XML string into indented XML string.
     * 
     * @param input - raw XML string
     * @param indent - indentation required
     * @return formatted XML string
     */
    public static String prettyFormat(String input, int indent) {
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
        } catch (javax.xml.transform.TransformerException e) {
            throw new RuntimeException(e); //TODO simple exception handling, needs review
        }
    }
	
}
