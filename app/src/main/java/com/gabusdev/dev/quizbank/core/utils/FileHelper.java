package com.gabusdev.dev.quizbank.core.utils;

import android.content.Context;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHelper {
    public static Uri saveAndGetUri(Context context, String content, String fileName) throws IOException {
        File file = saveToFile(context, content, fileName);
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
    }

    public static File saveToFile(Context context, String content, String fileName) throws IOException {
        File dir = new File(context.getCacheDir(), "exports");
        if (!dir.exists()) dir.mkdirs();

        File file = new File(dir, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes());
        fos.close();
        return file;
    }

    public static File getFileFromUri(Context context, Uri uri) {
        String fileName = uri.getLastPathSegment();
        return new File(new File(context.getCacheDir(), "exports"), fileName);
    }
}
