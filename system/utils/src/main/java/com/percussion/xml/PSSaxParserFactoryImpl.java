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
package com.percussion.xml;

import com.percussion.utils.xml.PSEntityResolver;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * @author dougrand
 *
 * Create a parser for use in Rhythmyx. This parser has the entity resolver
 * set to an instance of our entity resolver. This class is currently
 * configured to work with Xerces.
 */
public class PSSaxParserFactoryImpl extends SAXParserFactory {


    private static ThreadLocal<SAXParserFactory> factoryThreadLocal = ThreadLocal.withInitial(() -> {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance("org.apache.xerces.jaxp.SAXParserFactoryImpl", null);
            factory.setNamespaceAware(true);
            factory.setFeature("http://xml.org/sax/features/namespaces",true);

            return factory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });


    @Override
    public SAXParser newSAXParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory parserFactory = factoryThreadLocal.get();
        SAXParser parser = parserFactory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setEntityResolver(PSEntityResolver.getInstance());
        return parser;
    }

    @Override
    public void setFeature(String name, boolean value) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        try {
            factoryThreadLocal.get().setFeature(name, value);
        } catch (SAXNotRecognizedException | SAXNotSupportedException e1 ) {
            throw e1;
        } catch (SAXException e) {
            throw new ParserConfigurationException("Failed to set feature name="+name+" value=");
        }
    }

    @Override
    public boolean getFeature(String name) throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException {
        try {
            return factoryThreadLocal.get().getFeature(name);
        } catch (SAXException e) {
            throw new ParserConfigurationException("Failed to get feature name="+name);
        }
    }

    @Override
    public void setNamespaceAware(boolean awareness) {
        factoryThreadLocal.get().setNamespaceAware(awareness);
    }

    @Override
    public void setValidating(boolean validating)
    {
        factoryThreadLocal.get().setValidating(validating);
    }
}
