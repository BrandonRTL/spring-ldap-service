package com.example.springldapservice.ldap.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FindFile {
    public static InputStream findFile(String keycloakConfigFile) {
        if (keycloakConfigFile.startsWith("classpath:")) {
            String classPathLocation = keycloakConfigFile.replace("classpath:", "");
            // Try current class classloader first
            InputStream is = FindFile.class.getClassLoader().getResourceAsStream(classPathLocation);
            if (is == null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classPathLocation);
            }

            if (is != null) {
                return is;
            } else {
                throw new RuntimeException("Unable to find config from classpath: " + keycloakConfigFile);
            }
        } else {
            // Fallback to file
            try {
                return new FileInputStream(keycloakConfigFile);
            } catch (FileNotFoundException fnfe) {
                throw new RuntimeException(fnfe);
            }
        }
    }
}