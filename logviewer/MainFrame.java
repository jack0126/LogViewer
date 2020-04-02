/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logviewer;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import java.awt.HeadlessException;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Objects;

/**
 *
 * @author Administrator
 */
final class MainFrame extends JFrame implements ActionListener, DocumentListener { 
    
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    
    private JButton btnPickFile;
    private JComboBox<String>cbCharset;
    private JCheckBox mTrim;
    private JTextField tfKeyword;
    private JCheckBox mIgnoreCase;
    private IListView mView;
    private JLabel mAppStatus;
    private JLabel mContextStatus;
    
    private JFileChooser mFileChooser;
    
    private IListModel mModel;
    
    private int mMaxLineLength;

    public MainFrame() throws HeadlessException {
        super(Manifest.NAME);
        setIconImage(Manifest.ICON);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initComplements();
        setAppStatus("准备就储.");
    }
    
    private void initComplements() {
        Box box = Box.createHorizontalBox();
        btnPickFile = new JButton("Read file as");
        btnPickFile.addActionListener(this);
        box.add(btnPickFile);
        
        cbCharset = new JComboBox<>();
        cbCharset.addItem("GBK");
        cbCharset.addItem("ASCII");
        cbCharset.addItem("UTF-8");
        cbCharset.addItem("Unicode");
        cbCharset.addActionListener(this);
        box.add(cbCharset);
        
        mTrim = new JCheckBox("Trim", false);
        box.add(mTrim);
        
        JLabel sparator = new JLabel("| ");
        sparator.setForeground(Manifest.CONTENT_VIEW_BACKGROUND_COLOR);
        box.add(sparator);
        
        JLabel keywordLabel = new JLabel("Keyword:");
        box.add(keywordLabel);
        
        tfKeyword = new JTextField();
        tfKeyword.getDocument().addDocumentListener(this);
        box.add(tfKeyword);
        
        mIgnoreCase = new JCheckBox("Aa", true);
        mIgnoreCase.addActionListener(this);
        box.add(mIgnoreCase);
        getContentPane().add(box, BorderLayout.NORTH);
        
        mModel = new IListModel();
        mView = new IListView(mModel);
        getContentPane().add(new JScrollPane(mView), BorderLayout.CENTER);
        
        Box statusBox = Box.createHorizontalBox();
        mAppStatus = new JLabel();
        mAppStatus.setFont(Manifest.COMMON_FONT);
        statusBox.add(mAppStatus);
        
        statusBox.add(Box.createHorizontalGlue());
        
        mContextStatus = new JLabel();
        mContextStatus.setFont(Manifest.COMMON_FONT);
        statusBox.add(mContextStatus);
        getContentPane().add(statusBox, BorderLayout.SOUTH);
        
        mFileChooser = new JFileChooser();
        SupportedFileType filter = new SupportedFileType();
        mFileChooser.addChoosableFileFilter(filter);
        mFileChooser.setFileFilter(filter);
    }

    public void setAppStatus(String text) {
        mAppStatus.setText(text);
    }
    
    public void setContextStatus(String text) {
        mContextStatus.setText(text);
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        documentFiltration();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        documentFiltration();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        documentFiltration();
    }
    
    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == btnPickFile) {
            onPickFileClick();
        } else if (source == mIgnoreCase) {
            onIgnoreCaseClick();
        } else if (source == cbCharset) {
            File file = mFileChooser.getSelectedFile();
            if (file != null && file.isFile()) {
                readDataFromFile(file, getCharset());
            }
        }
    }
    
    private String getKeyword() {
        return Objects.toString(tfKeyword.getText(), "");
    }
    
    private synchronized void documentFiltration() {
        String keyword = getKeyword();
        mModel.filtration(keyword, !mIgnoreCase.isSelected());
        if (keyword.length() != 0) {
            setContextStatus("找到 " + mModel.getSize() + "个匹配行！");
        } else {
            setContextStatus("");
        }
    }
    
    private Charset getCharset() {
        try {
            String charsetName = Objects.toString(cbCharset.getSelectedItem(), "GBK");
            return Charset.forName(charsetName);
        } catch(UnsupportedCharsetException | IllegalCharsetNameException e) {
            e.printStackTrace();
        }
        return Charset.defaultCharset();
    }
    
    private void onPickFileClick() {
        File historyDir = History.instance.getPreviousReadDir();
        mFileChooser.setCurrentDirectory(historyDir);
        
        if (JFileChooser.APPROVE_OPTION != mFileChooser.showOpenDialog(this)) {
            return;
        }
        
        File file = mFileChooser.getSelectedFile();
        if (file == null || file.isDirectory()) {
            return;
        }

        setTitle(Manifest.NAME + " - " + file.getAbsolutePath());
        readDataFromFile(file, getCharset());
    }

    private void onIgnoreCaseClick() {
        if (getKeyword().length() != 0) {
            documentFiltration();
        }
    }

    private synchronized void readDataFromFile(File file, Charset charset) {
        if (!file.exists() || !file.isFile()) {
            return;
        }

        setAppStatus("正在加载...");
        mModel.beginLoad();// begin change of model

        mMaxLineLength = 0;
        boolean isTrim = mTrim.isSelected();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
            String line;
            while((line = reader.readLine()) != null) {
                if (isTrim && line.length() == 0) {
                    continue;
                }
                mMaxLineLength = Math.max(mMaxLineLength, line.length());
                mModel.addElement(new IListModel.IItem(line));
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch(IOException ignore) {
                }
            }
        }

        mModel.endLoad();// end load of model
        History.instance.setPreviousReadDir(file.getParentFile());
        History.instance.setPreviousReadFileName(file.getName());
        setAppStatus("已加载 " + mModel.getRawSize() + "行，最大行 " + mMaxLineLength + "个字符.");
        if (getKeyword().length() != 0) {
            documentFiltration();
        }
    }

}
