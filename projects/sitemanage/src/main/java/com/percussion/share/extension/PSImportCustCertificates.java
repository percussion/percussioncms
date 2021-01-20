/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.share.extension;

import com.percussion.server.IPSStartupProcess;
import com.percussion.server.IPSStartupProcessManager;
import com.percussion.server.PSServer;
import org.apache.log4j.Logger;

import java.io.*;
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

    private static final Logger log = Logger
            .getLogger(PSImportCustCertificates.class.getName());

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
            e.printStackTrace();
        }
    }
    @Override
    public void doStartupWork(Properties startupProps) throws Exception {

        if (!"true".equalsIgnoreCase(startupProps.getProperty(getPropName()))) {
            log.info(getPropName() + " is set to false or missing from startup properties file. Nothing to run.");
            return;
        }

        try {
                char[] password = "changeit".toCharArray();
                String certificatePath = System.getProperty("java.home") + "/lib/security/cacerts";
                File file = new File(certificatePath);
                InputStream localCertIn = new FileInputStream(file);

                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(localCertIn, password);
                localCertIn.close();

                //Read all certificates in the given directory
                File custCertificateDir = new File(PSServer.getRxDir(),"rxconfig/trusted_certificates");
                File[] certificates = custCertificateDir.listFiles();
                if(certificates != null && (certificates.length > 0)){
                    for (int i=0;i<certificates.length;i++) {
                        File cert = certificates[i];
                        appendCertKey (cert,keystore);
                    }
                    File keystoreFile = new File(certificatePath);
                    // Save the new keystore contents
                    FileOutputStream out = new FileOutputStream(keystoreFile);
                    keystore.store(out, password);
                    out.close();
                }else{
                    log.info("No Certificate Files found in : " + custCertificateDir.getPath());
                }

        }catch (Exception e) {
            log.error("Error while importing customer trusted certificates into the central cacerts keystore.", e);
        }

        log.info(getPropName() + " has completed.");
    }


    private static void appendCertKey ( File file , KeyStore keystore) throws Exception {
        try {
            String fname = file.getPath();
            String sName = file.getName();
            FileInputStream fis = new FileInputStream(fname);
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

            DataInputStream dis = new DataInputStream(fis);
            byte[] bytes = new byte[dis.available()];
            dis.readFully(bytes);
            ByteArrayInputStream certIn = new ByteArrayInputStream(bytes);
            BufferedInputStream bis = new BufferedInputStream(certIn);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate cert = cf.generateCertificate(bis);
            keystore.setCertificateEntry(alias, cert);
            certIn.close();
            dis.close();
            fis.close();
            bis.close();
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
