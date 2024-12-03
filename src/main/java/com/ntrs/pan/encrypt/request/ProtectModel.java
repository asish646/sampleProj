package com.ntrs.pan.encrypt.request;

import java.util.List;

public class ProtectModel {

    private String systemId;
    private String requestor;
    private String tla;
    private String requestId;
    private List<Permission> permissionList;

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getRequestor() {
        return requestor;
    }

    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    public String getTla() {
        return tla;
    }

    public void setla(String tla) {
        this.tla = tla;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public List<Permission> getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(List<Permission> permissionList) {
        this.permissionList = permissionList;
    }

    @Override
    public String toString() {
        return "ProtectModel [systemId=" + systemId + ", requestor=" + requestor + " tla=" + tla + ", requestId=" + requestId + ", permissionList=" + permissionList + "]";
    }

}