package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import logger.LogFileGenerator;
import serverDriver.SelendroidServerDriver;
import testers.AutomatedTester;
import testers.BreadthFirstTester;
import testers.DepthFirstTester;
import testers.LogReplayTester;
import testers.RandomTester;


/**
 * 
 * Oracle: Implementing/searching for an oracle for this test-suite was a hard task. Originally our team believed log-files may 
 * serve as the oracle for our testers, but this consideration was undermine by the uniqueness of log-files across different applications.  
 * For example, a log-file for employee directory would be completely different than a log file for another hybrid app. As a result, 
 * our team came up with the test-case/coverage JUnit test class.  It allows the tester to hard code the minimum test cases that need to be
 * run as well as the minimum amount of coverage that needs to be reached before the JUnit test can pass. This allows for terminating condition
 * testing as well as produce log files that will end at the exact spot an error occurred. 
 * 
 * This JUnit test class was designed to ensure that the testers could be run for a certain amount 
 * of test cases without throwing errors, as well as return a minimum amount of coverage after those test 
 * cases have been run.  The default number of test cases is 5, with the default coverage of 30%. It is recommended 
 * that 100 test cases are run and above 95% of coverage has been achieved. 
 * @author Thomas Raddatz
 *
 */
public class TestersJUnitTestSuite {
	
	/////////////////SET MINIMUM TEST CASES AND COVERAGE/////////////////////////
	int testCases = 5; //minimum test cases 5 --> 5 test cases must be completed before completion &&
	float testCoverage = (float) .3; //testCoverage = .3 --> 30% test coverage required before completion 
	////////////////////////////////////////////////////////////////////////////
	
	private static SelendroidServerDriver driver;
	String apkPath = "..\\\\Artifacts\\\\employee-directory.apk";
	String capa = "io.selendroid.directory:0.0.1";
	private boolean setUpIsDone = false;
	String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456890!@#$%^&*()_+,./<>?;':";
	ArrayList<String> testInput = new ArrayList<String>();
	char[] alphaArr = alpha.toCharArray();
	int testComplete;

	@Before
	public void setup() {
		driver = new SelendroidServerDriver(apkPath, capa);
		for (char c : alphaArr) {testInput.add("" + c);}
		testComplete = 0;
		sleep(1000);
		
	}

	private void sleep(long milliseconds) {
        try	{Thread.sleep(milliseconds);}
        catch (java.lang.InterruptedException e) {e.printStackTrace();}
    }
	
	/**
	 * Test a breadth-first path through the application, checking if the tester is 
	 * behaving properly. If the tester returns an error than it is fundamentally broken
	 */
	@Test
	public void testBreadthFirstTester() {
		String directoryPath = "";
		LogFileGenerator logFileGenerator = new LogFileGenerator(directoryPath);
		BreadthFirstTester bfsTester = new BreadthFirstTester(logFileGenerator, testInput, driver);
		AutomatedTester automatedTester = new AutomatedTester(driver, bfsTester);
        automatedTester.setTester(bfsTester);
        //Loop through the desired amount of test cases, stopping after a pre-determined coverage has been reached
        while(driver.getCoverage() < testCoverage || testCases > testComplete) {
        	sleep(500);
            automatedTester.singleTest();
            testComplete += 1;
        }
        assertTrue(driver.getCoverage() >= testCoverage);
        assertTrue(testCases <= testComplete);
        System.out.printf("Test Cases Completed: %d \nTest Coverage: %.2f", testComplete, driver.getCoverage());
    	logFileGenerator.close();
    	sleep(500);
	}
	
	
	/**
	 * Test a non-deterministic path through the application, checking if the tester is 
	 * behaving properly. If the tester returns an error than it is fundamentally broken
	 */
	@Test
	public void testRandomTester() {
		String directoryPath = "";
		LogFileGenerator logFileGenerator = new LogFileGenerator(directoryPath);
		RandomTester randomTester = new RandomTester(logFileGenerator, testInput, driver);
		AutomatedTester automatedTester = new AutomatedTester(driver, randomTester);
        automatedTester.setTester(randomTester);
        //Loop through the desired amount of test cases, stopping after a pre-determined coverage has been reached
        while(driver.getCoverage() < testCoverage || testCases > testComplete) {
        	sleep(500);
            automatedTester.singleTest();
            testComplete += 1;
        }
        assertTrue(driver.getCoverage() >= testCoverage);
        assertTrue(testCases <= testComplete);
        System.out.printf("Test Cases Completed: %d \nTest Coverage: %.2f", testComplete, driver.getCoverage());
    	logFileGenerator.close();
    	sleep(500);
	}
	/**
	 * Test a Depth-First path through the application, checking if the tester is 
	 * behaving properly. If the tester returns an error than it is fundamentally broken
	 */
	@Test
	public void testDepthFirstTester() {
		String directoryPath = "";
		LogFileGenerator logFileGenerator = new LogFileGenerator(directoryPath);
		DepthFirstTester depthTester = new DepthFirstTester(logFileGenerator, testInput, driver);
		AutomatedTester automatedTester = new AutomatedTester(driver, depthTester);
        automatedTester.setTester(depthTester);
        //Loop through the desired amount of test cases, stopping after a pre-determined coverage has been reached
        while(driver.getCoverage() < testCoverage || testCases > testComplete) {
        	sleep(500);
            automatedTester.singleTest();
            testComplete += 1;
        }
        assertTrue(driver.getCoverage() >= testCoverage);
        assertTrue(testCases <= testComplete);
        System.out.printf("Test Cases Completed: %d \nTest Coverage: %.2f", testComplete, driver.getCoverage());
    	logFileGenerator.close();
    	sleep(500);
	}
	
	
	/**
	 * Performs a randomTest and then runs the recorded log-file again.  It is assumed this input would throw an error if the log
	 * file was corrupt, thus failing the test.  This JUnit test will record the first random test, then shut-down and restart.
	 * Upon restarting, the previous log-file will be chosen and run through the logReplayTester.
	 */
	@Test
	public void testLogReplayTester() {
		String directoryPath = "";
		LogFileGenerator logFileGenerator = new LogFileGenerator(directoryPath);
		RandomTester randomTester = new RandomTester(logFileGenerator, testInput, driver);
		AutomatedTester automatedTester = new AutomatedTester(driver, randomTester);
        automatedTester.setTester(randomTester);
        File logToReplay = logFileGenerator.getFilePath();
        //Loop through the desired amount of test cases, stopping after a pre-determined coverage has been reached
        while(driver.getCoverage() < testCoverage || testCases > testComplete) {
        	sleep(500);
            automatedTester.singleTest();
            testComplete += 1;
        }
    	logFileGenerator.close();
    	sleep(500);
    	
    	driver.end();
		sleep(1000);
		driver = new SelendroidServerDriver(apkPath, capa);
		for (char c : alphaArr) {testInput.add("" + c);}
		testComplete = 0;
		sleep(1000);
		
        testComplete = 0;
        AutomatedTester automatedTester1 = new AutomatedTester(driver, new DepthFirstTester(logFileGenerator, testInput, driver));
        automatedTester1.setTester(new LogReplayTester(driver, logToReplay));
        while(testCases >= testComplete) {
        	sleep(500);
            automatedTester1.singleTest();
            testComplete += 1;
        }
	}

	@After
	public void teardown() {
		driver.end();
	}
}




	
	
