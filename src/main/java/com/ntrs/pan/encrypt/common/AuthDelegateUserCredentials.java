package com.ntrs.pan.encrypt.common;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.aad.msal4j.*;
import com.microsoft.informationprotection.IAuthDelegate;
import com.microsoft.informationprotection.Identity;

import java.net.Proxy;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AuthDelegateUserCredentials implements IAuthDelegate {
    private static String AUTHORITY = "";
    private static Set<String> SCOPE = Collections.singleton("");
    private static AuthenticationContext context = null;
    private static ClientCredential credential = null;
    private static Proxy proxy;
    private static int proxyPort = 443;
    private String userName;
    private String password;
    private String clientId;
    private String sccToken;
    private String protectionToken;
    private String ClientSeceret = "Zuo8Q~q5.Q1q~71Gfij052qBQlLi~ivc7G1rPcaY";

    public AuthDelegateUserCredentials(String userName, String password, String clientId, String sccToken, String protectionToken) {
        this.userName = userName;
        this.password = password;
        this.sccToken = sccToken;
        this.clientId = clientId;
        this.protectionToken = protectionToken;
    }

    @Override
    public String acquireToken(Identity userName, String authority, String resource, String claims) {
        if (resource.endsWith("/")) {

            SCOPE = Collections.singleton(resource + ".default");
        } else {
            SCOPE = Collections.singleton(resource + "/.default");
        }
        AUTHORITY = authority;
        String token = "";
        try {
            token = acquireTokenInteractive().accessToken();
            System.out.println("Token==>" + token);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return token;
    }

    private IAuthenticationResult acquireTokenInteractive() throws Exception {
        // Load token cache from file and initialize token cache aspect. The token cache will have
        IClientCredential credential = ClientCredentialFactory.createFromSecret(ClientSeceret);
        ConfidentialClientApplication cca = ConfidentialClientApplication.builder("3c5b6c7b-b9a7-47f6-b502-8c9b10247de2", credential)
                .authority("https://login.microsoftonline.com/2434528d-4270-4977-81dd-a6308c1761a3")
                .build();
        //List<String> list = Arrays.asList("openid", "profile", "offline_access", "https://syncservice.0365syncservice.com/.default");
        Set<String> SCOPEList = new HashSet<>(SCOPE);
        ClientCredentialParameters parameters =
                ClientCredentialParameters.builder(SCOPEList)
                        .build();
        IAuthenticationResult result = cca.acquireToken(parameters).join();
        return result;
    }
}