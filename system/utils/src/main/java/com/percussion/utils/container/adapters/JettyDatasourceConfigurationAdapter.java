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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.utils.container.adapters;

import com.percussion.error.PSExceptionUtils;
import com.percussion.util.PSProperties;
import com.percussion.utils.container.DefaultConfigurationContextImpl;
import com.percussion.utils.container.IPSConfigurationAdapter;
import com.percussion.utils.container.IPSJdbcJettyDbmsDefConstants;
import com.percussion.utils.container.IPSJndiDatasource;
import com.percussion.utils.container.PSJndiDatasourceImpl;
import com.percussion.utils.container.PSStaticContainerUtils;
import com.percussion.utils.container.config.ContainerConfig;
import com.percussion.utils.container.config.model.impl.BaseContainerUtils;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.jdbc.IPSDatasourceResolver;
import com.percussion.utils.jdbc.PSDatasourceResolver;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JettyDatasourceConfigurationAdapter implements IPSConfigurationAdapter<DefaultConfigurationContextImpl> {

    public static final Logger ms_log = LogManager.getLogger(JettyDatasourceConfigurationAdapter.class);


    private Path dsPropertiesFile = Paths.get("jetty","base","etc","perc-ds.properties");
    private Path dsXmlFile = Paths.get("jetty","base","etc","perc-ds.xml");

    private static String dsTemplate = null;
    private static final int DEFAULT_IDLE_TIMEOUT=30;

     static  {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream(); InputStream is = JettyDatasourceConfigurationAdapter.class.getClassLoader().getResourceAsStream("com/percussion/utils/container/jetty/jetty-ds-template.xml")) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            dsTemplate = result.toString("UTF-8").replaceAll("\\r\\n", "\n").replaceAll("\\r", "\n");
        } catch (IOException e) {
            ms_log.error("Cannot load jetty-ds-template.xml from classpath", e);
        }
    }

    public Class<? extends ContainerConfig> getConfigClass()
    {
        return BaseContainerUtils.class;
    }

    private boolean enabled = true;

    @Override
    public void load(DefaultConfigurationContextImpl configurationContext) {
        Path rxDir = configurationContext.getRootDir();
        BaseContainerUtils containerUtils = configurationContext.getConfig();

        Properties properties = loadProperties(rxDir.resolve(dsPropertiesFile));

        List<IPSJndiDatasource> dataSources = containerUtils.getDatasources();

        loadRxDatasources(rxDir.resolve(dsXmlFile).toFile(), configurationContext.getEncKey(), properties, dataSources);
        if (dataSources!=null && !dataSources.isEmpty())
            containerUtils.setDatasources(dataSources);
        PSDatasourceResolver resolver = getDataSourceResolver(rxDir);

        if(resolver.getRepositoryDatasource()!=null)
            containerUtils.setDatasourceResolver(resolver);

    }



    @Override
    public synchronized void save(DefaultConfigurationContextImpl configurationContext) {
        BaseContainerUtils containerUtils = configurationContext.getConfig();

        Path rxDir = configurationContext.getRootDir();
        enabled = containerUtils.isEnabled();
        try {
            saveRxDatasources(rxDir.resolve(dsXmlFile).toFile(), rxDir.resolve(dsPropertiesFile), containerUtils.getDatasources(), configurationContext.getEncKey());
            saveDatasourceResolver(rxDir,containerUtils.getDatasourceResolver());


        } catch (IOException | SAXException e) {
            ms_log.error("Error saving jetty datasource file: {}" , PSExceptionUtils.getMessageForLog(e));
        }
    }


    public void loadRxDatasources(File dsFile, String secretKey, Properties props, List<IPSJndiDatasource> dsList)
           {

        synchronized (this) {
            List<IPSJndiDatasource> newDSList = new ArrayList<>();


            boolean needsEncryption = false;

            List<Integer> ids = getDsIdsFromProperties(props);
            for (Integer id : ids) {
                String prefix = IPSJdbcJettyDbmsDefConstants.JETTY_DS_PREFIX + "." + id + ".";
                String name = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_NAME_SUFFIX);
                if (name != null)
                    name = name.trim();

                String driverName = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_DRIVER_NAME_SUFFIX);
                if (driverName != null)
                    driverName = driverName.trim();

                String driverClassName = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_DRIVER_CLASS_SUFFIX);
                if (driverClassName != null)
                    driverClassName = driverClassName.trim();

                String server = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_SERVER_SUFFIX);
                if (server != null) {
                    server = server.trim();
                }

                String uid = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_UID_SUFFIX);
                if (uid != null) {
                    uid = uid.trim();
                }
                String pwd = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_PWD_SUFFIX);
                if (pwd != null)
                    pwd = pwd.trim();

                String encrypted = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_PWD_ENCRYPTED_SUFFIX);
                if (encrypted != null)
                    encrypted = encrypted.trim();

                String idleMs = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_IDLE_MS);
                if (idleMs != null)
                    idleMs = idleMs.trim();

                if (encrypted != null && encrypted.equalsIgnoreCase("Y")) {
                    try {
                        pwd = PSEncryptor.decryptString(PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR),pwd);

                    }catch(PSEncryptionException | java.lang.IllegalArgumentException e){
                        pwd = PSLegacyEncrypter.getInstance(
                                PathUtils.getRxPath().toAbsolutePath().toString().concat(
                                PSEncryptor.SECURE_DIR)
                        ).decrypt(pwd, PSLegacyEncrypter.getInstance(
                                PathUtils.getRxPath().toAbsolutePath().toString().concat(
                                        PSEncryptor.SECURE_DIR)).getPartOneKey(),null);
                    }
                } else {
                    needsEncryption = true;
                }

                IPSJndiDatasource ds = new PSJndiDatasourceImpl(name, driverName, driverClassName, server, uid, pwd);
                ds.setId(id);

                if (idleMs != null ) {
                    try {
                        if (NumberUtils.isNumber(idleMs)) {
                            int ms = Integer.parseInt(idleMs);
                            if (ms > 60000)
                                ds.setIdleTimeout(ms / 60000);
                        }
                    }catch (NumberFormatException nf){
                        ds.setIdleTimeout(DEFAULT_IDLE_TIMEOUT);
                    }
                }

                String max = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONNECTIONS_MAX);
                if (max != null)
                    max = max.trim();

                if (max != null && NumberUtils.isNumber(max))
                    ds.setMaxConnections(Integer.parseInt(max));

                String min = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONNECTIONS_MIN);
                if (min != null)
                    min = min.trim();

                if (min != null && NumberUtils.isNumber(min))
                    ds.setMinConnections(Integer.parseInt(min));

                String connectionTest = props.getProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONNECTIONTEST_SUFFIX);
                if (connectionTest != null)
                    connectionTest = connectionTest.trim();

                ds.setConnectionTestQuery(connectionTest);

                newDSList.add(ds);

                System.out.println("Loaded datasource "+ds.toString());
            }


            if (newDSList.size()>0)
            {
                dsList.clear();
                dsList.addAll(newDSList);
            }

        };
    }

    /**
     * @param props
     */
    private List<Integer> getDsIdsFromProperties(Properties props) {
        List<Integer> ids = new ArrayList<Integer>();

        String pattern = IPSJdbcJettyDbmsDefConstants.JETTY_DS_PREFIX + "\\.(\\d+)\\."
                + IPSJdbcJettyDbmsDefConstants.JETTY_NAME_SUFFIX;
        Pattern r = Pattern.compile(pattern);

        for (Map.Entry<Object, Object> propEntry : props.entrySet()) {
            Matcher m = r.matcher((String) propEntry.getKey());
            if (m.find()) {
                int index = Integer.parseInt(m.group(1));
                ids.add(index);

            }
        }
        return ids;
    }

    private synchronized void saveRxDatasources(File dsFile, Path propertyFile, List<IPSJndiDatasource> datasources, String secretKey) throws IOException, SAXException {

        if (datasources == null || datasources.size() == 0)
            throw new IllegalArgumentException("datasources may not be null");

        // update jetty-ds.xml
        // get list of indexes
        updateJettyDatasourceXml(dsFile,datasources);
        //

        // Load original file and merge in changes
        Properties props = this.loadProperties(propertyFile);

        HashSet<Object> keys = new HashSet<Object>(props.keySet());

        // Remove existing datasource entries but keep everything else

        keys.stream().filter(s -> s.toString()
                .startsWith(IPSJdbcJettyDbmsDefConstants.JETTY_DS_PREFIX))
                .forEach(s -> props.remove(s));


        File tempFile = PSStaticContainerUtils.getTempFile(propertyFile.toFile());
        boolean success = false;
        try(FileWriter fw = new FileWriter(tempFile)){
            try (BufferedWriter writer = new BufferedWriter(fw)) {

                for (IPSJndiDatasource datasource : datasources) {
                    int index = datasource.getId();

                    String prefix = IPSJdbcJettyDbmsDefConstants.JETTY_DS_PREFIX + "." + index + ".";

                    long idleMs = datasource.getIdleTimeout();
                    if (idleMs < 1000) {
                        idleMs = idleMs * 60 * 1000l;
                    }
                    props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_IDLE_MS, Long.toString(idleMs));

                    props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONNECTIONS_MAX, Integer.toString(datasource.getMaxConnections()));
                    props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONNECTIONS_MIN, Integer.toString(datasource.getMinConnections()));

                    props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_DRIVER_CLASS_SUFFIX, datasource.getDriverClassName());
                    props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_DRIVER_NAME_SUFFIX, datasource.getDriverName());

                    props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_SERVER_SUFFIX, datasource.getServer());
                    try {
                        String encPwd = PSEncryptor.encryptProperty(PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR),propertyFile.toAbsolutePath().toString(),IPSJdbcJettyDbmsDefConstants.JETTY_PWD_SUFFIX,datasource.getPassword());
                        props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_PWD_SUFFIX,
                                encPwd);
                        props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_PWD_ENCRYPTED_SUFFIX, "Y");
                    } catch (PSEncryptionException e) {
                        props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_PWD_SUFFIX,
                                datasource.getPassword());
                        props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_PWD_ENCRYPTED_SUFFIX, "N");
                    }


                    props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_UID_SUFFIX, datasource.getUserId());
                    if (StringUtils.isNotEmpty(datasource.getConnectionTestQuery()))
                        props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_CONNECTIONTEST_SUFFIX, datasource.getConnectionTestQuery());

                    if (datasource.getName() != null)
                        props.setProperty(prefix + IPSJdbcJettyDbmsDefConstants.JETTY_NAME_SUFFIX, datasource.getName());
                }
                // save properties to project root folder
                this.storeProperties(props, writer, propertyFile.toFile().getName());

                success = true;
            }
            } catch (IOException e) {
                ms_log.error("error saving properties file propertyFile {}",
                        PSExceptionUtils.getMessageForLog(e));
            ms_log.debug(e);
        }

        if (success) {
            //Make sure file is writeable.
            if(!Files.exists(propertyFile))
                propertyFile = Files.createFile(propertyFile);
            //Make sure file is writeable.
            propertyFile.toFile().setWritable(true);

            FileUtils.copyFile(tempFile, propertyFile.toFile());

        }

        Files.deleteIfExists(tempFile.toPath());
    }

    private boolean updateJettyDatasourceXml(List<IPSJndiDatasource> datasources, Document doc) throws IOException, SAXException {
        Map<String, IPSJndiDatasource> idNameMap = datasources.stream().collect(
                Collectors.toMap(IPSJndiDatasource::getName, Function.identity()));
        int maxId = datasources.stream().mapToInt(IPSJndiDatasource::getId).max().orElse(0);
        if (maxId < 0) maxId = 0;
        boolean updated = false;
        // load source doc

        Element root = doc.getDocumentElement();

        NodeList entries = doc.getElementsByTagName("New");

        for (int index = 0; index < entries.getLength(); index++) {
            String dsValue = null;
            int propIndex = -1;
            Node node = entries.item(index);
            Node className = node.getAttributes().getNamedItem("class");
            Node id = node.getAttributes().getNamedItem("id");
            if (className != null && id != null) {
                String idString = id.getTextContent();
                String idIndex = StringUtils.substringAfter(idString, IPSJdbcJettyDbmsDefConstants.JETTY_DS_PREFIX
                        + ".");

                if (idIndex != null && StringUtils.isNumeric(idIndex)) {
                    propIndex = Integer.parseInt(idIndex);
                }

                if (propIndex > maxId)
                    maxId = propIndex;

                String classValue = className.getNodeValue();
                if (classValue.equals("org.eclipse.jetty.plus.jndi.Resource")) {
                    NodeList childNodes = node.getChildNodes();
                    int argCount = 0;

                    for (int i = 0; i < childNodes.getLength(); i++) {
                        Node argNode = childNodes.item(i);
                        if (argNode.getNodeName().equals("Arg")) {
                            if (argCount++ == 1) {
                                ms_log.debug("Found datasource name arg={}", argNode.getTextContent());
                                dsValue = StringUtils.substringAfter(argNode.getTextContent(), "java:");

                                if (dsValue != null) {
                                    IPSJndiDatasource ds = idNameMap.get(dsValue);
                                    if (ds == null) {
                                        node.getParentNode().removeChild(node);
                                        ms_log.debug("Removing ds entry {}", dsValue);
                                        updated = true;
                                    } else {
                                        if (propIndex != ds.getId())
                                            ds.setId(propIndex);

                                        idNameMap.remove(dsValue);
                                        ms_log.debug("Found {}", dsValue);
                                    }

                                }
                                break;
                            }


                        }

                    }
                }
            }

        }


        if (idNameMap.size() > 0) updated = true;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        for (IPSJndiDatasource ds : idNameMap.values()) {
            maxId++;

            String newXml = MessageFormat.format(dsTemplate, String.valueOf(maxId), ds.getName());

            try (InputStream is = new ByteArrayInputStream(newXml.getBytes())) {
                Document xml = PSXmlDocumentBuilder.createXmlDocument(is, false);

                Node entryNode = xml.getElementsByTagName("New").item(0);

                PSXmlDocumentBuilder.copyTree(doc, root, entryNode);
                String d = PSXmlDocumentBuilder.toString(doc);
                ms_log.debug("New items {}", d);
                idNameMap.get(ds.getName()).setId(maxId);
            }

        }
        return updated;
    }

    private Document createNewDatasourceXml() throws IOException, SAXException {
        Document doc;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        String newXml = dsTemplate;

        try (InputStream is = new ByteArrayInputStream(newXml.getBytes())) {
            doc = PSXmlDocumentBuilder.createXmlDocument(is, false);

            Node docElement = doc.getDocumentElement();
            NodeList children = docElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                docElement.removeChild(children.item(i));
            }

        }
        return doc;
    }

    private boolean updateJettyDatasourceXml(File file, List<IPSJndiDatasource> datasources) throws IOException, SAXException {
        Document doc = null;
        boolean updated = false;

        if (!file.exists() || file.length() == 0 ) {
            doc = createNewDatasourceXml();
            updated = updateJettyDatasourceXml(datasources, doc);
        } else {
            try (InputStream sourceIn = new FileInputStream(file)) {

                doc = PSXmlDocumentBuilder.createXmlDocument(sourceIn, true);
                //If XML doesn't have a root Node or empty Root Node, then create new file
                //May happen in case of corrupted file
                if(doc == null || doc.getDocumentElement() == null ||
                        doc.getDocumentElement().getChildNodes() == null
                        || doc.getDocumentElement().getChildNodes().getLength() == 0){
                    doc = createNewDatasourceXml();
                }
                updated = updateJettyDatasourceXml(datasources, doc);
            } catch (IOException | SAXException e) {
                ms_log.error("Could not parse or update jetty datasource configuration, so recreating {} Error: {}", file.getAbsolutePath(), e.getMessage());
                ms_log.debug(e.getMessage(),e);
                //In case File is corrupted, create a new file.
                doc = createNewDatasourceXml();
                updated = updateJettyDatasourceXml(datasources, doc);
            }

        }

        if (updated) {
            //Make sure the file is writeable before we try to write to it.
            file.setWritable(true);

            try (OutputStream out = new FileOutputStream(file)) {
                PSXmlDocumentBuilder.write(doc, out);
            } catch (IOException e) {
                ms_log.error("Cound not save jetty datasource configuration jetty-ds.xml", e);
                throw e;
            }

        }

        return true;

    }


    private PSDatasourceResolver getDataSourceResolver(Path rxDir)
    {
        PSDatasourceResolver resolver = new PSDatasourceResolver();
        Properties props = loadProperties(rxDir.resolve(dsPropertiesFile));
        resolver.setProperties(props);

        return resolver;
    }


    private void saveDatasourceResolver(Path rxDir, IPSDatasourceResolver resolver)
    {
        File propertiesFile = rxDir.resolve(dsPropertiesFile).toFile();
        // get properties from filesystem.
        Properties props = PSStaticContainerUtils.getProperties(rxDir.resolve(dsPropertiesFile).toFile());
        // Remove existing connection entries but keep everything else
        Set<Object> keys = new HashSet<Object>(props.keySet());

        keys.stream().filter(p -> p.toString().startsWith(IPSJdbcJettyDbmsDefConstants.JETTY_CONN_PREFIX))
                .forEach(p -> props.remove(p));

        props.putAll(resolver.getProperties());

        saveProperties(props, propertiesFile);
    }


    private void storeProperties(Properties props, BufferedWriter writer, String fileName) throws IOException {
        props.store(writer, null);
    }

    private Properties loadProperties(Path filePath) {
        PSProperties props = new PSProperties();
        if (Files.exists(filePath)) {
            try (InputStream is = Files.newInputStream(filePath)) {
                props.load(is);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return props;
    }

    private synchronized void saveProperties(Properties  props, File dbPropertiesFile)
    {

        try {
            PSStaticContainerUtils.saveProperties(props, dbPropertiesFile);
        }
        catch (IOException e)
        {
            ms_log.error("Unable to save properties file {}", dbPropertiesFile.getAbsolutePath());
        }
    }

}

