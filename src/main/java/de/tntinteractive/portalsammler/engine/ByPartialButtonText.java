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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class ByPartialButtonText extends By {

    private final String text;

    public ByPartialButtonText(String text) {
        this.text = text;
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        final List<WebElement> ret = new ArrayList<WebElement>();
        for (final WebElement button : context.findElements(By.tagName("button"))) {
            if (button.getText().contains(this.text)) {
                ret.add(button);
            }
        }
        return ret;
    }

}
