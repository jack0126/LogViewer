/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logviewer;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author Administrator
 */
class KeywordFiltrationTask extends RecursiveTask<List<IListModel.IItem>> {
    
    private static final int TASK_DEPTH = 8192;
    
    private final List<IListModel.IItem>data;
    private final int offset;
    private final int count;
    private final int end;
    private final String keyword;
    private final boolean ignoreCase;
    private final List<IListModel.IItem>result;
    
    public KeywordFiltrationTask(List<IListModel.IItem>data, int offset, int count, int end, String keyword, boolean ignoreCase) {
        this.data = Objects.requireNonNull(data);
        this.offset = offset;
        this.count = count;
        this.end = end;
        this.keyword = keyword != null ? ignoreCase ? keyword.toLowerCase() : keyword : "";
        this.ignoreCase = ignoreCase;
        this.result = new ArrayList(count);
    }
    
    @Override
    protected List<IListModel.IItem> compute() {
        if (count <= TASK_DEPTH) {
            for (int i = 0; i < count; i++) {
                int index = offset + i;
                if (index >= end) {
                    break;
                }
                
                IListModel.IItem item = data.get(index);
                if (keyword.length() == 0) {
                    result.add(item);
                } else {
                    String line = item.getLine();
                    if (ignoreCase) {
                        line = line.toLowerCase();
                    }
                    if (line.contains(keyword)) {
                        result.add(item);
                    }
                }
            }// end for
            return result;
        } else {
            ArrayList<ForkJoinTask<List<IListModel.IItem>>>taskList = new ArrayList(32);
            for (int i = 0; i < count; i += TASK_DEPTH) {
                KeywordFiltrationTask task = new KeywordFiltrationTask(data, offset + i, TASK_DEPTH, end, keyword, ignoreCase);
                taskList.add(task.fork());
            }
            for (ForkJoinTask<List<IListModel.IItem>>task : taskList) {
                result.addAll(task.join());
            }
            /*
            taskList.stream().map((task) -> task.join()).forEachOrdered((list) -> {
                result.addAll(list);
            });
            */
            return result;
        }
    }// end method of compute
}
