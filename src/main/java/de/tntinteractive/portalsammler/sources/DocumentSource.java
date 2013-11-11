package de.tntinteractive.portalsammler.sources;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.tntinteractive.portalsammler.engine.ByPartialButtonText;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SourceSettings;

public abstract class DocumentSource {

    protected static final int WAIT_TIME = 30;

    private final String id;

    public DocumentSource(String id) {
        this.id = id;
    }

    public final String getId() {
        return this.id;
    }

   public abstract void poll(SourceSettings settings, SecureStore store) throws Exception;

   protected final WebDriver createDriver(String url) {
       final WebDriver driver = new HtmlUnitDriver(true);

       driver.manage().timeouts().implicitlyWait(WAIT_TIME, TimeUnit.SECONDS);
       driver.get(url);
       return driver;
   }

   protected static boolean startsWith(String name, String prefix) {
       return name != null && name.startsWith(prefix);
   }

   protected static void clickLink(final WebDriver driver, String partialText) {
       final WebElement link = driver.findElement(By.partialLinkText(partialText));
       link.click();
   }

   protected static void waitForPresence(WebDriver driver, By by) {
       (new WebDriverWait(driver, WAIT_TIME)).until(ExpectedConditions.presenceOfElementLocated(by));
   }

   protected static By byPartialButtonText(String text) {
       return new ByPartialButtonText(text);
   }

}
