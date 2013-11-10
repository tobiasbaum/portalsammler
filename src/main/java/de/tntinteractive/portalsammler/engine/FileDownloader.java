package de.tntinteractive.portalsammler.engine;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class FileDownloader {

    private final WebDriver driver;
    private int httpStatusOfLastDownloadAttempt = 0;

    public FileDownloader(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Lädt die Datei aus dem href-Attribut runter und gibt den Inhalt als Byte-Array zurück.
     * Ist also nur für Dateien bis zu einer gewissen Größe sinnvoll.
     */
    public byte[] downloadFile(WebElement element) throws IOException, URISyntaxException {
        return this.downloader(element, "href");
    }

    /**
     * Gets the HTTP status code of the last download file attempt
     */
    public int getHTTPStatusOfLastDownloadAttempt() {
        return this.httpStatusOfLastDownloadAttempt;
    }

    /**
     * Load in all the cookies WebDriver currently knows about so that we can mimic the browser cookie state
     */
    private BasicCookieStore mimicCookieState(Set<Cookie> seleniumCookieSet) {
        final BasicCookieStore mimicWebDriverCookieStore = new BasicCookieStore();
        for (final Cookie seleniumCookie : seleniumCookieSet) {
            final BasicClientCookie duplicateCookie = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
            duplicateCookie.setDomain(seleniumCookie.getDomain());
            duplicateCookie.setSecure(seleniumCookie.isSecure());
            duplicateCookie.setExpiryDate(seleniumCookie.getExpiry());
            duplicateCookie.setPath(seleniumCookie.getPath());
            mimicWebDriverCookieStore.addCookie(duplicateCookie);
        }

        return mimicWebDriverCookieStore;
    }

    /**
     * Lädt die Datei runter, die im übergebenen Attribut steht und liefert ihren Inhalt.
     */
    private byte[] downloader(WebElement element, String attribute) throws IOException, URISyntaxException {
        final String fileToDownloadLocation = element.getAttribute(attribute);
        if (fileToDownloadLocation.trim().equals("")) {
            throw new NullPointerException("The element you have specified does not link to anything!");
        }

        final URL fileToDownload = new URL(fileToDownloadLocation);

        final HttpClient client = HttpClientBuilder.create().build();
        final BasicHttpContext localContext = new BasicHttpContext();

        localContext.setAttribute(HttpClientContext.COOKIE_STORE, this.mimicCookieState(this.driver.manage().getCookies()));

        final HttpGet httpget = new HttpGet(fileToDownload.toURI());
        httpget.setConfig(RequestConfig.custom().setRedirectsEnabled(true).build());

        final HttpResponse response = client.execute(httpget, localContext);
        this.httpStatusOfLastDownloadAttempt = response.getStatusLine().getStatusCode();
        //TEST
        System.out.println("status code = " + this.httpStatusOfLastDownloadAttempt);
        try {
            return IOUtils.toByteArray(response.getEntity().getContent());
        } finally {
            response.getEntity().getContent().close();
        }

    }

}