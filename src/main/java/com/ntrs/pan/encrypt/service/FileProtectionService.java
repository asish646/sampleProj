package com.ntrs.pan.encrypt.service;

import com.ntrs.pan.encrypt.request.ProtectModel;
import org.apache.commons.fileupload.FileItem;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface FileProtectionService {
    void executeProtect(ProtectModel protectRequest, FileItem fileToProtect) throws ExecutionException, InterruptedException, IOException, java.text.ParseException;
}
