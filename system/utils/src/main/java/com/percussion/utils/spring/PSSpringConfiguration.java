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

package com.percussion.utils.spring;

import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles the loading of Spring beans from a Spring configuration file. The
 * file must conform to the "spring-beans.dtd", and beans are loaded and saved
 * using the {@link IPSBeanConfig} interface.
 * <p>
 * Note that calls {@link #setBean(IPSBeanConfig)} will only add the bean to the
 * in-memory configuration and that {@link #save()} must be called to save the
 * changes to the configuration file.
 */
public class PSSpringConfiguration
{
   /**
    * The name of the bean containing the datasource resolver definition.
    */
   public static final String DS_RESOLVER_NAME = "sys_datasourceResolver";

   /**
    * The name of the bean containing the hibernate dialect map defintion.
    */
   public static final String HIBERNATE_DIALECT_MAP_NAME = "sys_hibernateDialects";

   /**
    * Constructs an instance of the configuration from the source file.
    * 
    * @param configFile The source configuration file, may not be
    *           <code>null</code> and must conform to the "spring-beans.dtd"
    *           DTD.
    * @throws SAXException If the file cannot be parsed.
    * @throws IOException If there is an error reading from the file.
    * @throws FileNotFoundException If the file cannot be found.
    * @throws PSInvalidXmlException If the file format is invalid.
    */
   public PSSpringConfiguration(File configFile) throws FileNotFoundException,
         IOException, SAXException, PSInvalidXmlException {
      if (configFile == null)
         throw new IllegalArgumentException("configFile may not be null");

      m_configFile = configFile;

      // load the doc

      FileInputStream in = null;
      Document doc;
      try
      {
         in = new FileInputStream(configFile);
         doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException e)
            {
            }
         }
      }

      // find all beans
      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Element root = (Element) tree.getCurrent();
      if (!BEANS.equals(root.getNodeName()))
      {
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING,
               BEANS);
      }

      m_beanDataMap = new LinkedHashMap<String, PSBeanData>();
      Element beanEl = tree.getNextElement(IPSBeanConfig.BEAN_NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      while (beanEl != null)
      {
         String id = PSSpringBeanUtils.getBeanName(beanEl);
         String className = PSSpringBeanUtils.getClassName(beanEl);
         PSBeanData data = new PSBeanData();
         
         data.m_className = className;
         
         data.m_element = beanEl;
         m_beanDataMap.put(id, data);
         beanEl = tree.getNextElement(IPSBeanConfig.BEAN_NODE_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }

      initConfigDoc(doc);
   }

   
   /**
    * Gets the bean with the specified name from the configuration. The class
    * specified by supplied bean name must implement {@link IPSBeanConfig} and
    * have a ctor that takes no parameters.
    * 
    * @param name The name of the bean, may not be <code>null</code> or empty.
    * 
    * @return The requested bean configuration, never <code>null</code>.
    * 
    * @throws PSInvalidXmlException If the XML in the spring configuration is
    *            invalid for the specified bean.
    * @throws RuntimeException if no matching bean is found.
    */
   public Object getBean(String name) throws PSInvalidXmlException
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");

      IPSBeanConfig bean = null;
      PSBeanData data = m_beanDataMap.get(name);
      if (data != null)
      {
         bean = PSSpringBeanUtils.createBean(data.m_className, data.m_element);
      }
      else
      {
         throw new RuntimeException("Bean with name \"" + name
               + "\" not found in file: " + m_configFile);
      }

      return bean;
   }
   
   /**
    * Return the root xml element of the specified bean definition.  This is 
    * intended for limited use where {@link #getBean(String)} cannot be used
    * i.e. there is no implementation of {@link IPSBeanConfig} available.
    * 
    * @param name The name of the bean, may not be <code>null</code> or empty.
    * 
    * @return The bean root element, or <code>null</code> if the specified
    * bean cannot be found.  
    */
   public Element getBeanXml(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      Element root = null;
      PSBeanData data = m_beanDataMap.get(name);
      if (data != null)
         root = data.m_element;
      
      return root;
   }
   
   /**
    * Set the root xml element of the specified bean definition. This is 
    * intended for limited use where {@link #setBean(IPSBeanConfig)} cannot
    * be used, i.e. there is no implementation of {@link IPSBeanConfig} 
    * available.
    * 
    * @param name the name of the bean, never <code>null</code> or empty.
    * @param xmlel the element representing the bean's data, 
    *   never <code>null</code>.
    */
   public void setBeanXml(String name, Element xmlel)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      if (xmlel == null)
      {
         throw new IllegalArgumentException("xmlel may not be null");
      }
      PSBeanData data = new PSBeanData();
      data.m_className = xmlel.getAttribute("class");
      data.m_element = xmlel;
      m_beanDataMap.put(name, data);
   }

   /**
    * Gets the set of bean id's from the configuration.
    * 
    * @return an iterator over the set of bean id's from the configuration.
    */
   public Iterator<String> getBeanIds()
   {
      return m_beanDataMap.keySet().iterator();
   }
   
   /**
    * Saves the supplied bean to the in-memory configuration. {@link #save()}
    * must be called to persist the change to the underlying configuration file.
    * 
    * @param beanConfig The bean configuration, may not be <code>null</code>.
    *           See {@link #getBean(String)} for more info.
    */
   public void setBean(IPSBeanConfig beanConfig)
   {
      PSBeanData data = m_beanDataMap.get(beanConfig.getBeanName());
      if (data == null)
      {
         data = new PSBeanData();
         m_beanDataMap.put(beanConfig.getBeanName(), data);
      }
      data.m_className = beanConfig.getClassName();
      data.m_element = beanConfig.toXml(m_configDoc);
   }

   /**
    * Remove the specified bean from the configuration.  {@link #save()}
    * must be called to persist the change to the underlying configuration file.
    * 
    * @param name The bean name, may not be <code>null</code> or empty.
    * 
    * @return <code>true</code> if the specified bean was found and removed,
    * <code>false</code> if not.
    */
   public boolean removeBean(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      return m_beanDataMap.remove(name) != null;
   }
   
   /**
    * Saves the in-memory configuration to the file specified during
    * construction.
    * 
    * @throws IOException If there are any errors writing to the file.
    */
   public void save() throws IOException
   {
      Element root = m_configDoc.getDocumentElement();

      // add all elements as children
      for (PSBeanData data : m_beanDataMap.values())
      {
         PSXmlDocumentBuilder.copyTree(m_configDoc, root, data.m_element);
      }

      // save to the file
      FileOutputStream out = null;
      try
      {
         out = new FileOutputStream(m_configFile);
         PSXmlDocumentBuilder.write(m_configDoc, out);
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (IOException e)
            {
            }
         }
      }

      // reinitialize the config doc
      initConfigDoc(m_configDoc);
   }

   /**
    * Creates a new, empty document with which elements are created when
    * {@link #setBean(IPSBeanConfig)} is called, and to which all elements are
    * added when {@link #save()} is called.
    * <p>
    * Copies all the attributes of the old root element to the new document
    * to maintain any such information. Checks and adds the spring namespace
    * schema configuration and xsi schema location to the attributes.
    * 
    * @param doc the document to copy the attributes from, assumed never
    *           <code>null</code>
    */
   private void initConfigDoc(Document doc)
   {
      Element oldroot = doc.getDocumentElement();

      m_configDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element beans = m_configDoc.createElement(BEANS);
      m_configDoc.appendChild(beans);

      NamedNodeMap attrmap = oldroot.getAttributes();
      int attrlen = attrmap.getLength();
      for (int i = 0; i < attrlen; i++)
      {
         Attr attr = (Attr) attrmap.item(i);
         beans.setAttribute(attr.getName(), attr.getValue());
      }
      
      if (StringUtils.isBlank(beans.getAttribute("xmlns")))
      {
         // Add the essential xmlns attributes when converting to Spring 2
         beans.setAttribute("xmlns",
               "http://www.springframework.org/schema/beans");
         beans.setAttribute("xmlns:xsi",
               "http://www.w3.org/2001/XMLSchema-instance");
         beans.setAttribute("xmlns:aop",
               "http://www.springframework.org/schema/aop");
         beans.setAttribute("xmlns:tx", 
               "http://www.springframework.org/schema/tx");
         beans.setAttribute("xsi:schemaLocation",
               "http://www.springframework.org/schema/beans "
               + "http://www.springframework.org/schema/beans/spring-beans-2.0.xsd "
               + "http://www.springframework.org/schema/tx "
               + "http://www.springframework.org/schema/tx/spring-tx-2.0.xsd "
               + "http://www.springframework.org/schema/aop "
               + "http://www.springframework.org/schema/aop/spring-aop-2.0.xsd");
      }
      
      // copy any non-beans elements
      PSXmlTreeWalker walker = new PSXmlTreeWalker(oldroot);
      Element child = walker.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      while (child != null)
      {
         if (!child.getNodeName().equals(IPSBeanConfig.BEAN_NODE_NAME))
            PSXmlDocumentBuilder.copyTree(m_configDoc, beans, child);            
         child = walker.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      
      // Check doc for
      // <tx:annotation-driven transaction-manager="sys_transactionManager"/>
      // and add if missing
      NodeList ad = m_configDoc.getElementsByTagNameNS(
            "http://www.springframework.org/schema/tx", "annotation-driven");
      if (ad.getLength() == 0)
      {
         Element tx = 
            m_configDoc.createElement("tx:annotation-driven");
         tx.setAttribute("transaction-manager", "sys_transactionManager");
         Element docel = m_configDoc.getDocumentElement();
         if (docel.getFirstChild() != null)
         {
            docel.insertBefore(tx, docel.getFirstChild());
         }
         else
         {
            docel.appendChild(tx);
         }
      }      
   }

   /**
    * The file specified during construction, never <code>null</code> or
    * modified after that.
    */
   private File m_configFile;

   /**
    * The document used for adding beans and saving the config. See
    * {@link #initConfigDoc(Document)} for details.
    */
   private Document m_configDoc;

   /**
    * Map of bean id's to bean data objects, initialized during construction,
    * never <code>null</code> after that.
    */
   private Map<String, PSBeanData> m_beanDataMap;

   // private xml constants
   /**
    * Beans element text
    */
   private static final String BEANS = "beans";

   /**
    * Simple structure to hold a bean's class name and element data.
    */
   class PSBeanData
   {
      /**
       * The class name
       */
      String m_className;

      /**
       * The element data
       */
      Element m_element;
   }
   
   
}
