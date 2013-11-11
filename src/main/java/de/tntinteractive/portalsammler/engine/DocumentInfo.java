package de.tntinteractive.portalsammler.engine;

import java.util.Date;

public class DocumentInfo implements Comparable<DocumentInfo> {

    private final DocumentFormat format;
    private final String sourceId;
    private String keywords = "";
    private long date = new Date().getTime();

    private DocumentInfo(String sourceId, DocumentFormat format) {
        this.sourceId = sourceId;
        this.format = format;
    }

    public static DocumentInfo create(String sourceId, DocumentFormat format) {
        return new DocumentInfo(sourceId, format);
    }

    public static DocumentInfo parse(String stringForm) {
        final int firstComma = stringForm.indexOf(',');
        final int secondComma = stringForm.indexOf(',', firstComma + 1);
        final int thirdComma = stringForm.indexOf(',', secondComma + 1);
        final long date = Long.parseLong(stringForm.substring(0, firstComma));
        final DocumentFormat format = DocumentFormat.valueOf(stringForm.substring(firstComma + 1, secondComma));
        final String sourceId = stringForm.substring(secondComma + 1, thirdComma).replace("\\k", ",").replace("\\\\", "\\");
        final String keywords = stringForm.substring(thirdComma + 1);
        final DocumentInfo ret = new DocumentInfo(sourceId, format);
        ret.date = date;
        ret.keywords = keywords;
        return ret;
    }

    public String getSourceId() {
        return this.sourceId;
    }

    public void setDate(Date date) {
        this.date = date.getTime();
    }

    public Date getDate() {
        return new Date(this.date);
    }

    public void addKeywords(String text) {
        this.keywords = (this.keywords + " " + text.replace('\n', ' ').replace('\r', ' ')).trim().replaceAll(" +", " ");
    }

    public String getKeywords() {
        return this.keywords;
    }

    public String asString() {
        return this.date + "," + this.format + "," + this.sourceId.replace("\\", "\\\\").replace(",", "\\k") + "," + this.keywords;
    }

    @Override
    public int compareTo(DocumentInfo o) {
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
    public boolean equals(Object o) {
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
