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
package com.percussion.user.service.impl;

import com.percussion.share.dao.PSFileDataRepository;
import com.percussion.share.dao.PSXmlFileDataRepository;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.user.data.PSLdapConfig;
import com.percussion.user.data.PSLdapConfig.PSLdapServer;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Loads and contains the Directory service configuration.
 * <p>
 * The file is located here: 
 * <pre>
 * rxconfig/LDAP/ldapserver.xml
 * </pre>
 * 
 * @see #getData()
 * @see #poll()
 * @author adamgent
 *
 */
@Component("directoryServiceConfig")
@Lazy
public class PSDirectoryServiceConfig 
    extends PSXmlFileDataRepository<PSDirectoryServiceConfig.LdapConfigData, PSLdapConfig>
{

    private String configFileName = "ldapserver.xml";
    
    public PSDirectoryServiceConfig()
    {
        super(PSLdapConfig.class);
    }

    /**
     * Holds the config.
     * @author adamgent
     *
     */
    public static class LdapConfigData {
        private PSLdapConfig ldapConfig;

        /**
         * The object representation of the configuration file.
         * @return maybe <code>null</code> if no configuration file is found.
         */
        public PSLdapConfig getLdapConfig()
        {
            return ldapConfig;
        }

        public void setLdapConfig(PSLdapConfig ldapConfig)
        {
            this.ldapConfig = ldapConfig;
        }
        
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected LdapConfigData update(Set<PSFileDataRepository.PSFileEntry> files)
            throws IOException, PSValidationException, PSXmlFileDataRepositoryException {
        LdapConfigData data = new LdapConfigData();
        if ( ! files.isEmpty() ) {
            PSLdapConfig config = fileToObject(files.iterator().next());
            PSLdapServer server = config.getServer();
            if (server != null)
            {
                PSBeanValidationUtils.validate(server).throwIfInvalid();
            }
            data.setLdapConfig(config);
        }
        return data;
    }

    @Override
    protected Collection<File> getFiles() throws IOException
    {
        File f = getLdapConfigFile();
        Collection<File> rvalue;
        if (f.isFile()) {
            rvalue = Collections.singleton(f);
        }
        else {
            rvalue = Collections.emptySet();
        }
        return rvalue;
    }

    private File getLdapConfigFile()
    {
        
        return  new File(getRepositoryDirectory(), configFileName);
    }

    public void setConfigFileName(String configFileName)
    {
        this.configFileName = configFileName;
    }

    /**
     * Clears the password from the config file.
     * @throws PSXmlFileDataRepositoryException
     */
    public void clearPassword() throws PSXmlFileDataRepositoryException {
        try
        {
            clearPassword(getLdapConfigFile());
        }
        catch (FileNotFoundException e)
        {
            throw new PSXmlFileDataRepositoryException("Error reading file: ", e);
        }
        catch (XMLStreamException | DocumentException e)
        {
            throw new PSXmlFileDataRepositoryException("XML error clearing password:", e);
        }
        catch (IOException e)
        {
            throw new PSXmlFileDataRepositoryException("Error writing to XML to clear password: ", e);
        }
    }
    
    /**
     * Clears the password element of the LDAP config file.
     * If the file does not exist an error will <strong>NOT</strong>
     * be thrown. Instead <code>false</code> will be returned.
     * <p>
     * Before:
     * <pre>
     * &lt;LdapConfig&gt;
     * &lt;LdapServer&gt;
     * &lt;password&gt;Stuff&lt;/password&gt;
     * ...
     * </pre>
     * Will Now Be:
     * <pre>
     * &lt;LdapConfig&gt;
     * &lt;LdapServer&gt;
     * &lt;password&gt;&lt;/password&gt;
     * </pre>
     * @param file never <code>null</code> but may not point to a valid file.
     * @return <code>true</code> if the password was needed to be cleared.
     * @throws XMLStreamException
     * @throws IOException 
     * @throws DocumentException 
     */
    protected synchronized boolean clearPassword(File file) throws XMLStreamException, IOException, DocumentException {
        
        FileReader sr = null;
        
        if ( ! file.isFile() ) return false;
        Document doc;
        try
        {
            /*
             * We use the SAXON reader as it can handle XML Comments correctly.
             * STAX would be preferred but seems to not handle comments right now.
             */
            SAXReader reader = new SAXReader();
            sr = new FileReader(file);
            doc = reader.read(sr);
            List<?> es = doc.getRootElement().elements("LdapServer");
            if (es == null || es.isEmpty()) return false;
            Element e = (Element) es.get(0);
            es = e.elements("password");
            if (es == null || es.isEmpty()) return false;
            e = (Element) es.get(0);
            if ("".equals(e.getText())) return false;
            e.setText("");
        }
        finally
        {
            IOUtils.closeQuietly(sr);
        }
        notNull(doc, "Programming Error");

        try(FileOutputStream stream =new FileOutputStream(file) )
        {
            XMLWriter writer = new XMLWriter(stream);
            writer.write(doc);
        }
        
        return true;
    }
    
    @Value("${rxdeploydir}/rxconfig/LDAP")
    @Override
    public void setRepositoryDirectory(String widgetsRepositoryDirectory)
    {
        super.setRepositoryDirectory(widgetsRepositoryDirectory);
    }
}

