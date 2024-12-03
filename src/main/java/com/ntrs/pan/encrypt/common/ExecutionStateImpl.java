
package com.ntrs.pan.encrypt.common;

import com.microsoft.informationprotection.AssignmentMethod;
import com.microsoft.informationprotection.Label;
import com.microsoft.informationprotection.ProtectionDescriptor;
import com.microsoft.informationprotection.internal.utils.Pair;
import com.microsoft.informationprotection.policy.*;
import com.microsoft.informationprotection.policy.action.ActionType;

import java.util.ArrayList;
import java.util.List;

public class ExecutionStateImpl extends ExecutionState {

    private Label newLabel;
    private String applicationScenarioId;

    public ExecutionStateImpl(Label newLabel, String applicationScenarioId) {
        this.newLabel = newLabel;
        this.applicationScenarioId = applicationScenarioId;
    }

    @Override
    public Label getNewLabel() {
        return newLabel;
    }

    @Override
    public AssignmentMethod getNewLabelAssignmentMethod() {
        return AssignmentMethod.AUTO;
    }

    @Override
    public ProtectionDescriptor getProtectionDescriptor() {
        return new ProtectionDescriptor(new ArrayList<>(), null);
    }

    @Override
    public String getContentFormat() {
        return ContentFormat.File;
    }

    @Override
    public MetadataVersion getContentMetadataVersion() {
        return new MetadataVersion(0, MetadataVersionFormat.DEFAULT);
    }

    @Override
    public ActionType getSupportedActions() {
        return ActionType.Custom;
    }

    @Override
    public Pair<Boolean, String> isDowngradeJustified() {
        return new Pair<>(true, "Justification Message");
    }

    @Override
    public String getContentIdentifier() {
        return "FilePathorMailSubject";
    }

    @Override
    public List<MetadataEntry> getContentMetadata(List<String> names, List<String> namePrefixes) {
        return new ArrayList<>();
    }


    @Override
    public String GetApplicationScenarioId() {
        return this.applicationScenarioId;
    }
}
