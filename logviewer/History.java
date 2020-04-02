/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Properties;
/**
 *
 * @author Administrator
 */
public enum History {

    instance;

    private static final String HISTORY_FILE_NAME = "history.properties";
    
    private static final String PREVIOUS_READ_DIR = "previous.read.dir";

    private static final String PREVIOUS_READ_FILE_NAME = "previous.read.filename";

    private final Properties mHistoryCache;
    
    History() {
        mHistoryCache = new Properties();
        loadLocalHistory();
    }
    
    private String get(String key) {
        return mHistoryCache.getProperty(key);
    }
    
    private void set(String key, String value) {
        String oldValue = mHistoryCache.getProperty(key);
        if (!value.equals(oldValue)) {
            mHistoryCache.setProperty(key, value);
            updateLocalHistory();
        }
    }
    
    private File getLocalHistoryFile() {
        File userDataDir = new File(System.getProperty("user.home"), "." + Manifest.NAME);
        if (!userDataDir.exists()) {
            userDataDir.mkdir();
        }
        if (!userDataDir.isDirectory()) {
            userDataDir.delete();
            userDataDir.mkdir();
        }
        
        File historyFile = new File(userDataDir, HISTORY_FILE_NAME);
        if (historyFile.exists()) {
            if (historyFile.isFile()) {
                return historyFile;
            } else {
                historyFile.delete();
            }
        }
        
        try {
            historyFile.createNewFile();
        } catch(IOException ignore) {
        }
        return historyFile;
    }
    
    private void loadLocalHistory() {
        FileInputStream input = null;
        try {
            input = new FileInputStream(getLocalHistoryFile());
            mHistoryCache.load(input);
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch(IOException ignore) {
                }
            }
        }
    }
    
    private synchronized void updateLocalHistory() {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(getLocalHistoryFile().getAbsoluteFile());
            mHistoryCache.store(writer, "");
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    public File getPreviousReadDir() {
        String path = get(PREVIOUS_READ_DIR);
        if (path != null) {
            return new File(path);
        }
        return new File(".");
    }
    
    public void setPreviousReadDir(File dir) {
        if (dir.isFile()) {
            dir = dir.getParentFile();
        }
        set(PREVIOUS_READ_DIR, dir.getAbsolutePath().replace("\\", "/"));
    }

    public String getPreviousReadFileName() {
        return get(PREVIOUS_READ_FILE_NAME);
    }

    public void setPreviousReadFileName(String filename) {
        if (filename != null && !filename.isEmpty()) {
            set(PREVIOUS_READ_FILE_NAME, filename);
        }
    }
}
