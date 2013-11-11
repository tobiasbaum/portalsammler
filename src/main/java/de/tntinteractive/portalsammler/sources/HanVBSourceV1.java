package de.tntinteractive.portalsammler.sources;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import de.tntinteractive.portalsammler.engine.DocumentFormat;
import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.engine.SourceSettings;

public class HanVBSourceV1 extends DocumentSource {

    static final SettingKey USER = new SettingKey("VR-Kennung");
    static final SettingKey PASSWORD = new SettingKey("Online-PIN");

    public HanVBSourceV1(String id) {
        super(id);
    }

    @Override
    public void poll(SourceSettings settings, SecureStore store) throws Exception {
        final WebDriver driver = this.createDriver("https://www.hanvb.de/ptlweb/WebPortal?bankid=0744");

        final WebElement userField = driver.findElement(By.id("vrkennungalias"));
        userField.sendKeys(settings.get(USER));

        final WebElement passwordField = driver.findElement(By.id("pin"));
        passwordField.sendKeys(settings.get(PASSWORD));

        passwordField.submit();

        waitForPresence(driver, By.linkText("Postkorb"));

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
                }
            }

        } finally {
            clickLink(driver, "Logout");
        }
    }

    private boolean isNotLabeled(WebElement div) {
        return !this.isLabeled(div, "");
    }

    private boolean isLabeled(WebElement div, String string) {
        for (final WebElement label : div.findElements(By.tagName("label"))) {
            if (label.getText().contains(string)) {
                return true;
            }
        }
        return false;
    }

    private Date parseTimestamp(String time) throws ParseException {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(time);
    }

}
