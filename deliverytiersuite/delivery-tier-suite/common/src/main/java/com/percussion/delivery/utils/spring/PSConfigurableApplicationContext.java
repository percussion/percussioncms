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
package com.percussion.delivery.utils.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

/**
 * Provides a configurable Context loader that can be used as the contextClass param
 * in a web.xml.  Will default to WEB-INF/beans.xml if it doesn't find a context 
 * in either {$catalina.base}/conf/perc/perc-context.properties or in 
 * WEB-INF/perc-context.properties.
 * 
 * The perc/conf location will always override what is defined in WEB-INF.
 * 
 *  example perc-context.properties
 *  ##################################### 
 *  # Specifies the context location to use.  May be over-ridden by placing this 
 *  # properties file into the {$catalina.base}/conf/perc/ folder.
 *  #
 *  # RDBMS - Hibernate Application Context
 *  contextLocation=/WEB-INF/beans.xml
 *  #
 *  # NOSQL - MongoDB Application Context
 *  #contextLocation=/WEB-INF/beans_mongodb.xml
 *  #############################################
 *
 */
@Configuration
public class PSConfigurableApplicationContext extends XmlWebApplicationContext
{

    private static final String DEFAULT_CONTEXT_CONFIG = "/WEB-INF/beans.xml";
    private static final String PERC_CONTEXT_PROPS = "/WEB-INF/perc-context.properties";
    private static final String PERC_CONTEXT_PROPS_USER = "/conf/perc/perc-context.properties";
    private static final String PERC_CONTEXT_LOC = "contextLocation";
    private static final String CATALINA_BASE = "catalina.base";

    //Log4j2 may not be present when this is run - so use java basic logger
    private static final Logger log = LogManager.getLogger(PSConfigurableApplicationContext.class);

    PSConfigurableApplicationContext(){
        super();
    }

    /**
     * Initialize the bean definition reader used for loading the bean
     * definitions of this context. Default implementation is empty.
     * <p>Can be overridden in subclasses, e.g. for turning off XML validation
     * or using a different XmlBeanDefinitionParser implementation.
     *
     * @param beanDefinitionReader the bean definition reader used by this context
     * @see XmlBeanDefinitionReader#setValidationMode
     * @see XmlBeanDefinitionReader#setDocumentReaderClass
     */
    @Override
    protected void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
        beanDefinitionReader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_NONE);
        beanDefinitionReader.setValidating(false);

        super.initBeanDefinitionReader(beanDefinitionReader);
    }

    /***
     * A convenience method for unit tests to use when testing multiple 
     * contexts.  This should be called prior to loading the context in 
     * a given test.
     * 
     * @param location The location to be set. For example: /WEB-INF/beans_mongodb.xml
     * @throws IOException 
     * @throws URISyntaxException 
     */
    public static void switchContextLocation(String location) throws IOException, URISyntaxException{
        
        Properties p = new Properties();
        try (InputStream rs = PSConfigurableApplicationContext.class.getResourceAsStream(PERC_CONTEXT_PROPS)){
            p.load(rs);
            p.setProperty(PERC_CONTEXT_LOC, location);
        }
    
        URL url = PSConfigurableApplicationContext.class.getResource(PERC_CONTEXT_PROPS);
        try (OutputStream fs = new FileOutputStream(new File(url.toURI()))) {
            p.store(fs,null);
        }
    }

    @Override
    public String[] getConfigLocations() {
        return getDefaultConfigLocations();
    }


    @Override
    protected String[] getDefaultConfigLocations() {

        Properties props = new Properties();
        String tomcatBase=null;
        String targetContext = null;

        //Get the properties from the server perc/conf dir
        tomcatBase = System.getProperty(CATALINA_BASE);

        //User configured properties
        try(FileInputStream fs = new FileInputStream(tomcatBase + PERC_CONTEXT_PROPS_USER)){

            if(tomcatBase != null){
                props.load(fs);
            }
                
            targetContext = props.getProperty(PERC_CONTEXT_LOC, null);
        } catch (IOException e) {
            log.info(e.getMessage());
        }

        if(targetContext == null){
            //WEB-IF properties
            try(InputStream in = Objects.requireNonNull(this.getServletContext()).getResourceAsStream(PERC_CONTEXT_PROPS))
            {
                    props.load(in);
                    targetContext = props.getProperty(PERC_CONTEXT_LOC,null);
                    log.info("Selected {} from {}",targetContext , PERC_CONTEXT_LOC );
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        }
        
        //Fall back to defaults if none of the properties are found.
        if(targetContext == null || targetContext.equals("")){
            log.info("Unable to find a configured ContextLocation - selecting default: {}",
                    DEFAULT_CONTEXT_CONFIG);
            targetContext = DEFAULT_CONTEXT_CONFIG;
        }

        return new String[]{targetContext};
    }


}
