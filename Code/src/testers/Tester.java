package testers;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import serverDriver.SelendroidServerDriver;

/**
 * This class implements the master Tester functionality
 * which multiple other testers extend.  
 * It does this by taking in a Selendroid Server Driver
 * instance and performing various actions on the driver.
 */
abstract public class Tester {

    static String inputStringForBackspace = "Keys.BACK_SPACE";
    private static String targetStringForBack = "back";
    private static String tagNameForInput = "input";
    private static String tagNameForClick = "a";// why tho? is that what the buttons are tagged?

    private static int defaultDelay = 2000;//TODO should be whatever is set in settings?

    private SelendroidServerDriver driver;

    private ArrayList<String> testStrings;

	/**
	 * Creates an instance of Tester.
	 * 
	 * @param driver - a previously-created instance of SelendroidServerDriver
	 * @param testStrings - the strings to be used for testing
	 */
	Tester(SelendroidServerDriver driver, ArrayList<String> testStrings) {
	    this.driver = driver;
        this.testStrings = testStrings;
    }

    /**
     * Checks whether possibleTargets contains targets to be tested.
     * 
     * @param possibleTargets - the collection of targets to be tested
     * @return - true if possibleTargets contains elements else false
     */
    abstract boolean test(ArrayList<WebElement> possibleTargets);

    /**
     * Returns the number of pages visited from the driver.
     * 
     * @return - the number of pages visited. 
     */
    int getPagesVisited() {
        return driver.getPagesVisited();
    }
    
    /**
     * Returns the number of links clicked from the driver.
     * 
     * @return - the number of links clicked
     */
    int getLinksClicked() {
        return driver.getLinksClicked();
    }
    
    /**
     * Returns the number of links found from the driver. 
     * 
     * return - the number of links found
     */
    int getLinksFound() {
        return driver.getLinksFound();
    }

    /**
     * Inserts text at the given target located at the given index with the given input string. 
     * 
     * @param target - the target to insert text into
     * @param targetIndex - the index of the target to insert text into
     * @param inputString - the string to insert into the target
     */
    void insertText(WebElement target, int targetIndex, String inputString) {
        if (isBackspace(inputString)) {target.sendKeys(Keys.BACK_SPACE);}
        else {target.sendKeys(inputString);}
    }

    /**
     * Calls click button without using the sub-class's overriding implementation. 
     * 
     * @param target - the target to be clicked. 
     */
    private void silentClickButton(WebElement target) {
        driver.putElementInView(target);
        driver.clickTargetJS(target);
    }

    /**
     * Clicks on a given target at the given target index. 
     * 
     * @param target - the target to be clicked
     * @param targetIndex - the index of the target to be clicked
     */
    void clickButton(WebElement target, int targetIndex) {silentClickButton(target);}

    /**
     * Performs the action implied by the type of the given target. 
     * 
     * @param target - the target to be interacted with
     * @param targetIndex - the index of the target to be interacted with
     * @param inputString - the string to be used in text-input interaction
     */
    void doAction(WebElement target, int targetIndex, String inputString) {
        if (isInput(target)) {insertText(target, targetIndex, inputString);}
        else if (isClickable(target)) {
        	driver.setPageAsVisited(target.getAttribute("href"));
            if (isExternalLink(target)) {followExternalLinkAndReturn(target, targetIndex);}
            else {clickButton(target, targetIndex);}
        }
    }

    /**
     * Sends a hardware back signal to the driver
     */
    void hardwareBack() {
        driver.hardwareBack();
    }

    /**
     * Sends a webview back signal to the driver
     * 
     * @return - true if a successful signal was sent 
     */
    boolean webviewBack() {
        driver.webviewBack();
        return true;
    }

    /**
     * Calls silent delay without using a subclass's overriding implementation. 
     */
    private void silentDelay() {
    	try {
			Thread.sleep(defaultDelay);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Error: Interrupted during delay in LoggingTester.");
		}
    }

    /**
     * Sends a delay in between actions. 
     */
    void delay() {silentDelay();}

    /**
     * Sends a hardware back signal after the specified delay. 
     */
    void hardwareBackAfterDelay() {
        this.silentDelay();
        driver.hardwareBack();
    }

    /**the driver will die if we try to go back once it's already on an external page,
     * so we need to do this as one command*/
    void followExternalLinkAndReturn(WebElement target, int targetIndex) {
        if (!target.getAttribute("href").startsWith("sms")) {
            if (target.getAttribute("href").startsWith("http")) {
            	this.delay();
            	this.hardwareBack();
            }
            else {
                silentClickButton(target);
            	this.delay();
                driver.resumeApp();
            }
        }
    }

    /**
     * Checks whether the given target is an input widget
     * 
     * @param target - the target to check
     * @return - true if the given target is an input widget else false
     */
    boolean isInput(WebElement target) {
        return target.getTagName().equals(tagNameForInput);
    }

    /**
     * Checks whether the given string sends the backspace character.
     * 
     * @param inputString - the string to be checked
     * @return - true if the given string sends the backspace character else false
     */
    boolean isBackspace(String inputString) {
        return inputString.equals(inputStringForBackspace);
    }

    /**
     * Checks whether the given target is click-able.
     * 
     * @param target - the target to check
     * @return - true if the target is clickable else false
     */
    boolean isClickable(WebElement target) {
        return target.getTagName().equals(tagNameForClick);
    }

    /**
     * Checks whether the given target string sends the back signal.
     * 
     * @param targetString - the string to be examined
     * @return - true if the target string sends the back signal else false
     */
    boolean isBack(String targetString) {
        return targetString.equals(targetStringForBack);
    }

    /**
     * Checks whether the given target is an external link.
     * 
     * @param target - the target to check
     * @return - true if the given link is external else false
     */
    boolean isExternalLink(WebElement target) {
        return (!target.getAttribute("href").startsWith("file://"));
    }

    /**
     * Returns the length of the input target.
     * 
     * @param inputTarget - the target to get the length of
     * @return - the length of the target 
     */
    int getLengthInInput(WebElement inputTarget) {
        return inputTarget.getAttribute("value").length();
    }

    /**
     * Checks whether the only possible element on the page is an input box. 
     * 
     * @param inputTarget - the target to be checked; its length must be something other than 0
     * @param possibleTargetsOnPage - the collection of targets on the page
     * @return - true if the only target on the page is an input box else false
     */
    boolean isDeadendInputSearch(WebElement inputTarget, int possibleTargetsOnPage) {
        // This does not seem like a reliable way to check this
        // Maybe check the difference between the possibleTargets when no text is entered vs. when text is? if they're the same, it's a deadend.
        return possibleTargetsOnPage<=1 && getLengthInInput(inputTarget)!=0;
    }

    /**
     * Checks whether or not the given input target is empty. 
     * 
     * @param inputTarget - the target to check
     * @return - true if the given input target is empty else false
     */
    boolean isEmptyInputSearch(WebElement inputTarget) {
        return getLengthInInput(inputTarget)==0;
    }

    /**
     * Returns a hash of the entire page's source. 
     *  
     * @return the hashed page source
     */
    int getPageHash() {
        return driver.extractPageSource().hashCode();
    }
}
