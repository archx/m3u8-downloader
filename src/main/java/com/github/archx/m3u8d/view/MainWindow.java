package com.github.archx.m3u8d.view;

import com.github.archx.m3u8d.component.FileListComponent;
import com.github.archx.m3u8d.component.LogOutputComponent;
import com.github.archx.m3u8d.component.TaskInputComponent;
import lombok.val;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 主窗体
 *
 * @author archx
 * @since 2020/9/4 21:45
 */
public class MainWindow extends JFrame {

    private ExecutorService executorService;

    public MainWindow(String title) throws HeadlessException {
        super(title);
        setSize(500, 620);
    }

    /**
     * 启动函数
     */
    public void startup() {
        // 初始化菜单
        initMenu();

        // 头部组件
        val head = new TaskInputComponent(this);
        add(head, BorderLayout.NORTH);

        // 表格组件
        val table = new FileListComponent(this);
        table.setExecutorService(executorService);
        add(table, BorderLayout.CENTER);

        // 日志组件
        val logOutput = new LogOutputComponent();
        add(logOutput, BorderLayout.SOUTH);

        // 设置回调
        head.setTaskHandler(table);
        table.setLogOutputHandler(logOutput);

        // 展示界面
        showUI();
    }

    private void initMenu() {
        // 菜单由以下部分组成
        // 菜单栏 -> 菜单 -> 菜单元素
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(238, 238, 238));

        JMenu menu = new JMenu("查看 (M)");
        menu.setMnemonic(KeyEvent.VK_M); // 设置快捷键 Alt + M

        JMenuItem about = new JMenuItem("关于 (A)");
        about.setMnemonic(KeyEvent.VK_A);

        // 点击事件
        about.addActionListener((e) -> {
            JOptionPane.showMessageDialog(
                    this,
                    "Java Swing 学习练习程序",
                    "关于 M3U8 Downloader",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        menu.add(about);

        // 分割线
        menu.addSeparator();

        JMenuItem ff = new JMenuItem("如何合并TS文件");
        ff.addActionListener((e) -> {
            String msg = "ffmpeg -y -f concat -safe 0 -i list.txt -c copy out.mp4";
            JOptionPane.showMessageDialog(
                    this,
                    msg,
                    "ffmpeg 合并文件",
                    JOptionPane.INFORMATION_MESSAGE);

        });
        menu.add(ff);

        menuBar.add(menu);

        setJMenuBar(menuBar);
    }

    private void showUI() {
        // 屏幕居中
        setLocationRelativeTo(null);
        // 窗体可见
        setVisible(true);
        // 禁止窗体缩放
        setResizable(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void setExecutorService(ThreadPoolExecutor executor) {
        this.executorService = executor;
    }
}
