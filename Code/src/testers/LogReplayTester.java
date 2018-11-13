package testers;

import serverDriver.SelendroidServerDriver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import org.openqa.selenium.WebElement;


/**
 * Takes the file from the path indicated at the start of server, 
 * and run through the same commands stored in
 * the log file from a PREVIOUS runthrough.
 */
public class LogReplayTester extends LoggingAwareTester {

    private javafx.scene.control.TextArea leftConsole;
    private BufferedReader BufferIn = null;

    /**
     * Constructor for LogReplayTester, sets up connection with driver
     */
    public LogReplayTester(SelendroidServerDriver driver, File logFile, javafx.scene.control.TextArea leftConsole){
        super(driver, null);
        this.leftConsole = leftConsole;
        readProvidedLogfile(logFile);
    }

    public LogReplayTester(SelendroidServerDriver driver, File logFile){
    	super(driver, null);
        this.leftConsole = null;
        readProvidedLogfile(logFile);
    }
    
    private void printToConsole(String line) {
    	if(leftConsole != null) {
    		leftConsole.appendText(String.format("%s\n", line));}
    }

    /**
     * Read log file from user directed path.
     * First copies it into a temp file.
     */
    private void readProvidedLogfile(File logFile) {
        try {BufferIn = new BufferedReader(new FileReader(logFile.getAbsoluteFile()));}
        catch (Exception e) {printToConsole(e.toString());}
    }

    /**Handle closing the file etc.*/
    private void reachedEndOfFile() {
        printToConsole("Reached end of the logfile.");
        try {BufferIn.close();}
        catch (java.io.IOException e) {printToConsole(e.toString());}
    }

    /**Returns the next line of the log that is an action, or an empty string on EOF*/
    private String getNextAction() {
        String line = null;
        do {
            try {line = BufferIn.readLine();}
            catch (java.io.IOException e) {printToConsole(e.toString());}
            if (line!=null) {printToConsole(line);}
        } while (line!=null && !isAction(line));
        if (line!=null) {return line;}
        else {reachedEndOfFile();}
        return "";
    }

    /**Main test method that runs off of AutomatedTester.*/
    public boolean test(ArrayList<WebElement> possibleTargets){
        String action = getNextAction();
        if (!action.equals("")) {
            doAction(action, possibleTargets);
            return true;
        }
        else {return false;}
    }

}
