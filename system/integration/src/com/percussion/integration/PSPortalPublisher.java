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
package com.percussion.integration;

import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

/**
 * An object to represent the DTD sepcified in sys_PortalPublisher.dtd.
 */
public class PSPortalPublisher
{
   /**
    * Constructs a new portal publisher object.
    * 
    * @param in an input stream from where to construct this object, may not
    *    be <code>null</code>.
    * @throws Exception for any error parsing the input stream.
    */
   public PSPortalPublisher(InputStream in) throws Exception
   {
      if (in == null)
         throw new IllegalArgumentException("the input stream cannot be null");
      
      m_doc = PSXmlDocumentBuilder.createXmlDocument(new InputSource(in), 
         false);
      
      NodeList test = m_doc.getElementsByTagName("Content");
      if (test != null && test.getLength() > 0)
         m_content = new Content(m_doc.getDocumentElement());
      
      test = m_doc.getElementsByTagName("MetaData");
      if (test != null && test.getLength() > 0)
         m_metaData = new MetaData(m_doc.getDocumentElement());
         
      getTargets();
   }
   
   /**
    * Get the list of all specified targets.
    * 
    * @return an iterator over <code>Target</code> objects, never 
    *    <code>null</code>, may be empty.
    * @throws Exception for any error parsing the input document.
    */
   public Iterator getTargets() throws Exception
   {
      if (m_targets == null)
      {
         m_targets = new HashMap();
         
         NodeList targets = m_doc.getElementsByTagName("Target");
         if (targets != null)
         {
            for (int i=0, length=targets.getLength(); i<length; i++)
            {
               Target target = new Target((Element) targets.item(i));
               m_targets.put(target.getName(), target);
            }
         }
      }
      
      return m_targets.values().iterator();
   }
   
   /**
    * Get the target for the supplied name.
    * 
    * @param name the target name, not <code>null</code>.
    * @return the target if found, <code>null</code> otherwise.
    */
   public Target getTarget(String name)
   {
      if (name == null)
         throw new IllegalArgumentException("target name cannot be null");
         
      return (Target) m_targets.get(name);
   }
   
   /**
    * Get the content to be published.
    * 
    * @return the content to be published, never <code>null</code>.
    */
   public Content getContent()
   {
      return m_content;
   }
   
   /**
    * Get the meta data to be published.
    * 
    * @return the meta data to be published, may be <code>null</code>.
    */
   public MetaData getMetaData()
   {
      return m_metaData;
   }
   
   /**
    * Get the required attribute from the supplied source. Throws and 
    * <code>IllegalArgumentException</code> it the attribute is 
    * <code>null</code> or empty. 
    * 
    * @param source the source from which to get the attribute from, assumed
    *    not <code>null</code>.
    * @param name the attribute name to look for, assumed not <code>null</code>.
    * @return the attribute value, never <code>null</code> or empty.
    */
   private String getRequiredAttribute(Element source, String name)
   {
      String attr = source.getAttribute(name);

      if (attr == null || attr.trim().length() == 0)
      {
         Object args[] = 
         { 
            source.getTagName(),
            name
         };
         String errorMsg = 
            "The element \"{0}\" misses the required attribute \"{1}\"";
            
         throw new IllegalArgumentException(
            MessageFormat.format(errorMsg, args));
      }
      
      return attr;
   }
   
   /**
    * Validates the supplied value against the provided array. The 
    * validation made is case sensitive.
    * 
    * @param value the value to be validated, assumed not 
    *    <code>null</code>.
    * @param validValues an array of <code>String</code> objects with all
    *    valid values, assumed not <code>null</code>.
    * @return <code>null</code> if the supplied value is valid, a coma 
    *    separated list of valid values as <code>String</code> otherwise.
    */
   private String validate(String value, String[] validValues)
   {
      String result = "";
      
      for (int i=0; i<validValues.length; i++)
      {
         if (value.equals(validValues[i]))
            return null;

         if (i > 0)
            result += ", ";            
         result += validValues[i];
      }
      
      return result;
   }
   
   /**
    * A class that represents a <code>Target</code> as specified in 
    * sys_PortalPublisher.dtd.
    */
   public class Target
   {
      /**
       * Constructs a target out of the provided source.
       * 
       * @param source the source element to construct the target from,
       *    not <code>null</code>.
       */
      public Target(Element source)
      {
         if (source == null)
            throw new IllegalArgumentException("source cannot be null");

         m_name = getRequiredAttribute(source, "name");
         m_type = getRequiredAttribute(source, "type");
         String validTypes = validate(m_type, VALID_TYPES);
         if (validTypes != null)
         {
            Object args[] = 
            {
               m_type, 
               validTypes
            };
            String errorMsg = 
               "The supplied type \"{0}\" is not valid. Valid types are \"{1}\".";
               
            throw new IllegalArgumentException(
               MessageFormat.format(errorMsg, args));
         }
         
         if (isJndiTarget())
         {
            m_value = getRequiredAttribute(source, "value");
            
            NodeList test = source.getElementsByTagName("DatabaseInfo");
            if (test != null)
            {
               // there is always only 1 database info element
               Element databaseInfo = (Element) test.item(0);
               if (databaseInfo == null)
                  throw new IllegalArgumentException(
                     "The \"DatabaseInfo\" element is required for jndi types.");
                  
               m_driverType = getRequiredAttribute(databaseInfo, "drivertype");
               if (m_driverType.startsWith("oracle"))
                  m_origin = getRequiredAttribute(databaseInfo, "origin");
               else
                  m_origin = databaseInfo.getAttribute("origin");
            }
         }
      }
      
      /**
       * Get the target name.
       * 
       * @return the target name, never <code>null</code> or empty.
       */
      public String getName()
      {
         return m_name;
      }
      
      /**
       * Get the target value for JNDI targets.
       * 
       * @return the target value if this is a JNDI target, <code>null</code> 
       *    otherwise.
       */
      public String getValue()
      {
         return m_value;
      }
      
      /**
       * Is this a JNDI target?
       * 
       * @return <code>true</code> if it is, <code>false</code> otherwise, which
       *    means it is a file system target.
       */
      public boolean isJndiTarget()
      {
         return m_type.equalsIgnoreCase("jndi");
      }
      
      /**
       * Get the database driver type.
       *  
       * @return the datebase driver type or <code>null</code> if this is 
       *    a file system target.
       */
      public String getDriverType()
      {
         return m_driverType;
      }
      
      /**
       * Get the database origin (the schema for oracle).
       * 
       * @return the database origin, may be <code>null</code>. Only required
       *    for oracle.
       */
      public String getOrigin()
      {
         return m_origin;
      }
      
      /** 
       * The target name, never <code>null</code> or empty after construction.
       */
      private String m_name = null;
      
      /** 
       * The target value used for JNDI resource, <code>null</code> otherwise.
       */
      private String m_value = null;
      
      /** 
       * The target type, never <code>null</code> or empty after construction.
       * One of <code>VALID_TARGETS</code>.
       */
      private String m_type = null;

      /** 
       * The backend driver type. This is required for 'jndi' type targets, 
       * ignored otherwise.
       */
      private String m_driverType = null;

      /** 
       * The backend origin (schema for oracle). This is required for 'jndi' 
       * type targets if the driver type is oracle. For all other driver
       * types this is optional but used if provided.
       */
      private String m_origin = null;
      
      /**
       * The type attribute value used if the target is the file system.
       */
      public final String TYPE_PATH = "path";
      
      /**
       * The type attribute value used if the target is a JNDI lookup.
       */
      public final String TYPE_JNDI = "jndi";
   
      /**
       * An array with all valid values supported for the type attribute 
       * in the <code>Target</code> element.
       */
      public final String[] VALID_TYPES = 
      {
         TYPE_PATH,
         TYPE_JNDI
      };
   }
   
   /**
    * A class that represents a <code>Property</code> as specified in 
    * sys_PortalPublisher.dtd.
    */
   public class Property
   {
      /**
       * Constructs a property out of the provided source.
       * 
       * @param source the source element to construct the property from,
       *    not <code>null</code>.
       */
      public Property(Element source)
      {
         if (source == null)
            throw new IllegalArgumentException("source cannot be null");

         m_name = getRequiredAttribute(source, "name");
         m_type = getRequiredAttribute(source, "type");
         String validTypes = validate(m_type, VALID_TYPES);
         if (validTypes != null)
         {
            Object args[] = 
            {
               m_type, 
               validTypes
            };
            String errorMsg = 
               "The supplied type \"{0}\" is not valid. Valid types are \"{1}\".";
               
            throw new IllegalArgumentException(
               MessageFormat.format(errorMsg, args));
         }
            
         m_pattern = source.getAttribute("pattern");
         if (m_pattern == null || m_pattern.trim().length() == 0)
            m_pattern = DEFAULT_FORMAT;
         
         NodeList values = source.getElementsByTagName("Value");
         for (int i=0, length=values.getLength(); i<length; i++)
         {
            Element value = (Element) values.item(i);
            NodeList texts = value.getChildNodes();
            if (texts != null && texts.getLength() > 0)
               m_values.add(((Text) texts.item(0)).getData());
         }
      }
      
      /**
       * Get the property name.
       * 
       * @return the property name, never <code>null</code> or empty.
       */
      public String getName()
      {
         return m_name;
      }
      
      /**
       * Is this property of type 'string'?
       * 
       * @return <code>true</code> if it is, <code>false</code> otherwise.
       */
      public boolean isString()
      {
         return m_type.equalsIgnoreCase(TYPE_STRING);
      }
      
      /**
       * Is this property of type 'numeric'?
       * 
       * @return <code>true</code> if it is, <code>false</code> otherwise.
       */
      public boolean isNumeric()
      {
         return m_type.equalsIgnoreCase(TYPE_NUMERIC);
      }
      
      /**
       * Is this property of type 'dateTime'?
       * 
       * @return <code>true</code> if it is, <code>false</code> otherwise.
       */
      public boolean isDate()
      {
         return m_type.equalsIgnoreCase(TYPE_DATETIME);
      }
      
      /**
       * Get the format pattern fo rproperties of type 'dateTime'.
       * 
       * @return the foramt pattern, never <code>null</code> or empty if this
       *    property is of type 'dateTime', otherwise <code>null</code>.
       */
      public String getPattern()
      {
         return m_pattern;
      }
      
      /**
       * Get the list of values.
       * 
       * @return an iterator over all property values as <code>String</code>,
       *    never <code>null</code>, may be empty.
       */
      public Iterator getValues()
      {
         return m_values.iterator();
      }
      
      /**
       * Get all values as a coma separated <code>String</code>.
       * 
       * @return a coma separated <code>String</code> with all values, never
       *    <code>null</code>, may be empty.
       */
      public String getValuesString()
      {
         String values = "";
         
         for (int i=0; i<m_values.size(); i++)
         {
            if (i > 0)
               values += ", ";
               
            Object value = m_values.get(i);
            if (value != null)
               values += value.toString();
         }
         
         return values;
      }

      /**
       * The property name, never <code>null</code> or empty after construction.
       */
      private String m_name = null;
      
      /**
       * The property type, never <code>null</code> or empty after construction.
       * One of 'string', 'numeric' or 'dateTime'.
       */
      private String m_type = null;
      
      /**
       * The format pattern if type is 'dateTime'. Never <code>null</code> or
       * empty after construction if of type 'dateTime', always 
       * <code>null</code> otherwise.
       */
      private String m_pattern = null;
      
      /**
       * A list of values as <code>String</code>. Never <code>null</code> after
       * construction, may be empty.
       */
      private List m_values = new ArrayList();
   
      /**
       * The 'string' type used as value for the <code>Property</code> type 
       * attribute.
       */
      public final String TYPE_STRING = "string";
      
      /**
       * The 'numeric' type used as value for the <code>Property</code> type 
       * attribute.
       */
      public final String TYPE_NUMERIC = "numeric";
      
      /**
       * The 'dataTime' type used as value for the <code>Property</code> type 
       * attribute.
       */
      public final String TYPE_DATETIME = "dateTime";
   
      /**
       * An array with all valid values supported for the type attribute in the
       * <code>Property</code> element.
       */
      public final String[] VALID_TYPES = 
      {
         TYPE_STRING,
         TYPE_NUMERIC,
         TYPE_DATETIME
      };

      /**
       * Default date format that will be used to format properties of type date
       * if no pattern is supplied.
       */
      public final String DEFAULT_FORMAT = "yyyy-MM-dd";
   }
   
   /**
    * A class that represents a <code>Content</code> element as specified in 
    * sys_PortalPublisher.dtd.
    */
   public class Content
   {
      /**
       * Constructs a content object out of the provided source.
       * 
       * @param source the source element to construct the property from,
       *    not <code>null</code>.
       */
      public Content(Element source)
      {
         if (source == null)
            throw new IllegalArgumentException("source cannot be null");
         
         // there is only one content element
         Element content = 
            (Element) source.getElementsByTagName("Content").item(0);
            
         m_target = getRequiredAttribute(content, "target");
         m_url = getRequiredAttribute(content, "url");
      }
      
      /**
       * Get the target name for this content.
       * 
       * @return the target name, never <code>null</code> or empty.
       */
      public String getTarget()
      {
         return m_target;
      }
      
      /**
       * Get the content url, an absolute url from wher to retrieve the
       * content item.
       * 
       * @return the url from where to get the content, never <code>null</code>
       *    or empty.
       */
      public String getUrl()
      {
         return m_url;
      }
      
      /**
       * The target to which this will be published to, initialized in
       * constructor, never <code>null</code> or empty after that.
       */
      private String m_target = null;
      
      /**
       * The absolut content url from wher to retrieve the content to publish, 
       * initialized in constructor, never <code>null</code> or empty after 
       * that.
       */
      private String m_url = null;
   }
   
   /**
    * A class that represents a <code>MetaData</code> as specified in 
    * sys_PortalPublisher.dtd.
    */
   public class MetaData
   {
      /**
       * Constructs a meta data object out of the provided source.
       * 
       * @param source the source element to construct the meta data from,
       *    not <code>null</code>.
       */
      public MetaData(Element source)
      {
         if (source == null)
            throw new IllegalArgumentException("source cannot be null");
         
         // there is only one meta data element
         Element metaData = 
            (Element) source.getElementsByTagName("MetaData").item(0);
            
         m_target = getRequiredAttribute(metaData, "target");
         
         m_properties = new HashMap();
         NodeList properties = metaData.getElementsByTagName("Property");
         if (properties != null)
         {
            for (int i=0, length=properties.getLength(); i<length; i++)
            {
               Property property = new Property((Element) properties.item(i));
               m_properties.put(property.getName(), property);
            }
         }
      }
   
      /**
       * Get the meta data target.
       * 
       * @return the target name for the meta data, never <code>null</code> or
       *    empty.
       */
      public String getTarget()
      {
         return m_target;
      }
       
      /**
       * Get the list of all specified properties.
       * 
       * @return an iterator over <code>Property</code> objects, never 
       *    <code>null</code>, may be empty.
       */
      public Iterator getProperties()
      {
         return m_properties.values().iterator();
      }
      
      /**
       * Get the property for the supplied name.
       * 
       * @param name the property name, not <code>null</code>.
       * @return the requested property or <code>null</code> if not found.
       */
      public Property getProperty(String name)
      {
         if (name == null)
            throw new IllegalArgumentException("property name cannot be null");
            
         return (Property) m_properties.get(name);
      }
   
      /**
       * The target to which this will be published to, initialized in
       * constructor, never <code>null</code> or empty after that.
       */
      private String m_target = null;
      
      /**
       * A map of publishing properties as specified in sys_PortalPublisher.dtd.
       * Initialized while constructed, never <code>null</code>, may be empty 
       * after that.
       */
      private Map m_properties = null;
   }
    
   /**
    * The document that will contain the sys_PortalPublisher.dtd object.
    * Never <code>null</code> after constuction.
    */
   private Document m_doc = null;
   
   /**
    * A <code>Content</code> object as specified int the sys_PortalPublisher.dtd.
    * Initialized while constructed, may be <code>null</code> after that.
    */
   private Content m_content = null;
   
   /**
    * A <code>MetaData</code> object as specified int the sys_PortalPublisher.dtd.
    * Initialized while constructed, may be <code>null</code> after that.
    */
   private MetaData m_metaData = null;
   
   /**
    * A map of publishing targets as specified in sys_PortalPublisher.dtd.
    * Initialized while constructed, never <code>null</code>, may be empty 
    * after that.
    */
   private Map m_targets = null;
}
