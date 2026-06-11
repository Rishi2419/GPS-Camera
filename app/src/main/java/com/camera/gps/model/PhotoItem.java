package com.camera.gps.model;

import com.camera.gps.database.entity.Photo;

// Photo item wrapper
public class PhotoItem implements CreationItem {
    private Photo photo;

    public PhotoItem(Photo photo) {
        this.photo = photo;
    }

    public Photo getPhoto() {
        return photo;
    }

    @Override
    public int getItemType() {
        return TYPE_PHOTO_ITEM;
    }
}