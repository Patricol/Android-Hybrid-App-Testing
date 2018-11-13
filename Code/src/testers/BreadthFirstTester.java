package testers;

import java.util.ArrayList;

import org.openqa.selenium.WebElement;

import logger.LogFileGenerator;
import serverDriver.SelendroidServerDriver;

/**
 * BreadthFirstTester is an extension of TreeInspiredTester and provides
 * a coverage testing methodology in the form of a breadth-first search for a 
 * tree of pages and their elements.  
 */
public class BreadthFirstTester extends TreeInspiredTester {

	/**
	 * Creates an instance of BreadthFirstTester with the given log file generator
	 * instance, strings to be tested with, and Selendroid server driver instance.
	 * 
	 * @param logFile - an instance of the LogFileGenerator
	 * @param testStrings - the strings to be used during testing
	 * @param driver - an instance of SelendroidServerDriver on the APK being tested
	 */
	public BreadthFirstTester(LogFileGenerator logFile, ArrayList<String> testStrings, SelendroidServerDriver driver) {
		super(logFile, testStrings, driver);
	}

    /* (non-Javadoc)
     * @see testers.TreeInspiredTester#planNextMoves(java.util.ArrayList)
     * 
     * Notes from the author: this comprises the main functionality of the breadth-first search tester and 
     * as such it handles pages in a special way. It must make use of newFutureTargets.push(targetStringForBack)
     * rather than planActionForTarget(0) for pathFromStart to work. That convention will be standard for general 
     * applications however where the back button is not always target 0.
     */
    void planNextMoves(ArrayList<WebElement> possibleTargets) {
    	
        if (!wasFullyVisited(getPageHash())) {
        	
            if (isInput(possibleTargets.get(0))) {
                planActionForTarget(handlePage(getPageHash(), possibleTargets));
            } else {
                addPathFromStart();
                
                while (!wasFullyVisited(getPageHash())) {
                    int nextTarget = handlePage(getPageHash(), possibleTargets);
                    planActionForTarget(nextTarget);
                    
                    if (!isExternalLink(possibleTargets.get(nextTarget))) {
                        newFutureTargets.push(composeWebviewBackMessage());
                    }
                }
                
                addPathToStart();
                saveNewTargetsForLater();
            }
        }
        
        if (futureTargets.isEmpty() && newFutureTargets.isEmpty()) {moveLaterFutureTargetsToIterator();}
	}
}
