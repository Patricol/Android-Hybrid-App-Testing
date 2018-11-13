package testers;

import java.util.ArrayList;

import org.openqa.selenium.WebElement;

import logger.LogFileGenerator;
import serverDriver.SelendroidServerDriver;

/**
 * DepthFirstTester is an extension of TreeInspiredTester and provides
 * a coverage testing methodology in the form of a depth-first search for a 
 * tree of pages and their elements.  
 */
public class DepthFirstTester extends TreeInspiredTester {

	/**
	 * Creates an instance of DepthFirstTester with the given log file generator
	 * instance, strings to be tested with, and Selendroid server driver instance.
	 * 
	 * @param logFile - an instance of the LogFileGenerator
	 * @param testStrings - the strings to be used during testing
	 * @param driver - an instance of SelendroidServerDriver on the APK being tested
	 */
	public DepthFirstTester(LogFileGenerator logFile, ArrayList<String> testStrings, SelendroidServerDriver driver) {
		super(logFile, testStrings, driver);
	}

	/* (non-Javadoc)
	 * Note: this serves as the main function of the depth-first search tester.
	 * @see testers.TreeInspiredTester#planNextMoves(java.util.ArrayList)
	 */
	void planNextMoves(ArrayList<WebElement> possibleTargets) {
		planActionForTarget(handlePage(getPageHash(), possibleTargets));
		newFutureTargets.push(composeWebviewBackMessage());
	}
}
