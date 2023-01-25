/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.share.extension;

import com.percussion.error.PSExceptionUtils;
import com.percussion.server.IPSStartupProcess;
import com.percussion.server.IPSStartupProcessManager;
import com.percussion.server.PSServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Startup process that will auto-import customer trusted certificates
 * into the central cacerts keystore when the server starts.<br/><br/>
 *
 * @author Santosh Dhariwal
 *
 */
public class PSImportCustCertificates implements IPSStartupProcess {

    private static final Logger log = LogManager.getLogger(PSImportCustCertificates.class.getName());

    public PSImportCustCertificates(){

    }

    /***
     * Allow for running from the command line
     * @param args
     */
    public static void main(String[] args){
        Properties props = new Properties();
        props.setProperty(PSImportCustCertificates.class.getSimpleName(),"true");

        PSImportCustCertificates run = new PSImportCustCertificates();
        try {
            run.doStartupWork(props);
        } catch (Exception e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }
    @Override
    public void doStartupWork(Properties startupProps) throws Exception {

        if (!"true".equalsIgnoreCase(startupProps.getProperty(getPropName()))) {
            log.info(getPropName() + " is set to false or missing from startup properties file. Nothing to run.");
            return;
        }

        char[] password = "changeit".toCharArray();
        String certificatePath = System.getProperty("java.home") + "/lib/security/cacerts";
        File file = new File(certificatePath);
        try(InputStream localCertIn = new FileInputStream(file)){

            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(localCertIn, password);
            localCertIn.close();

            //Read all certificates in the given directory
            File custCertificateDir = new File(PSServer.getRxDir(),"rxconfig/trusted_certificates");
            File[] certificates = custCertificateDir.listFiles();
            if(certificates != null && (certificates.length > 0)){
                for (int i=0;i<certificates.length;i++) {
                    File cert = certificates[i];
                    if(!cert.isDirectory()) {
                        appendCertKey(cert, keystore);
                    }
                }
                File keystoreFile = new File(certificatePath);
                // Save the new keystore contents
                try(FileOutputStream out = new FileOutputStream(keystoreFile)) {
                    keystore.store(out, password);
                }
            }else{
                log.info("No Certificate Files found in : " + custCertificateDir.getPath());
            }

        }catch (Exception e) {
            log.error("Error while importing customer trusted certificates into the central cacerts keystore.", e);
        }

        log.info(getPropName() + " has completed.");
    }


    private static void appendCertKey ( File file , KeyStore keystore) throws Exception {

            String fname = file.getPath();
            String sName = file.getName();
            try(FileInputStream fis = new FileInputStream(fname)){
                String alias = sName + " : " + fis.getChannel().size();
                //If this Certificate is already added, then return
                if (keystore.containsAlias(alias)) {
                    fis.close();
                    return;
                }
                //if Certificate name is same and size is different, then we need to replace the certificate
                Enumeration<String> aliases = keystore.aliases();
                while (aliases.hasMoreElements()) {
                    String str = aliases.nextElement();
                    if (str.contains(sName)) {
                        keystore.deleteEntry(str);
                        break;
                    }
                }

                try(DataInputStream dis = new DataInputStream(fis)) {
                    byte[] bytes = new byte[dis.available()];
                    dis.readFully(bytes);
                    try(ByteArrayInputStream certIn = new ByteArrayInputStream(bytes)) {
                        try(BufferedInputStream bis = new BufferedInputStream(certIn)) {
                            CertificateFactory cf = CertificateFactory.getInstance("X.509");
                            Certificate cert = cf.generateCertificate(bis);
                            keystore.setCertificateEntry(alias, cert);
                        }
                    }
                }
        }catch(Exception e){
            log.error("Error while importing customer trusted certificate File Name : " + file.getName(), e);
        }

    }

    @Override
    public void setStartupProcessManager(IPSStartupProcessManager mgr) {
        mgr.addStartupProcess(this);
    }

    static String getPropName() {
        return PSImportCustCertificates.class.getSimpleName();
    }

}
