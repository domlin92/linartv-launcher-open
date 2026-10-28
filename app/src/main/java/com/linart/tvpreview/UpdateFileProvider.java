package com.linart.tvpreview;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Hands one downloaded update (cache/updates/*.apk) to Android's installer. Not exported;
 * the installer gets a one-time read permission on the single file.
 */
public final class UpdateFileProvider extends ContentProvider {
    static Uri uriFor(android.content.Context context, File apk) {
        return Uri.parse("content://" + context.getPackageName() + ".updates/" + Uri.encode(apk.getName()));
    }

    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        String name = uri.getLastPathSegment();
        if (name == null || name.contains("/") || !name.endsWith(".apk")) throw new FileNotFoundException();
        File apk = new File(new File(getContext().getCacheDir(), "updates"), name);
        if (!apk.isFile()) throw new FileNotFoundException();
        return ParcelFileDescriptor.open(apk, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public String getType(Uri uri) { return "application/vnd.android.package-archive"; }
    @Override public boolean onCreate() { return true; }
    @Override public Cursor query(Uri u, String[] p, String s, String[] a, String o) { return null; }
    @Override public Uri insert(Uri u, ContentValues v) { return null; }
    @Override public int delete(Uri u, String s, String[] a) { return 0; }
    @Override public int update(Uri u, ContentValues v, String s, String[] a) { return 0; }
}
