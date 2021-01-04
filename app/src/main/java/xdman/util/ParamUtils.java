package xdman.util;

import xdman.XDMApp;

import javax.swing.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

public class ParamUtils {
    public static void sendParam(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            String value = params.get(key);
            sb.append(key).append(":").append(value).append("\n");
        }

        InetAddress addr = InetAddress.getLoopbackAddress();

        String req = "GET /cmd HTTP/1.1\r\n" +
                "Content-Length: " + sb.length() + "\r\n" +
                "Host: " + addr.getHostName() + "\r\n" +
                "Connection: close\r\n\r\n" +
                sb;

        String resp = null;
        try (Socket sock = new Socket(InetAddress.getLoopbackAddress(), 9614)) {
            InputStream in = sock.getInputStream();
            OutputStream out = sock.getOutputStream();
            out.write(req.getBytes());
            resp = NetUtils.readLine(in);
            resp = resp.split(" ")[1];
        } catch (Exception ignored) {
        }

        if (!"200".equals(resp)) {
            JOptionPane.showMessageDialog(XDMApp.getInstance().getMainWindow(), "An older version of XDM is already running.");
        }
    }
}
