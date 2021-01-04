package xdman.model;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import xdman.util.ObjectMapperFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParsedHookData {

    private String url;
    private String file;
    private Map<String, String> requestHeaders;
    private Map<String, String> responseHeaders;
    private long contentLength;
    private String contentType;
    private String ext;
    private boolean partialResponse;

    public static List<ParsedHookData> parseLinks(byte[] bytes) {
        ObjectMapper mapper = ObjectMapperFactory.instance;
        JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, ParsedHookData.class);
        try {
            return ObjectMapperFactory.instance.readValue(bytes, findStartPos(bytes, '['), bytes.length, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ParsedHookData parse(byte[] bytes) {
        try {
            return ObjectMapperFactory.instance.readValue(bytes, findStartPos(bytes, '{'), bytes.length, ParsedHookData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int findStartPos(byte[] bytes, char c) {
        int counter = 0;
        for (byte aByte : bytes) {
            if (aByte == c) {
                break;
            }

            counter++;
        }

        if (counter == bytes.length) {
            throw new RuntimeException("Invalid data");
        }
        return counter;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public HeaderCollection getRequestHeadersCollection() {
        return new HeaderCollection(requestHeaders.entrySet().stream().map(x -> new HttpHeader(x.getKey(), x.getValue())).collect(Collectors.toList()));
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public boolean isPartialResponse() {
        return partialResponse;
    }

    public void setPartialResponse(boolean partialResponse) {
        this.partialResponse = partialResponse;
    }
}
