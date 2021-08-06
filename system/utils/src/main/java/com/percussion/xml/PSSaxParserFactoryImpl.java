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
package com.percussion.xml;

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
            factory.setFeature("http://xml.org/sax/features/namespaces",true);

            return factory;
        } catch (Exception e) {
            log.warn(e.getMessage());
            log.debug(e.getMessage(),e);
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
            log.warn(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new SAXNotSupportedException(e.getMessage());
        }
    }

    @Override
    public void setNamespaceAware(boolean awareness) {
        try {
            factoryThreadLocal.get().setNamespaceAware(awareness);
        }catch(java.lang.UnsupportedOperationException e){
            log.warn(e.getMessage());
            log.debug(e.getMessage(),e);
        }
    }

    @Override
    public void setValidating(boolean validating)
    {
        try {
            factoryThreadLocal.get().setValidating(validating);
        }catch(java.lang.UnsupportedOperationException e){
            log.warn(e.getMessage());
            log.debug(e.getMessage(),e);
        }
    }

    @Override
    public void setXIncludeAware(boolean state) {
        try {
            factoryThreadLocal.get().setXIncludeAware(state);
        }catch(java.lang.UnsupportedOperationException e){
            log.warn(e.getMessage());
            log.debug(e.getMessage(),e);
        }
    }
}
