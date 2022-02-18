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
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSConnector;
import com.percussion.utils.container.adapters.DtsConnectorConfigurationAdapter;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        Properties prop = new Properties();
        try (FileInputStream inputStream = new FileInputStream(percCatalinaFile)) {
                prop.load(inputStream);
        }
        return prop;
    }

    private void updateConfiguration(File prodPath)  {
        File serverXmlFile = new File(prodPath,SERVER_XML);
        String version = getUpgradingFromVersion();
        boolean updatefrom53 = version != null && !StringUtils.isBlank(version) && version.equalsIgnoreCase("5.3");

        if (serverXmlFile.exists() && updatefrom53 )
        {
            DtsConnectorConfigurationAdapter adapter = new DtsConnectorConfigurationAdapter();
            DefaultConfigurationContextImpl config = new DefaultConfigurationContextImpl(Paths.get(getRootDir()), "ENC_KEY");

            adapter.load(config);

            /* TODO: The way this config is loading is over complicated needs simplified
            doesn't take into account if there are more DTS instances registered
            than just a production or staging dts */
            Properties properties = new Properties();
            List<IPSConnector> connectors = null;
            if(prodPath.getAbsolutePath().contains(STAGING_PATH) || prodPath.getAbsolutePath().contains(STAGING_PATH_WIN)){
                PSLogger.logInfo("Processing Staging DTS configuration..");
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
                PSLogger.logInfo("Processing Production DTS configuration..");
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
                                    PSLogger.logInfo("Captured property:" + "http." + key + ":=" + oldProps.get(key));
                                }
                            }

                        } else if (scheme.equals("https")) {
                            Map<String, String> oldProps = connector.getProperties();
                            for (String key : oldProps.keySet()) {
                                if(key.equals("port") ) {
                                    properties.put("https." + key, oldProps.get(key));
                                    PSLogger.logInfo("Captured property:" + "https." + key + ":=" + oldProps.get(key));
                                }else if(key.equals("keystoreFile")){
                                    properties.put("https.certificateKeystoreFile", oldProps.get(key));
                                    properties.put("https.keystoreFile", oldProps.get(key));
                                    PSLogger.logInfo("Captured property:" + "https.certificateKeystoreFile" + ":=" + oldProps.get(key));
                                    PSLogger.logInfo("Captured property:" + "https.keystoreFile" + ":=" + oldProps.get(key));

                                }else if(key.equals("keystorePass") ){
                                    properties.put("https.certificateKeystorePassword" , oldProps.get(key));
                                    properties.put("https.keystorePass" , oldProps.get(key));
                                    PSLogger.logInfo("Captured property:" + "https.certificateKeystorePassword" + ":=" + oldProps.get(key));
                                    PSLogger.logInfo("Captured property:" + "https.keystorePass" + ":=" + oldProps.get(key));

                                } else if(key.equals("keyAlias")){
                                    properties.put("https.certificateKeyAlias", oldProps.get(key));
                                    properties.put("https.keyAlias", oldProps.get(key));
                                    PSLogger.logInfo("Captured property:" + "https.certificateKeyAlias" + ":=" + oldProps.get(key));
                                    PSLogger.logInfo("Captured property:" + "https.keyAlias" + ":=" + oldProps.get(key));

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

    /**
     * return the previously installed version if possible.
     * @return null or the version
     */
    private String getUpgradingFromVersion() {
        PSLogger.logInfo("In Get version");
        Project p = this.getProject();
        if(p == null)
            return null;
        String backupdir = p.getProperty("new.backup.dir");

        if(backupdir == null)
            return null;
        PSLogger.logInfo("Backup Dir Found:" + backupdir);
        File versionProps = new File(backupdir + File.separator + "Version.properties");
        if (!versionProps.exists())
            return null;
        PSLogger.logInfo("PreviousVersion Prop File Found");
        try (InputStream in = new FileInputStream(versionProps)) {
            Properties props = new Properties();
            props.load(in);
            String major = props.getProperty("majorVersion");
            String minor = props.getProperty("minorVersion");
            String version = new String();
            if (major != null) {
                PSLogger.logInfo("Major Version:" + major);
                version = major;
                if (minor != null) {
                    version = version + "." + minor;
                    PSLogger.logInfo("Version:" + version);
                    return version;
                }
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }


}
