package xdman.ui.components;

import org.tukaani.xz.XZInputStream;
import xdman.Config;
import xdman.DownloadListener;
import xdman.downloaders.http.HttpDownloader;
import xdman.downloaders.metadata.HttpMetadata;
import xdman.os.OperationSystem;
import xdman.os.enums.OSBasedComponentFile;
import xdman.ui.res.ColorResource;
import xdman.ui.res.FontResource;
import xdman.util.FFExtractCallback;
import xdman.util.FileUtils;
import xdman.util.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static xdman.util.XDMUtils.getScaledInt;

public class ComponentInstaller extends JDialog implements DownloadListener, FFExtractCallback {
    private static final long serialVersionUID = -7332687839394110921L;
    private final String url;
    private final String tmpFile;
    private JLabel label;
    private JProgressBar jProgressBar;
    private HttpDownloader downloader;

    public ComponentInstaller() {
        OSBasedComponentFile file = OperationSystem.OS.getComponentFile();
        this.url = "http://xdman.sourceforge.net/components/" + file.getComponent();
        this.tmpFile = UUID.randomUUID().toString();
        initUI();
        start();
    }

    private void start() {
        HttpMetadata metadata = new HttpMetadata();
        metadata.setUrl(url);
        System.out.println(url);
        downloader = new HttpDownloader(metadata.getId(), Config.getInstance().getTemporaryFolder(), metadata);
        downloader.registerListener(this);
        downloader.start();
    }

    @Override
    public void downloadFinished(String id) {
        extractFFmpeg();
        label.setText("Installing...");
        setVisible(false);
    }

    @Override
    public void downloadFailed(String id) {
        deleteTmpFiles(id);
        JOptionPane.showMessageDialog(this, "Failed to download components");
        setVisible(false);
    }

    @Override
    public void downloadStopped(String id) {
        deleteTmpFiles(id);
        setVisible(false);
    }

    @Override
    public void downloadConfirmed(String id) {

    }

    @Override
    public void downloadUpdated(String id) {
        int val = downloader.getProgress();
        jProgressBar.setValue(val);
    }

    @Override
    public String getOutputFolder(String id) {
        return Config.getInstance().getTemporaryFolder();
    }

    @Override
    public String getOutputFile(String id, boolean update) {
        return tmpFile;
    }

    private void deleteTmpFiles(String id) {
        FileUtils.deleteTempFile(id);
    }

    private void extractFFmpeg() {
        ZipInputStream zipIn = null;
        OutputStream out = null;
        try {
            File input = new File(Config.getInstance().getTemporaryFolder(), tmpFile);
            zipIn = new ZipInputStream(new XZInputStream(new FileInputStream(input)));

            while (true) {
                ZipEntry ent = zipIn.getNextEntry();
                if (ent == null)
                    break;
                String name = ent.getName();
                File outFile = new File(Config.getInstance().getDataFolder(), name);
                out = new FileOutputStream(outFile);
                byte[] buf = new byte[8192];
                while (true) {
                    int x = zipIn.read(buf);
                    if (x == -1)
                        break;
                    out.write(buf, 0, x);
                }
                out.close();
                out = null;
                outFile.setExecutable(true);
            }
            input.delete();
        } catch (Exception e) {
            Logger.log(e);
            JOptionPane.showMessageDialog(this, "Component installation failed");
        } finally {
            try {
                zipIn.close();
                if (out != null)
                    out.close();
            } catch (Exception e) {
                Logger.log(e);
            }
        }
    }

    public void stop() {
        cancel();
    }

    private void initUI() {
        setSize(getScaledInt(400), getScaledInt(300));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setModal(true);
        setAlwaysOnTop(true);
        setResizable(false);
        setTitle("Installing XDM");
        setLayout(null);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBounds(0, 0, getScaledInt(400), getScaledInt(60));
        titlePanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Installing FFmpeg multimedia library");
        titleLabel.setBorder(new EmptyBorder(getScaledInt(10), getScaledInt(10), getScaledInt(10), getScaledInt(10)));
        titleLabel.setFont(FontResource.getBigBoldFont());
        titleLabel.setForeground(Color.BLACK);
        titlePanel.add(titleLabel);
        add(titlePanel);

        label = new JLabel("Downloading...");
        label.setBounds(getScaledInt(20), getScaledInt(80), getScaledInt(360), getScaledInt(30));
        add(label);

        jProgressBar = new JProgressBar();
        jProgressBar.setBounds(getScaledInt(20), getScaledInt(110), getScaledInt(350), getScaledInt(10));
        add(jProgressBar);

        JButton btn = createButton2();
        btn.setBounds(getBounds().width - getScaledInt(80) - getScaledInt(20), getBounds().height - getScaledInt(80), getScaledInt(80), getScaledInt(30));
        add(btn);

        btn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

    }

    private JButton createButton2() {
        JButton btn = new CustomButton("Cancel");
        btn.setBackground(ColorResource.getDarkBtnColor());
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(FontResource.getNormalFont());
        return btn;
    }

    private void cancel() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel component downloads?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            downloader.stop();
        }
    }
}
