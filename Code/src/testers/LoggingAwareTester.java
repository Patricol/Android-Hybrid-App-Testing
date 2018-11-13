package testers;

import org.openqa.selenium.WebElement;
import serverDriver.SelendroidServerDriver;

import java.util.ArrayList;


/**
 * LoggingAwareTester is an extension of Tester and provides
 * a record of visited pages on a stack for the testers to use to determine traversal. 
 */
abstract class LoggingAwareTester extends Tester {
    private static String logMessageButtonClick = "clickedTarget: ";
    private static String logMessageTextboxInput = "Input to Target#";
    private static String logMessageActionPrefix = "\taction: ";
    private static String logMessageCommentPrefix = "\tcomment_";
    private static String logMessageDelay = "Slept";
    private static String logMessageHardwareBack = "Used Hardware Back";
    private static String logMessageHardwareBackAfterDelay = "Used Delayed Hardware Back";
    private static String logMessageWebviewBack = "Used Webview Back";
    private static String logMessageDataSeparator = ":";
    private static String logMessageFollowExternalLinkAndReturn = "Briefly Visiting Target#";
    
    /**
     * Creates an instance of the logging aware tester with the provided
     * Selendroid server driver instance and testing strings. 
     * 
     * @param driver - an instance of SelendroidServerDriver
     * @param testStrings - the strings to be tested with
     */
    LoggingAwareTester(SelendroidServerDriver driver, ArrayList<String> testStrings) {super(driver, testStrings);}

    /**
     * Returns a string describing the action to complete on the passed target.
     * It does this based on the characteristics of the target given. 
     * 
     * @param target - the target to determine an action for
     * @param targetIndex - index of the target in the WebView
     * @return a string to be used on the provided target
     */
    String composeActionForTarget(WebElement target, int targetIndex) {
        if (isClickable(target)) {
            if (isExternalLink(target)) {
                return composeFollowExternalLinkAndReturnMessage(targetIndex);
            } else {return composeClickButtonMessage(targetIndex);}
        } else if (isInput(target)) {
            String inputString;
            if (isEmptyInputSearch(target)) {inputString = "a";}
            else {inputString = inputStringForBackspace;}
            return composeInsertTextMessage(targetIndex, inputString);
        } else {
            System.out.println("composeActionForTarget given unclickable, uninputable target.");
            return "";
        }
    }

    /**
     * Strips out unnecessary formatting so that the action may be logged
     * in JSON formatting. 
     * 
     * @param action - the action string to be stripped
     * @return the stripped action string
     */
    private String stripCoreOfAction(String action) {
        return action.substring(logMessageActionPrefix.length() + 1, action.length() - 2);
        // actions have quotes so they can be string types within JSON output
    }

    /**
     * Performs the given action string upon the list of possible targets
     * 
     * @param action - the action to perform
     * @param possibleTargets - the collection of potential targets to be acted upon
     */
    void doAction(String action, ArrayList<WebElement> possibleTargets) {
        if (action.startsWith(logMessageActionPrefix)) {
            action = stripCoreOfAction(action);
        } else {System.out.println(String.format("SHOULDN'T REACH HERE EVER. line was \"%s\"", action));}
        int targetIndex;
        String inputString = "";

        if (action.startsWith(logMessageButtonClick)) {
            targetIndex = Integer.parseInt(action.substring(logMessageButtonClick.length()));
            doAction(possibleTargets.get(targetIndex), targetIndex, inputString);
        } else if (action.startsWith(logMessageTextboxInput)) {
            targetIndex = Integer.parseInt(action.substring(logMessageTextboxInput.length(), action.indexOf(logMessageDataSeparator)));
            inputString = action.substring(action.indexOf(logMessageDataSeparator) + 1);
            doAction(possibleTargets.get(targetIndex), targetIndex, inputString);
        } else if (action.startsWith(logMessageFollowExternalLinkAndReturn)) {
            targetIndex = Integer.parseInt(action.substring(logMessageFollowExternalLinkAndReturn.length()));
            doAction(possibleTargets.get(targetIndex), targetIndex, inputString);
        } else if (action.equals(logMessageDelay)) {
            delay();
        } else if (action.equals(logMessageHardwareBack)) {
            hardwareBack();
        } else if (action.equals(logMessageHardwareBackAfterDelay)) {
            hardwareBackAfterDelay();
        } else if (action.equals(logMessageWebviewBack)) {
            webviewBack();
        } else {
            System.out.println(String.format("SHOULDN'T REACH HERE EVER. action was \"%s\"", action));
        }
    }

    /**
     * Constructs a formatted comment out of the given string. 
     * 
     * @param commentString - the string to be formatted
     * @return - the resulting formatted string
     */
    static String composeComment(String commentString) {
        return String.format("%s%s", logMessageCommentPrefix, commentString);
    }

    /**
     * Constructs a formatted action message out of the given string.
     * 
     * @param action - the action string to format
     * @return - the resulting formatted string
     */
    private static String composeActionMessage(String action) {
        return String.format("%s\"%s\",", logMessageActionPrefix, action);
    }

    /**
     * Constructs an action to send a webview back signal
     *  
     * @return - the resulting string
     */
    static String composeWebviewBackMessage() {return composeActionMessage(logMessageWebviewBack);}

    /**
     * Constructs an action to hit the hardware back button
     * 
     * @return - the resulting string
     */
    static String composeHardwareBackMessage() {return composeActionMessage(logMessageHardwareBack);}

    /**
     * Constructs an action to delay input
     * 
     * @return - the resulting string
     */
    static String composeDelayMessage() {return composeActionMessage(logMessageDelay);}

    /**
     * Constructs an action message with the given action and target index
     * 
     * @param action - the action to construct a message with
     * @param targetIndex - target index to construct a message with
     * @return - the formatted action message
     */
    private static String composeActionWithTargetMessage(String action, int targetIndex) {
        return composeActionMessage(String.format("%s%s", action, targetIndex));
    }

    /**
     * Constructs an action to click a button
     * 
     * @param targetIndex - the target index to construct an action with
     * @return - the created action
     */
    static String composeClickButtonMessage(int targetIndex) {
        return composeActionWithTargetMessage(logMessageButtonClick, targetIndex);
    }

    /**
     * Constructs an action to follow an external link
     * 
     * @param targetIndex - the index of the target to construct an action with
     * @return - the created action
     */
    static String composeFollowExternalLinkAndReturnMessage(int targetIndex) {
        return composeActionWithTargetMessage(logMessageFollowExternalLinkAndReturn, targetIndex);
    }

    /**
     * Constructs an action with the given target and data message
     * 
     * @param action - the action to use
     * @param targetIndex - the index of the target to be acted upon
     * @param data - additional data to include in the action
     * @return - the constructed action 
     */
    private static String composeActionWithTargetAndDataMessage(String action, int targetIndex, String data) {
        return composeActionMessage(String.format("%s%s%s%s", action, targetIndex, logMessageDataSeparator, data));
    }

    /**
     * Constructs an action for inserting a text message
     * 
     * @param targetIndex - the index of the target to be acted upon
     * @param inputString - the input string to create the message with
     * @return - the constructed action 
     */
    static String composeInsertTextMessage(int targetIndex, String inputString) {
        return composeActionWithTargetAndDataMessage(logMessageTextboxInput, targetIndex, inputString);
    }

    /**
     * Constructs an action to send the hardware back signal after a specified delay
     * 
     * @return - the constructed action 
     */
    static String composeHardwareBackAfterDelayMessage() {
        return composeActionMessage(logMessageHardwareBackAfterDelay);
    }

    /**
     * Returns true/false whether or not the given line starts with a comment
     * 
     * @param line - the line to be examined
     * @return - true if the line starts with a comment else false
     */
    boolean isComment(String line) {return line.startsWith(logMessageCommentPrefix);}

    /**
     * Returns true/false whether or not the given line starts with an action 
     * 
     * @param line - the line to be examined
     * @return - true if the line starts with an action else false
     */
    boolean isAction(String line) {return line.startsWith(logMessageActionPrefix);}

    /**
     * Returns true/false whether or not the given action is a click button action
     * 
     * @param action - the action to be examined
     * @return - true if the action is a click button action else false
     */
    boolean isClickButton(String action) {return stripCoreOfAction(action).startsWith(logMessageButtonClick);}

    /**
     * Returns true/false whether or not the given action is a follow external link and return action
     * 
     * @param action - the action to be examined
     * @return - true if the action is a follow external link and return action else false
     */
    boolean isFollowExternalLinkAndReturn(String action) {return stripCoreOfAction(action).startsWith(logMessageFollowExternalLinkAndReturn);}
}
