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

package com.percussion.delivery.caching.impl;

import com.percussion.delivery.caching.IPSCacheManager;
import com.percussion.delivery.caching.IPSCacheProviderPlugin;
import com.percussion.delivery.caching.PSCacheManagerException;
import com.percussion.delivery.caching.data.PSCacheConfig;
import com.percussion.delivery.caching.data.PSCacheProvider;
import com.percussion.delivery.caching.data.PSCacheRegion;
import com.percussion.delivery.caching.data.PSInvalidateRequest;
import com.percussion.delivery.caching.utils.PSJaxbUtils;
import com.percussion.delivery.utils.security.PSSecureProperty;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.UnmarshalException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Provides methods to invalidate the cache according to the given
 * parameters. It handles different plugins according to the configuration.
 * 
 * @author miltonpividori
 *
 */
public class PSCacheManager implements IPSCacheManager
{
    /**
     * Log for this class.
     */
    private Log log = LogFactory.getLog(getClass());
    
    /**
     * Cache configuration content. It's quietly closed once
     * the cache manager is started up.
     */
    private InputStream cacheConfigStream;
    
    /**
     * Cache configuration object.
     */
    private PSCacheConfig cacheConfig;
    
    /**
     * The Spring application context;
     */
    private ApplicationContext appContext;
    
    /**
     * Map with all provider plugins indexed by class name (fully
     * qualified).
     */
    private Map<String, IPSCacheProviderPlugin> providersMap =
        new HashMap<>();
    
    /**
     * Array of all provider properties who's values should be encrypted within the
     * cache config xml file.
     */
    private static final String[] securedPropNames = new String[]{"password"};
    
    /**
     * Loads the default configuration file, specified in Spring.
     * 
     * @throws Exception If there is any error reading while getting
     * an InputStream object from the Spring's Resource one.
     */
    public PSCacheManager(Resource externalCacheConfigResource, Resource localCacheConfigResource) throws Exception
    {
        Validate.notNull(externalCacheConfigResource);
        Validate.notNull(localCacheConfigResource);        
        Resource resource = externalCacheConfigResource.exists() 
           ? externalCacheConfigResource : localCacheConfigResource;
        try
        {            
            if(externalCacheConfigResource.exists())                
            {
               secureConfig(resource); 
            }
            log.info("Loading cache config: " + resource.getFile().getAbsolutePath());
            this.cacheConfigStream = resource.getInputStream();            
        }
        catch (FileNotFoundException e)
        {
            log.error("Configuration file not found", e);
            throw e;
        }
        catch (Exception e)
        {
            log.error("Error in reading the configuration file", e);
            throw e;
        }
    }
    
    /**
     * Loads the specified cache configuration.
     * 
     * @param cacheConfigStream An InputStream object containing the
     * Cache configuration content. Never <code>null</code>.
     */
    public PSCacheManager(InputStream cacheConfigStream)
    {
        this.cacheConfigStream = cacheConfigStream;
    }
    
    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
//    public void setApplicationContext(ApplicationContext appContext) throws BeansException
//    {
//        this.appContext = appContext;
//    }
    
    /**
     * @return an unmodifiable map of the providers map
     */
    public Map<String, IPSCacheProviderPlugin> getProvidersMap()
    {
        return Collections.unmodifiableMap(providersMap);
    }

    /**
     * Initializes the cache manager. It loads the configuration file,
     * creates all specified providers and then initializes each one.
     */
    public void init()
    {
        loadConfigFile(this.cacheConfigStream);
        
        // Close InputStream of the configuration file
        IOUtils.closeQuietly(cacheConfigStream);
        
        // Load all providers
        String fullProviderName;
        Class tempClass = null;
        
        for (PSCacheRegion cacheRegion : cacheConfig.getCacheRegion())
        {
            fullProviderName = cacheRegion.getProvider().getPlugin();
            
            // Do not continue if the plugin is already loaded
            if (providersMap.containsKey(fullProviderName))
                continue;
            
            try
            {
                log.info("Loading provider class: " + fullProviderName);
                
                tempClass = Class.forName(fullProviderName);
            }
            catch (ClassNotFoundException e)
            {
                log.error("Provider class not found: " + fullProviderName, e);
                continue;
            }
            
            log.info("Verifying the provider class");
            if (tempClass != null && !IPSCacheProviderPlugin.class.isAssignableFrom(tempClass))
            {
                log.error("The provider class must implement " +
                        IPSCacheProviderPlugin.class.getSimpleName() +
                        " interface: " + fullProviderName);
                
                continue;
            }
            
            log.info("Instantiating provider plugin");
            IPSCacheProviderPlugin providerPluginInstance = null;
            try
            {
                providerPluginInstance = (IPSCacheProviderPlugin) tempClass.newInstance();
            }
            catch (InstantiationException e)
            {
                log.error("Error in instanciating the provider plugin. " +
                        "Make sure the nullary constructor is present and the " +
                        "class is not abstract: " + fullProviderName, e);
                continue;
            }
            catch (IllegalAccessException e)
            {
                log.error("Error in instanciating the provider plugin. " +
                		"Make sure the nullary constructor is accessible: " +
                		fullProviderName, e);
                continue;
            }
            catch (Exception e)
            {
                log.error("Unknown error in instantiating the provider plugin: " +
                        fullProviderName, e);
                continue;
            }
            
            log.info("Initializing provider plugin");
            try
            {
                providerPluginInstance.initialize(cacheConfig, appContext);
            }
            catch (Exception e)
            {
                log.error("Error in initializing provider plugin: " +
                        fullProviderName, e);
                continue;
            }
            
            providersMap.put(fullProviderName, providerPluginInstance);
            
            log.info("Provider plugin loaded successfully: " + fullProviderName);
        }
    }
    
    /**
     * Secures the config by encrypting passwords.
     * @param resource assumed not <code>null</code>.
     * @throws Exception 
     */
    private void secureConfig(Resource resource) throws Exception
    {
        File config = resource.getFile();
        PSCacheConfig configObj = null;
        InputStream is = null;
        try
        {
            is = new FileInputStream(config);
            configObj = PSJaxbUtils.unmarshall(is, PSCacheConfig.class, false);    
        }
        finally
        {
            if(is != null)
                is.close();
        }
        if(configObj != null)
        {
            boolean modified = false;
            for(PSCacheRegion region : configObj.getCacheRegion())
            {
                PSCacheProvider provider = region.getProvider();
                for(String key : securedPropNames)
                {
                    String val = provider.getProperty(key);
                    if(StringUtils.isNotBlank(val) && !PSSecureProperty.isValueClouded(val))
                    {
                        provider.getProperties().put(key, PSSecureProperty.getClouded(val, null));
                        modified = true;
                    }
                }
            }
            if(modified)
            {
                
                PrintStream os = null;
                try
                {
                    String xml = PSJaxbUtils.marshall(configObj, false);
                    os = new PrintStream(config);
                    os.print(format(xml));
                    os.flush();
                }
                finally
                {
                    if(os != null)
                        os.close();
                }
            }
        }
    }
    
    /**
     * Format the xml so it does not just become one long unreadable string
     * 
     * @param unformattedXml assumed not blank and valid xml.
     * @return formatted xml string
     */
    @SuppressWarnings("deprecation")
    private String format(String unformattedXml)
    {
        try
        {
            final Document document = parseXmlFile(unformattedXml);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse xml file into document object to allow for serializer cleanup.
     * @param in assumed not <code>null</code>.
     * @return parsed document, never <code>null</code>.
     */
    private Document parseXmlFile(String in)
    {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        }
        catch (ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
        catch (SAXException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Retrieve the Cache configuration currently used by the cache manager.
     * 
     * @return cache config, never <code>null</code>.
     */   
    public PSCacheConfig getConfig()
    {
        return cacheConfig;
    }
    
    /**
     * Loads the configuration content specified by the parameter.
     * 
     * @param cacheConfigStream An InputStream with the configuration
     * file content. Must not be <code>null</code>.
     */
    private void loadConfigFile(InputStream cacheConfigStream)
    {
        Validate.notNull(cacheConfigStream);
        
        try
        {
            log.info("Loading cache manager configuration");
            
            this.cacheConfig = PSJaxbUtils.unmarshall(cacheConfigStream, PSCacheConfig.class, true);
            
            log.info("Cache manager configuration loaded successfully");
        }
        catch (FileNotFoundException e)
        {
            log.error("Configuration XML file was not found", e);
        }
        catch (UnmarshalException e)
        {
            String message = e.getMessage();
            
            if (e.getLinkedException() != null)
                message = e.getLinkedException().getMessage();
            
            log.error("Error unmarshaling the configuration XML file. Make sure it conforms " +
                    "with the XML schema: " + message, e);
        }
        catch (Exception e)
        {
            log.error("Unknown error reading the configuration XML file: " + e.getMessage(), e);
        }
    }   
    
    /*
     * (non-Javadoc)
     * @see com.percussion.delivery.caching.IPSCacheManager#invalidate(com.percussion.delivery.caching.data.PSInvalidateRequest)
     */
    public void invalidate(PSInvalidateRequest request)
    {
        log.info("Invalidation method called");
        
        // Verify that the region is specified in configuration
        if (cacheConfig.getRegion(request.getRegionName()) == null)
        {
            log.warn("The region specified in the request does not exist in the " +
            		"configuration");
            return;
        }
        
        // Verify that there is a provider for the given region
        String providerName = getProviderPluginByRegionName(request.getRegionName());
        
        if (providerName == null)
        {
            log.warn("No provider for that region name found");
            return;
        }
        
        IPSCacheProviderPlugin cachePlugin = null;
        if (providersMap.containsKey(providerName))
            cachePlugin = providersMap.get(providerName);
        else
        {
            log.warn("Provider plugin was not found (maybe it was not properly loaded): " +
                    providerName);
            return;
        }
        
        // Call invalidate in the provider plugin
        try
        {
            cachePlugin.invalidate(request);
            
            log.info("Invalidate call in provider plugin was successfull: " +
                    providerName);
        }
        catch (PSCacheManagerException e)
        {
            log.error("Error when running 'invalidate' in provider: " +
                    providerName, e);
        }
        catch (Exception e)
        {
            log.error("Unknown error when running 'invalidate' in provider: " +
                    providerName, e);
        }
    }
    
    /**
     * Gets the provider plugin class according to the region given.
     * 
     * @param regionName Region name where to get the provider from.
     * Must not be <code>null</code>.
     * @return A string with the provider plugin class. Maybe <code>null</code>
     * or empty.
     */
    private String getProviderPluginByRegionName(String regionName)
    {
        PSCacheRegion cacheRegion = cacheConfig.getRegion(regionName);
        
        if (cacheRegion != null)
            return cacheRegion.getProvider().getPlugin();
        
        return null;
    }

    
}
