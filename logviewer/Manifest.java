/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logviewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;

/**
 *
 * @author Administrator
 */
public final class Manifest {
    public static final String NAME = "LogViewer";
    public static final String VERSION_NAME = "1.0";
    public static final int VERSION_CODE = 1;
    public static final String AUTHOR = "Jack";
    public static final Image ICON = Toolkit.getDefaultToolkit().getImage(Manifest.class.getResource("icon.png"));
    
    public static final Color CONTENT_VIEW_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    
    public static final Font COMMON_FONT = new Font("宋体", Font.PLAIN, 12);
    public static final Font COMMON_BOLD_FONT = new Font("宋体", Font.BOLD, 14);
    
}
