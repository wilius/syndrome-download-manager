package xdman.ui.components;

import xdman.videoparser.youtubedl.YoutubeDlVideo;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class VideoTableModel extends AbstractTableModel {

    /**
     *
     */
    private static final long serialVersionUID = -1338853497580208127L;
    ArrayList<VideoItemWrapper> list;

    public VideoTableModel() {
        list = new ArrayList<>();
    }

    public void setList(ArrayList<VideoItemWrapper> list) {
        this.list.clear();
        this.list.addAll(list);
        this.fireTableDataChanged();
    }

    public ArrayList<YoutubeDlVideo> getSelectedVideoList() {
        ArrayList<YoutubeDlVideo> selectedList = new ArrayList<>();
        for (int i = 0; i < this.list.size(); i++) {
            VideoItemWrapper w = list.get(i);
            if (w.checked) {
                selectedList.add(w.videoItem);
            }
        }
        return selectedList;
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public Object getValueAt(int r, int c) {
        return list.get(r);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return VideoItemWrapper.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        list.set(rowIndex, (VideoItemWrapper) aValue);
    }

    public void clear() {
        list.clear();
        fireTableDataChanged();
    }

}

class VideoItemWrapper {
    boolean checked;
    YoutubeDlVideo videoItem;
}
