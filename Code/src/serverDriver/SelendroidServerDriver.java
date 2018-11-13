package serverDriver;

import io.selendroid.standalone.SelendroidConfiguration;
import io.selendroid.standalone.SelendroidLauncher;
import io.selendroid.common.SelendroidCapabilities;
import io.selendroid.client.SelendroidDriver;
import io.selendroid.client.SelendroidKeys;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * The purpose of this class is to provide an interface with the Selendroid server itself. 
 * It is responsible for starting the Selendroid Server and extracting information from the application.
 */
public class SelendroidServerDriver {
	private SelendroidLauncher selendroidServer;
	private SelendroidDriver driver;
	private HashMap<Integer, String> pagesFound = new HashMap<>();
	private HashMap<Integer, String> pagesClicked = new HashMap<>();
	private int linksFound = 1;
	private int linksClicked = 1;
	
	public ArrayList<WebElement> possibleTargets;
	static public double defaultWaitTimeSeconds = 0.5;
	
	
	/**
	 * Creates a SelendroidServerDriver instance with a specified
	 * application APK path and capabilities and launches a Selendroid 
	 * server with those parameters.
	 * 
	 * Launches the Selendroid server with the previously given launch
     * parameters.
     *
	 * @param apkPath - the path of the target APK
	 * @param capabilities - the capabilities of the APK (see the help guide on "capabilities" for more information) 
	 */
	public SelendroidServerDriver(String apkPath, String capabilities) {
        SelendroidConfiguration config = new SelendroidConfiguration();
        config.addSupportedApp(apkPath); // Specifies target APK
        this.selendroidServer = new SelendroidLauncher(config);
        selendroidServer.launchSelendroid();
        SelendroidCapabilities caps = new SelendroidCapabilities(capabilities); // Passes capabilities to Selendroid server
        // AppId for hybridtestapp.apk: com.example.hybridtestapp:1.0
        try {driver = new SelendroidDriver(caps);} // Makes a new driver
        catch (java.lang.Exception e) {e.printStackTrace();} // Couldn't start Selendroid.
	}

	/**
	 * Extracts the current activity's hierarchy view. 
	 * 
	 * Note: remains unimplemented as in previous codebase; does not fit into priorities of user stories. 
	 * 
	 * @return the overall hierarchy of the application's current activity
	 */
	public String extractHierarchy() {
        switchToWebView();
		return String.format("%s\n", driver.getPageSource());
	}

	
	/**
	 * Extracts the entire page source for the current page. 
	 * 
	 * The entire page source includes the WebView HTML and the widget hierarchy. 
	 * 
	 * @return page source as a string
	 */
	public String extractPageSource() {
		String firstSource = driver.getPageSource();
		return String.format("%s\n%s", firstSource, extractHierarchy());
	}

	/**
	 * Returns just the WebView HTML source. 
	 * 
	 * @return the HTML source of the WebView as a string
	 */
	public String extractWebViewHTML() {
	    //TODO figure out if this is actually supposed to do the same thing as extractHierarchy()
	    return extractHierarchy();
	}

	/**
	 * Extracts all interactive elements from the current WebView.
	 * 
	 * These found elements are then added to an array of possible
	 * targets. All elements are found, but only the ones with 
	 * attributes suggesting that they are interactive are kept. 
	 *
	 * @return the list of elements as a formatted string
	 */
	public String extractElements() {
	    switchToWebView();
		
		// Get a list of all elements
		List<WebElement> elementsByCss = driver.findElements(By.cssSelector("*")); 
		
		possibleTargets = new ArrayList<>();
		System.out.println(elementsByCss);
		String outputString = "";
		int count = 0;
		
		for (WebElement e : elementsByCss) {
			if (e.getTagName().equals("a") || e.getTagName().equals("input")) { // check if they're ones we want
				possibleTargets.add(e);
				count += 1;
	
	            // Make the formatting pretty!
	            String format = "\t[%s]: %s\n";
	            String tagName = String.format(format, "Tag Name", e.getTagName());
	            String name = String.format(format, "Name", e.getTagName());
	            String href = String.format(format, "href", e.getAttribute("href"));
	            String id = String.format(format, "ID", e.getAttribute("id"));
	            String location = String.format(format, "Location", e.getLocation());
	            String dimension = String.format(format, "Dimension", e.getSize());
	            outputString = String.format("%s[Element %s]: %s\n%s%s%s%s%s%s \n", outputString, Integer.toString(count), e.getText(), tagName, name, href, id, location, dimension);
				
				int hashCode = e.getAttribute("href").hashCode();
				if (e.getTagName().equals("a") && !pagesFound.containsKey(hashCode)) {
					linksFound += 1;
					pagesFound.put(hashCode, e.getAttribute("href"));
				}
			}
		}
	    outputString = String.format("%sTotal elements found: %s", outputString, Integer.toString(count)); // Print the total amount of elements found
		System.out.println(outputString);
		return(outputString); 
	}

	/**
	 * Allows a tester to remove a hashed paged if it has been clicked,
	 * as well as add it to the list of clicked pages
	 */
	public void setPageAsVisited(String HREF) {
		int hashCode = HREF.hashCode();
		if (!pagesClicked.containsKey(hashCode)) {
			linksClicked += 1;
			pagesClicked.put(hashCode, HREF);
		}
	}
	
	/**Returns the number of links clicked during the test */
	public int getLinksClicked() {
		return linksClicked;
	}
	
	/**Returns the number of links found during the test */
	public int getLinksFound() {
		return linksFound;
	}
	/**
	 * Extracts the names of all elements in the WebView and returns them
	 * as a formatted XML string. 
	 * 
	 * @return names of elements found as an XML string
	 */
	public String extractElementNames(){
	    switchToWebView();
		// Get a list of all elements
		List<WebElement> elementsByCss = driver.findElements(By.cssSelector("*")); 
		String outputString = "";
		
		for (WebElement e : elementsByCss) {
			
			if (e.getTagName().equals("input")) { // Check if they're ones we want
	            String name = String.format("name=\"%s\"", e.getAttribute("name"));
	            String type = String.format("type=\"%s\"", e.getAttribute("type"));
	            String value = String.format("value=\"%s\"", e.getAttribute("value"));
				System.out.println(type);
	            System.out.println(name);
				System.out.println(value);
	            outputString = String.format("%s<%s %s %s %s/>", outputString, e.getTagName(), type, name, value);
			} 
			else if (e.getTagName().equals("a")) {
				
	            String name = String.format("name=\"%s\"", e.getAttribute("name"));
	            String href = String.format("href=\"%s\"", e.getAttribute("href"));
				outputString = String.format("%s<%s %s %s/>", outputString, e.getTagName(), name, href);
			}
			
		}
		
		return(outputString); 
	}

	/**
	 * Extracts all elements whose names match the given keyword
	 * 
	 * @param keyword - the keyword to match
	 * @return outputString - formatted output of found elements 
	 */
	public String extractElementsByKeyword(String keyword){
		List<WebElement> elements = driver.findElements((By.name(keyword)));
		String outputString = "";
		
		for (WebElement e: elements) {
			String foundElement = e.getTagName();
			System.out.println(foundElement);
			outputString = String.format("%s | %s", outputString, foundElement);
		}
		
		return outputString;
	}

	/**
	 * Returns the number of pages visited.
	 * 
	 * @return number of pages visited, if any
	 */
	public int getPagesVisited() {
	    return pagesClicked.size();
	}

	/**
	 * Returns the coverage of the test.
	 * Coverage percentage is represented as a floating point value
	 * equivalent to the equation (links clicked / links found)
	 *  
	 * @return the coverage percentage of the test as a float
	 */
	public float getCoverage() {
		return (float)linksClicked / (float)linksFound;
	}
	
	/**
	 * Forces the driver to switch to the WebView context
	 * 
	 * This is necessary if the driver was previously switched to the native view context
	 * as nothing can be extracted from the WebView if the WebView context isn't being used.
	 */
	public void switchToWebView(){
	    driver.switchTo().window("WEBVIEW");
	}

	/**
     * Simulates pressing the hardware back button on an Android device 
     * for the purpose of going back to a previous activity while testing. 
     */
    public void hardwareBack(){
    	new Actions(driver).sendKeys(SelendroidKeys.BACK).perform();
    }
 
    /**
     * Simulates pressing the WebView back button for the purpose of 
     * going back a page in the WebView history.
     */
    public void webviewBack(){
        driver.executeScript("window.history.go(-1)");
    }
    
	/**
	 * Resumes the application from the foreground. 
	 * 
	 * You'll notice that this calls driver.backgroundApp() first. 
	 * The reason for this is that in the context of our application, 
	 * when you "leave" the app the app is still technically in the 
	 * foreground in some cases, like when you're on the phone screen
	 * or texting screen. Nothing happens if you call backgroundApp() if its
	 * already in the background, so this calls it just in case. 
	 */
	public void resumeApp(){
		driver.backgroundApp(); 
		driver.resumeApp();
	}

	/**
	 * Hides the application by sending it to the background. 
	 */
	public void hideApp(){
		driver.backgroundApp();
	}
	
	/**
	 * Closes the application being tested. 
	 */
	public void closeApp() {
		driver.close();
	}
	
	/**
	 * Tears down the Selendroid server.
	 * 
	 * This method is useful for cleanup and necessary
	 * if different Selendroid capabilities must be selected.
	 */
	public void end(){ 
		if(driver != null){driver.quit();}
	    if (selendroidServer != null) {selendroidServer.stopSelendroid();}
	}

	/**
	 * Scrolls the application window until the given target is in view.
	 * @param target
	 */
	public void putElementInView(WebElement target) {
		driver.executeScript("arguments[0].scrollIntoView(true);", target);
	}
	
	/**
	 * Interacts with the given target by simulating a click in JavaScript.
	 * 
	 * This is done to ensure that the target is always present in the list view. 
	 * @param target
	 */
	public void clickTargetJS(WebElement target) {
		driver.executeScript("arguments[0].click();", target);
		//TODO isn't this the same thing as target.click()?
	}
}