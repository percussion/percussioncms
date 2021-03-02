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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.share.dao;

import net.sf.json.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.removeEnd;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Various serializing/marshalling static methods.
 * <p>
 * Also has utilities for data conversion and copying of data 
 * from one object to another.
 *  
 * @author adamgent
 *
 */
public class PSSerializerUtils
{

    /**
     * The standard marshalling of objects is currently done with JAXB.
     * @param <T> object type
     * @param object  never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static <T> String marshal(T object) {
        StringWriter sw = new StringWriter();
        try
        {

           PSJaxbContext.createMarshaller(object.getClass()).marshal(object, sw);
           return sw.toString();
        }
        catch (JAXBException e)
        {
           log.error("Unable to marshall JAXB object:" + object.toString(), e);
           return null;
        }
    }
    
    /**
     * See {@link #marshal(Object)}.
     * This should be used lightly as it does not have validation.
     * @param <T>
     * @param dataField never <code>null</code>.
     * @param type never <code>null</code>.
     * @return never <code>null</code>.
     */
    @SuppressWarnings("unchecked")
   public static <T> T unmarshal(String dataField, Class<T> type)
    {
        T object;
        StringReader reader = new StringReader(dataField);
        try
      {
         object = (T) PSJaxbContext.createUnmarshaller(type).unmarshal(reader);
         return object;
      }
      catch (JAXBException e)
      {
         log.error("Unable to unmarshall JAXB object:" + dataField);
         return null;
      }
        
    }
    
    
    /**
     * Unmarshals an XML stream into an Object validating against its schema.
     * <p>
     * The schema is assumed to be in the same java class package as the type parameter
     * with the same name but ending in <code>.xsd</code>
     * <p>
     * <b>Example:</b>
     * <p> 
     * <b>Class:</b> <code>com.percussion.Stuff.class</code><p><b>Schema:</b> <code>com.percussion.Stuff.xsd</code>
     * <p>
     * Note: This requires the schema file to be put into the jar when deployed. 
     * @param <T> type to unmarshal
     * @param stream never <code>null</code>.
     * @param type never <code>null</code>.
     * @return never <code>null</code>.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshalWithValidation(InputStream stream, Class<T> type) throws Exception
    {
        notNull(stream, "stream");
        notNull(type, "type");
        SchemaFactory schemaFactory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source source = new StreamSource(type.getResourceAsStream(type.getSimpleName() + ".xsd"));

        Schema schema = schemaFactory.newSchema(source);
        Unmarshaller u =  PSJaxbContext.createUnmarshaller(type);
        u.setSchema(schema);
        return (T) u.unmarshal(stream);
    }
    
    /**
     * Will convert an object into an XML representation but in JSON format.
     * This is useful for determining what the REST services JSON output of an object is.
     * <p>
     * This is <strong>not</strong> a object->JSON conversion but rather a
     * object->XML->JSON conversion.
     * 
     * @param <T> object type.
     * @param object
     * @return a JSON object representing an XML document.
     * @throws Exception Cannot marshal the object.
     */
    public static <T> String getJsonXmlFromObject(T object) throws Exception {
        StringWriter sw = new StringWriter();
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller m = context.createMarshaller();
        MappedNamespaceConvention c = new MappedNamespaceConvention();
        XMLStreamWriter xw  = new MappedXMLStreamWriter(c, sw);
        m.marshal(object, xw);
        return sw.getBuffer().toString();
    }
    
    public static <T> List<T> copyFullToSummaries(List<? extends T> froms, Class<T> type) {
        List<T> newList = new ArrayList<>();
        for(T from : froms) {
            T sum;
            try
            {
                sum = type.newInstance();
            }
            catch (InstantiationException e)
            {
                throw new RuntimeException(e);
            }
            catch (IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
            copyFullToSummary(from, sum);
            newList.add(sum);
        }
        return newList;
        
    }
    
    public static <T> void copyFullToSummary(T from, T to) {
        try
        {
            BeanUtils.copyProperties(to, from);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new RuntimeException(e);
        }
        
    }
    
    /**
     * Does a clone of an object using bean reflection.
     * <p>
     * The object should following the java bean standards and
     * have a empty constructor.
     * @param <T> Type of object.
     * @param from object to clone from. never <code>null</code>.
     * @return never <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T from) {
        String err = "Cannot clone bean";
        try
        {
            return (T) BeanUtils.cloneBean(from);
        }
        catch (Exception e)
        {
            throw new RuntimeException(err,e);
        }
    }
    
    /**
     * 
     * Parses a simple JSON string turning it into a native Java object.
     * Example valid JSON:
     * <pre>
     * "a" // string
     *  1 // number
     * { "a" : 1, "b" : 2 } // object
     * ['a','b'] //list
     * </pre>
     * <strong>Blank and empty strings will return null</strong>
     * <em>
     * This should be used for simple JSON values and not complex objects.
     * Use either JAXB or json-lib for complex values.
     * </em>
     * @param json either a JSON string, number, array or object.
     * @return either a list, number, string, map or <code>null</code>.
     * 
     */
    @SuppressWarnings("unchecked")
    public static Object getObjectFromJson(String json) {
        try
        {
            JSONArray obj = JSONArray.fromObject('[' + json + ']');
            
            if (obj.isEmpty())
                return null;
            
            Object pre = obj.get(0);
            Object data = null;
            if (pre instanceof JSONArray) {
                data = new ArrayList<Object>((JSONArray) pre);
            }
            else if( pre instanceof JSONObject) {
                data = JSONObject.toBean((JSONObject)pre);
            }
            else if( pre instanceof JSONNull) {
                data = null;
            }
            else {
                data = pre;
            }   
            return data;
        }
        catch (JSONException e)
        {
            if(log.isDebugEnabled())
                log.warn("Bad json string: " + json);
            return json;
        }    
    }
    
    /**
     * The inverse of {@link #getObjectFromJson(String)}.
     * @param obj a bean, list, number, or string, never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static String getJsonFromObject(Object obj) {
      String data = JSONSerializer.toJSON(asList(obj)).toString();
      data = removeStart(data, "[");
      data = removeEnd(data, "]");
      return data;
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSSerializerUtils.class);
    
}
