package logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import gui.MainGUI;


/**
 * Creates a .txt file that will log all the inputs and interactive elements selected by 
 * this application on webview. 
 * 
 */
public class LogFileGenerator {
	private PrintWriter writer;
	private MainGUI mainGui = null;
	private String fileName;
	private File filePath;
	private static String logFileDefaultName = "Hybrid_App_Tester";
	public static String logFileDefaultExtension = ".log";
	
	/**
	 * Default constructor that will create the .txt file at where the code of the application is
	 */
	public LogFileGenerator(){
	    this("");
	}
	
	/**
	 * Constructor that will create the .txt file at the path that the user specified
	 * 
	 * @param path - the desired path to store the log file
	 */
	public LogFileGenerator(String path){
		File newFile;
		int fileNumber = -1;
		
		if (path.equals("")) {path = System.getProperty("user.dir");}
		
		do {newFile = new File(path, this.makeFileName(++fileNumber));} 
		while (newFile.exists());
		
		this.fileName = this.makeFileName(fileNumber);
		
		try{writer = new PrintWriter(path + File.separator + this.makeFileName(fileNumber), "UTF-8");}
		catch (IOException e) {System.err.println("Caught IOException: " + e.getMessage());}
		this.filePath = newFile;
	}
	
	public File getFilePath() {
		return this.filePath;
	}
	
	private String makeFileName(int fileNumber){
	    return String.format("%s.%s%s", logFileDefaultName, fileNumber, logFileDefaultExtension);
	}
	
	/**
	 * Sets the GUI that the logger should be writing its view to
	 */
	public void setGUI(MainGUI gui){
		this.mainGui = gui;
	}
	
	/**
	 * Writes input into the log file
	 * @param input to store into log file
	 */
	public void log(String input){
		writer.println(input);
		writer.flush();
		if(mainGui != null) {
			mainGui.toLogger(String.format("%s\n", input));
		}
	}
	
	/**
	 * Appends the summary statistics of a given test to the logfile
	 *  
	 * @param testType - the type of test being performed
	 * @param pagesVisited - the number of pages successfully visited during the test
	 * @param linksClicked - the number of links successfully clicked during the test
	 * @param linksFound - the number of links discovered during the test
	 * @param coverage = the percentage coverage for the entire application
	 */
	public void logSummaryStatistics(String testType, int pagesVisited, int linksClicked, int linksFound, float coverage){
		writer.println("--SUMMARY STATISTICS--\n");
		writer.println("The " + testType + " has been terminated");
		writer.println("The tester:\n ");
		writer.println("\tVisited " + pagesVisited + " unique pages\n");
		writer.println("\tClicked " + linksClicked + " unique components\n");
		writer.println("\tFound " + linksFound + " unique components\n");
		writer.println("\tYielded an overall component coverage of " + coverage + "\n-- --\n");
		writer.flush();
		if(mainGui != null) {
			mainGui.toExtraction(String.format("--SUMMARY STATISTICS--\n"
					+ "The %s has been terminated\n"
					+ "The tester:\n "
					+ "\tVisited %d unique pages\n"
					+ "\tClicked %d unique components\n"
					+ "\tFound %d unique components\n"
					+ "\tYielded an overall component coverage of %.3f\n"
					+ "-- --\n", 
					testType, pagesVisited, linksClicked, linksFound, coverage));
		}
	}

	/**
	 * Returns the name of the log file being written to in a particular instance. To be used 
	 * for comparison purposes in tests.
	 */
	public String getFileName() {
		return this.fileName;
	}
	
	/**
	 * Closes file
	 */
	public void close (){
		writer.close();
	}
}