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
package com.percussion.delivery.caching.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang.Validate;

/**
 * Helper utilities for Jaxb.
 * @author erikserating
 *
 */
public class PSJaxbUtils
{
    
    // Private constructor to prohibit instantiation.
    private PSJaxbUtils(){}
    
    /**
     * Takes an InputStream of an XML file and unmarshals it into the given
     * type.
     * 
     * @param stream an InputStream of an XML file.
     * @param type the class to be unmarshalled to, cannot be  <code>null</code>.
     * @param validate if true then validation will be performed against schema.
     * @return An object of the given type, which has been unmarshalled from
     * the XML file.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshall(InputStream stream, Class<T> type, boolean validate) throws Exception
    {
        Validate.notNull(stream, "stream");
        
        JAXBContext context = JAXBContext.newInstance(type);
        Unmarshaller u =  context.createUnmarshaller();
        
        if (validate)
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source source = new StreamSource(type.getResourceAsStream(type.getSimpleName() + ".xsd"));
            Schema schema = schemaFactory.newSchema(source);
            u.setSchema(schema);
        }
        return (T) u.unmarshal(stream);
    }
    
    /**
     * Takes an xml string and unmarshalls it into the given
     * type.
     * 
     * @param stream an InputStream of an XML file.
     * @param type the class to be unmarshalled to, cannot be  <code>null</code>.
     * @param validate if true then validation will be performed against schema.
     * @return An object of the given type, which has been unmarshalled from
     * the XML file.
     * @throws Exception
     */
    public static <T> T unmarshall(String input, Class<T> type, boolean validate) throws Exception
    {
        InputStream is = null;
        try
        {
            is = new ByteArrayInputStream(input.getBytes("UTF8"));
            return unmarshall(is, type, validate);
        }
        finally
        {
            if(is != null)
                is.close();
        }
    }
    
    /**
     * Marshalls a JAXB object into its xml string representation.
     * @param obj the jaxb object to marshall, cannot be <code>null</code>.
     * @param validate if true then validation will be performed against schema.
     * @return xml representation of the jaxb object. Never<code>null</code>.
     * @throws Exception
     */
    public static String marshall(Object obj,  boolean validate) throws Exception
    {
        if(obj == null)
            throw new IllegalArgumentException("obj cannot be null.");
        Class clazz = obj.getClass();
        JAXBContext context = JAXBContext.newInstance(clazz);
        Marshaller m = context.createMarshaller();
        StringWriter writer = new StringWriter();
        if (validate)
        {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source source = new StreamSource(clazz.getResourceAsStream(clazz.getSimpleName() + ".xsd"));
            Schema schema = schemaFactory.newSchema(source);
            m.setSchema(schema);
        }
        m.marshal(obj, writer);
        return writer.toString();
        
    }
}
