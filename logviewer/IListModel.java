/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logviewer;

import javax.swing.ListModel;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import javax.swing.event.ListDataListener;

import javax.swing.event.ListDataEvent;

/**
 *
 * @author Administrator
 */
public class IListModel implements ListModel {
    
    private final static ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool(Math.max(8, Runtime.getRuntime().availableProcessors() * 2));
    
    private static final ArrayList<IItem>EMPTY_LIST = new ArrayList();
    
    private final List<ListDataListener>mDataListenerList = new ArrayList(8);
    
    private final LinkedList<IItem>mTempList = new LinkedList();

    private ArrayList<IItem>mRawList = EMPTY_LIST;
    private List<IItem>mVisibleList = EMPTY_LIST;
    
    public synchronized void filtration(String keyword, boolean ignoreCase) {
        int count = mRawList.size();
        if (count > 0) {
            KeywordFiltrationTask task = new KeywordFiltrationTask(mRawList, 0, count, count, keyword, ignoreCase);
            mVisibleList = FORK_JOIN_POOL.submit(task).join();
            notifyDataChanged();
        }
    }

    void beginLoad() {
        mTempList.clear();
        mRawList = EMPTY_LIST;
        mVisibleList = EMPTY_LIST;
        notifyDataChanged();
    }

    void addElement(IItem item) {
        Objects.requireNonNull(item);
        mTempList.add(item);
        item.setNumber(mTempList.size());
    }

    void endLoad() {
        mRawList = new ArrayList(mTempList);
        mTempList.clear();
        mVisibleList = new ArrayList(mRawList);
        IItem.setWidthOfLineNumber(Integer.toString(mRawList.size()).length());
        notifyDataChanged();
    }

    public int getIndexByLineNumber(int lineNumber) {
        int rawIndex = lineNumber - 1;
        if (mVisibleList.size() == mRawList.size()) {
            return rawIndex;
        } else {
            return mVisibleList.indexOf(mRawList.get(rawIndex));
        }
    }
    
    public int getRawSize() {
        return mRawList.size();
    }
    
    @Override
    public int getSize() {
        return mVisibleList.size();
    }

    @Override
    public Object getElementAt(int index) {
        return mVisibleList.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        mDataListenerList.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        mDataListenerList.remove(l);
    }
    
    private void notifyDataChanged() {
        if (mDataListenerList.size() > 0) {
            ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
            mDataListenerList.forEach((l) -> l.contentsChanged(event));
        }
    }
    
    public static class IItem {

        private static volatile int sWidthOfLineNumber;

        static void setWidthOfLineNumber(int widthOfLineNumber) {
            sWidthOfLineNumber = widthOfLineNumber;
        }
        
        private int number;
        private String line;

        public IItem(String line) {
            this.line = line;
        }

        private void setNumber(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }

        public String getLine() {
            return line;
        }

        private final static ThreadLocal<StringBuilder> sStringBuilderCache = new ThreadLocal<StringBuilder>();
        
        @Override
        public String toString() {
            StringBuilder sb = sStringBuilderCache.get();
            if (sb == null) {
                sb = new StringBuilder(4096);
                sStringBuilderCache.set(sb);
            }
            
            sb.setLength(0);
            String sNumber = Integer.toString(number);
            for (int i = sNumber.length(); i < sWidthOfLineNumber; i++) {
                sb.append(' ');
            }
            sb.append(sNumber);
            sb.append(": ");
            sb.append(line);
            return sb.toString();
        }
        
    }
}
