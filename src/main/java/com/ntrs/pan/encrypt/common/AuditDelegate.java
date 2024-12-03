package com.ntrs.pan.encrypt.common;
import java.util.HashMap;
import java.util.List;
import com.microsoft.informationprotection.AuditEvent;
import com.microsoft.informationprotection. EnableAuditSetting;
import com.microsoft.informationprotection. EventContext;
import com.microsoft.informationprotection. EventProperty;
import com.microsoft.informationprotection. IAuditDelegate;

public class AuditDelegate implements IAuditDelegate {
    private HashMap<String, List<EventProperty>> eventProperties;
    private EnableAuditSetting auditSetting;
    @Override
    public void flush() {
    }
    @Override
    public void writeEvent (AuditEvent auditEvent, EventContext eventContext) {
        eventProperties.put (auditEvent.getName(), auditEvent.getProperties());
    }
    @Override
    public void setEnableAuditSetting (EnableAuditSetting enableAuditSetting) {
        auditSetting = enableAuditSetting;
    }
    boolean HasEvent (String eventName) {
        return eventProperties.get(eventName) != null;
    }
    public List<EventProperty> GetEventProperties (String eventName) {
        return eventProperties.get(eventName);
    }
}