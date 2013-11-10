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
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import de.tntinteractive.portalsammler.engine.ByPartialButtonText;
import de.tntinteractive.portalsammler.engine.DocumentInfo;
import de.tntinteractive.portalsammler.engine.FileDownloader;
import de.tntinteractive.portalsammler.engine.SecureStore;
import de.tntinteractive.portalsammler.engine.SettingKey;
import de.tntinteractive.portalsammler.engine.SourceSettings;

public class IngDibaSourceV1 extends DocumentSource {

    static final SettingKey USER = new SettingKey("Konto/Depotnummer");
    static final SettingKey PASSWORD = new SettingKey("Passwort");
    static final SettingKey CODE = new SettingKey("Secure Code");

    private static final int WAIT_TIME = 30;

    private final String id;

    public IngDibaSourceV1(String id) {
        this.id = id;
    }

    @Override
    public void poll(SourceSettings settings, SecureStore store) throws Exception {
        final WebDriver driver = new HtmlUnitDriver(true);

        driver.manage().timeouts().implicitlyWait(WAIT_TIME, TimeUnit.SECONDS);
        driver.get("https://banking.ing-diba.de/app/login");

        final WebElement userField = driver.findElement(By.name("view:kontonummer:border:border_body:kontonummer"));
        userField.sendKeys(settings.get(USER));

        final WebElement passwordField = driver.findElement(By.name("view:pin:border:border_body:pin"));
        passwordField.sendKeys(settings.get(PASSWORD));

        passwordField.submit();

        (new WebDriverWait(driver, WAIT_TIME)).until(ExpectedConditions.presenceOfElementLocated(By.className("dbkpBoard")));

        final List<Integer> missingValues = new ArrayList<Integer>();
        for (final WebElement possibleKeyInput : driver.findElements(By.tagName("input"))) {
            final String missingValuePrefix = "view:key:border:border_body:key:dbkpDisplayDiv:values:";
            final String name = possibleKeyInput.getAttribute("name");
            if (startsWith(name, missingValuePrefix)) {
                final String s = name.substring(missingValuePrefix.length(), missingValuePrefix.length() + 1);
                missingValues.add(Integer.parseInt(s));
            }
        }

        final String code = settings.get(CODE);

        for (final Integer missing : missingValues) {
            final String number = Character.toString(code.charAt(missing));
            final WebElement numberButton = driver.findElement(byPartialButtonText(number));
            numberButton.click();
        }

        final WebElement login = driver.findElement(byPartialButtonText("Anmelden"));
        login.click();

        (new WebDriverWait(driver, WAIT_TIME)).until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText("Post-Box")));
        clickLink(driver, "Post-Box");

        (new WebDriverWait(driver, WAIT_TIME)).until(ExpectedConditions.invisibilityOfElementLocated(By.id("busy")));

        System.out.println("=======================================================");
        System.out.println(driver.getPageSource());
        System.out.println("=======================================================");
        System.out.println(driver.getCurrentUrl());

        try {
            final FileDownloader d = new FileDownloader(driver);
            for (final WebElement row : driver.findElements(By.tagName("tbody"))) {
                final DocumentInfo metadata = DocumentInfo.create(this.id);

                for (final WebElement cell : row.findElements(By.tagName("td"))) {
                    final String text = cell.getText();
                    if (this.isDate(text)) {
                        metadata.setDate(this.parseDate(text));
                    } else {
                        metadata.addKeywords(text);
                    }
                }

                final WebElement link = row.findElement(By.tagName("a"));
                System.out.println(link.getAttribute("id") + ", " + link.getAttribute("href"));
                final byte[] file = d.downloadFile(link);
                store.storeDocument(metadata, file);
            }

        } finally {
            clickLink(driver, "Log-out");
        }
    }

    private Date parseDate(String text) throws ParseException {
        return new SimpleDateFormat("dd.MM.yyyy").parse(text);
    }

    private boolean isDate(String text) {
        return text.matches("[0-3][0-9]\\.[0-1][0-9]\\.[0-9][0-9][0-9][0-9]");
    }

    private static boolean startsWith(String name, String prefix) {
        return name != null && name.startsWith(prefix);
    }

    private static void clickLink(final WebDriver driver, String partialText) {
        final WebElement link = driver.findElement(By.partialLinkText(partialText));
        link.click();
    }

    private static By byPartialButtonText(String text) {
        return new ByPartialButtonText(text);
    }

    public String getId() {
        return this.id;
    }

}
