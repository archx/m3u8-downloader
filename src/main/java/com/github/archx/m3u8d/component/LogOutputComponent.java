package com.github.archx.m3u8d.component;

import com.github.archx.m3u8d.uitl.Utils;
import com.github.archx.m3u8d.view.handler.LogOutputHandler;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * 日志输出组件
 *
 * @author archx
 * @since 2020/9/4 23:06
 */
public class LogOutputComponent extends JPanel implements LogOutputHandler {

    private JTextArea textArea;

    public LogOutputComponent() {
        BorderLayout layout = new BorderLayout();
        setLayout(layout);
        setBorder(new EmptyBorder(10, 10, 10, 10));


        initUI();
    }

    @Override
    public void log(String message) {
        String format = String.format("[%s] %s\n", Utils.getDatetime(), message);
        textArea.append(format);
        // 滚动条自动滚动
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

    @Override
    public void clear() {
        textArea.setText("");
    }

    private void initUI() {

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));
        // 带标题的边框
        Border etched = BorderFactory.createEtchedBorder();
        Border border = BorderFactory.createTitledBorder(etched, "日志输出");
        panel.setBorder(border);

        textArea = new JTextArea();
        // 禁止编辑
        textArea.setEditable(false);
        // 显示10行
        textArea.setRows(10);

        panel.add(new JScrollPane(textArea));

        add(panel, BorderLayout.CENTER);
    }

}
