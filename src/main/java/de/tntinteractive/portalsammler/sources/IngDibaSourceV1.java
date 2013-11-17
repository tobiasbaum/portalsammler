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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.tntinteractive.portalsammler.engine.DocumentFormat;
import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.FileDownloader;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.gui.UserInteraction;

public class IngDibaSourceV1 extends DocumentSource {

    static final SettingKey USER = new SettingKey("Konto/Depotnummer");
    static final SettingKey PASSWORD = new SettingKey("Passwort");
    static final SettingKey CODE = new SettingKey("Secure Code");

    public IngDibaSourceV1(String id) {
        super(id);
    }

    @Override
    public Pair<Integer, Integer> poll(SourceSettings settings, UserInteraction gui,
            SecureStore store) throws Exception {
        final WebDriver driver = this.createDriver("https://banking.ing-diba.de/app/login");

        final WebElement userField = driver.findElement(By.name("view:kontonummer:border:border_body:kontonummer"));
        userField.sendKeys(settings.get(USER, gui));

        final WebElement passwordField = driver.findElement(By.name("view:pin:border:border_body:pin"));
        passwordField.sendKeys(settings.get(PASSWORD, gui));

        passwordField.submit();

        waitForPresence(driver, By.className("dbkpBoard"));

        final List<Integer> missingValues = new ArrayList<Integer>();
        for (final WebElement possibleKeyInput : driver.findElements(By.tagName("input"))) {
            final String missingValuePrefix = "view:key:border:border_body:key:dbkpDisplayDiv:values:";
            final String name = possibleKeyInput.getAttribute("name");
            if (startsWith(name, missingValuePrefix)) {
                final String s = name.substring(missingValuePrefix.length(), missingValuePrefix.length() + 1);
                missingValues.add(Integer.parseInt(s));
            }
        }

        final String code = settings.get(CODE, gui);

        for (final Integer missing : missingValues) {
            final String number = Character.toString(code.charAt(missing));
            clickButton(driver, number);
        }

        clickButton(driver, "Anmelden");

        waitForPresence(driver, By.partialLinkText("Post-Box"));
        clickLink(driver, "Post-Box");

        (new WebDriverWait(driver, WAIT_TIME)).until(ExpectedConditions.invisibilityOfElementLocated(By.id("busy")));

        int newDocs = 0;
        int knownDocs = 0;
        try {
            final FileDownloader d = new FileDownloader(driver);
            for (final WebElement row : driver.findElements(By.tagName("tbody"))) {
                final DocumentInfo metadata = DocumentInfo.create(this.getId(), DocumentFormat.PDF);

                for (final WebElement cell : row.findElements(By.tagName("td"))) {
                    final String text = cell.getText();
                    if (this.isDate(text)) {
                        metadata.setDate(parseDate(text));
                    } else {
                        metadata.addKeywords(text);
                    }
                }

                if (!store.containsDocument(metadata)) {
                    final WebElement link = row.findElement(By.tagName("a"));
                    final byte[] file = d.downloadFile(link);
                    store.storeDocument(metadata, file);
                    newDocs++;
                } else {
                    knownDocs++;
                }
            }

        } finally {
            clickLink(driver, "Log-out");
        }
        return Pair.of(newDocs, knownDocs);
    }

    private boolean isDate(String text) {
        return text.matches("[0-3][0-9]\\.[0-1][0-9]\\.[0-9][0-9][0-9][0-9]");
    }

}
