package xdman.monitoring;

import xdman.model.HeaderCollection;
import xdman.util.Logger;
import xdman.util.NetUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Request {
    private String url;
    private HeaderCollection headers;
    private byte[] body;

    public void read(InputStream in) throws IOException {
        String reqLine = NetUtils.readLine(in);
        Logger.log(reqLine);
        if (reqLine.isEmpty()) {
            throw new IOException("Invalid request line: " + reqLine);
        }

        String[] arr = reqLine.split(" ");
        if (arr.length != 3) {
            throw new IOException("Invalid request: " + reqLine);
        }

        this.url = arr[1];
        this.headers = new HeaderCollection();
        headers.loadFromStream(in);
        String header = headers.getValue("Content-Length");
        if (header == null) {
            throw new IOException("Invalid request: cannot read content-length");
        }

        long len = Long.parseLong(header);
        body = new byte[(int) len];
        int off = 0;
        while (len > 0) {
            int x = in.read(body, off, body.length - off);
            if (x == -1) {
                throw new IOException("Unexpected EOF");
            }
            len -= x;
            off += x;
        }
    }

    public final String getUrl() {
        return url;
    }

    public final HeaderCollection getHeaders() {
        return headers;
    }

    public final byte[] getBody() {
        return body;
    }
}
