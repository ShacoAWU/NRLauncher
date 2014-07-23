package org.nr.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class FileUtils {

    protected static String getCheckSum(File file) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new InternalError("MD5 not supported!");
        }
        FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
        byte[] buffer = new byte[65535];
        try {
            int i;
            while ((i = fis.read(buffer)) > 0)
                messageDigest.update(buffer, 0, i);
        } catch (Exception e) {
            return null;
        } finally {
            try {
                fis.close();
            } catch (IOException ignored) {
            }
        }
        return String.format("%1$032x", new BigInteger(1, messageDigest.digest()));
    }

    protected static boolean loadArchive(File archive) {
        URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(classLoader, archive.toURI().toURL());
            return true;
        } catch (NoSuchMethodException | InvocationTargetException | MalformedURLException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

}
