/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logviewer;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.ListModel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Administrator
 */
public final class IListView extends JList implements ActionListener, ListDataListener {
    
    private JMenuItem miGoto;
    private JMenuItem miCopy;
    private JMenuItem miAbout;
    
    private IListModel mModel;
    
    public IListView(IListModel dataModel) {
        super(dataModel);
        mModel = dataModel;
        mModel.addListDataListener(this);
        
        setBackground(Manifest.CONTENT_VIEW_BACKGROUND_COLOR);
        setFont(Manifest.COMMON_FONT);
        initPopupMenu();
        
        registerKeyboardAction((e) -> onMenuItemCopy(), KeyStroke.getKeyStroke("ctrl R"), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction((e) -> onMenuItemGoto(), KeyStroke.getKeyStroke("ctrl G"), WHEN_IN_FOCUSED_WINDOW);
    }
    
    private void initPopupMenu() {
        miCopy = new JMenuItem("(R)复制行(Ctrl+R)");
        miCopy.setMnemonic('R');
        miCopy.setFont(Manifest.COMMON_FONT);
        miCopy.setEnabled(false);
        miCopy.addActionListener(this);
        
        miGoto = new JMenuItem("(G)跳转到行(Ctrl+G)");
        miGoto.setMnemonic('G');
        miGoto.setFont(Manifest.COMMON_FONT);
        miGoto.setEnabled(false);
        miGoto.addActionListener(this);
        
        
        miAbout = new JMenuItem("(A)关于");
        miAbout.setMnemonic('A');
        miAbout.setFont(Manifest.COMMON_FONT);
        miAbout.addActionListener(this);
        
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(miCopy);
        popupMenu.add(miGoto);
        popupMenu.add(new JSeparator());
        popupMenu.add(miAbout);
        
        setComponentPopupMenu(popupMenu);
    }

    @Override
    public void setModel(ListModel model) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void intervalAdded(ListDataEvent e) {
        contentsChanged(null);
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        contentsChanged(null);
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        boolean enable = mModel.getSize() != 0;
        miCopy.setEnabled(enable);
        miGoto.setEnabled(enable);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == miGoto) {
            onMenuItemGoto();
        } else if (source == miCopy) {
            onMenuItemCopy();
        } else if (source == miAbout) {
            onMenuItemAbout();
        }
    }

    private void onMenuItemGoto() {
        if (!miGoto.isEnabled()) {
            return;
        }
        
        Object inputValue = JOptionPane.showInputDialog(null,
                "请输入行号：", "跳转到指定行", 
                JOptionPane.QUESTION_MESSAGE, null, null, null);
        if (inputValue != null) {
            try {
                int lineNumber = Integer.parseInt(inputValue.toString());
                IListModel model = (IListModel) getModel();
                if (lineNumber > 0 && lineNumber <= model.getRawSize()) {
                    int index = model.getIndexByLineNumber(lineNumber);
                    if (index > -1) {
                        setSelectedIndex(index);
                        ensureIndexIsVisible(index);
                    }
                }
            } catch(NumberFormatException ignore) {
            }
        }
    }
    
    private void onMenuItemCopy() {
        if (!miCopy.isEnabled()) {
            return;
        }
        
        if (getSelectedIndex() < mModel.getSize()) {
            IListModel.IItem item = (IListModel.IItem) getSelectedValue();
            if (item != null) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(new StringSelection(item.getLine()), null);
            }
        }
    }
    
    private void onMenuItemAbout() {
        String info = "这是一个以行为单位的文本筛选浏览工具。\n";
        info += "        软件名称：    " + Manifest.NAME + "\n";
        info += "        软件版本：    " + Manifest.VERSION_NAME + "\n";
        info += "        软件作者：    " + Manifest.AUTHOR + "\n";
        JOptionPane.showOptionDialog(null,
                info, "关于 " + Manifest.NAME, 
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, null, null);
    }
    
}
