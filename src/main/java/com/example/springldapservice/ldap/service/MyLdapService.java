package com.example.springldapservice.ldap.service;

import com.example.springldapservice.ldap.util.LdifImportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.ldap.LdapServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import static com.example.springldapservice.ldap.util.LdifImportUtil.importLdifFromFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyLdapService {
    @Value("${ldap.import.file.path}")
    private String ldifFile;
    @Value("${ldap.host}")
    private String bindHost;

    private final DirectoryService directoryService;
    private final LdapServer ldapServer;

    public void addUser(String userData) throws Exception {
        LdifImportUtil.importLdifContent(directoryService, userData);
    }

    @PostConstruct
    private void postConstruct() throws Exception {
        importLdifFromFile(bindHost, ldifFile, directoryService);
        start();
    }

    private void start() throws Exception {
        log.info("Starting LDAP server");
        ldapServer.start();
        if (ldapServer.isStarted() && ldapServer.getDirectoryService().isStarted()) {
            log.info("LDAP server started");
        }
        else {
            log.error("Failed to start! LDAP server started - {}, Directory service started - {}"
                    ,ldapServer.isStarted(), ldapServer.getDirectoryService().isStarted());
        }
    }
}
