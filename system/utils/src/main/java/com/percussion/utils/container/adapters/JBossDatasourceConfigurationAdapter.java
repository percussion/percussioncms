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

package com.percussion.utils.container.adapters;

import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSConfigurationAdapter;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSMissingApplicationPolicyException;
import com.percussion.utils.container.PSSecureCredentials;
import com.percussion.utils.container.jboss.PSJBossJndiDatasource;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.jdbc.PSDatasourceResolver;
import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.spring.PSSpringConfiguration;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.percussion.utils.jdbc.PSDatasourceResolver.DATASOURCE_RESOLVER_NAME;

public class JBossDatasourceConfigurationAdapter implements IPSConfigurationAdapter<DefaultConfigurationContextImpl> {

    public static Log ms_log = LogFactory.getLog(JBossDatasourceConfigurationAdapter.class);


    private Path appServer = Paths.get("AppServer","server","rx");
    private Path dsConfigFile = appServer.resolve(Paths.get("deploy","rx-ds.xml"));
    private Path loginConfigFile = appServer.resolve(Paths.get("conf","login-config.xml"));
    private Path springConfigFile = appServer.resolve(Paths.get("deploy","rxapp.ear","rxapp.war","WEB-INF","config","spring","server-beans.xml"));
    

    @Override
    public void load(DefaultConfigurationContextImpl configurationContext) {
        Path root = configurationContext.getRootDir();
        Path dsConfig = root.resolve(dsConfigFile);
        Path loginConfig = root.resolve(loginConfigFile);

        Path rxDir = configurationContext.getRootDir();
        if (!Files.exists(rxDir.resolve("AppServer")))
            return;

        if (jbossNotExists(root)) return;

        try {
            List<IPSJndiDatasource> datasources = loadRxDatasources(dsConfig.toFile(),loginConfig.toFile() , configurationContext.getEncKey());
            configurationContext.getConfig().setDatasources(datasources);

            configurationContext.getConfig().setDatasourceResolver(getDataSourceResolver(root));
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }




    @Override
    public void save(DefaultConfigurationContextImpl configurationContext) {
        /*
        Path root = configurationContext.getRootDir();
        if (jbossNotExists(root)) return;
        try {
            saveRxDatasources(root.resolve(dsConfigFile).toFile(), root.resolve(loginConfigFile).toFile(), configurationContext.getConfig().getDatasources(),configurationContext.getEncKey());
            saveDatasourceResolver(root,configurationContext.getConfig().getDatasourceResolver());

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

         */
    }

    private boolean jbossNotExists(Path root) {
        Path dsConfig = root.resolve(dsConfigFile);
        Path loginConfig = root.resolve(loginConfigFile);
        if (!Files.exists(dsConfig) || !Files.exists(loginConfig))
        {
            ms_log.debug("Debug cannot find jboss config skipping");
            return true;
        }
        return false;
    }

    /**
     * Load all JNDI datasources from the specified file.  Any settings
     * configured that are not supported by the {@link IPSJndiDatasource} class
     * are loaded and preserved when the datasource is saved
     * (see {@link #saveRxDatasources(File, File, List, String)}.
     *
     * @param dsFile The file from which the datasources should be loaded,
     * identified by . May not be <code>null</code>
     * and must be a file conforming to the JBoss "jboss-ds_1_5.dtd" DTD.
     *
     * @param loginCfgFile The file from which encrypted credentials are
     * loaded, identified by the .  May not be
     * <code>null</code> and must conform to the JBoss "security_config.dtd" DTD.
     *
     * @param secretKey The key to use when decrypting passwords, may not be
     * <code>null</code> or empty.
     *
     * @return A list of datasources, never <code>null</code>, may be empty.
     *
     * @throws IOException If any errors occur reading from the files.
     * @throws SAXException If either document is malformed.
     * @throws PSInvalidXmlException If either document does not conform to the
     * expected format.
     * @throws PSMissingApplicationPolicyException If a datasource specfies a
     * security domain name that cannot be located in the supplied
     * <code>logingCfgFile</code>
     */
    private   List<IPSJndiDatasource> loadRxDatasources(File dsFile,
                                                        File loginCfgFile, String secretKey)
            throws IOException, SAXException, PSInvalidXmlException,
            PSMissingApplicationPolicyException
    {
        if (dsFile == null || !dsFile.exists())
            throw new IllegalArgumentException(
                    "dsFile may not be null and must exist");

        if (StringUtils.isBlank(secretKey))
            throw new IllegalArgumentException(
                    "sharedKey may not be null or empty");


        List<IPSJndiDatasource> dsList = new ArrayList<IPSJndiDatasource>();

        try (
                FileInputStream dsIn = new FileInputStream(dsFile))
        {

            Document dsDoc = PSXmlDocumentBuilder.createXmlDocument(dsIn, false);

            NodeList dsNodes = dsDoc.getElementsByTagName("local-tx-datasource");

            int len = dsNodes.getLength();
            for (int i = 0; i < len; i++)
            {
                Element dsNode = (Element) dsNodes.item(i);
                PSJBossJndiDatasource ds = new PSJBossJndiDatasource(dsNode);
                this.updateCredentials(ds, loginCfgFile, secretKey);
                dsList.add(ds);
            }
        }
        return dsList;

    }

    /**
     * Load the specified security domain from the supplied cfg file.
     *
     * @param loginCfgFile The cfg file, assumed not <code>null</code>, to
     * exist, and to be in the expected format.
     * @param secDomain The name of the security domain for which the credentials
     * are to be loaded, assumed not <code>null</code> or empty.
     *
     * @return The credentials, may be <code>null</code> if a match is not found.
     *
     * @throws IOException If there are any errors reading from the file.
     * @throws SAXException If the document is malformed.
     * @throws PSInvalidXmlException
     */
    public static  PSSecureCredentials loadSecureCredentials(File loginCfgFile,
                                                             String secDomain) throws IOException, SAXException, PSInvalidXmlException
    {
        FileInputStream cfgIn = null;
        try
        {
            PSSecureCredentials creds = null;

            cfgIn = new FileInputStream(loginCfgFile);
            Document cfgDoc = PSXmlDocumentBuilder.createXmlDocument(cfgIn, false);
            NodeList nodes = cfgDoc.getElementsByTagName(
                    PSSecureCredentials.APP_POLICY_NODE_NAME);
            int len = nodes.getLength();
            for (int i = 0; i < len && creds == null; i++)
            {
                Element node = (Element) nodes.item(i);
                if (PSSecureCredentials.isMatch(node, secDomain))
                    creds = new PSSecureCredentials(node);
            }

            return creds;
        }
        finally
        {
            if (cfgIn != null)
            {
                try
                {
                    cfgIn.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }

    private void updateCredentials(PSJBossJndiDatasource ds, File loginCfgFile,  String secretKey) throws PSInvalidXmlException, IOException, SAXException, PSMissingApplicationPolicyException{
        if (loginCfgFile == null || !loginCfgFile.exists())
            throw new IllegalArgumentException(
                    "loginCfgFile may not be null and must exist");

        String secDomain = ds.getSecurityDomain();
        if (secDomain != null)
        {
            PSSecureCredentials creds = loadSecureCredentials(loginCfgFile,
                    secDomain);
            if (creds == null)
            {
                throw new PSMissingApplicationPolicyException(secDomain,
                        loginCfgFile.getAbsolutePath());
            }
            ds.setUserId(creds.getUserId());
            String pw = "";
            try{
                pw = PSEncryptor.getInstance().decrypt(creds.getPassword());
            }catch(PSEncryptionException e){
                pw = PSLegacyEncrypter.getInstance().decrypt(creds.getPassword(),
                        secretKey);
            }
            ds.setPassword(pw);

            // clear the security domain
            ds.setSecurityDomain(null);
        }


    }

    /**
     * Saves the supplied JNDI datasource configurations to the supplied files,
     * replacing any existing configurations. For existing datasources loaded by
     * {@link #saveRxDatasources(File, File, List, String)}, any settings that
     * were configured but not supported by the {@link IPSJndiDatasource} class
     * are saved intact. See that method for more detailed parameter information.
     * Note that {@link IPSJndiDatasource#setSecurityDomain(String)} will be
     * called on all supplied datasources.
     *
     * @param dsFile The file to which the datasources should be saved, may not
     * be <code>null</code>.
     * @param loginCfgFile The file to which encrypted credentials are saved, may
     * not be <code>null</code>.
     * @param datasources The list of datasources to saved, may not be
     * <code>null</code>, may be empty.
     * @param secretKey The key to use when decrypting passwords, may not be
     * <code>null</code> or empty.
     * @throws IOException If there is an error saving to the files.
     * @throws SAXException If there is an error reading an existing file.
     */
    private void saveRxDatasources(File dsFile, File loginCfgFile,
                                   List<IPSJndiDatasource> datasources, String secretKey) throws IOException,
            SAXException
    {
        if (dsFile == null || !dsFile.exists())
            throw new IllegalArgumentException(
                    "dsFile "+ dsFile+" may not be null and must exist");

        if (loginCfgFile == null || !loginCfgFile.exists())
            throw new IllegalArgumentException(
                    "loginCfgFile may not be null and must exist");

        if (StringUtils.isBlank(secretKey))
            throw new IllegalArgumentException(
                    "sharedKey may not be null or empty");

        if (datasources == null)
            throw new IllegalArgumentException("datasources may not be null");

        Document doc = PSXmlDocumentBuilder.createXmlDocument();
        Element root = doc.createElement("datasources");
        PSXmlDocumentBuilder.replaceRoot(doc, root);

        List<PSSecureCredentials> creds = new ArrayList<PSSecureCredentials>();
        for (IPSJndiDatasource ds : datasources)
        {
            PSJBossJndiDatasource jbossDs =null;
            jbossDs = (ds instanceof PSJBossJndiDatasource) ? (PSJBossJndiDatasource)ds : new PSJBossJndiDatasource(ds);


            if (!StringUtils.isBlank(ds.getUserId()))
            {
                String pw =  ds.getPassword();
                try{
                    pw = PSEncryptor.getInstance().encrypt(
                            ds.getPassword());
                } catch (PSEncryptionException e) {
                    ms_log.error("Error encrypting password: " + e.getMessage(),e);
                }

                PSSecureCredentials cred = new PSSecureCredentials(
                        ds.getName(), ds.getUserId(), pw );
                ds.setSecurityDomain(cred.getSecurityDomainName());

                creds.add(cred);
            }
            //todo convert to xml
            root.appendChild(jbossDs.toXml(doc));


        }

        // save the datasources, then the credentials

        try ( FileOutputStream out = new FileOutputStream(dsFile); )
        {
            PSXmlDocumentBuilder.write(doc, out);
            saveSecureCredentials(loginCfgFile, creds);
        }

    }
    /**
     * Saves the supplied list of credentials, replacing any application-policy
     * elements in the specified file that represent credentials for Rhythmyx
     * datasources.
     *
     * @param loginCfgFile The file, assumed not <code>null</code> and to exist
     * and to be in the expected format.
     * @param creds The credentials to save, assumed not <code>null</code>.
     *
     * @throws IOException If there are any errors reading from or writing to the
     * config file.
     * @throws SAXException If the file is malformed.
     */
    private  void saveSecureCredentials(File loginCfgFile,
                                        List<PSSecureCredentials> creds) throws IOException, SAXException
    {
        FileInputStream cfgIn = null;
        FileOutputStream cfgOut = null;
        try
        {
            // read in existing doc
            cfgIn = new FileInputStream(loginCfgFile);
            Document cfgDoc = PSXmlDocumentBuilder.createXmlDocument(cfgIn, false);
            Element root = cfgDoc.getDocumentElement();

            // remove all current credentials
            NodeList children = root.getElementsByTagName(
                    PSSecureCredentials.APP_POLICY_NODE_NAME);

            Map<String, Element> curMap = new HashMap<String, Element>();
            int idx = 0;
            Element node;
            while((node = (Element) children.item(idx++)) != null)
            {
                if (PSSecureCredentials.isSecureCredentials(node))
                    curMap.put(PSSecureCredentials.getName(node), node);
            }

            // append the new ones, replace existing,
            for (PSSecureCredentials cred : creds)
            {
                Element curNode = curMap.remove(cred.getSecurityDomainName());
                Element newNode = cred.toXml(cfgDoc);
                if (curNode == null)
                    root.appendChild(newNode);
                else
                    root.replaceChild(newNode, curNode);
            }

            // remove unused
            for (Element oldNode : curMap.values())
            {
                PSXmlDocumentBuilder.removeElement(oldNode);
            }


            cfgOut = new FileOutputStream(loginCfgFile);
            PSXmlDocumentBuilder.write(cfgDoc, cfgOut);

        }
        finally
        {
            if (cfgIn != null)
            {
                try
                {
                    cfgIn.close();
                }
                catch (IOException e)
                {
                }
            }

            if (cfgOut != null)
            {
                try
                {
                    cfgOut.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }

    public PSDatasourceResolver getDataSourceResolver(Path rxDir)
    {
        File configFile = rxDir.resolve(springConfigFile).toFile();

        PSSpringConfiguration springConfig;

        PSDatasourceResolver dsResolver = null;
        try
        {
            springConfig = new PSSpringConfiguration(
                    configFile);
            dsResolver =
                    (PSDatasourceResolver) springConfig.getBean(
                            DATASOURCE_RESOLVER_NAME);
        }
        catch (PSInvalidXmlException | IOException | SAXException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dsResolver;
    }

    public void saveDatasourceResolver(Path rxDir, IPSDatasourceResolver dsResolver)
    {
        File configFile = rxDir.resolve(springConfigFile).toFile();

        try
        {
            PSSpringConfiguration springConfig = new PSSpringConfiguration(
                    configFile);

            springConfig.setBean(dsResolver);
            springConfig.save();
        }
        catch (PSInvalidXmlException | IOException | SAXException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}

