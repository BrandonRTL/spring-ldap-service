package com.example.springldapservice.ldap.util;

import com.example.springldapservice.ldap.common.FindFile;
import com.example.springldapservice.ldap.common.StreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.exception.LdapEntryAlreadyExistsException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.apache.directory.server.core.api.DirectoryService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LdifImportUtil {

    public static void importLdifContent(DirectoryService directoryService, String ldifContent) throws Exception {
        log.info("Import Content: {}", ldifContent);

        try (LdifReader ldifReader = new LdifReader(IOUtils.toInputStream(ldifContent))) {
            for (LdifEntry ldifEntry : ldifReader) {
                log.info("Entry: {}", ldifEntry);
                try {
                    directoryService.getAdminSession().add(new DefaultEntry(directoryService.getSchemaManager(), ldifEntry.getEntry()));
                } catch (LdapEntryAlreadyExistsException ignore) {
                    log.info("Entry " + ldifEntry.getDn() + " already exists. Ignoring.");
                }
            }
        }
    }

    public static void importLdifFromFile(String bindHost, String ldifFile, DirectoryService directoryService) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("hostname", bindHost);
        InputStream is = FindFile.findFile(ldifFile);
        String ldifContent = StrSubstitutor.replace(StreamUtil.readString(is), map);
        log.info("Content : {}", ldifContent);

        importLdifContent(directoryService, ldifContent);
    }
}
