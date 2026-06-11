package com.camera.gps.model.Ads;
import java.util.Map;

public class AdIds {
    private String publisher;
    private Map<String, String> ids;

    public AdIds(String publisher, Map<String, String> ids) {
        this.publisher = publisher;
        this.ids = ids;
    }

    public String getPublisher() {
        return publisher;
    }

    public Map<String, String> getIds() {
        return ids;
    }
}

