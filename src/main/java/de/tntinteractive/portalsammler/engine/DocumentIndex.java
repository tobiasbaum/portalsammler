package de.tntinteractive.portalsammler.engine;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DocumentIndex {

    private final TreeMap<DocumentInfo, Map<String, String>> documents = new TreeMap<DocumentInfo, Map<String, String>>();

    public Set<DocumentInfo> getAllDocuments() {
        return this.documents.keySet();
    }

    public Map<String, String> getFilePosition(DocumentInfo id) {
        return this.documents.get(id);
    }

    public void putDocument(DocumentInfo id, Map<String, String> filePosition) {
        this.documents.put(id, filePosition);
    }

}
