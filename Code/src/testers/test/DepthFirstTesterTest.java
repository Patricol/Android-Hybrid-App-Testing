package testers.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.*;
import org.openqa.selenium.WebElement;

import serverDriver.SelendroidServerDriver;
import testers.AutomatedTester;
import testers.DepthFirstTester;

public class DepthFirstTesterTest extends TesterTest {

	@Test
	public void testPlanNextMoves() {
		SelendroidServerDriver newDriver = getDriver();
		DepthFirstTester depthTester = new DepthFirstTester(getLogFileGenerator(), getTestInput(), newDriver);
		AutomatedTester automatedTester = new AutomatedTester(newDriver, depthTester);
		
		newDriver.extractElements();
		ArrayList<WebElement> possibleTargets = newDriver.possibleTargets;
		
		fail("Not yet implemented");
	}

}
