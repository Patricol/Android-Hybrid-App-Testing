package testers.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.*;
import org.openqa.selenium.WebElement;

import serverDriver.SelendroidServerDriver;
import testers.AutomatedTester;
import testers.RandomTester;

public class RandomTesterTest extends TesterTest {

	@Test
	public void testTest() {
		SelendroidServerDriver newDriver = getDriver();
		RandomTester randomTester = new RandomTester(getLogFileGenerator(), getTestInput(), newDriver);
		AutomatedTester automatedTester = new AutomatedTester(newDriver, randomTester);

		newDriver.extractElements();
		ArrayList<WebElement> possibleTargets = newDriver.possibleTargets;

		fail("Not yet implemented");
	}
}
