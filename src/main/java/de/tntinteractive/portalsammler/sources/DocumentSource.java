/*
    Copyright (C) 2013  Tobias Baum <tbaum at tntinteractive.de>

    This file is a part of Portalsammler.

    Portalsammler is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Portalsammler is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Portalsammler.  If not, see <http://www.gnu.org/licenses/>.
 */
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
