package com.ntrs.pan.encrypt.controller;

import com.microsoft.azure.functions.HttpRequestMessage;
import org.apache.commons.fileupload.RequestContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class AzureHttpRequestContext implements RequestContext {

    private final InputStream stream;
    private final HttpRequestMessage<Optional<byte[]>> request;

    public AzureHttpRequestContext(HttpRequestMessage<Optional<byte[]>> request) {
        this.request = request;
        byte[] body = request.getBody().orElse(new byte[0]);
        this.stream = new ByteArrayInputStream(body);
    }

    @Override
    public String getCharacterEncoding() {
        return Optional.ofNullable(request.getHeaders().get("content-type")).map(i -> {
            if (i.contains("charset=")) {
                return i.split("charset=")[1];
            }
            return StandardCharsets.UTF_8.name();
        }).orElse(StandardCharsets.UTF_8.name());
    }

    @Override
    public String getContentType() {
        return Optional.ofNullable(request.getHeaders().get("content-type"))
                .orElse("multipart/form-data");
    }

    @Override
    public int getContentLength() {
        return request.getBody().map(i -> i.length).orElse(0);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.stream;
    }
}