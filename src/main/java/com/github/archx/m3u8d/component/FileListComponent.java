package com.github.archx.m3u8d.component;

import com.github.archx.m3u8d.download.DownloadTask;
import com.github.archx.m3u8d.download.listener.DownloadListener;
import com.github.archx.m3u8d.entity.TaskMetaEntity;
import com.github.archx.m3u8d.entity.TsFileEntity;
import com.github.archx.m3u8d.view.handler.LogOutputHandler;
import com.github.archx.m3u8d.view.handler.TaskHandler;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

/**
 * 文件列表组件
 *
 * @author archx
 * @since 2020/9/4 22:55
 */
@Slf4j
public class FileListComponent extends JPanel implements TaskHandler, DownloadListener {

    private final Component parentComponent;

    private JPanel wrapper;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton start;
    private JButton clear;

    private ExecutorService executorService;
    private LogOutputHandler logOutputHandler;

    private TaskMetaEntity taskMeta;
    private DownloadTask downloadTask;

    private int isPause = 1;

    public FileListComponent(Component parentComponent) {
        this.parentComponent = parentComponent;

        BorderLayout layout = new BorderLayout();
        setLayout(layout);
        // 设置内边距
        setBorder(new EmptyBorder(10, 10, 10, 10));

        wrapper = new JPanel();
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        add(wrapper, BorderLayout.CENTER);
        initUI();
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setLogOutputHandler(LogOutputHandler logOutputHandler) {
        this.logOutputHandler = logOutputHandler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(TaskMetaEntity task) {

        if (this.taskMeta != null) {
            JOptionPane.showMessageDialog(parentComponent, "当前下载任务未完成", "M3U8 Downloader", JOptionPane.WARNING_MESSAGE);
            return;
        }

        this.taskMeta = task;
        // 渲染数据
        Vector<Object> dataVector = tableModel.getDataVector();
        dataVector.clear();

        List<String> fragments = task.getFragments();
        for (String fragment : fragments) {
            Vector<Object> data = new Vector<>();
            data.add(fragment.substring(fragment.lastIndexOf("/") + 1));
            data.add(task.getUrl());
            data.add(0);
            dataVector.add(data);
        }

        // 初始化任务
        this.downloadTask = new DownloadTask(executorService, taskMeta, this);

        // 更新UI
        start.setEnabled(true);
        clear.setEnabled(true);
        isPause = 1;
        table.updateUI();
        logOutputHandler.clear();

        // 生成清单文件
        generateListTxt();
    }

    private void generateListTxt() {
        // 生成文件和目录
        String path = taskMeta.getPath();
        File file = new File(path);
        boolean flg = false;
        if (!file.exists() || !file.isDirectory()) {
            flg = file.mkdirs();
        } else {
            flg = true;
        }
        if (flg) {
            // 生成清单文件
            File listTxt = new File(path + "/list.txt");
            try (OutputStream ops = new FileOutputStream(listTxt)) {
                for (TsFileEntity ts : downloadTask.getTsFileList()) {
                    ops.write(String.format("file '%s'\n", ts.getName()).getBytes());
                }
                ops.flush();
            } catch (IOException ex) {
                log.error("生成清单文件失败", ex);
            }
        }
    }

    @Override
    public void start(String name, String url, long maxLength) {
        // 文件下载开始
        logOutputHandler.log("正在下载 【" + name + "】");
    }

    @Override
    public void completed(String name, String url, String path) {
        logOutputHandler.log("【" + name + "】下载完成 ");
    }

    @Override
    public void failed(String name, String url, String message) {
        logOutputHandler.log("【" + name + "】 下载失败； " + message);
    }

    @Override
    public void processing(String name, String url, int progress) {
        int row = 0;
        while (row < tableModel.getRowCount()) {
            Object valueAt = tableModel.getValueAt(row, 0);
            if (name.equals(valueAt)) {
                tableModel.setValueAt(progress, row, 2);
                break;
            }
            row++;
        }
    }

    private void initUI() {
        // 表格
        buildTable();
        // 按钮
        buildButton();
    }

    private void buildButton() {
        JPanel panel = new JPanel();
        FlowLayout layout = new FlowLayout();
        // 右对齐
        layout.setAlignment(FlowLayout.RIGHT);
        // 组件从右往左添加
        panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        panel.setLayout(layout);

        start = new JButton("全部开始");
        start.setEnabled(false);

        start.addActionListener((e) -> {
            if (isPause == 1) {
                start.setEnabled(false);
                logOutputHandler.log("开始下载...");
                this.downloadTask.start();
                isPause = 0;
            }
        });

        clear = new JButton("清空队列");
        clear.setEnabled(false);

        clear.addActionListener((e) -> {
            // 清空列表
            tableModel.getDataVector().clear();
            table.updateUI();
            taskMeta = null;
            clear.setEnabled(false);
            start.setEnabled(false);

            logOutputHandler.clear();
        });

        panel.add(start);
        panel.add(clear);

        wrapper.add(panel, BorderLayout.SOUTH);
    }

    private void buildTable() {
        String[] columns = {"文件名称", "链接地址", "进度"};
        // 表格
        tableModel = new DefaultTableModel(null, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 不可编辑
                return false;
            }
        };
        table = new JTable(tableModel);
        // 表格高度
        table.setRowHeight(30);
        // 百分比
        TableColumn column = table.getColumnModel().getColumn(2);
        column.setCellRenderer(new BarComponent());
        column.setMinWidth(100);
        column.setMaxWidth(100);

        // 禁止拖动
        table.setDragEnabled(true);
        // 禁止拖动表头
        table.getTableHeader().setReorderingAllowed(false);
        // 滚动范围
        table.setPreferredScrollableViewportSize(new Dimension(465, 280));

        // 滚动面板
        wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
