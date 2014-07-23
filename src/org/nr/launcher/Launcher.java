package org.nr.launcher;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Method;

public final class Launcher extends JFrame implements Runnable {

    private boolean active;
    private Screen screen = new Screen(this);
    private ClientDownloader clientDownloader;
    private Thread thread;
    private int progress = 0;
    private String text = "";

    public static void main(String[] args) {
        new Thread(new Launcher()).start();
    }

    private Launcher() {
        super("Near-Reality Launcher");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        add(screen);
        pack();
        setVisible(true);
    }

    protected final void initializeClient(boolean succeeded) {
        if (succeeded) {
            String dataDirPath = FileManager.getDataDirPath();
            FileUtils.loadArchive(new File(dataDirPath + "client.jar"));
            try {
                Method main = Class.forName("org.nr.client.Client").getDeclaredMethod("main", new Class[]{String[].class});
                main.setAccessible(true);
                main.invoke(null, new Object[]{new String[0]});
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Reflection attempt failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            setVisible(false);
            this.thread.interrupt();
            this.active = false;
            this.screen = null;
            this.clientDownloader = null;
            this.thread = null;
            System.gc();
            Thread.currentThread().interrupt();
            return;
        }
        this.active = false;
    }

    public final void run() {
        String dataDirPath = FileManager.getDataDirPath();
        File clientFile = new File(dataDirPath + "client.jar");
        if (clientFile.exists()) {
            this.clientDownloader = new ClientDownloader(clientFile, this, FileUtils.getCheckSum(clientFile));
            setProgressBarText(0, "Preparing to verify gamepack version.");
        } else {
            this.clientDownloader = new ClientDownloader(clientFile, this, null);
            setProgressBarText(0, "Preparing to download gamepack.");
        }
        if (this.clientDownloader != null) {
            this.thread = new Thread(this.clientDownloader);
            this.thread.start();
            this.active = true;
        }
        while (this.active) {
            this.screen.draw();
        }
    }

    protected final synchronized int getProgress() {
        return this.progress;
    }

    protected final synchronized String getText() {
        return this.text;
    }

    protected final synchronized void setProgressBarText(int progress, String text) {
        this.progress = progress;
        this.text = text;
    }
}