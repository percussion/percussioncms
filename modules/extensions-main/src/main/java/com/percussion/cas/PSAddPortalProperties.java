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
package com.percussion.cas;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.text.MessageFormat;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a properties element as used in portal publisher assemblers.
 */
public class PSAddPortalProperties extends PSSimpleJavaUdfExtension
{
   /**
    * Creates the <code>Properties</code> element as specified in the 
    * sys_PortalPublisher.dtd for the supplied parameters.
    * 
    * @params the property attributes and values for all properties that need
    *    to be created. For each property a group of 4 parameters is expected
    *    in the order name, type, pattern and value. The pattern can be 
    *    <code>null</code> or empty. If empty and the type is set to dateTime,
    *    the default pattern "yyyy-MM-dd" is used. Multiple values can be
    *    supplied as a coma separated list. May not be <code>null</code> or
    *    empty.
    * @param request the request to be processed, assumed not <code>null</code>.
    * @throws PSConversionException for all errors, including all parameter
    *    validation errors.
    * @return an <code>Element</code> with the <code>Properties</code> format
    *    as specified in sys_PortalPublisher.dtd, never <code>null</code>.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;
      
      if (size < PROPERTY_ATTRS.length)
         throw new PSConversionException(0, "At least one property is required");
         
      if ((size % PROPERTY_PARAMSIZE) != 0)
      {
         int errorCode = 0;
         Object args[] = { new Integer(PROPERTY_PARAMSIZE) };
         String errorMsg = "Each property requires \"{0}\" parameters.";
            
         throw new PSConversionException(errorCode, 
            MessageFormat.format(errorMsg, args));
      }
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element properties = doc.createElement(PROPERTIES_ELEM);
      for (int i=0; i<size; i+=PROPERTY_PARAMSIZE)
      {
         Element property = createProperty(params, i, doc);
         if (property == null)
            break;
            
         properties.appendChild(property);
      }
         
      return properties;
   }
   
   /**
    * Creates one <code>Property</code> element as specified in 
    * sys_PortalPublisher.dtd.
    * 
    * @param params the parameters to use for the property creation, assumed
    *    not <code>null</code> or empty.
    * @param offset the offset from where in the <code>params</code> parameter
    *    to start, assumed to be a valid offset for the supplied 
    *    <code>params</code>.
    * @param doc the document for which to create the <code>Property</code>
    *    element, assumed not <code>null</code>.
    * @return the <code>Property</code> element as specified in 
    *    sys_PortalPublisher.dtd, <code>null</code> if the property name was
    *    unspecified (either <code>null</code> or empty).
    * @throws PSConversionException for any invalid property parameter.
    */
   private Element createProperty(Object[] params, int offset, Document doc)
      throws PSConversionException
   {
      String propertyType = "";
      Element propertyElem = doc.createElement(PROPERTY_ELEM);
      for (int i=0; i<PROPERTY_ATTRS.length; i++)
      {
         int index = i+offset;
         
         String attrName = PROPERTY_ATTRS[i];
         String attrValue = 
            (params[index] == null) ? "" : params[index].toString().trim();

         // stop parsing properties if the first invalid property name is found
         if (attrName.equalsIgnoreCase(PROPERTY_NAME) && attrValue.length() == 0)
            return null;
            
         // validate and save the property type for later
         if (attrName.equalsIgnoreCase(PROPERTY_TYPE))
         {
            String validTypes = validateType(attrValue);
            if (validTypes != null)
            {
               int errorCode = 0;
               Object args[] = { attrValue, validTypes };
               String errorMsg = 
                  "The supplied type \"{0}\" is not valid. Valid types are \"{1}\".";
                  
               throw new PSConversionException(errorCode, 
                  MessageFormat.format(errorMsg, args));
            }
            
            propertyType = attrValue;
         }
            
         if (attrValue.length() == 0)
         {
            if (attrName.equalsIgnoreCase(PROPERTY_PATTERN))
            {
               // use default pattern if none was supplied and the type is date
               if (propertyType.equalsIgnoreCase(TYPE_DATETIME))
                  attrValue = DEFAULT_FORMAT;
            }
            else
            {
               int errorCode = 0;
               Object args[] = { attrName };
               String errorMsg = 
                  "Property attribute \"{0}\" cannot be null or empty.";
                  
               throw new PSConversionException(errorCode, 
                  MessageFormat.format(errorMsg, args));
            }
         }
         
         if (attrValue.length() > 0)
            propertyElem.setAttribute(attrName, attrValue);
      }
      
      int index = offset + PROPERTY_ATTRS.length;
      String value = 
         (params[index] == null) ? "" : params[index].toString().trim();
      if (value.length() == 0)
      {
         PSXmlDocumentBuilder.addEmptyElement(doc, propertyElem, VALUE_ELEM);
      }
      else
      {
         StringTokenizer tokens = new StringTokenizer(value, ",");
         while (tokens.hasMoreTokens())
            PSXmlDocumentBuilder.addElement(doc, propertyElem, VALUE_ELEM, 
               tokens.nextToken().trim());
      }
            
      return propertyElem;
   }
   
   /**
    * Validates the supplied property type. The validation made is case 
    * sensitive.
    * 
    * @param type the property type to be validated, assumed not 
    *    <code>null</code>.
    * @return <code>null</code> if the supplied type is valid, a coma separated 
    *    list of valid types as <code>String</code> otherwise.
    */
   private String validateType(String type)
   {
      String validTypes = "";
      
      for (int i=0; i<VALID_TYPES.length; i++)
      {
         if (type.equals(VALID_TYPES[i]))
            return null;

         if (i > 0)
            validTypes += ", ";            
         validTypes += VALID_TYPES[i];
      }
      
      return validTypes;
   }
   
   /**
    * Unit test.
    * @param args not used.
    */
   public static void main(String[] args)
   {
      PSAddPortalProperties test = new PSAddPortalProperties();
      
      Object[] params = 
      {
         "searchKeywords",
         "string",
         "",
         "foo, bar",
         "excludeFromSearch",
         "string",
         "",
         "no",
         "regions",
         "string",
         "",
         "1, 2",
         "",
         "string",
         "",
         "en-us",
         "creationDate",
         "dateTime",
         "",
         "20030612",
         "emptyValue",
         "dateTime",
         "",
         ""
      };
      
      try
      {
         Element properties = (Element) test.processUdf(params, null);
         System.out.println(PSXmlDocumentBuilder.toString(properties));
      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Default date format that will be used to format properties of type date
    * if no pattern is supplied.
    */
   private static final String DEFAULT_FORMAT = "yyyy-MM-dd";
   
   /**
    * The name attribute for the <code>Property</code> element.
    */
   private static final String PROPERTY_NAME = "name";
   
   /**
    * The type attribute for the <code>Property</code> element.
    */
   private static final String PROPERTY_TYPE = "type";
   
   /**
    * The pattern attribute for the <code>Property</code> element.
    */
   private static final String PROPERTY_PATTERN = "pattern";
   
   /**
    * An array of all attributes for the <code>Property</code> element.
    */
   private static final String[] PROPERTY_ATTRS = 
   {
      PROPERTY_NAME,
      PROPERTY_TYPE,
      PROPERTY_PATTERN,
   };
   
   /**
    * The number of parameters needed for each <code>Property</code> element.
    */
   private static final int PROPERTY_PARAMSIZE = PROPERTY_ATTRS.length + 1;
   
   /**
    * The 'string' type used as value for the <code>Property</code> type 
    * attribute.
    */
   private static final String TYPE_STRING = "string";
   
   /**
    * The 'numeric' type used as value for the <code>Property</code> type 
    * attribute.
    */
   private static final String TYPE_NUMERIC = "numeric";
   
   /**
    * The 'dataTime' type used as value for the <code>Property</code> type 
    * attribute.
    */
   private static final String TYPE_DATETIME = "dateTime";
   
   /**
    * An array with all valid values supported for the type attribute in the
    * <code>Property</code> element.
    */
   private static final String[] VALID_TYPES = 
   {
      TYPE_STRING,
      TYPE_NUMERIC,
      TYPE_DATETIME
   };
   
   /**
    * The following constants define elment names as specified in 
    * sys_PortalPublisher.dtd.
    */
   private static final String PROPERTIES_ELEM = "Properties";
   private static final String PROPERTY_ELEM = "Property";
   private static final String VALUE_ELEM = "Value";
}
