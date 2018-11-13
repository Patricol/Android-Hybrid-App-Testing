package testers;

import serverDriver.SelendroidServerDriver;

/** 
 * This class implements the automated testing functionality. 
 * It does this by launching a thread for the Selendroid server driver 
 * and feeding it testing input and interactions.
 */
public class AutomatedTester {
	private SelendroidServerDriver serverDriver;
	private TestingThread testingThread;
	private Tester tester;
	private int waitTime;
	
	
	/**
	 * Creates an instance of AutomatedTester.
	 * 
	 * @param serverDriver - a SelendroidServerDriver instance based on the APK to be tested
	 * @param tester - a Tester instance representing the type of tester to be used (see the help guide for testing types)
	 */
	public AutomatedTester(SelendroidServerDriver serverDriver, Tester tester){
        this.serverDriver = serverDriver;
        this.waitTime = (int) SelendroidServerDriver.defaultWaitTimeSeconds*1000;
		this.tester = tester;
	}

	/**
	 * Starts the testing thread.
	 */
	public void startTesting() {
		testingThread = new TestingThread(serverDriver, waitTime, tester);
	}

	/**
	 * Puts the testing thread to sleep for the given wait time.
	 * 
	 * This wait time is necessary to allow for various parts of the UI 
	 * to finish being animating, loading, or interacted with.
	 *  
	 * @param waitTime - the time to wait in milliseconds as a long
	 */
	static void sleep(long waitTime) {
        try {Thread.sleep(waitTime);}
        catch (java.lang.InterruptedException e) {e.printStackTrace();}
    }
	
	/**
	 * Performs a single test upon the current page.
	 * 
	 * @param serverDriver - an instance of SelendroidServerDriver on the APK to be tested
	 * @param waitTime - the time to wait in milliseconds as a long
	 * @param tester - an instance of Tester representing the type of tester to be used (see the help guide for testing types)
	 * @return boolean whether or not there are possible targets left to be tested
	 */
	static boolean singleTest(SelendroidServerDriver serverDriver, int waitTime, Tester tester) {
	    boolean unfinished = true;
        // Extract and execute inputs
		try {
			serverDriver.extractElements();
			unfinished = tester.test(serverDriver.possibleTargets);
        } catch (org.openqa.selenium.WebDriverException e) {
            System.out.println("WebDriverException caught...");
            serverDriver.resumeApp();
        } catch (Exception e) {
			//Returns us to the application
            e.printStackTrace();
			serverDriver.resumeApp();
		}

		// Wait to allow input to be carried out.
		// TODO: use our own sleep method? 
        sleep(waitTime);
		return unfinished;
	}

	/**
	 * Overloaded, "default" singleTest method 
	 * 
	 * In this implementation of singleTest the serverDriver, waitTime, and tester 
	 * fields of the AutomatedTester instance are used by default. 
	 */
	public void singleTest() {
	    singleTest(this.serverDriver, this.waitTime, this.tester);
    }

	/**
	 * Stops the testing thread.
	 */
	public void stopTesting() {testingThread.stopThread();}
	
	/**
	 * Pauses the testing thread.
	 */
	public void pauseInput() {testingThread.pauseThread();}

	/**
	 * Resumes the testing thread.
	 */
	public void resumeInput() {testingThread.resumeThread();}

	/**
	 * Sets the wait time to be used. 
	 * 
	 * Users can specify an interval to be used in between
	 * testing commands being sent to the server driver either from 
	 * within the GUI or via this method. This is useful in instances 
	 * when a user might want to monitor more closely the actions taking
	 * place. 
	 * @param interval - the wait time to be used in milliseconds as an int
	 */
	public void setWaitTime(int interval) {this.waitTime = interval;}
	
	public void setTester(Tester newTester) {this.tester = newTester;}
	
	public Tester getTester() {return this.tester;}
}

/**
 * TestingThread is a runnable testing thread for the tester being used
 * on the provided Selendroid server driver instance. The resulting 
 * thread will run as long as there are untested elements provided or
 * it is otherwise stopped manually.
 */
class TestingThread implements Runnable{
	SelendroidServerDriver serverDriver;
	private Tester tester;
	private Thread thrd;
	private boolean suspended;
	private boolean stopped;
	private boolean unfinished;
	private int waitTime;
	
	/**
	 * Creates an instance of TestingThread with the given Selendroid server driver, 
	 * wait time interval, and tester to be used. This constructor is to be used by 
	 * AutomatedTester
	 * 
	 * @param serverDriver - an instance of SelendroidServerDriver on the APK being tested
	 * @param waitTime - the wait time to be used in milliseconds as an int
	 * @param tester - a Tester instance representing the type of tester to be used (see the help guide for testing types)
	 */
	TestingThread(SelendroidServerDriver serverDriver, int waitTime, Tester tester) {
		this.thrd = new Thread(this);
		this.tester = tester;
		this.serverDriver = serverDriver;
		this.waitTime = waitTime;
		this.suspended = false;
		this.stopped = false;
		System.out.println("Wait time of : " + Integer.toString(waitTime));
		thrd.start();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public synchronized void run() {
        System.out.println("Started generating inputs");
        
        while (!stopped) {
            
        	while (!suspended && !stopped) {
                unfinished = AutomatedTester.singleTest(serverDriver, waitTime, tester);
                AutomatedTester.sleep(300);
                if (!unfinished) {stopThread();}
            }
            while (suspended) {AutomatedTester.sleep(300);}// Sleep while we're paused    
        }
        
        thrd.interrupt();
        System.out.println("Testing thread stopped.");
	}
	
	/**
	 * Sets a flag indicating to the thread that it should stop. 
	 * 
	 * Note: the thread may only stop after it has completed the most-
	 * recently sent test command. 
	 */
	void stopThread(){stopped = true;}
	
	/**
	 * Sets a flag indicating to the thread that it should suspend. 
	 * 
	 * Note: the thread may only enter suspension after it has 
	 * completed the most-recently sent test command. 
	 */
	void pauseThread(){
		suspended = true;
		System.out.println("Testing thread paused...");
	}
	
	/**
	 * Sets a flag indicating that the thread should resume execution. 
	 */
	void resumeThread(){
		suspended = false;
		System.out.println("Resuming thread...");
	}
}