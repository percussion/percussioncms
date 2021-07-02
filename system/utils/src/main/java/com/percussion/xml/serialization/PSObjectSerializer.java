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
package com.percussion.xml.serialization;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.util.PSCharSetsConstants;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.beans.IntrospectionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This is a singleton and can restore an object from its XML representation and
 * convert an object to its xml representation. It maintains a registry of all
 * classes that can be serilized to and from XML. The registry
 * (classregistry.xml) is read from class path location specified during call to
 * {@link #registerBeanClasses(Class)} and cached all along. This method must be
 * called before getting the singleton by calling {@link #getInstance()}
 * otherwise you cannot do much with it. It is an error trying to serialize or
 * deserialize an object of unregistered class an exception will be thrown in
 * such a case. The registry is basically list of fully qualified class names.
 * These classes must be in class path during runtime. The .betwixt file for
 * each class (if exists) must reside along with each class.
 * <p>
 * The file classregistry.xml has a simple DTD as follows:
 * <p>
 * &lt;!ELEMENT class EMPTY&gt; &lt;!ATTLIST class name (CDATA) #REQUIRED &gt;
 * &lt;!ELEMENT registry (class+)&gt;
 * <p>
 * This is a wrapper class on
 * {@link com.percussion.services.utils.xml.PSXmlSerializationHelper}
 * 
 * 
 * 
 * @version 6.0
 * @created 09-Sep-2005 2:49:09 PM
 */
public class PSObjectSerializer
{
   /**
    * Private ctor for the singleton. Registers all bean classes that are listed
    * in the registry file classregistry.xml file. This file is expected to be
    * located where this class is..
    */
   private PSObjectSerializer()
   {
   }

   /**
    * Register all bean classes that are listed in the file classregistry.xml
    * file with bean reader.If it fails to register a class from the registry,
    * the warning is logged. An error is logged if there is any error reading
    * and parsing the registry file and the serializer will still work as long
    * as the class is registered with the serialize using
    * {@link PSXmlSerializationHelper#addType(Class)} method.
    * 
    * @param classRegistryStreamSrc the classregistry.xml file is loaded from
    * this class package, must not be <code>null</code>.
    */
   synchronized public void registerBeanClasses(Class classRegistryStreamSrc)
   {
      if (classRegistryStreamSrc == null)
      {
         throw new IllegalArgumentException(
            "classRegistryStreamSrc must not be null");
      }
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(new InputSource(
            classRegistryStreamSrc.getResourceAsStream(CLASSREGISTRY_XML)),
            false);
         NodeList nl = doc.getElementsByTagName("class");
         for (int i = 0; i < nl.getLength(); i++)
         {
            Element e = (Element) nl.item(i);
            String className = e.getAttribute("name");
            try
            {
               PSXmlSerializationHelper.addType(Class.forName(className));
            }
            catch (ClassNotFoundException e1)
            {
               m_logger.warn("Could not register bean class.", e1);
            }
         }
      }
      catch (Exception e)
      {
         m_logger.error("Error reading class registry file", e);
      }
   }

   /**
    * Access the singleton object. The instance may not be all that useful if
    * accessed before calling {@link #registerBeanClasses(Class)}.
    * 
    * @return the only object of this class, never <code>null</code>.
    */
   public static PSObjectSerializer getInstance()
   {
      if (serializer == null)
         serializer = new PSObjectSerializer();
      return serializer;
   }

   /**
    * Deserialize or restore an object from its XML representation.Throws an
    * exception if the object type - XML is not a registered one.
    * 
    * @return the object restored. The type of the object depends on how the
    *         object type - XML mapping was registered with this class.
    * @param element Root element of the XML representation of the object to
    *           restore. Must not be <code>null</code>.
    * @throws PSObjectSerializerException
    */
   public Object fromXml(Element element) throws PSObjectSerializerException
   {
      if (element == null)
         throw new IllegalArgumentException("element must not be null");

      return fromXmlString(PSXmlDocumentBuilder.toString(element));
   }

   /**
    * Deserialize or restore an object from its XML string representation.Throws
    * an exception if the object type - XML is not a registered one.
    * 
    * @return the object restored. The type of the object depends on how the
    *         object type - XML mapping was registered with this class.
    * @param xmlString XML stringrepresentation of the object to restore. Must
    *           not be <code>null</code> or empty.
    * @throws PSObjectSerializerException
    */
   public Object fromXmlString(String xmlString)
         throws PSObjectSerializerException
   {
      if (xmlString == null)
         throw new IllegalArgumentException("xmlString must not be null");

      xmlString = xmlString.trim();

      if (xmlString.length() == 0)
         throw new IllegalArgumentException("xmlString must not empty");

      try
      {
         return PSXmlSerializationHelper.readFromXML(xmlString);
      }
      catch (IOException e)
      {
         throw new PSObjectSerializerException(e);
      }
      catch (SAXException e)
      {
         throw new PSObjectSerializerException(e);
      }
   }

   /**
    * Converts a given object to its XML representation. The DTD of the element
    * is as per that specified in the registry.
    * 
    * @return DOM element of the XML representation of the object, never
    *         <code>null</code>.
    * @param object Object to serialize to XML, must not be <code>null</code>.
    * @throws IntrospectionException
    * @throws SAXException
    * @throws IOException
    * @throws ParserConfigurationException
    */
   public Element toXml(Object object) throws IOException, SAXException,
         IntrospectionException, ParserConfigurationException
   {
      if (object == null)
         throw new IllegalArgumentException("object must not be null");

      ByteArrayInputStream bais = new ByteArrayInputStream(toXmlString(object)
            .getBytes(PSCharSetsConstants.rxStdEnc()));
      return PSXmlDocumentBuilder.createXmlDocument(bais, false)
            .getDocumentElement();
   }

   /**
    * Converts a given object to its XML representation. The DTD of the element
    * is as per that specified in the registry.
    * 
    * @return XML string representation of the object, never <code>null</code>
    *         or empty.
    * @param object Object to serialize to XML, must not be <code>null</code>.
    * @throws IntrospectionException
    * @throws SAXException
    * @throws IOException
    */
   public String toXmlString(Object object) throws IOException, SAXException,
         IntrospectionException
   {
      return PSXmlSerializationHelper.writeToXml(object);
   }

   /**
    * Only instance of this class, initialized in {@link #getInstance()} for the
    * first time.
    */
   private volatile static PSObjectSerializer serializer = null;

   /**
    * Logger instance for this class. Never <code>null</code>.
    */
   private static final Logger m_logger = LogManager.getLogger(PSObjectSerializer.class);

   /**
    * Clones the supplied object by serializing to XML and then deserializing
    * back to object. Assumes the object type has been registered with the
    * serializer and appropriate betwixt files are avaialble or defaults work.
    * 
    * @param source object to clone, must not be <code>null</code>.
    * 
    * @return cloned object, may be <code>null</code> if the object could not
    *         be restored from XML.
    * 
    * @throws PSObjectSerializerException any error during serialization or
    *            deserialization.
    */
   public Object cloneObject(Object source) throws PSObjectSerializerException
   {
      if (source == null)
      {
         throw new IllegalArgumentException("source must not be null");
      }
      try
      {
         return fromXml(toXml(source));
      }
      catch (Exception e)
      {
         throw new PSObjectSerializerException(e);
      }
   }

   /**
    * Name of the class registry file. This has a simple DTD and contains a list
    * of all classes that need to be serialized and deserialized.
    */
   private static final String CLASSREGISTRY_XML = "classregistry.xml";
}
