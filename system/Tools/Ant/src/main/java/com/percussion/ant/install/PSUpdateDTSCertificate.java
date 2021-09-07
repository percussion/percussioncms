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
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.security.KeyStore;
import java.security.cert.Certificate;

public class PSUpdateDTSCertificate extends PSAction {
    private static String PROD_PATH = "Deployment";
    private static String STAGING_PATH = "Staging/Deployment";
    private static String CATALINA_PROPERTIES = "Server/conf/perc/perc-catalina.properties";
    private static String newKeyStoreName  = "Server/conf/.keystoreNew";
    private static String oldKeyStoreName  = "Server/conf/.keystore";
    private static String CERT_ALIAS="tomcat";
    private static char[] DEFAULT_PWD="changeit".toCharArray();

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

    /**
     * Get KeyStore Password from Catalina.properties
     * @param prodPath
     * @return
     * @throws IOException
     */
    private String getKeyStorePasswordFromCatalinaProperties(File prodPath) throws IOException {
        File percCatalinaFile = new File(prodPath, CATALINA_PROPERTIES);
        Properties properties = new Properties();
         try (FileInputStream inputStream = new FileInputStream(percCatalinaFile)) {
            if (inputStream != null) {
                properties.load(inputStream);
                return properties.getProperty("https.keystorePass");
            } else {
                throw new FileNotFoundException("property file '" + CATALINA_PROPERTIES + "' not found in the classpath");
            }
        }
    }

    /**
     * Update New Default Certificate in the path specified
     * @param prodPath
     */
    private void updateCertificate(File prodPath) {

        File newCertFile = new File(prodPath+ File.separator + newKeyStoreName);
        if(!newCertFile.exists()){
            //That means it is a new Install, no need to upgrade certificate..thus return
            return;
        }
        PSLogger.logInfo("Upgrading Default DTS Self Signed Certificate..." + prodPath);
        updateCertificate(prodPath,newCertFile,DEFAULT_PWD,true);
        //Delete the new Cert File after updating
        newCertFile.delete();
        PSLogger.logInfo("Done Upgrading Default DTS Self Signed Certificate..." + prodPath);
    }

    /**
     * Try's to update certificate by opening the keystore with default password, incase fails, then gets the pwd from
     * Catalina.properties and opens the keystore. If Successful in opening the keystore, then updates the certificate.
     * @param prodPath
     * @param newCertFile
     * @param pwd
     */
    private void updateCertificate(File prodPath,File newCertFile,char[] pwd,boolean withDefaultPwd){
        try {
            KeyStore oldKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try(FileInputStream io = new FileInputStream(prodPath + File.separator + oldKeyStoreName)) {
                try {
                    //Load exisitng keystore using above information
                    oldKeyStore.load(io, pwd);
                }catch(IOException ex){
                    //Incase we have already tried with Catalina.properties Pwd, then exit.Else it will go in loop
                    if(!withDefaultPwd){
                        return;
                    }else {
                        //Load keystore password from Catalina.properties
                        String pwdStr = getKeyStorePasswordFromCatalinaProperties(prodPath);
                        if (pwdStr != null) {
                            updateCertificate(prodPath, newCertFile, pwdStr.toCharArray(), false);
                            return;
                        } else {
                            return;
                        }
                    }
                }
                KeyStore newKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                try(FileInputStream io2 = new FileInputStream(prodPath + File.separator + newKeyStoreName)) {
                    //Load new keystore file with new certificate came with new build
                    newKeyStore.load(io2, DEFAULT_PWD);
                    Certificate[] newCertChain = newKeyStore.getCertificateChain(CERT_ALIAS);
                    //update existing keystore with new certificate
                    oldKeyStore.setKeyEntry(CERT_ALIAS,newKeyStore.getKey(CERT_ALIAS,pwd),pwd, newCertChain);
                }
            }

        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException io) {
            PSLogger.logError("Error Failed to Upgrading Default DTS Self Signed Certificate: " + io.getMessage());
        }
        PSLogger.logInfo("Done Upgrading Default DTS Self Signed Certificate..." + prodPath);
    }
}
