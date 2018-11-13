package testers;

import logger.LogFileGenerator;
import org.openqa.selenium.WebElement;
import serverDriver.SelendroidServerDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * HistoryAwareTester is an extension of StackBasedTester and provides
 * a record of visited pages on a stack for the testers to use to 
 * determine traversal. 
 */
abstract class HistoryAwareTester extends StackBasedTester {
    private static String fullyVisitedIndicator = "0";
    private HashMap<Integer, List<String>> visited = new HashMap<>();

    
    /**
     * Creates an instance of HistoryAwareTester with the given log file 
     * generator, input test strings, and Selendroid server driver instance. 
     * 
     * @param logFile - an instance of the LogFileGenerator
	 * @param testStrings - the strings to be used during testing
	 * @param driver - an instance of SelendroidServerDriver on the APK being tested
     */
    HistoryAwareTester(LogFileGenerator logFile, ArrayList<String> testStrings, SelendroidServerDriver driver) {
        super(logFile, testStrings, driver);
    }

    /**
     * Returns the number of pages visited. 
     * 
     * @return the number of pages visited.
     */
    int getNumberOfPagesVisited() {
        return visited.size();
    }

    /**
     * Adds a new page to the record of visited pages. 
     * 
     * Note: the first index of each record contains a number indicating
     * the index of the next web element to be acted upon.
     * 
     * @param pageHash - the index to be used when hashing targets into the visited map
     * @param possibleTargets - the list of targets to be added
     */
    private void addPage(int pageHash, ArrayList<WebElement> possibleTargets) {
        List<String> linksAndMeta = new ArrayList<>();
        linksAndMeta.add("1"); // Index of the next to go to. 0 if whole set is done.
        
        for (WebElement possibleTarget : possibleTargets) {
            linksAndMeta.add(possibleTarget.getAttribute("href"));
        }
        
        if (linksAndMeta.size() == 2) {
            linksAndMeta.set(0, "0");
        }
        
        visited.put(pageHash, linksAndMeta);
    }

    /**
     * Adds a new page to the record of visited pages but 
     * only if it's a newly-identified page. 
     * 
     * @param pageHash - the index to be used when hashing targets into the visited map
     * @param possibleTargets - the list of targets to be added
     */
    private void addPageIfNew(int pageHash, ArrayList<WebElement> possibleTargets) {
        if (!visited.containsKey(pageHash)) {addPage(pageHash, possibleTargets);}
    }

    /**
     * Mars a page as visited in the record of visited pages. 
     * 
     * @param pageHash - the index to be used when hashing targets into the visited map
     */
    private void markAnotherSeen(int pageHash) {
        Integer numberNowSeen = getNextTargetIndex(pageHash);
        if (wasFullyVisited(pageHash) || numberNowSeen==visited.get(pageHash).size()-2) {numberNowSeen = -1;}
        visited.get(pageHash).set(0, String.format("%s", numberNowSeen + 1));
    }

    /**
     * Returns true if all web elements on a page have been marked as seen.
     * 
     * @param pageHash - the index to be used when hashing targets into the visited map
     * @return true if all web elements on a page have been marked as seen
     */
    boolean wasFullyVisited(int pageHash) {
        return visited.containsKey(pageHash) && fullyVisitedIndicator.equals(visited.get(pageHash).get(0));
    }

    /**
     * Returns the index of the intended next target to be visited. 
     * 
     * @param pageHash - the index to be used when hashing targets into the visited map
     * @return the index of the next target intended to be visited
     */
    private int getNextTargetIndex(int pageHash) {
        return Integer.parseInt(visited.get(pageHash).get(0));
    }

    /**
     * Returns the index of the intended next target to be visited 
     * after marking the page associated with that index as visited. 
     * 
     * @param pageHash - the index to be used when hashing targets into the visited map
     * @return the index of the next target intended to be visited
     */
    private int getTargetIndexAndMarkAsVisited(int pageHash) {
        Integer nextTargetIndex = getNextTargetIndex(pageHash);
        markAnotherSeen(pageHash);
        return nextTargetIndex;
    }

    /**
     * Adds a new page if it's new and then returns the target index 
     * after marking it as visited. 
     * 
     * @param pageHash - the index to be used when hashing targets into the visited map
     * @param possibleTargets - the list of targets to be added
     * @return the index of the next target intended to be visited
     */
    int handlePage(int pageHash, ArrayList<WebElement> possibleTargets) {
        addPageIfNew(pageHash, possibleTargets);
        return getTargetIndexAndMarkAsVisited(pageHash);
    }
}
