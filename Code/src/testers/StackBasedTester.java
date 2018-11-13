package testers;

import logger.LogFileGenerator;
import org.openqa.selenium.WebElement;
import serverDriver.SelendroidServerDriver;

import java.util.ArrayList;
import java.util.Stack;

/**
 * StackBasedTester is an extension of LoggingTester and provides
 * a stack-based approach to 'replaying' a log file of previously-
 * recorded interactions with the APK. 
 */
abstract class StackBasedTester extends LoggingTester {
    Stack<String> futureTargets;
    Stack<String> newFutureTargets;
    private Stack<String> laterFutureTargets;
    private Stack<String> tempStack;
    private ArrayList<String> pathFromStart;

    /**
     * Creates an instance of StackBasedTester with the given log file
     * generator, input test strings, and Selendroid server driver instance.
     * 
     * @param logFile - an instance of the LogFileGenerator
	 * @param testStrings - the strings to be used during testing
	 * @param driver - an instance of SelendroidServerDriver on the APK being tested
     */
    StackBasedTester(LogFileGenerator logFile, ArrayList<String> testStrings, SelendroidServerDriver driver) {
        super(logFile, testStrings, driver);
        this.futureTargets = new Stack<>();
        this.newFutureTargets = new Stack<>();
        this.laterFutureTargets = new Stack<>();
        this.tempStack = new Stack<>();
        this.pathFromStart = new ArrayList<>();
    }

    /**
     * Pops elements off of the top of the stack and adds them to the collection
     * of targets yet to be visited.
     * 
     * @param stack - the stack of elements to be added to the colletion of future targets
     */
    private void moveStackToIterator(Stack<String> stack) {
        while (!stack.isEmpty()) {futureTargets.push(stack.pop());}
    }

    /**
     * Moves the future targets to the iterator. 
     */
    void moveFutureTargetsToIterator() {moveStackToIterator(newFutureTargets);}

    /**
     * Moves later future target stack elements to the iterator. 
     */
    void moveLaterFutureTargetsToIterator() {moveStackToIterator(laterFutureTargets);}

    /**
     * Store new targets in the later future targets stack. 
     */
    void saveNewTargetsForLater() {
        while (!newFutureTargets.isEmpty()) {tempStack.push(newFutureTargets.pop());}
        while (!tempStack.isEmpty()) {laterFutureTargets.push(tempStack.pop());}
    }

    /**
     * Adds targets that are yet to be tested to the stack of new future targets.
     * 
     * @param target -- the target to be added to the stack
     * @param targetIndex - the index of the target to be added to the stack
     */
    void planActionForTarget(WebElement target, int targetIndex) {
        newFutureTargets.push(composeActionForTarget(target, targetIndex));
    }

    /**
     * Performs the next action from the top of the future targets stack.
     * 
     * @param possibleTargets - the list of potential targets to be interacted with
     */
    void doNextPlannedAction(ArrayList<WebElement> possibleTargets) {
        String action = futureTargets.pop();

        if (action.equals(composeWebviewBackMessage())) {pathFromStart.remove(pathFromStart.size()-1);}
        else if (isClickButton(action) && !isInput(possibleTargets.get(0))) {pathFromStart.add(action);}

        doAction(action, possibleTargets);
    }

    /**
     * Pushes all commands necessary to navigate to the current state from the starting page 
     * to the new future targets stack. 
     */
    void addPathFromStart() {
        for (String aPathFromStart : pathFromStart) {
            newFutureTargets.push(aPathFromStart);
        }
    }

    /**
     * Pushes all commands necessary to navigate to the starting state from the current state
     * to the new future targets stack. 
     */
    void addPathToStart() {
        for (int i=0; i<pathFromStart.size(); i++) {
            newFutureTargets.push(composeWebviewBackMessage());
        }
    }
}
