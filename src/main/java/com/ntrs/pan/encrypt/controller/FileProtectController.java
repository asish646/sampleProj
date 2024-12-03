package com.ntrs.pan.encrypt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.ntrs.pan.encrypt.request.ProtectModel;
import com.ntrs.pan.encrypt.service.FileProtectionService;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

@Component
public class FileProtectController {

    @Autowired
    FileProtectionService fileProtectionService;

    @FunctionName("protect")
    public HttpResponseMessage protectFile(@HttpTrigger(name = "protect", methods = {
            HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS, dataType = "binary") HttpRequestMessage<Optional<byte[]>> request)
            throws FileUploadException, IOException, InterruptedException, ParseException {

        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);

        Optional<byte[]> bodyBytes = request.getBody();
        if (!bodyBytes.isPresent()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Request is Empty").build();
        }

        List<FileItem> items = upload.parseRequest(new AzureHttpRequestContext(request));

        String jsonPart = null;
        FileItem fileItem = null;

        for (FileItem item : items) {
            if (item.isFormField()) {
                if ("jsonRequest".equalsIgnoreCase(item.getFieldName())) {
                    jsonPart = item.getString();
                }

            } else {
                if ("file".equalsIgnoreCase(item.getFieldName())) {
                    fileItem = item;
                }
            }
        }

        if (jsonPart == null || fileItem == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Invalid input request please pass the form data in request").build();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ProtectModel protectModel = objectMapper.readValue(jsonPart, ProtectModel.class);
        byte[] response = null;

        try {
            FileProtectService fileProtectService = new FileProtectService();
            response = fileProtectService.executeProtect(protectModel, fileItem, true);
            String originalFileName = fileItem.getName();
            String localFileName = originalFileName.substring(0, originalFileName.lastIndexOf(".")) + ".pfile";
            String localPath = "C:/Users/KT247/testing/protected/" + localFileName;
            java.nio.file.Files.write(java.nio.file.Paths.get(localPath), response);
        } catch (Exception e) {
            e.printStackTrace();
            return request
                    .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("The request is failed due to: " + e.getMessage()).build();
        }
        return request
                .createResponseBuilder(HttpStatus.OK)
                .header("content-type", "application/octect-stream")
                .body(response).build();
    }
}