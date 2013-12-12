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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import de.tntinteractive.portalsammler.engine.DocumentFormat;
import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.engine.UserInteraction;

public final class HanVBSourceV1 extends DocumentSource {

    static final SettingKey USER = new SettingKey("VR-Kennung");
    static final SettingKey PASSWORD = new SettingKey("Online-PIN");

    public HanVBSourceV1(final String id) {
        super(id);
    }

    @Override
    public Pair<Integer, Integer> poll(final SourceSettings settings, final UserInteraction gui,
            final SecureStore store) throws Exception {
        final WebDriver driver = this.createDriver("https://www.hanvb.de/ptlweb/WebPortal?bankid=0744");

        final WebElement userField = driver.findElement(By.id("vrkennungalias"));
        userField.sendKeys(settings.get(USER, gui));

        final WebElement passwordField = driver.findElement(By.id("pin"));
        passwordField.sendKeys(settings.get(PASSWORD, gui));

        passwordField.submit();

        waitForPresence(driver, By.linkText("Postkorb"));

        int newDocs = 0;
        int knownDocs = 0;
        try {
            clickLink(driver, "Postkorb");
            waitForPresence(driver, By.name("confirmDeleteMultiMessage"));

            final WebElement form = driver.findElement(By.name("confirmDeleteMultiMessage"));
            final WebElement tbody = form.findElement(By.tagName("tbody"));

            final List<String> links = new ArrayList<String>();
            for (final WebElement tr : tbody.findElements(By.tagName("tr"))) {
                final List<WebElement> tds = tr.findElements(By.tagName("td"));
                final WebElement link = tds.get(2).findElement(By.tagName("a"));
                links.add(link.getAttribute("href"));
            }

            for (final String link : links) {
                driver.get(link);

                final DocumentInfo info = DocumentInfo.create(this.getId(), DocumentFormat.TEXT);
                final StringBuilder content = new StringBuilder();

                driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
                final WebElement fieldset = driver.findElement(By.tagName("fieldset"));
                for (final WebElement div : fieldset.findElements(By.tagName("div"))) {
                    if (this.isLabeled(div, "Eingang")) {
                        info.setDate(this.parseTimestamp(div.findElement(By.className("gad-field-box")).getText()));
                    } else if (this.isLabeled(div, "Betreff")) {
                        final WebElement data = div.findElement(By.className("gad-field-box"));
                        info.addKeywords(data.findElement(By.tagName("span")).getAttribute("title"));
                    } else if (this.isNotLabeled(div)) {
                        content.append(div.getText());
                    }
                }
                driver.manage().timeouts().implicitlyWait(WAIT_TIME, TimeUnit.SECONDS);

                if (!store.containsDocument(info)) {
                    store.storeDocument(info, content.toString().getBytes("UTF-8"));
                    newDocs++;
                } else {
                    knownDocs++;
                }
            }

        } finally {
            clickLink(driver, "Logout");
        }
        return Pair.of(newDocs, knownDocs);
    }

    private boolean isNotLabeled(final WebElement div) {
        return !this.isLabeled(div, "");
    }

    private boolean isLabeled(final WebElement div, final String string) {
        for (final WebElement label : div.findElements(By.tagName("label"))) {
            if (label.getText().contains(string)) {
                return true;
            }
        }
        return false;
    }

    private Date parseTimestamp(final String time) throws ParseException {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(time);
    }

}
