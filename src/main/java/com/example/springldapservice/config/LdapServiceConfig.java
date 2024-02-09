package com.example.springldapservice.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.factory.DefaultDirectoryServiceFactory;
import org.apache.directory.server.core.factory.JdbmPartitionFactory;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.apache.directory.server.protocol.shared.transport.Transport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

import static com.example.springldapservice.ldap.util.LdifImportUtil.importLdifContent;

@Configuration
@Slf4j
public class LdapServiceConfig {

    @Value("${ldap.dn}")
    private String baseDN;
    @Value("${ldap.host}")
    private String bindHost;
    @Value("${ldap.port}")
    private int bindPort;
    private boolean enableAccessControl = false;
    private boolean enableAnonymousAccess = true;
    private boolean setConfidentialityRequired = false;

    @Bean
    public DirectoryService directoryService() throws Exception {
        // Parse "keycloak" from "dc=keycloak,dc=org"
        String dcName = baseDN.split(",")[0];
        dcName = dcName.substring(dcName.indexOf("=") + 1);
        System.setProperty( "apacheds.partition.factory", JdbmPartitionFactory.class.getName());

        DefaultDirectoryServiceFactory dsf = new DefaultDirectoryServiceFactory();
        DirectoryService service = dsf.getDirectoryService();
        service.setAccessControlEnabled(enableAccessControl);
        service.setAllowAnonymousAccess(enableAnonymousAccess);
        service.getChangeLog().setEnabled(false);

        dsf.init(dcName + "DS");

        Partition partition = dsf.getPartitionFactory().createPartition(
                service.getSchemaManager(),
                service.getDnFactory(),
                dcName,
                this.baseDN,
                1000,
                new File(service.getInstanceLayout().getPartitionsDirectory(), dcName));
        partition.initialize();

        partition.setSchemaManager(service.getSchemaManager());

        // Inject the partition into the DirectoryService
        service.addPartition( partition );

        // Last, process the context entry
        String entryLdif =
                "dn: " + baseDN + "\n" +
                        "dc: " + dcName + "\n" +
                        "objectClass: top\n" +
                        "objectClass: domain\n\n";
        importLdifContent(service, entryLdif);

        return service;
    }

    @Bean
    protected LdapServer ldapServer() throws Exception {
        LdapServer ldapServer = new LdapServer();

        ldapServer.setServiceName("DefaultLdapServer");
        ldapServer.setSearchBaseDn(this.baseDN);
        ldapServer.setConfidentialityRequired(this.setConfidentialityRequired);

        Transport ldap = new TcpTransport(this.bindHost, this.bindPort, 3, 50);
        ldapServer.addTransports( ldap );
        ldapServer.setDirectoryService( directoryService() );

        return ldapServer;
    }
}
