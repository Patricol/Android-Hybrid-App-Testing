package test;

import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import serverDriver.SelendroidServerDriver;

public class NewSelendroidDriverTest {
	private static SelendroidServerDriver driver;
	/**
	 * Setup the SelendroidServerDriver only once.
	 * If already setup, sleep the thread 1 second between tests
	 */
	@BeforeClass
	public static void setup() {

		//these path names can change
		String apkPath = "..\\\\\\\\Artifacts\\\\\\\\employee-directory.apk";
		String capa = "io.selendroid.directory:0.0.1";
        driver = new SelendroidServerDriver(apkPath, capa);
        //Wait for driver to load
        try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * This test asserts that there are elements on the page that can be extracted.
	 * The test will return false if the application is native or null
	 */
	@Test 
	public void testExtractElements() {
		String failedString = "Total elements found: 0";
		assertTrue(!failedString.equals(driver.extractElements()));
	}
	
	/**
	 * This test checks if the page source can be extracted, the default of which is "".
	 */
	@Test
	public void testExtractPageSource() {
		String emptyPage = "";
		assertTrue(!emptyPage.equals(driver.extractPageSource()));
	}
	
	/**
	 * Tests if an element can be extracted from a page
	 */
	@Test 
	public void testExtractElementNames() {
		String emptyElements = "";
		assertTrue(!emptyElements.equals(driver.extractElementNames()));
	}
	
	@AfterClass
	public static void teardown() {
		driver.end();
	}

}