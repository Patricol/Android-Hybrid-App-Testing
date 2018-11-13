package testers;

import java.util.ArrayList;
import java.util.Random;
import org.openqa.selenium.WebElement;
import logger.LogFileGenerator;
import serverDriver.SelendroidServerDriver;
import java.io.BufferedReader;
import java.io.FileReader;

public class RandomTester extends LoggingTester {

    private Random random = new Random();

    private ArrayList<String> input;
	
	/**
	 * Default constructor that links the log file to the input generator.
	 * Inputs generated as well as interactive elements chosen are written into this log file
	 */
	public RandomTester(LogFileGenerator logFile, ArrayList<String> testStrings, SelendroidServerDriver driver) {
		super(logFile, testStrings, driver);
		input = testStrings;
		//readDictionary();
	}
	
	/**
	 * This method sends a random input to the application undergoing testing
     *
	 * @param possibleTargets - arraylist of web elements that can be interacted with.
	 */
	public boolean test(ArrayList<WebElement> possibleTargets) {
		logBracket(true);
		logPagesVisited();
		logPossibleTargetsData(possibleTargets);
		int randomIndex = (int)(possibleTargets.size()*Math.random());
		WebElement target = possibleTargets.get(randomIndex);
		touchElement(target, randomIndex, possibleTargets.size());
		logBracket(false);
		return true;
	}
	

	private String getRandomLineFromDictionary() {
        return input.get(random.nextInt(input.size()));
    }

	/**Act on the target based on its type*/
	private void touchElement(WebElement target, int targetIndex, int numTargets) {
	    String inputString = "";
        if (isInput(target)) {// clean the input box before enter an name
        	while (getLengthInInput(target)!=0) {
        		inputString = inputStringForBackspace;
                doAction(target, targetIndex, inputString);
        	}
            if (isDeadendInputSearch(target, numTargets)) {
                logComment("alert: \"Dead-end search detected...\",");
                inputString = inputStringForBackspace;
            } else {inputString = getRandomLineFromDictionary();}
		}	
		doAction(target, targetIndex, inputString);
	}
}