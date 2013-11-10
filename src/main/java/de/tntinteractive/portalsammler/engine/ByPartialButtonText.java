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
