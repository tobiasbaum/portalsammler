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

import java.util.Date;

public final class DocumentInfo implements Comparable<DocumentInfo> {

    private final DocumentFormat format;
    private final String sourceId;
    private String keywords = "";
    private long date = new Date().getTime();

    private DocumentInfo(final String sourceId, final DocumentFormat format) {
        this.sourceId = sourceId;
        this.format = format;
    }

    public static DocumentInfo create(final String sourceId, final DocumentFormat format) {
        return new DocumentInfo(sourceId, format);
    }

    public static DocumentInfo parse(final String stringForm) {
        final int firstComma = stringForm.indexOf(',');
        final int secondComma = stringForm.indexOf(',', firstComma + 1);
        final int thirdComma = stringForm.indexOf(',', secondComma + 1);
        final long date = Long.parseLong(stringForm.substring(0, firstComma));
        final DocumentFormat format = DocumentFormat.valueOf(stringForm.substring(firstComma + 1, secondComma));
        final String sourceId =
                stringForm.substring(secondComma + 1, thirdComma).replace("\\k", ",").replace("\\\\", "\\");
        final String keywords = stringForm.substring(thirdComma + 1);
        final DocumentInfo ret = new DocumentInfo(sourceId, format);
        ret.date = date;
        ret.keywords = keywords;
        return ret;
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public void setDate(final Date date) {
        this.date = date.getTime();
    }

    public Date getDate() {
        return new Date(this.date);
    }

    public void addKeywords(final String text) {
        this.keywords = (this.keywords + " " + text.replace('\n', ' ').replace('\r', ' ')).trim().replaceAll(" +", " ");
    }

    public String getKeywords() {
        return this.keywords;
    }

    public String asString() {
        return this.date + ","
                + this.format + ","
                + this.sourceId.replace("\\", "\\\\").replace(",", "\\k") + ","
                + this.keywords;
    }

    @Override
    public int compareTo(final DocumentInfo o) {
        int cmp = Long.compare(o.date, this.date);
        if (cmp != 0) {
            return cmp;
        }
        cmp = this.sourceId.compareTo(o.sourceId);
        if (cmp != 0) {
            return cmp;
        }
        return this.keywords.compareTo(o.keywords);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DocumentInfo)) {
            return false;
        }
        return this.compareTo((DocumentInfo) o) == 0;
    }

    @Override
    public int hashCode() {
        return (int) this.date;
    }

    public DocumentFormat getFormat() {
        return this.format;
    }

}
