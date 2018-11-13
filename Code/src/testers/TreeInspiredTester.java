package testers;

import logger.LogFileGenerator;
import org.openqa.selenium.WebElement;
import serverDriver.SelendroidServerDriver;

import java.util.ArrayList;

/**
 * TreeInspiredTester is an extension of HistoryAwareTester and provides
 * a record of visited pages on in a tree for the testers to use to 
 * determine traversal. 
 */
abstract class TreeInspiredTester extends HistoryAwareTester {

    private ArrayList<WebElement> possibleTargets;

    /**
     * Creates an instance of TreeInspiredTester with the given log file 
     * generator, input test strings, and Selendroid server driver instance.
     * 
     * @param logFile - an instance of the LogFileGenerator
	 * @param testStrings - the strings to be used during testing
	 * @param driver - an instance of SelendroidServerDriver on the APK being tested
     */
    TreeInspiredTester(LogFileGenerator logFile, ArrayList<String> testStrings, SelendroidServerDriver driver) {
        super(logFile, testStrings, driver);
    }

    /* (non-Javadoc)
     * Note: this comprises the basic loop for both depth-first-search testing and breadth-first-search testing.
     * @see testers.Tester#test(java.util.ArrayList)
     */
    public boolean test(ArrayList<WebElement> possibleTargets) {
    		logBracket(true);
        logPagesVisited();
        logPossibleTargetsData(possibleTargets);
        this.possibleTargets = possibleTargets;
        if (possibleTargets.size()!=0) {planNextMoves(this.possibleTargets);}
        else {newFutureTargets.push(composeWebviewBackMessage());}
        moveFutureTargetsToIterator();
        boolean doAction = !futureTargets.isEmpty();
        if (doAction) {doNextPlannedAction(this.possibleTargets);}
        logBracket(false);
        return doAction;
    }

    /**
     * This is an abstract method for determining which moves should be taken next
     * based on the provided possible targets. 
     * 
     * See extending classes for implementation. 
     * 
     * @param possibleTargets - the list of targets to be added
     */
    abstract void planNextMoves(ArrayList<WebElement> possibleTargets);

    /**
     * Uses the index of the target to determine which interaction to take next
     * in the tester.
     * 
     * @param targetIndex
     */
    void planActionForTarget(int targetIndex) {
        super.planActionForTarget(possibleTargets.get(targetIndex), targetIndex);
    }
}
