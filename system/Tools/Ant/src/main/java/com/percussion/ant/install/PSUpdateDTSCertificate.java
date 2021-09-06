/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.security.KeyStore;
import java.security.cert.Certificate;

public class PSUpdateDTSCertificate extends PSAction {
    private static String PROD_PATH = "Deployment";
    private static String STAGING_PATH = "Staging/Deployment";
    private static String STAGING_PATH_WIN = "Staging\\Deployment";
    private static String CATALINA_PROPERTIES = "Server/conf/perc/perc-catalina.properties";
    private static String newKeyStoreName  = "Server/conf/.keystoreNew";
    private static String CERT_ALIAS="tomcat";

    @Override
    public void execute()
    {
        String rxDir = getRootDir();

        File dtsRoot = new File(rxDir,PROD_PATH );
        if(dtsRoot.exists()) {
            updateCertificate(dtsRoot);
        }

        File staingDtsRoot = new File(rxDir,STAGING_PATH);
        if(staingDtsRoot.exists()) {
            updateCertificate(staingDtsRoot);
        }
    }

    private Properties loadPercCatalinaProperties(File percCatalinaFile) throws IOException {
        Properties prop = new Properties();
        try (FileInputStream inputStream = new FileInputStream(percCatalinaFile)) {
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + CATALINA_PROPERTIES + "' not found in the classpath");
            }
        }
        return prop;
    }

    private void updateCertificate(File prodPath) {

        File newCertFile = new File(prodPath+ File.separator + newKeyStoreName);
        if(!newCertFile.exists()){
            //That means it is a new Install, no need to upgrade certificate..thus return
            return;
        }
        Properties properties = new Properties();
        PSLogger.logInfo("Upgrading DTS SSL Certificate..." + prodPath);

        File percCatalinaFile = new File(prodPath, CATALINA_PROPERTIES);
        try {
            if (percCatalinaFile.exists()) {
                //Get Keystore file path and keystore ppassword from perc-catalina.properties
                properties = loadPercCatalinaProperties(percCatalinaFile);
                String oldKeyStoreFileName = properties.getProperty("https.keystoreFile");
                String OldPassword = properties.getProperty("https.keystorePass");
                KeyStore oldKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                try(FileInputStream io = new FileInputStream(prodPath + File.separator + "Server" + File.separator + oldKeyStoreFileName)) {
                    //Load exisitng keystore using above information
                    oldKeyStore.load(io, OldPassword.toCharArray());
                    KeyStore newKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    try(FileInputStream io2 = new FileInputStream(prodPath + File.separator + newKeyStoreName)) {
                        //Load new keystore file with new certificate came with new build
                        newKeyStore.load(io2, "changeit".toCharArray());
                        Certificate newCert = newKeyStore.getCertificate(CERT_ALIAS);
                        //update existing keystore with new certificate
                        oldKeyStore.setCertificateEntry(CERT_ALIAS, newCert);
                        //Delete the new Cert File after updating
                        newCertFile.delete();
                    }
                }
            }
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException io) {
            PSLogger.logError("Error loading perc-catalina.properties: " + io.getMessage());
        }
        PSLogger.logInfo("Done Upgrading DTS SSL Certificate..." + prodPath);
    }
}
