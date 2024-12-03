package com.ntrs.pan.encrypt.request;

import java.util.List;

public class Permission {
    private String userId;
    private List<String> rights;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getRights() {
        return rights;
    }

    public void setRights(List<String> rights) {
        this.rights = rights;
    }
}
