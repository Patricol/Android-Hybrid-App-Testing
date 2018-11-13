package testers;

import logger.LogFileGenerator;
import org.openqa.selenium.WebElement;
import serverDriver.SelendroidServerDriver;

import java.util.ArrayList;

/**
 * Logging Tester is an extension of the LoggingAwareTester class and allows
 * for testing based on a log file.
 */
abstract class LoggingTester extends LoggingAwareTester {

    private LogFileGenerator logFile; 
    private boolean action_ongoing = false;

    /**
     * Creates an instance of the logging tester class
     * 
     * @param logFile - the log file used for generating the action sequence
     * @param testStrings - the strings to be used for testing
     * @param driver - an instance of Selendroid Server Driver
     */
    LoggingTester(LogFileGenerator logFile, ArrayList<String> testStrings, SelendroidServerDriver driver) {
        super(driver, testStrings);
        this.logFile = logFile;
    }

    /**
     * Sets tester to its start state
     */
    private void startingAction() {
        if (!action_ongoing) {
            logBracket(true);
            action_ongoing = true;
        }
    }

    /**
     * Sets tester to ending state
     */
    private void endingAction() {
        if (action_ongoing) {
            logBracket(false);
            action_ongoing = false;
        }
    }

    /* (non-Javadoc)
     * @see testers.Tester#doAction(org.openqa.selenium.WebElement, int, java.lang.String)
     */
    void doAction(WebElement target, int targetIndex, String inputString) {
        startingAction();
        logTargetData(target);
        super.doAction(target, targetIndex, inputString);
        endingAction();
    }

    /* (non-Javadoc)
     * @see testers.LoggingAwareTester#doAction(java.lang.String, java.util.ArrayList)
     */
    void doAction(String action, ArrayList<WebElement> possibleTargets) {
        startingAction();
        super.doAction(action, possibleTargets);
        endingAction();
    }

    /* (non-Javadoc)
     * @see testers.Tester#webviewBack()
     */
    boolean webviewBack() {
        logFile.log(composeWebviewBackMessage());
        return super.webviewBack();
    }

    /* (non-Javadoc)
     * @see testers.Tester#hardwareBack()
     */
    void hardwareBack() {
        logFile.log(composeHardwareBackMessage());
        super.hardwareBack();
    }

    /* (non-Javadoc)
     * @see testers.Tester#delay()
     */
    void delay() {
        logFile.log(composeDelayMessage());
        super.delay();
    }

    /**
     * Sends the target label and data to the logger (logfile)
     * 
     * @param label - the element label to be written
     * @param data - the element data to be written
     */
    private void logTargetDataLine(String label, String data) {
    		logComment(String.format("%s: \"%s\",", label, data));
    }

    /**
     * Logs basic information about the given web view element in JSON format for easier parsing.
     * 
     * @param target - the intended target to log
     */
    private void logTargetData(WebElement target) {
    		logTargetDataLine("tagName", target.getTagName());
        logTargetDataLine("text", target.getText());
        logTargetDataLine("location", String.format("%s", target.getLocation()));
        logTargetDataLine("dimension", String.format("%s", target.getSize()));
        logTargetDataLine("hypertextReference", target.getAttribute("href"));
    }

    /**
     * Logs data from the possible targets collection. 
     * 
     * @param possibleTargets - the possible targets to log
     */
    void logPossibleTargetsData(ArrayList<WebElement> possibleTargets) {
    		logComment(String.format("pageComponents: %s,", possibleTargets.size()));
    }

    /**
     * Logs a given comment string
     * 
     * @param commentString - the string to log
     */
    void logComment(String commentString) {
        for (String commentLine : commentString.split("\n")) {
            logFile.log(composeComment(commentLine));
        }
    }

    /**
     * Logs an open or closed curly brace to the log based on whether the box is open or closed.
     *  
     * @param open - if true an open brace is used else a closed brace is used
     */
    void logBracket(boolean open) {
		if(open) {logFile.log("{");}
		else {logFile.log("}");}
    }
    
    /**
     * Logs the number of pages visited during testing
     */
    void logPagesVisited() {
    		logComment(String.format("pagesVisited: %s,", String.valueOf(getPagesVisited())));
    }
    
    /**
     * Logs the number of links clicked during testing
     */
    void logLinksClicked() {
		logComment(String.format("linksClicked: %s,", String.valueOf(getLinksClicked())));
    }
    
    /**
     * Logs the number of links found 
     */
    void logLinksFound() {
		logComment(String.format("linksFound: %s,", String.valueOf(getLinksFound())));
    }

    /* (non-Javadoc)
     * @see testers.Tester#insertText(org.openqa.selenium.WebElement, int, java.lang.String)
     */
    void insertText(WebElement target, int targetIndex, String inputString) {
        super.insertText(target, targetIndex, inputString);
        logFile.log(composeInsertTextMessage(targetIndex, inputString));
    }

    /* (non-Javadoc)
     * @see testers.Tester#clickButton(org.openqa.selenium.WebElement, int)
     */
    void clickButton(WebElement target, int targetIndex) {
        super.clickButton(target, targetIndex);
        logFile.log(composeClickButtonMessage(targetIndex));
    }

    /* (non-Javadoc)
     * @see testers.Tester#followExternalLinkAndReturn(org.openqa.selenium.WebElement, int)
     */
    void followExternalLinkAndReturn(WebElement target, int targetIndex) {
        //Should log sms click the same as everything else. If it were logged as a click, the program would assume that the click brings the app to another page.
        logFile.log(composeFollowExternalLinkAndReturnMessage(targetIndex));//must be in the front, otherwise an error could kick it out midway through hardwareBack.
        super.followExternalLinkAndReturn(target, targetIndex);
    }

    /* (non-Javadoc)
     * @see testers.Tester#hardwareBackAfterDelay()
     */
    void hardwareBackAfterDelay() {
        logFile.log(composeHardwareBackAfterDelayMessage());
        super.hardwareBackAfterDelay();
    }
}
