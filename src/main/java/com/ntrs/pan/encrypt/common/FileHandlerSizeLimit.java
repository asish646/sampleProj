

package com.ntrs.pan.encrypt.common;

import com.microsoft.informationprotection.ContentLabel;
import com.microsoft.informationprotection.IStream;
import com.microsoft.informationprotection.Label;
import com.microsoft.informationprotection.ProtectionDescriptor;
import com.microsoft.informationprotection.file.IFileHandler;
import com.microsoft.informationprotection.file.IFileInspector;
import com.microsoft.informationprotection.file.LabelingOptions;
import com.microsoft.informationprotection.file.ProtectionSettings;
import com.microsoft.informationprotection.internal.utils.Pair;
import com.microsoft.informationprotection.policy.action.Action;
import com.microsoft.informationprotection.protection.IProtectionHandler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FileHandlerSizeLimit implements IFileHandler {
    @Override
    public CompletableFuture<Void> RegisterContentForTrackingAndRevocationAsync(boolean arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletableFuture<Void> RevokeContentAsync() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletableFuture<Collection<Action>> classifyAsync() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletableFuture<Boolean> commitAsync(String arg0) { // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletableFuture<Boolean> commitAsync(IStream arg0) { // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteLabel(LabelingOptions arg0) {
// TODO Auto-generated method stub
    }

    @Override
    public CompletableFuture<String> getDecryptedTemporaryFileAsync() {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletableFuture<IStream> getDecryptedTemporaryStreamAsync() { // TODO Auto-generated method stub
        return null;


    }

    @Override
    public ContentLabel getLabel() {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getOutputFileName() {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Pair<String, String>> getProperties(int arg0) {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public IProtectionHandler getProtection() {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public CompletableFuture<IFileInspector> inspectAsync() {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isModified() {
// TODO Auto-generated method stub
        return false;
    }

    @Override
    public void notifyCommitSuccessful(String arg0) {
// TODO Auto-generated method stub
    }

    @Override
    public void removeProtection() {
// TODO Auto-generated method stub
    }

    @Override
    public void setLabel(Label arg0, LabelingOptions arg1, ProtectionSettings arg2) { // TODO Auto-generated method stub
    }

    @Override
    public void setProtection(IProtectionHandler arg0) {
// TODO Auto-generated method stub
    }

    @Override
    public void setProtection(ProtectionDescriptor arg0, ProtectionSettings arg1) { // TODO Auto-generated method stub
    }
}