/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logviewer;

import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager;
import java.awt.EventQueue;

/**
 *
 * @author Administrator
 */
public class LogViewer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        
        EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
        com.jpos.scanner.ScannerSvc110
    }
    
}
