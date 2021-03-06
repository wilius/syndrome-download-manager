package xdman.monitoring;

import xdman.XDMApp;
import xdman.downloaders.metadata.HdsMetadata;
import xdman.downloaders.metadata.manifests.F4MManifest;
import xdman.model.ParsedHookData;
import xdman.util.Logger;
import xdman.util.StringUtils;
import xdman.util.XDMUtils;

import java.io.*;

public class F4mHandler {
    public static boolean handle(File f4mfile, ParsedHookData data) {
        try {
            StringBuffer buf = new StringBuffer();
            InputStream in = new FileInputStream(f4mfile);
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            while (true) {
                String ln = r.readLine();
                if (ln == null) {
                    break;
                }
                buf.append(ln + "\n");
            }
            in.close();
            Logger.log("HDS manifest validating...");
            if (buf.indexOf("http://ns.adobe.com/f4m/1.0") < 0) {
                Logger.log("No namespace");
                return false;
            }
            if (buf.indexOf("manifest") < 0) {
                Logger.log("No manifest keyword");
                return false;
            }
            if (buf.indexOf("drmAdditional") > 0) {
                Logger.log("DRM");
                return false;
            }
            if (buf.indexOf("media") == 0 || buf.indexOf("href") > 0 || buf.indexOf(".f4m") > 0) {
                Logger.log("Not a valid manifest");
                return false;
            }

            Logger.log("URL: " + data.getUrl());
            F4MManifest manifest = new F4MManifest(data.getUrl(), f4mfile.getAbsolutePath());
            long[] bitRates = manifest.getBitRates();
            Logger.log("Bitrates: " + bitRates.length);
            for (long bitRate : bitRates) {
                HdsMetadata metadata = new HdsMetadata();
                metadata.setUrl(data.getUrl());
                metadata.setBitRate((int) bitRate);
                metadata.setHeaders(data.getRequestHeadersCollection());
                String file = data.getFile();
                if (StringUtils.isNullOrEmptyOrBlank(file)) {
                    file = XDMUtils.getFileName(data.getUrl());
                }
                XDMApp.getInstance().addMedia(metadata, file + ".flv", "FLV " + bitRate + " bps");
            }
            Logger.log("Manifest valid");
            return true;
        } catch (Exception e) {
            Logger.log(e);
            return false;
        }
    }
}
