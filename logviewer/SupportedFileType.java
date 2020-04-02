/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logviewer;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Administrator
 */
class SupportedFileType extends FileFilter {
    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        String name = file.getName();
        return name.endsWith(".txt") || name.endsWith(".log");
    }

    @Override
    public String getDescription() {
        return "支持的文件(*.txt,*.log)";
    }
}
