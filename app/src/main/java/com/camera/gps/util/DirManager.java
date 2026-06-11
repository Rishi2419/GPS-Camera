package com.camera.gps.util;

import java.io.File;
import java.io.IOException;

import kotlin.jvm.internal.DefaultConstructorMarker;
import com.camera.gps.MyApplication;
import com.camera.gps.R;

public final class DirManager {
    public static final Companion Companion = new Companion(null);

    public static final class Companion {
        public Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        private Companion() {
        }

        public File generateFile() {
            File file = new File(DirManager.Companion.getSentImagesFolder());
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            return file;
        }

        public void createNoMediaFile(File folderPath) {
            File file = new File(folderPath + "/.nomedia");
            try {
                if (file.exists()) {
                    return;
                }
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public String getSentImagesFolder() {
            File file = new File(MyApplication.Companion.context().getFilesDir(),
                    MyApplication.Companion.context().getString(R.string.app_name) + "/");
            if (!file.exists()) file.mkdirs();
            return file.getAbsolutePath();
        }


//        public String getSentImagesFolder() {
//            File file = new File(MyApplication.Companion.context().getCacheDir(), MyApplication.Companion.context().getString(R.string.app_name) + "/");
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//            DirManager.Companion.createNoMediaFile(file);
//            return file.getAbsolutePath();
//        }
    }
}