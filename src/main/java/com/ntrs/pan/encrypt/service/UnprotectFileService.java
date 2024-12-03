package com.ntrs.pan.encrypt.service;

public interface UnprotectFileService {
    void executeUnProtect(String unprotectRequest, byte[] fileToUnprotect);
}
