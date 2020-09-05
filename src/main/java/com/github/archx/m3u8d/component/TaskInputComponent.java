package com.github.archx.m3u8d.component;

import com.github.archx.m3u8d.entity.TaskMetaEntity;
import com.github.archx.m3u8d.uitl.NetUtils;
import com.github.archx.m3u8d.uitl.Utils;
import com.github.archx.m3u8d.view.handler.TaskHandler;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Optional;

/**
 * 任务输入组件
 *
 * @author archx
 * @since 2020/9/4 21:46
 */
public class TaskInputComponent extends JPanel implements ActionListener {

    private final Component parentComponent;

    private JTextField linkTextField;
    private JTextField dirTextField;

    private TaskHandler taskHandler;

    public TaskInputComponent(Component parentComponent) {
        this.parentComponent = parentComponent;

        // 设置布局管理器
        BorderLayout layout = new BorderLayout();
        // 组件之间的间距为 10
        layout.setHgap(10);
        setLayout(layout);

        // 设置内边距
        setBorder(new EmptyBorder(10, 10, 10, 10));

        initUI();
    }

    public void setTaskHandler(TaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String url = linkTextField.getText();
        String path = dirTextField.getText();
        if (StringUtils.isAllBlank(url) ||
                !(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("ftp://"))) {
            JOptionPane.showMessageDialog(parentComponent, "请输入有效的链接地址", "M3U8 Downloader", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 随机目录
        if (StringUtils.isAllBlank(path)) {
            path = NetUtils.getRootDirectory() + "/" + Utils.getRandomDirectoryName();
            dirTextField.setText(path);
        }

        Optional<TaskMetaEntity> taskMeta = NetUtils.getTaskMeta(path, url);
        if (taskMeta.isPresent()) {
            if (taskHandler != null) taskHandler.handle(taskMeta.get());
        } else {
            JOptionPane.showMessageDialog(parentComponent, "链接地址解析失败，查看错误日志了解详情", "处理失败", JOptionPane.ERROR_MESSAGE);
        }

        // 清空
        // linkTextField.setText("");
        // dirTextField.setText("");
    }

    private void initUI() {
        // WEST 左边
        buildWestComponent();
        // CENTER
        buildCenterComponent();
        // EAST 右边
        buildEastComponent();
    }

    private void buildEastComponent() {
        JButton button = new JButton("解析");
        button.addActionListener(this);
        add(button, BorderLayout.EAST);
    }


    private void buildWestComponent() {
        JPanel panel = new JPanel();
        // 格子布局，两行一列
        panel.setLayout(new GridLayout(2, 1));

        panel.add(new JLabel("链接地址", JLabel.RIGHT));
        panel.add(new JLabel("保存目录", JLabel.RIGHT));

        add(panel, BorderLayout.WEST);
    }

    private void buildCenterComponent() {
        JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(2, 1));

        linkTextField = new JTextField();
        // 测试地址
        linkTextField.setText("http://vodcdn.video.taobao.com/oss/ali-video/bbae2fc164d9517913ed5a9117f8455b/video.m3u8");

        dirTextField = new JTextField();

        // 设置文本框首选宽高
        Dimension dimension = new Dimension(300, 30);
        linkTextField.setPreferredSize(dimension);
        dirTextField.setPreferredSize(dimension);

        panel.add(linkTextField);

        // 选择目录
        JPanel chosePane = new JPanel();
        chosePane.setLayout(new BorderLayout());
        chosePane.add(dirTextField, BorderLayout.CENTER);

        JButton button = new JButton("浏览");
        button.addActionListener((e) -> {
            JFileChooser fileChooser = new JFileChooser();

            FileSystemView fsv = FileSystemView.getFileSystemView();
            File homeDirectory = fsv.getHomeDirectory(); // 桌面路径

            fileChooser.setCurrentDirectory(homeDirectory);
            fileChooser.setDialogTitle("选择保存的目录");
            fileChooser.setApproveButtonText("确定");
            // 只选择目录
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int dialog = fileChooser.showOpenDialog(parentComponent);
            if (JFileChooser.APPROVE_OPTION == dialog) {
                String path = fileChooser.getSelectedFile().getPath();
                dirTextField.setText(path);
            }
        });
        chosePane.add(button, BorderLayout.EAST);

        panel.add(chosePane);

        add(panel, BorderLayout.CENTER);
    }

}
