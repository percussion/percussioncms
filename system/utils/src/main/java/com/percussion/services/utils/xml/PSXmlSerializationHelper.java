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
package com.percussion.services.utils.xml;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.xml.IPSXmlSerialization;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.betwixt.IntrospectionConfiguration;
import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.io.read.*;
import org.apache.commons.betwixt.strategy.HyphenatedNameMapper;
import org.apache.commons.betwixt.strategy.NameMapper;
import org.apache.commons.betwixt.strategy.PropertySuppressionStrategy;
import org.apache.commons.betwixt.strategy.TypeBindingStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper methods for handling serialization to and from XML. This sets up
 * betwixt with default classes that handle the translation of class names to
 * element names and in particular sets up type mapping information to assist in
 * converting back from xml; an area that betwixt is very weak in.
 * 
 * @author dougrand
 */
public class PSXmlSerializationHelper
{
   /**
    * Static for logging
    */
   private static final Logger log = LogManager.getLogger(PSXmlSerializationHelper.class);

   /**
    * Static used for method lookup
    */
   static final Class NOARGS[] = new Class[0];

   /**
    * This class dictates a strategy that suppresses the persistence of certain
    * object properties. With the exception of "class", which is directly
    * supressed, the suppression information is derived from annotation
    * information on the given getter method.
    */
   static class SuppressionStrategy extends PropertySuppressionStrategy
   {
      @Override
      public boolean suppressProperty(Class classContainingTheProperty,
            Class propertyType, String propertyName)
      {
         String name = StringUtils.capitalize(propertyName);

         if (name.equalsIgnoreCase("class"))
            return true;

         try
         {
            Method m = null;
            try
            {
               m = classContainingTheProperty.getMethod("get" + name, NOARGS);
            }
            catch (Exception e)
            {
               m = classContainingTheProperty.getMethod("is" + name, NOARGS);
            }

            if (m != null)
            {
               IPSXmlSerialization ann = m
                     .getAnnotation(IPSXmlSerialization.class);
               if (ann != null)
               {
                  return ann.suppress();
               }
            }
            return false;
         }
         catch (Exception e)
         {
            return false;
         }
      }
   }

   /**
    * The name mapper translates from our naming conventions to a simpler
    * convension (by stripping PS or IPS from class names). In addition, it
    * takes awkward mixed case names, like AAType, that the default behavior of
    * the HyphenatedNameMapper turns into a-a-type and turns it into Aatype,
    * which the mapper handles more benignly.
    */
   static class PSNameMapper extends HyphenatedNameMapper
   {
      public String mapTypeToElementName(String name)
      {
         if (name.startsWith("PS"))
            name = name.substring(2);
         else if (name.startsWith("IPS"))
            name = name.substring(3);

         if (name.contains("$"))
         {
            int i = name.indexOf("$");
            name = name.substring(i + 1);
         }

         // Proper case multiple capitals, i.e. GUID -> Guid
         StringBuilder b = new StringBuilder();
         boolean was_cap = false;
         for (int i = 0; i < name.length(); i++)
         {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch))
            {
               if (was_cap)
               {
                  b.append(Character.toLowerCase(ch));
                  continue;
               }
               else
               {
                  was_cap = true;
               }
            }
            else
            {
               was_cap = false;
            }
            b.append(ch);
         }

         return super.mapTypeToElementName(b.toString());
      }

   }

   /**
    * A content handler that searches for the id of the object, which is used to
    * extract the id from the xml. This is contained in the first attribute 
    * named "guid" or the first element named "guid" that is found.
    */
   static class FindIdAttribute extends DefaultHandler
   {
      /**
       * The id, <code>null</code> until the handler has found the id.
       */
      String m_id = null;
      
      /**
       * Set to <code>true</code> if a guid element is found. Then the next
       * text found will be grabbed for the id.
       */
      boolean m_nextText = false;

      /*
       * (non-Javadoc)
       * 
       * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
       *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
       */
      @Override
      public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException
      {
         m_id = attributes.getValue("guid");
         if (!StringUtils.isBlank(m_id))
         {
            throw new SAXException("Done");
         }
         if (qName.equals("guid"))
         {
            m_nextText = true;
         }
      }

      @Override
      public void characters(char[] ch, int start, int length) throws SAXException
      {
         if (m_nextText)
         {
            m_id = new String(ch, start, length);
            throw new SAXException("Done");
         }
         else
         {
            super.characters(ch, start, length);
         }
      }



      /**
       * Get the id
       * 
       * @return the id
       */
      public String getId()
      {
         return m_id;
      }
   }

   /**
    * A specific implementation of this, see {@link TypeBindingStrategy}
    */
   static class PSTypeBindingStrategy extends TypeBindingStrategy
   {
      @Override
      public BindingType bindingType(Class bindingClass)
      {
         if (Enum.class.isAssignableFrom(bindingClass))
            return TypeBindingStrategy.BindingType.PRIMITIVE;
         else if (IPSGuid.class.isAssignableFrom(bindingClass))
            return TypeBindingStrategy.BindingType.PRIMITIVE;
         else
            return TypeBindingStrategy.DEFAULT.bindingType(bindingClass);
      }
   }

   /**
    * Maps element names to implementation classes. Used for deserialization.
    */
   static Map<String, Class> ms_typeMap = new HashMap<String, Class>();

   /**
    * Add a type and element name to the mappings
    * 
    * @param elementName the element name, never <code>null</code> or empty
    * @param type the class, never <code>null</code>
    */
   public static synchronized void addType(String elementName, Class type)
   {
      if (elementName == null || StringUtils.isBlank(elementName))
      {
         throw new IllegalArgumentException(
               "elementName may not be null or empty");
      }
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      ms_typeMap.put(elementName, type);
   }

   /**
    * Add a type to the mappings. This method does the default translation of
    * the name to an element name for the registration by using the class
    * {@link PSNameMapper}.
    * 
    * @param type the class, never <code>null</code>
    */
   public static synchronized void addType(Class type)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      PSNameMapper mapper = new PSNameMapper();
      String name = mapper.mapTypeToElementName(type.getSimpleName());
      addType(name, type);
   }

   /**
    * Holds the suppression strategy singleton.
    */
   private static SuppressionStrategy ms_supStrategy = null;

   /**
    * Holds the type strategy singleton.
    */
   private static TypeBindingStrategy ms_typebinder = null;

   /**
    * Get and if necessary create the type binding singleton.
    * 
    * @return the singleton, never <code>null</code>
    */
   private static synchronized TypeBindingStrategy getTypeBindingStrategyInstance()
   {
      if (ms_typebinder == null)
      {
         ms_typebinder = new PSTypeBindingStrategy();
      }
      return ms_typebinder;
   }

   /**
    * Get and if necessary create the suppression strategy singleton
    * 
    * @return the singleton, never <code>null</code>
    */
   private static synchronized SuppressionStrategy getSuppressionStrategyInstance()
   {
      if (ms_supStrategy == null)
      {
         ms_supStrategy = new SuppressionStrategy();
      }
      return ms_supStrategy;
   }

   /**
    * Bean creator instance, initialized in the getter
    */
   private static ChainedBeanCreator ms_beanCreator = null;

   /**
    * The object converter does custom conversions from specific internal Rx 
    * classes to string representations.  
    */
   private static PSBetwixtObjectConverter ms_converter = new PSBetwixtObjectConverter();

   /**
    * Get the bean creator. The bean creator handles the mapping from an element
    * name to a specific instance class. Uses the name mappings registered
    * with {@link #addType(String, Class)} and {@link #addType(Class)}.
    * @return the creator, never <code>null</code>
    */
   private static synchronized ChainedBeanCreator getBeanCreator()
   {
      if (ms_beanCreator == null)
      {
         ms_beanCreator = new ChainedBeanCreator()
         {
            public Object create(ElementMapping mapping, ReadContext context,
                  BeanCreationChain next)
            {
               String name = mapping.getName();
               Class implclass = ms_typeMap.get(name);
               if (implclass != null)
               {
                  try
                  {
                     return implclass.newInstance();
                  }
                  catch (Exception e)
                  {
                     log.error("Could not instantiate, Error: {}", e.getMessage());
                     log.debug(e.getMessage(), e);
                     throw new RuntimeException(e);
                  }
               }
               else
               {
                  return next.create(mapping, context);
               }
            }

         };
      }
      return ms_beanCreator;
   }

   /**
    * Setup the standard configuration for writing an object with betwixt
    * 
    * @param writer the bean writer, assumed never <code>null</code>
    */
   private static void standardBetwixtConfiguration(BeanWriter writer)
   {
      writer.setXMLIntrospector(createXMLIntrospector());
      writer.enablePrettyPrint();
      writer.getBindingConfiguration().setObjectStringConverter(ms_converter);
      writer.getBindingConfiguration().getIdMappingStrategy().reset();
   }

   /**
    * Setup the standard configuration for reading an object with betwixt
    * 
    * @param reader the bean reader, assumed never <code>null</code>
    * @param clazz the class to be read, may be <code>null</code>
    */
   private static void standardBetwixtConfiguration(BeanReader reader,
         Class clazz)
   {
      reader.setXMLIntrospector(createXMLIntrospector());
      reader.getBindingConfiguration().setObjectStringConverter(ms_converter);
      try
      {
         if (clazz != null)
         {
            reader.registerBeanClass(clazz);
         }
         else
         {
            for (Class c : ms_typeMap.values())
            {
               reader.registerBeanClass(c);
            }
         }
      }
      catch (IntrospectionException e1)
      {
         throw new RuntimeException(e1);
      }
   }

   /**
    * Creation code cribbed from sample in Betwixt
    * @return the singleton inspector instance
    */
   private static XMLIntrospector createXMLIntrospector()
   {
      XMLIntrospector introspector = new XMLIntrospector();

      IntrospectionConfiguration config = introspector.getConfiguration();
      NameMapper mapper = new PSNameMapper();
      config.setElementNameMapper(mapper);
      config.setAttributeNameMapper(mapper);
      config.setAttributesForPrimitives(false);
      config.setPropertySuppressionStrategy(getSuppressionStrategyInstance());
      config.setTypeBindingStrategy(getTypeBindingStrategyInstance());

      return introspector;
   }

   /**
    * Write the given object to an XML string. This method uses the commons
    * betwixt library to serialize the object using reflection. Please note that
    * you must have public methods for each and every property you wish to
    * persist to the XML string.
    * <p>
    * Properties that should not be persisted should have the
    * {@link IPSXmlSerialization} annotation added to their <code>get</code>
    * or <code>is</code> methods.
    * <p>
    * Note: ph - This method needs to be synchronized because the underlying
    * implementation library is not thread safe. What I found was that when
    * serializing an object to xml, sometimes an empty document would be created
    * even though the object was valid. This only happened when several objects
    * were being processed at the same time. The beanutils jars reported a
    * threading issue fixed in v1.8, but I tried the latest jars and they didn't
    * resolve the issue.
    * 
    * @param object the object to write, never <code>null</code>
    * @return the XML representation of the object
    * @throws IOException if there's a problem writing the object
    * @throws SAXException if there's a problem writing the object
    */
   synchronized public static String writeToXml(Object object) throws IOException,
         SAXException
   {
      Writer w = new StringWriter();
      BeanWriter writer = new BeanWriter(w);
      standardBetwixtConfiguration(writer);
      try
      {
         writer.write(object);
      }
      catch (IntrospectionException e)
      {
         throw new SAXException(e);
      }
      finally
      {
         writer.close();
         w.close();
      }

      return w.toString();
   }

   /**
    * Extract the guid from the "guid" attribute.
    * 
    * @param type the type, never <code>null</code>
    * @param xmlsource the xml source document, never <code>null</code> or
    *           empty
    * @return the guid, never <code>null</code>
    * 
    */
   public static IPSGuid getIdFromXml(PSTypeEnum type, String xmlsource)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      if (StringUtils.isBlank(xmlsource))
      {
         throw new IllegalArgumentException(
               "xmlsource may not be null or empty");
      }
      FindIdAttribute fia = new FindIdAttribute();
      SAXParserFactory fact = PSSecureXMLUtils.getSecuredSaxParserFactory(false);

      try
      {
         SAXParser parser = fact.newSAXParser();
         parser.parse(new ByteArrayInputStream(xmlsource.getBytes()), fia);
      }
      catch (SAXException e)
      {
         // Ignore, expected
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }

      if (!StringUtils.isBlank(fia.getId()))
      {
         PSGuid rval = new PSGuid(type, fia.getId());
         return rval;
      }
      else
         return null;
   }

   /**
    * Read the object's information from the given XML source using the betwixt
    * library. If you object has one or more properties that are expressed as
    * abstract classes or interfaces, you must register the needed classes by
    * calling {@link #addType(String, Class)}.
    * 
    * @param xmlsource the xml source, never <code>null</code> or empty
    * @param object the object to read, never <code>null</code>
    * 
    * @return the object created from the given XML source, never
    * <code>null</code>.
    * 
    * @throws IOException
    * @throws SAXException
    */
   public static Object readFromXML(String xmlsource, Object object)
         throws IOException, SAXException
   {
      if (object == null)
      {
         throw new IllegalArgumentException("object may not be null");
      }
      Object restored = readFromXML(xmlsource, object != null ? object
            .getClass() : null);
      try
      {
         BeanUtils.copyProperties(object, restored);
         return restored;
      }
      catch (Exception e)
      {
         // Find underlying cause Exception.
         if (e.getCause() != null) {
            log.error("Cause= ",e.getCause());
         }
         log.error(e.getMessage(), e);
         log.debug(e.getMessage(), e);
         throw new RuntimeException("Error copying bean properties",e);
      }
   }

   /**
    * Read the object's information from the given XML source using the betwixt
    * library. If you object has one or more properties that are expressed as
    * abstract classes or interfaces, you must register the needed classes by
    * calling {@link #addType(String, Class)}.
    * 
    * @param xmlString the xml source, never <code>null</code> or empty
    * @return clazz the class to read, may be <code>null</code>
    * @throws IOException
    * @throws SAXException
    */
   public static Object readFromXML(String xmlString, Class clazz)
         throws IOException, SAXException
   {
      if (StringUtils.isBlank(xmlString))
      {
         throw new IllegalArgumentException(
               "xmlString may not be null or empty");
      }
      BeanReader reader = new BeanReader();
      standardBetwixtConfiguration(reader, clazz);
      Reader r = new StringReader(xmlString);

      BeanCreationList chain = BeanCreationList.createStandardChain();
      chain.insertBeanCreator(1, getBeanCreator());
      reader.getReadConfiguration().setBeanCreationChain(chain);

      Object restored = reader.parse(r);
      if (restored == null)
      {
         throw new SAXException("No bean found");
      }
      return restored;
   }

   /**
    * Read object from string, returning reconstituted object, calls
    * {@link #readFromXML(String, Class)}
    * 
    * @param xmlString xml string, never <code>null</code> or empty
    * @return the read object, could be <code>null</code>
    * @throws SAXException
    * @throws IOException
    */
   public static Object readFromXML(String xmlString) throws IOException,
         SAXException
   {
      return readFromXML(xmlString, null);
   }
}
