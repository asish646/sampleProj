package com.ntrs.pan.encrypt.controller;

import com.microsoft.informationprotection.*;
import com.microsoft.informationprotection.exceptions.NoPermissionsException;
import com.microsoft.informationprotection.file.*;
import com.microsoft.informationprotection.internal.FunctionalityFilterType;
import com.microsoft.informationprotection.internal.callback.FileHandlerObserver;
import com.microsoft.informationprotection.internal.file.streams.ManagedOutputStream;
import com.microsoft.informationprotection.internal.utils.Pair;
import com.microsoft.informationprotection.protection.IProtectionHandler;
import com.microsoft.informationprotection.protection.Rights;
import com.ntrs.pan.encrypt.common.AuditDelegate;
import com.ntrs.pan.encrypt.common.AuthDelegateUserCredentials;
import com.ntrs.pan.encrypt.common.ConsentDelegate;
import com.ntrs.pan.encrypt.common.FileExecutionStateImpl;
import com.ntrs.pan.encrypt.request.Permission;
import com.ntrs.pan.encrypt.request.ProtectModel;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.microsoft.informationprotection.MIP.loadFileProfileAsync;

@Service
public class FileUnProtectService {

    private static final Logger logger = LoggerFactory.getLogger(FileProtectService.class);
    public static final String APPLICATION_NAME = "OP_Test_Purview Encryption";
    public static final String APPLICATION_VERSION = "1.14";
    private String clientId;
    private boolean enableAuditDelegateOverride = false;

    public byte[] executeUnProtect(ProtectModel protectRequest, FileItem fileToProtect, boolean isLocalEnv)
            throws ExecutionException, InterruptedException, IOException, java.text.ParseException {

        // Validate the request
        requestValidation(protectRequest);
        IStream iStream = new InputStreamWrapper(fileToProtect.getInputStream(), fileToProtect.getSize());

        if (StringUtils.isBlank(clientId)) {
            clientId = "3c5b6c7b-b9a7-47f6-b502-8c9b10247de2";
        }
        IAuthDelegate authDelegate = new AuthDelegateUserCredentials(protectRequest.getRequestor(), null, clientId,
                null, null);
        MipComponent mipComponent = MipComponent.FILE;
        MIP.initialize(mipComponent, null);
        ApplicationInfo appInfo = new ApplicationInfo(clientId, APPLICATION_NAME, APPLICATION_VERSION);
        MipContext mipContext = MIP
                .createMipContext(createMipConfigurationOverride(appInfo, "%Temp%", LogLevel.INFO, enableAuditDelegateOverride));
        List<Pair<String, String>> customSettings = new ArrayList<>();
        return handleFileCommand(protectRequest, iStream, CacheStorageType.IN_MEMORY, protectRequest.getRequestor(), authDelegate, mipContext,
                customSettings, true, null, null, true);
    }

    private static byte[] handleFileCommand(ProtectModel protectRequest, IStream fileToProtect, CacheStorageType cacheStorageType, String userName,
                                            IAuthDelegate authDelegate, MipContext mipContext, List<Pair<String, String>> customSettings,
                                            boolean getLabel, String enableFunctionality, String disableFunctionality, boolean useStreamApi)
            throws ExecutionException, InterruptedException, IOException, java.text.ParseException {

        FileEngineSettings engineSettings = new FileEngineSettings("123", authDelegate, "", "en-US");

        engineSettings.setIdentity(new Identity(userName));
        engineSettings.setCustomSettings(customSettings);
        engineSettings.setProtectionCloudEndpointBaseUrl(null);
        engineSettings.setPolicyCloudEndpointBaseUrl(null);
        engineSettings.setEnablePFile(true);
        engineSettings.setCloud(com.microsoft.informationprotection.Cloud.COMMERCIAL);
        setFileLabelFilters(engineSettings, enableFunctionality, disableFunctionality);

        ConsentDelegate consentDelegate = new ConsentDelegate();
        FileProfileSettings fileProfileSettings = new FileProfileSettings(mipContext, cacheStorageType, consentDelegate);

        Future<IFileProfile> fileProfileFuture = loadFileProfileAsync(fileProfileSettings);
        IFileProfile fileProfile = fileProfileFuture.get();
        Future<IFileEngine> fileEngineFuture = fileProfile.addEngineAsync(engineSettings);
        IFileEngine fileEngine = fileEngineFuture.get();
        Future<IFileHandler> fileHandlerFuture;
        DataState dataState = parseDataState(null);
        fileHandlerFuture = createFileHandler(fileEngine, fileToProtect, null, new FileHandlerObserver(), dataState);


        IFileHandler fileHandler = fileHandlerFuture.get();
        if (getLabel) {
            ContentLabel contentLabel = fileHandler.getLabel();
            IProtectionHandler protection = fileHandler.getProtection();
            printLabel(contentLabel);
            if (protection == null) {
                logger.info("File is not protected");
            } else {
                printProtection(protection);
            }
        }
        IProtectionHandler protection = fileHandler.getProtection();
        {
            if (protection != null && !protection.getAccessCheck(Rights.Export)) {
                throw new NoPermissionsException("A minimum right of EXPORT is required to change label or protection",
                        protection.getProtectionDescriptor().getReferrer(), protection.getOwner());
            }
        }

        List<String> users = protectRequest.getPermissionList().stream().map(Permission::getUserId).collect(Collectors.toList());
        List<String> rights = protectRequest.getPermissionList().stream().flatMap(i -> i.getRights().stream()).collect(Collectors.toList());

        return protect(fileHandler, users, rights, null, useStreamApi);
    }

    private static byte[] protect(IFileHandler fileHandler, List<String> users, List<String> rights, String expiration, boolean useStreamApi)
            throws ExecutionException, InterruptedException, IOException, java.text.ParseException {

        List<UserRights> userRights = new ArrayList<>();
        userRights.add(new UserRights(users, rights));

        ProtectionDescriptor protectionDescriptor = new ProtectionDescriptor(userRights, null);
        if (expiration != null) {
            protectionDescriptor.setContentValidUntil(new SimpleDateFormat("yyyy-MM-dd 'T'hh:mm:ss").parse(expiration));
        }
        protectionDescriptor.setOwner("UA12@ntrs.com");

        fileHandler.removeProtection();
//        fileHandler.setProtection(protectionDescriptor, new ProtectionSettings());
//        logger.error("++++Protected Users++++++++++++: {}, {}, {}, {}, {}",
//                fileHandler.getProtection().getIssuedTo(),
//                fileHandler.getProtection().getOwner(),
////                fileHandler
//                fileHandler.getProtection().getProtectionDescriptor().getName(),
//                fileHandler.getProtection().getAccessCheck("AA677@ntrs.com")
//        );
//        fileHandler.getProtection().getAccessCheck("AA677@ntrs.com");
        byte[] responseFile = commitAsync(fileHandler, useStreamApi);
        return responseFile;
    }

    private static byte[] commitAsync(IFileHandler fileHandler, boolean useStreamApi)
            throws ExecutionException, InterruptedException, IOException {
        String outputFile = fileHandler.getOutputFileName();
        String modifiedFile = FilenameUtils.getFullPath(outputFile) + FilenameUtils.getBaseName(outputFile)
                + "_unprotected." + FilenameUtils.getExtension(outputFile);
        if (FilenameUtils.getExtension(outputFile).equalsIgnoreCase("pfile")) {
            String oldFullName = FilenameUtils.getBaseName(outputFile);
            String oldExtension = FilenameUtils.getExtension(oldFullName);
            String oldName = FilenameUtils.getBaseName(oldFullName);
            modifiedFile = FilenameUtils.getFullPath(outputFile) + oldName + "_unprotected" + oldExtension + ".pfile";
        }
        logger.info("Committing changes");
        byte[] responseFile = null;
        boolean result;
        ManagedOutputStream outputStream = new ManagedOutputStream();
        try {
            if (useStreamApi) {
                //ManagedOutputStream outputStream = new ManagedOutputStream();
                result = fileHandler.commitAsync(outputStream).get();
                if (result) {
                    FileUtils.writeByteArrayToFile(new File(modifiedFile), outputStream.toByteArray());
                }
            } else {
                result = fileHandler.commitAsync(modifiedFile).get();
            }
        } catch (Exception ex) {
            File newFile = new File(modifiedFile);
            if (newFile.exists()) {
                if (!newFile.delete()) {
                    logger.info("Failed to delete the file: " + modifiedFile);

                }
            }
            throw ex;
        }
        if (result) {
            logger.info("New file created: " + modifiedFile);
        }
        responseFile = outputStream.toByteArray();
        return responseFile;
    }

    private void requestValidation(ProtectModel protectRequest) throws IllegalArgumentException {

        if (!StringUtils.isNoneBlank(protectRequest.getRequestor(), protectRequest.getSystemId(),
                protectRequest.getTla()) || CollectionUtils.isEmpty(protectRequest.getPermissionList())) {

            logger.error("Failed at requestValidation: {}", protectRequest.toString());
            throw new IllegalArgumentException("Missing required fields");
        }

        // Todo: add condition to check whether the provided file is protected already
//        IProtectionHandler protection = fileHandler.getProtection();
//        if (protection != null) {
//            fileHandler.removeProtection();
//        } else {
//            System.out.println("The file is not currently protected.");
//        }

    }

    private static MipConfiguration createMipConfigurationOverride(ApplicationInfo appInfo, String path, LogLevel logLevel, boolean isAuditOverride) {
        MipConfiguration mipConfiguration = new MipConfiguration(appInfo, path, logLevel, false);
        DiagnosticConfiguration diagnosticConfiguration = new DiagnosticConfiguration();
        if (isAuditOverride) {
            diagnosticConfiguration.setAuditDelegate(new AuditDelegate());
        }

        diagnosticConfiguration.setLocalCachingEnabled(true);
        mipConfiguration.setDiagnosticConfiguration(diagnosticConfiguration);
        return mipConfiguration;
    }

    private static void setFileLabelFilters(FileEngineSettings engineSettings, String enableFunctionality, String disableFunctionality) {
        if (enableFunctionality != null) {
            for (FunctionalityFilterType filter : createLabelFiltersFromString(enableFunctionality)) {
                engineSettings.configureFunctionality(filter, true);
            }
        }

        if (disableFunctionality != null) {
            for (FunctionalityFilterType filter : createLabelFiltersFromString(disableFunctionality)) {
                engineSettings.configureFunctionality(filter, false);
            }
        }
    }

    private static List<FunctionalityFilterType> createLabelFiltersFromString(String labelFilter)
            throws IllegalArgumentException {
        List<FunctionalityFilterType> retVal = new ArrayList<>();
        String[] entries = labelFilter.split(",");
        for (String filter : entries) {
            filter = filter.trim();
            if (filter.equalsIgnoreCase("None")){
                retVal.add(FunctionalityFilterType.NONE);
            } else if (filter.equalsIgnoreCase("CustomProtection")){
                retVal.add(FunctionalityFilterType.CUSTOM);
            } else if (filter.equalsIgnoreCase("TemplateProtection")){
                retVal.add(FunctionalityFilterType.TEMPLATE_PROTECTION);
            } else if (filter.equalsIgnoreCase("DoNotForwardProtection")){
                retVal.add(FunctionalityFilterType.DoNotForwardProtection);
            } else if (filter.equalsIgnoreCase("Adhoc Protection")){
                retVal.add(FunctionalityFilterType.ADHOC_PROTECTION);
            } else if (filter.equalsIgnoreCase("HyokProtection")){
                retVal.add(FunctionalityFilterType.HYOK_PROTECTION);
            } else if (filter.equalsIgnoreCase("PredefinedTemplateProtection")){
                retVal.add(FunctionalityFilterType.PREDEFINED_TEMPLATE);
            } else if (filter.equalsIgnoreCase("DoubleKeyProtection")){
                retVal.add(FunctionalityFilterType.DOUBLE_KEY_PROTECTION);
            } else if (filter.equalsIgnoreCase("DoubleKeyUserDefinedProtection")){
                retVal.add(FunctionalityFilterType.DOUBLE_KEY_USER_DEFINED_PROTECTION);
            }else if (filter.isEmpty()) {
                // Do nothing
            } else {
                throw new IllegalArgumentException("Filter type not recognized: " + filter);
            }
        }
        return retVal;
    }

    private static DataState parseDataState(String dataState) {

        if (dataState == null || dataState.length() == 0 || dataState.equalsIgnoreCase("Rest")) {
            return DataState.REST;
        }
        if (dataState.equalsIgnoreCase("Use")) {
            return DataState.USE;
        }
        if (dataState.equalsIgnoreCase("Motion")) {
            return DataState.MOTION;
        }

        throw new IllegalArgumentException("Content state is invalid");
    }

    private static Future<IFileHandler> createFileHandler(IFileEngine engine, IStream inputStream, String filePath, FileHandlerObserver observer, DataState dataState) {
        return engine.createFileHandlerAsync(inputStream, "%Temp%", false /* Disable audit discovery */, observer, new FileExecutionStateImpl(dataState));
    }

    private static void printLabel(ContentLabel contentLabel) {
        if (contentLabel == null) {
            logger.info("File is not labeled");
            return;
        }
        printContentLabel(contentLabel);
    }
    private static void printContentLabel(ContentLabel contentLabel) {
        Label label = contentLabel.label;
        logger.info("File is labeled as: " + label.getName());
        logger.info("Id:" + label.getId());
        String isPrivileged = contentLabel.assignmentMethod == AssignmentMethod.PRIVILEGED ? "True" : "False";
        logger.info("Privileged: " + isPrivileged);
        logger.info("Label Creation time: " + contentLabel.creationTime);
    }

    private static void printProtection(IProtectionHandler protection) {
        ProtectionDescriptor protectionDescriptor = protection.getProtectionDescriptor();
        if (protectionDescriptor.getProtectionType() == ProtectionType.TEMPLATE_BASED) {
            logger.info("File is protected with template");
        } else {
            logger.info("File is protected with custom permissions");
        }

        logger.info("Name: " + protectionDescriptor.getName());
        logger.info("Description: " + protectionDescriptor.getDescription());
        logger.info("Protection Type: " + protectionDescriptor.getProtectionType());
        logger.info("Template Id: " + protectionDescriptor.getTemplateId());
        Date contentValidUntil = protectionDescriptor.getContentValidUntil();

        if (contentValidUntil != null) {
            SimpleDateFormat expirationFormatter = new SimpleDateFormat("yyyy-MM-dd 'T'hh:mm:ss'Z'");
            logger.info("Content Expiration (UTC): " + expirationFormatter.format(contentValidUntil));
        }

        if (protectionDescriptor.getProtectionType() == ProtectionType.CUSTOM) {
            protectionDescriptor.getUserRights().forEach(item -> {
                logger.info("Users: ");
                item.getUsers().forEach(user -> System.out.print("" + user));
                System.out.print("Rights: ");
                item.getRights().forEach(right -> System.out.print("" + right));
            });
        } else {
            logger.info("Template Id: " + protectionDescriptor.getTemplateId());
        }
    }
}
