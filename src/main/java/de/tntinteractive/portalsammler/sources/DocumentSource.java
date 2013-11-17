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

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.RefreshHandler;

import de.tntinteractive.portalsammler.engine.ByPartialButtonText;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.gui.UserInteraction;

public abstract class DocumentSource {

    protected static final int WAIT_TIME = 30;

    private final String id;

    public DocumentSource(String id) {
        this.id = id;
    }

    public final String getId() {
        return this.id;
    }

    /**
     * Stellt eine Verbindung zum Portal her, ruft die aktuellen Dokumente ab und speichert sie im
     * übergebenen {@link SecureStore}. Wenn ein Dokument bereits im {@link SecureStore} vorliegt,
     * wird es nicht erneut heruntergeladen.
     *
     * @return Die erste Zahl ist die Anzahl neuer Dokumente, die zweite die untersuchter und schon bekannter.
     *          D.h. die Summe der beiden Zahlen ergibt die Gesamtanzahl an untersuchten Dokumenten.
     */
    public abstract Pair<Integer, Integer> poll(SourceSettings settings, UserInteraction gui,
            SecureStore store) throws Exception;

    private static final class AllOrNothingRefreshHandler implements RefreshHandler {

        private final ImmediateRefreshHandler immediateHandler = new ImmediateRefreshHandler();

        @Override
        public void handleRefresh(Page page, URL url, int seconds) throws IOException {
            //einige Seiten nutzen den refresh für echte Navigation, andere für Timeouts
            //  da echte Navigation eher kurz ist und Timeouts eher lang, wird hier über den Daumen gepeilt,
            //  was gewollt ist. Echte Navigation wird sofort vollzogen und Timeouts nie.
            if (seconds < 20) {
                this.immediateHandler.handleRefresh(page, url, seconds);
            }
        }

    }

    protected final WebDriver createDriver(String url) {
        final HtmlUnitDriver driver = new HtmlUnitDriver(true) {
            {
                this.getWebClient().setRefreshHandler(new AllOrNothingRefreshHandler());
            }
        };

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

    protected static void clickButton(final WebDriver driver, String partialText) {
        final WebElement button = driver.findElement(byPartialButtonText(partialText));
        button.click();
    }

    protected static void waitForPresence(WebDriver driver, By by) {
        (new WebDriverWait(driver, WAIT_TIME)).until(ExpectedConditions.presenceOfElementLocated(by));
    }

    protected static By byPartialButtonText(String text) {
        return new ByPartialButtonText(text);
    }

    protected static Date parseDate(String text) throws ParseException {
        return new SimpleDateFormat("dd.MM.yyyy").parse(text);
    }

}
