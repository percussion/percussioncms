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

package com.percussion.content;

import com.percussion.share.dao.PSSerializerUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.UnmarshalException;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Abstract root class for all generators. It provides methods to validate input
 * arguments (server URL and XML file), to load the XML file with the content to
 * be generated, and wrapper methods to generate and cleanup content. These last
 * two ones automatically loads XML content and call appropriate methods in the
 * implementing class. They also logs all errors. This class also provides
 * functionality to run a main method.
 * <p>
 * Note that this class has a different responsability than {@link PSGenerator
 * <T>}.
 * 
 * @author miltonpividori
 * 
 * @param <T> A JAXB class which represents the content to load from the XML
 *            file.
 */
public abstract class PSGenericContentGenerator<T>
{
    protected static Logger log = LogManager.getLogger(PSGenericContentGenerator.class);
    
    /**
     * The remote server URL.
     */
    protected String serverUrl;
    
    /**
     * The username to be used to authenticate against the server.
     */
    protected String username;
    
    /**
     * The password to be used to authenticate against the server.
     */
    protected String password;
    
    /**
     * An InputStream object where definition data will be read from.
     */
    protected InputStream xmlData;
    
    /**
     * Represents the root data element. All content to be generated is read
     * from here.
     */
    protected T content;
    
    /***
     * Represents the license id to be used for generating content. 
     */
    protected String licenseId;
    
    /**
     * 
     * @param serverUrl The server URL. Must not be <code>null</code> nor empty.
     * @param xmlData An InputStream object with the XML content which defines
     *            the data to be generated.
     * @param username The username to be used to authenticate against the
     *            server. May be <code>null</code> or empty.
     * @param password The password to be used to authenticate against the
     *            server. May be <code>null</code> or empty.
     * @param licenseId The license id to be used for this session. 
     */
    public PSGenericContentGenerator(String serverUrl, InputStream xmlData, String username, String password)
    {
        Validate.notEmpty(serverUrl);
        Validate.notNull(xmlData);
        
        this.serverUrl = serverUrl;
        this.xmlData = xmlData;
        this.username = username;
        this.password = password;
     
    }
    
    /**
     * 
     * @param serverUrl The server URL. Must not be <code>null</code> nor empty.
     * @param xmlData An InputStream object with the XML content which defines
     *            the data to be generated.
     * @param username The username to be used to authenticate against the
     *            server. May be <code>null</code> or empty.
     * @param password The password to be used to authenticate against the
     *            server. May be <code>null</code> or empty.
     * @param licenseId The license id to be used for this session. 
     */
    public PSGenericContentGenerator(String serverUrl, InputStream xmlData, String username, String password, String licenseId)
    {
        Validate.notEmpty(serverUrl);
        Validate.notNull(xmlData);
        
        this.serverUrl = serverUrl;
        this.xmlData = xmlData;
        this.username = username;
        this.password = password;
        this.licenseId = licenseId;
    }
    
    /**
     * 
     * @return An object which represents the data to be generated in the
     *         server. May be <code>null</code>. You can see
     *         {@link PSGenericContentGenerator#dataSuccessfullyLoaded()} to
     *         know if the data was successfully loaded.
     */
    public T getRootData()
    {
        return content;
    }

    /**
     * Return the result of the data loading process. It's intended for
     * testing purposes.
     * 
     * @return <code>true</code> if the data loading was successful. <code>false</code>
     * otherwise.
     */
    public boolean dataSuccessfullyLoaded()
    {
        return content != null;
    }

    /**
     * Loads the XML data to be generated in the server.
     * 
     * @return <code>true</code> if the loading process was successful.
     *         <code>false</code> otherwise.
     */
    protected boolean loadXmlData()
    {
        if (content != null)
            return true;
        
        log.info("Loading the XML content");
        
        try
        {
            content = PSSerializerUtils.unmarshalWithValidation(xmlData, getRootDataType());
            return true;
        }
        catch (FileNotFoundException e)
        {
            log.error("The XML file was not found");
            return false;
        }
        catch (UnmarshalException e)
        {
            log.error("Error when unmarshaling the XML file. Make sure it conforms " +
            		"with the XML schema: " + e.getLinkedException().getMessage());
            return false;
        }
        catch (Exception e)
        {
            log.error("Unknown error when reading the XML file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Utility method to run a "main" method from implementing classes. It takes all passed in arguments
     * from the command line, cleans up and generates contents according to the XML file given.
     * <p>
     * Note that when cleaning up, it only removes data defined in the XML file.
     * 
     * @param args An array with the server URL, username, password, and the path of the XML file. All this
     * data must be passed in the mentioned order. Although username and password are optional, they must
     * be specified when using the tool in this way. Must not be <code>null</code>.
     * @param generatorClass
     */
    @SuppressWarnings("rawtypes")
    protected static <K extends PSGenericContentGenerator> void runMainMethod(String[] args, Class<K> generatorClass)
    {
        Validate.notNull(args, "arguments must not be null");
        Validate.isTrue(args.length == 4, "some arguments were not specified");
        
        String url = args[0];
        String uid = args[1];
        String pw = args[2];
        String defFileName = args[3];
        
        Validate.notEmpty(url, "Server URL must be specified");
        Validate.notEmpty(defFileName, "XML file name must be specified");

        Constructor<K> ctor = null;
        try {
            ctor = generatorClass.getConstructor(String.class, InputStream.class, String.class, String.class);

            try(FileInputStream fs = new FileInputStream(defFileName)){
                PSGenericContentGenerator contentGenerator =
                    ctor.newInstance(url,fs , uid, pw);

                contentGenerator.cleanup();
                contentGenerator.generateContent();
            }

        } catch (IOException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
    }
    
    /**
     * Utility method to run a "main" method from implementing classes using
     * secure connection. It takes all passed in arguments from the command
     * line, cleans up and generates contents according to the XML file given.
     * <p>
     * Note that when cleaning up, it only removes data defined in the XML file.
     * 
     * @param args An array with the server URL, admin username, admin password,
     *            the path of the XML file, the secure URL and a boolean for
     *            allowing self signed certificate. All this data must be passed
     *            in the mentioned order. Although username and password are
     *            optional, they must be specified when using the tool in this
     *            way. Must not be <code>null</code>.
     * @param generatorClass
     */
    @SuppressWarnings("rawtypes")
    protected static <K extends PSGenericContentGenerator> void runMainMethodSecure(String[] args,
            Class<K> generatorClass)
    {
        Validate.notNull(args, "arguments must not be null");
        Validate.isTrue(args.length == 6, "some arguments were not specified");

        String url = args[0];
        String adminUser = args[1];
        String adminPassword = args[2];
        String xmlDefFileName = args[3];
        String secureUrl = args[4];
        Boolean allowSelfSignedCertificate = Boolean.parseBoolean(args[5]);

        Validate.notEmpty(url, "Server URL must be specified");
        Validate.notEmpty(xmlDefFileName, "XML file name must be specified");

        Constructor<K> ctor;
        try
        {
            ctor = generatorClass.getConstructor(String.class, String.class, Boolean.class, InputStream.class,
                    String.class, String.class);
            try(FileInputStream fi = new FileInputStream(xmlDefFileName)) {
                PSGenericContentGenerator contentGenerator = ctor.newInstance(url, secureUrl,
                        allowSelfSignedCertificate.booleanValue(), fi, adminUser,
                        adminPassword);

                contentGenerator.cleanup();
                contentGenerator.generateContent();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }    
    
    /**
     * Public method that loads the data source and calls the corresponding method
     * to cleanup content for all elements defined in it.
     */
    public void cleanup()
    {
        // Quit if the XML load failed
        if (!loadXmlData())
            return;
        
        log.info("Cleaning up content for all defined elements in the data source");
        
        try
        {
            cleanupAllContent();
        }
        catch (Exception e)
        {
            log.error("Error when cleaning up content", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Public method that loads the data source and calls the corresponding method
     * to generate content for all elements defined in it.
     */
    public void generateContent()
    {
        // Quit if the XML load failed
        if (!loadXmlData())
            return;
        
        log.info("Generating content for all defined elements in the data source");
        
        try
        {
            generateAllContent();
        }
        catch (Exception e)
        {
            log.error("Error when generating content", e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Cleans up all elements defined in the data source.
     */
    protected abstract void cleanupAllContent();
    
    /**
     * Generates all elements defined in the data source.
     */
    protected abstract void generateAllContent();
    
    /**
     * Returns the Class object of the root data object.
     * 
     * @return The Class object of the root data object. Should never
     * be <code>null</code>.
     */
    protected abstract Class<T> getRootDataType();
}
