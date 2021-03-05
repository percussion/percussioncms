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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.adapters.DtsConnectorConfigurationAdapter;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/****
 * This action is responsible for generating a properties file that
 * can be used to externalize the configuration of the DTS tomcat from
 * the server.xml file. It should be called before the server.xml is updated
 * or replaced.
 */
public class PSUpdateDTSConfiguration extends PSAction {

    private static String PROD_PATH = "Deployment";
    private static String STAGING_PATH = "Staging/Deployment";
    private static String STAGING_PATH_WIN = "Staging\\Deployment";
    private static String SERVER_XML = "Server/conf/server.xml.5.3";
    private static String CATALINA_PROPERTIES = "Server/conf/perc/perc-catalina.properties";

    @Override
    public void execute()
    {
        String rxDir = getRootDir();

        System.setProperty("javax.xml.parsers.DocumentBuilderFactory","com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.parsers.SAXParserFactory","com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");

        File dtsRoot = new File(rxDir,PROD_PATH );
        if(dtsRoot.exists()) {
            updateConfiguration(dtsRoot);
        }

        dtsRoot = new File(rxDir,STAGING_PATH);
        if(dtsRoot.exists()) {
            updateConfiguration(dtsRoot);
        }
    }

    private Properties loadPercCatalinaProperties(File percCatalinaFile) throws IOException {
        InputStream inputStream = null;
        Properties prop = new Properties();
        try {

            inputStream = new FileInputStream(percCatalinaFile);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + CATALINA_PROPERTIES + "' not found in the classpath");
            }


        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }
        return prop;
    }
    private void updateConfiguration(File prodPath)  {
        File serverXmlFile = new File(prodPath,SERVER_XML);

        if (serverXmlFile.exists())
        {

            DtsConnectorConfigurationAdapter adapter = new DtsConnectorConfigurationAdapter();
            DefaultConfigurationContextImpl config = new DefaultConfigurationContextImpl(Paths.get(getRootDir()), "ENC_KEY");

            adapter.load(config);

            /* TODO: The way this config is loading is over complicated needs simplified
            doesn't take into account if there are more DTS instances registered
            than just a production or staging dts */
            Properties properties = new Properties();
            List<IPSConnector> connectors = null;
            if(prodPath.getAbsolutePath().toString().contains(STAGING_PATH) || prodPath.getAbsolutePath().toString().contains(STAGING_PATH_WIN)){
                System.out.println("Processing Staging DTS configuration..");
               connectors = config.getConfig().getDtsConfig().getStagingDtsConnectorInfo().getConnectors();
                File percCatalinaFile = new File(prodPath,CATALINA_PROPERTIES);
                try {
                    if(percCatalinaFile.exists()) {
                        properties = loadPercCatalinaProperties(percCatalinaFile);
                    }else{
                        percCatalinaFile.createNewFile();
                    }
                }catch(IOException io){
                    PSLogger.logError("Error loading perc-catalina.properties: " + io.getMessage());
                }

            }else{
                System.out.println("Processing Production DTS configuration..");
                connectors = config.getConfig().getDtsConfig().getDtsConnectorInfo().getConnectors();
                File percCatalinaFile = new File(prodPath,CATALINA_PROPERTIES);
                try {
                    if(percCatalinaFile.exists()) {
                        properties = loadPercCatalinaProperties(percCatalinaFile);
                    }else{
                        percCatalinaFile.createNewFile();
                    }
                }catch(IOException io){
                    PSLogger.logError("Error loading perc-catalina.properties: " + io.getMessage());
                }

            }


                for (IPSConnector connector : connectors)
                {
                    if(!connector.isAJP()) {
                        String scheme = connector.getScheme();
                        if (scheme.equals("http")) {
                            Map<String, String> oldProps = connector.getProperties();

                            for (String key : oldProps.keySet()) {
                                if(key.equals("port") || key.equals("redirectPort")) {
                                    properties.put("http." + key, oldProps.get(key));
                                    System.out.println("Captured property:" + "http." + key + ":=" + oldProps.get(key));
                                }
                            }

                        } else if (scheme.equals("https")) {
                            Map<String, String> oldProps = connector.getProperties();
                            for (String key : oldProps.keySet()) {
                                if(key.equals("port") ) {
                                    properties.put("https." + key, oldProps.get(key));
                                    System.out.println("Captured property:" + "https." + key + ":=" + oldProps.get(key));
                                }else if(key.equals("keystoreFile")){
                                    properties.put("https.certificateKeystoreFile", oldProps.get(key));
                                    properties.put("https.keystoreFile", oldProps.get(key));
                                    System.out.println("Captured property:" + "https.certificateKeystoreFile" + ":=" + oldProps.get(key));
                                    System.out.println("Captured property:" + "https.keystoreFile" + ":=" + oldProps.get(key));

                                }else if(key.equals("keystorePass") ){
                                    properties.put("https.certificateKeystorePassword" , oldProps.get(key));
                                    properties.put("https.keystorePass" , oldProps.get(key));
                                    System.out.println("Captured property:" + "https.certificateKeystorePassword" + ":=" + oldProps.get(key));
                                    System.out.println("Captured property:" + "https.keystorePass" + ":=" + oldProps.get(key));

                                } else if(key.equals("keyAlias")){
                                    properties.put("https.certificateKeyAlias", oldProps.get(key));
                                    properties.put("https.keyAlias", oldProps.get(key));
                                    System.out.println("Captured property:" + "https.certificateKeyAlias" + ":=" + oldProps.get(key));
                                    System.out.println("Captured property:" + "https.keyAlias" + ":=" + oldProps.get(key));

                                }

                            }

                        }
                    }
                }
            try (FileOutputStream out = new FileOutputStream(
                    new File(prodPath, CATALINA_PROPERTIES))) {
                properties.store(out, "Generated by release upgrade");
            } catch (IOException e) {
                PSLogger.logError("Error migrating DTS properties:" + e.getMessage());
            }

        }
    }
}
