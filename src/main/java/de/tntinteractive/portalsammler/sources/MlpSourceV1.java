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

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import de.tntinteractive.portalsammler.engine.DocumentFormat;
import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.FileDownloader;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.engine.SourceSettings;
import de.tntinteractive.portalsammler.gui.UserInteraction;

public class MlpSourceV1 extends DocumentSource {

    static final SettingKey USER = new SettingKey("Benutzerkennung");
    static final SettingKey PASSWORD = new SettingKey("Online-PIN");

    public MlpSourceV1(String id) {
        super(id);
    }

    @Override
    public Pair<Integer, Integer> poll(SourceSettings settings, UserInteraction gui, SecureStore store) throws Exception {
        final WebDriver driver = this.createDriver("https://financepilot-pe.mlp.de/p04pepe/entry?rzid=XC&rzbk=0752");

        final WebElement userField = driver.findElement(By.id("txtBenutzerkennung"));
        userField.sendKeys(settings.get(USER, gui));

        final WebElement passwordField = driver.findElement(By.className("XPassword"));
        passwordField.sendKeys(settings.get(PASSWORD, gui));

        passwordField.submit();

        int newDocs = 0;
        int knownDocs = 0;
        try {
            clickLink(driver, "Postfach");

            clickButton(driver, "Dokumente anzeigen");

            final WebElement table = driver.findElement(By.id("tblKontoauszuege"));
            final WebElement tBody = table.findElement(By.xpath("tbody"));
            final FileDownloader d = new FileDownloader(driver);

            for (final WebElement tr : tBody.findElements(By.tagName("tr"))) {
                final List<WebElement> tds = tr.findElements(By.tagName("td"));
                final DocumentInfo doc = DocumentInfo.create(this.getId(), DocumentFormat.PDF);
                if (tds.get(2).getText().isEmpty()) {
                    continue;
                }
                doc.addKeywords(tds.get(0).getText());
                doc.addKeywords(tds.get(1).getText());
                doc.setDate(parseDate(tds.get(2).getText()));

                if (!store.containsDocument(doc)) {
                    final WebElement link = tds.get(0).findElement(By.tagName("a"));
                    final byte[] file = d.downloadFile(link);
                    store.storeDocument(doc, file);
                    newDocs++;
                } else {
                    knownDocs++;
                }
            }
        } finally {
            clickButton(driver, "Abmelden");
        }
        return Pair.of(newDocs, knownDocs);
    }

}
