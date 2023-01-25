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
package com.percussion.xml;

import com.percussion.error.PSExceptionUtils;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.security.xml.PSXmlSecurityOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;



/**
 * @author dougrand
 *
 * Create a parser for use in Percussion. This parser has the entity resolver
 * set to an instance of our entity resolver. This class is currently
 * configured to work with Xerces.
 */
public class PSSaxParserFactoryImpl extends SAXParserFactory {

    private static final Logger log = LogManager.getLogger(PSSaxParserFactoryImpl.class);

    private static final ThreadLocal<SAXParserFactory> factoryThreadLocal = ThreadLocal.withInitial(() -> {
        try {
            SAXParserFactory factory = PSSecureXMLUtils.getSecuredSaxParserFactory(
                    "org.apache.xerces.jaxp.SAXParserFactoryImpl", null,
                    new PSXmlSecurityOptions(
                            true,
                            true,
                            true,
                            false,
                            true,
                            false
                    ));
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://xml.org/sax/features/namespaces",true);
            factory.setFeature("http://xml.org/sax/features/namespace-prefixes",false);

            return factory;
        } catch (Exception e) {
            log.warn(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            return null;
        }
    });


    @Override
    public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory parserFactory = factoryThreadLocal.get();

        return parserFactory.newSAXParser();

    }

    @Override
    public void setFeature(String name, boolean value) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        try {
            factoryThreadLocal.get().setFeature(name, value);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e1 ) {
            log.warn(e1.getMessage());
            log.debug(e1.getMessage(),e1);
        }
    }

    @Override
    public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        try {
            return factoryThreadLocal.get().getFeature(name);
        } catch (SAXException e) {
            log.warn(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            throw new SAXNotSupportedException(e.getMessage());
        }
    }

    @Override
    public void setNamespaceAware(boolean awareness) {
        try {
            factoryThreadLocal.get().setNamespaceAware(awareness);
        }catch(java.lang.UnsupportedOperationException e){
            log.warn(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    @Override
    public void setValidating(boolean validating)
    {
        try {
            factoryThreadLocal.get().setValidating(validating);
        }catch(java.lang.UnsupportedOperationException e){
            log.warn(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }

    @Override
    public void setXIncludeAware(boolean state) {
        try {
            factoryThreadLocal.get().setXIncludeAware(state);
        }catch(java.lang.UnsupportedOperationException e){
            log.warn(PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
        }
    }
}
