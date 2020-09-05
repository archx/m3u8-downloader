package com.github.archx.m3u8d.component;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * 进度条组件
 *
 * @author archx
 * @since 2020/9/5 10:19
 */
public class BarComponent extends JProgressBar implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        setStringPainted(true);
        setValue(((int) value));
        return this;
    }
}
