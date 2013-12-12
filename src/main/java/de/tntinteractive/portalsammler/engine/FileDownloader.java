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

public final class FileDownloader {

    private final WebDriver driver;
    private int httpStatusOfLastDownloadAttempt;

    public FileDownloader(final WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Lädt die Datei aus dem href-Attribut runter und gibt den Inhalt als Byte-Array zurück.
     * Ist also nur für Dateien bis zu einer gewissen Größe sinnvoll.
     */
    public byte[] downloadFile(final WebElement element) throws IOException, URISyntaxException {
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
    private BasicCookieStore mimicCookieState(final Set<Cookie> seleniumCookieSet) {
        final BasicCookieStore mimicWebDriverCookieStore = new BasicCookieStore();
        for (final Cookie seleniumCookie : seleniumCookieSet) {
            final BasicClientCookie duplicateCookie =
                    new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
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
    private byte[] downloader(final WebElement element, final String attribute) throws IOException, URISyntaxException {
        final String fileToDownloadLocation = element.getAttribute(attribute);
        if (fileToDownloadLocation.trim().equals("")) {
            throw new NullPointerException("The element you have specified does not link to anything!");
        }

        final URL fileToDownload = new URL(fileToDownloadLocation);

        final HttpClient client = HttpClientBuilder.create().build();
        final BasicHttpContext localContext = new BasicHttpContext();

        localContext.setAttribute(HttpClientContext.COOKIE_STORE,
                this.mimicCookieState(this.driver.manage().getCookies()));

        final HttpGet httpget = new HttpGet(fileToDownload.toURI());
        httpget.setConfig(RequestConfig.custom().setRedirectsEnabled(true).build());

        final HttpResponse response = client.execute(httpget, localContext);
        this.httpStatusOfLastDownloadAttempt = response.getStatusLine().getStatusCode();
        try {
            return IOUtils.toByteArray(response.getEntity().getContent());
        } finally {
            response.getEntity().getContent().close();
        }

    }

}
