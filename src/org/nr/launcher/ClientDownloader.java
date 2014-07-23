package org.nr.launcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

final class ClientDownloader implements Runnable {

    private final Launcher launcher;
    private final String checkSum;
    private final File clientFile;

    protected ClientDownloader(File clientFile, Launcher launcher, String checkSum) {
        this.clientFile = clientFile;
        this.launcher = launcher;
        this.checkSum = checkSum;
    }

    public final void run() {
        int attempt = 0;
        while (attempt++ < 10) {
            if (attempt > 1)
                this.launcher.setProgressBarText(0, "Attempting to establish connection (" + attempt + "/10)");
            else
                this.launcher.setProgressBarText(0, "Attempting to establish connection");
            HttpURLConnection connection = null;
            try {
                (connection = (HttpURLConnection) new URL("http://near-reality.org/digest.php" + (this.checkSum != null ? "?md5sum=" + this.checkSum : "")).openConnection()).setUseCaches(false);
                connection.setDefaultUseCaches(false);
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36");
                connection.setRequestProperty("Cache-Control", "no-store,max-age=0,no-cache");
                connection.setRequestProperty("Expires", "0");
                connection.setRequestProperty("Pragma", "no-cache");
                connection.connect();
                this.launcher.setProgressBarText(0, "Web connection established!");
                int responseCode;
                if ((responseCode = connection.getResponseCode() / 100) == 4) {
                    this.launcher.setProgressBarText(0, "Unable to locate the remote archive.");
                } else {
                    if (responseCode == 3) {
                        this.launcher.setProgressBarText(100, "Cached gamepack is OK.");
                        this.launcher.initializeClient(true);
                        return;
                    }
                    if (responseCode == 2) {
                        this.launcher.setProgressBarText(0, "An update is available!");
                        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                        int length = connection.getContentLength();
                        byte[] buffer = new byte[65535];
                        int totalRead = 0;
                        try (
                                FileOutputStream fos = new FileOutputStream(this.clientFile);
                                InputStream inputStream = connection.getInputStream()
                        ) {
                            int read;
                            while ((read = inputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, read);
                                messageDigest.update(buffer, 0, read);
                                totalRead += read;
                                this.launcher.setProgressBarText(totalRead * 100 / length, "Downloading: " + totalRead / 1024 + " kB/" + length / 1024 + " kB");
                            }
                        }
                        if (String.format("%1$032x", new BigInteger(1, messageDigest.digest())).equals(connection.getHeaderField("md5sum"))) {
                            this.launcher.setProgressBarText(100, "Successfully downloaded gamepack!");
                            this.launcher.initializeClient(true);
                            return;
                        }
                        this.launcher.setProgressBarText(0, "Checksum mismatched. Retrying...");
                        this.launcher.initializeClient(false);
                    } else {
                        this.launcher.setProgressBarText(0, "Unrecognized HTTP response code." + null);
                    }
                }
            } catch (Exception e) {
                this.launcher.setProgressBarText(0, "Failed - retrying!");
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignored) {
                }
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
        }
    }

}