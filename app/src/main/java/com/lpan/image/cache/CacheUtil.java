/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lpan.image.cache;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Junk drawer of utility methods. */
final class CacheUtil {
  static final Charset US_ASCII = Charset.forName("US-ASCII");
  static final Charset UTF_8 = Charset.forName("UTF-8");

  private CacheUtil() {
  }

  static String readFully(Reader reader) throws IOException {
    try {
      StringWriter writer = new StringWriter();
      char[] buffer = new char[1024];
      int count;
      while ((count = reader.read(buffer)) != -1) {
        writer.write(buffer, 0, count);
      }
      return writer.toString();
    } finally {
      reader.close();
    }
  }

  /**
   * Deletes the contents of {@code dir}. Throws an IOException if any file
   * could not be deleted, or if {@code dir} is not a readable directory.
   */
  static void deleteContents(File dir) throws IOException {
    File[] files = dir.listFiles();
    if (files == null) {
      throw new IOException("not a readable directory: " + dir);
    }
    for (File file : files) {
      if (file.isDirectory()) {
        deleteContents(file);
      }
      if (!file.delete()) {
        throw new IOException("failed to delete file: " + file);
      }
    }
  }

  static void closeQuietly(/*Auto*/Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (RuntimeException rethrown) {
        throw rethrown;
      } catch (Exception ignored) {
      }
    }
  }

  static File getDiskCacheDir(Context context) {
    return context.getExternalCacheDir();
  }

  static long getStorageAvailableSize() {
    File storagefile = null;

    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
            || !isExternalStorageRemovable()) {
      storagefile = Environment.getExternalStorageDirectory();

      if (storagefile == null) {
        storagefile = Environment.getRootDirectory();
      }

    } else {
      storagefile = Environment.getRootDirectory();
    }

    StatFs sf = new StatFs(storagefile.getPath());
    long blockSize = sf.getBlockSize();
    long availCount = sf.getAvailableBlocks();
    return availCount * blockSize;

  }

  static boolean isExternalStorageRemovable() {
    return Environment.isExternalStorageRemovable();
  }

  public static String stringToMD5(String data) {
    if (TextUtils.isEmpty(data)) {
      return "";
    }
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(data.getBytes());
      byte b[] = md.digest();
      int i;
      StringBuffer buf = new StringBuffer("");
      for (int offset = 0; offset < b.length; offset++) {
        i = b[offset];
        if (i < 0) i += 256;
        if (i < 16) buf.append("0");
        buf.append(Integer.toHexString(i));
      }
      return buf.toString();

    } catch (NoSuchAlgorithmException e) {

      e.printStackTrace();
    }

    return null;

  }
}
