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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.cas;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.PSBase64Encoder;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.text.MessageFormat;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a properties element as used in portal publisher assemblers.
 */
public class PSCreatePortalPropertyList extends PSSimpleJavaUdfExtension
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
         throw new PSConversionException(
            0,
            "At least one property is required");

      if (((size - 1) % PROPERTY_PARAMSIZE) != 0)
      {
         int errorCode = 0;
         Object args[] = { new Integer(PROPERTY_PARAMSIZE)};
         String errorMsg = "Each property requires \"{0}\" parameters.";

         throw new PSConversionException(
            errorCode,
            MessageFormat.format(errorMsg, args));
      }

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      String elementName = params[0].toString();
      Element element = doc.createElement(elementName);
      doc.appendChild(element);

      for (int i = 1; i < size; i += PROPERTY_PARAMSIZE)
      {
         // Create property will add children to the doc
         if (createProperty(params, i, doc, element) == false)
            break;
      }

      return doc;
   }

   /**
    * Creates <code>Property</code> elements as specified in 
    * sys_newPortalPublisher.dtd.
    * 
    * @param params the parameters to use for the property creation, assumed
    *    not <code>null</code> or empty.
    * @param offset the offset from where in the <code>params</code> parameter
    *    to start, assumed to be a valid offset for the supplied 
    *    <code>params</code>.
    * @param doc the document for which to create the <code>Property</code>
    *    element, assumed not <code>null</code>.
    * @param parent the element on which to insert the created properties
    * @return false when the method is done processing parameters
    * @throws PSConversionException for any invalid property parameter.
    */
   private boolean createProperty(
      Object[] params,
      int offset,
      Document doc,
      Element parent)
      throws PSConversionException
   {
      String propertyName = "";
      String propertyType = "";
      String propertyPattern = null;

      for (int i = 0; i < PROPERTY_ATTRS.length; i++)
      {
         int index = i + offset;

         String attrName = PROPERTY_ATTRS[i];
         String attrValue =
            (params[index] == null) ? "" : params[index].toString().trim();

         // stop parsing properties if the first invalid property name is found
         if (attrName.equalsIgnoreCase(PROPERTY_NAME)
            && attrValue.length() == 0)
         {
            return false;
         }

         if (attrName == PROPERTY_NAME)
         {
            propertyName = attrValue;
         }

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

               throw new PSConversionException(
                  errorCode,
                  MessageFormat.format(errorMsg, args));
            }

            propertyType = attrValue;
         }

         if (attrName.equalsIgnoreCase(PROPERTY_PATTERN))
         {
            // use default pattern if none was supplied and the type is date
            if (attrValue.length() == 0
               && propertyType.equalsIgnoreCase(TYPE_DATETIME))
            {
               // Default is ok
               propertyPattern = DEFAULT_FORMAT;
            }
            else
            {
               propertyPattern = attrValue;
            }
         }
      }

      // Done grabbing parameters, now create the property
      int index = offset + PROPERTY_ATTRS.length;
      String value =
         (params[index] == null) ? "" : params[index].toString().trim();
      if (value.length() != 0)
      {
         String dels = null;

         // The string should only be tokenized automatically for non-string
         // values. For strings, the delimiter is given in the pattern. This
         // allows things like titles and abstracts to be passed through without
         // being tokenized
         if (propertyType.equals(TYPE_STRING))
         {
            if (propertyPattern != null && propertyPattern.trim().length() > 0)
            {
               dels = propertyPattern; // Use value for delimiter
               propertyPattern = null;
               // Not applicable for strings, so don't save
            }
         }
         else
         {
            dels = DELS;
         }

         if (dels != null)
         {
            StringTokenizer tokens = new StringTokenizer(value, dels);
            if (tokens.hasMoreTokens())
               while (tokens.hasMoreTokens())
               {
                  String token = tokens.nextToken().trim();
                  createPropertyElement(
                     doc,
                     parent,
                     propertyName,
                     propertyType,
                     propertyPattern,
                     token);
               }
         }
         else
         {
            createPropertyElement(
               doc,
               parent,
               propertyName,
               propertyType,
               propertyPattern,
               value);
         }
      }

      return true;
   }

   /**
    * Create a single property element and insert it into the document's child
    * list
    * 
    * @param doc
    * @param propertyName
    * @param propertyType
    * @param propertyPattern
    * @param value
    */
   private void createPropertyElement(
      Document doc,
      Element parent,
      String propertyName,
      String propertyType,
      String propertyPattern,
      String value)
   {
      Element propertyElem = doc.createElement(PROPERTY_ELEM);
      propertyElem.setAttribute("name", propertyName);
      propertyElem.setAttribute("type", propertyType);
      propertyElem.setAttribute("pattern", propertyPattern);
      
      boolean encoded = false;
      for (int i = 0; i < ENCODED_TYPES.length; i++)
      {
         String type = ENCODED_TYPES[i];
         if (type.equalsIgnoreCase(propertyType))
         {
            encoded = true;
            break;
         }
      }
      
      if (encoded)
      {
         String encString = PSBase64Encoder.encode(value);
         propertyElem.appendChild(doc.createTextNode(encString));
      }
      else
      {
         propertyElem.appendChild(doc.createTextNode(value));
      }
      parent.appendChild(propertyElem);
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

      for (int i = 0; i < VALID_TYPES.length; i++)
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
    * Default date format that will be used to format properties of type date
    * if no pattern is supplied.
    */
   private static final String DEFAULT_FORMAT = "yyyy-MM-dd";

   /**
    * The tokenizer separators to use when processing an item  
    */
   private static final String DELS = ",";

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
      { PROPERTY_NAME, PROPERTY_TYPE, PROPERTY_PATTERN, };

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
   private static final String TYPE_NUMERIC = "integer";

   /**
    * The 'dateTime' type used as value for the <code>Property</code> type 
    * attribute.
    */
   private static final String TYPE_DATETIME = "date";
   
   /**
    * The 'binary' type used as value for the <code>Property</code> type 
    * attribute. Indicates a blob column as a destination.
    */   
   private static final String TYPE_BLOB = "binary";
   
   /**
    * The 'clob' type used as value for the <code>Property</code> type 
    * attribute. Indicates a TEXT or CLOB column as a destination.
    */  
   private static final String TYPE_CLOB = "clob";

   /**
    * An array with all valid values supported for the type attribute in the
    * <code>Property</code> element.
    */
   private static final String[] VALID_TYPES =
      { TYPE_STRING, TYPE_NUMERIC, TYPE_DATETIME, TYPE_BLOB, TYPE_CLOB };
      
   /**
    * An array of types that must be base64 encoded.
    */
   private static final String[] ENCODED_TYPES = 
      { TYPE_BLOB, TYPE_CLOB };

   /**
    * The following constants define element names as specified in 
    * sys_newPortalPublisher.dtd.
    */
   private static final String PROPERTY_ELEM = "Property";
}
