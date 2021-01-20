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

import com.percussion.utils.jdbc.PSDatasourceConfig;
import com.percussion.utils.jdbc.PSDatasourceResolver;
import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Helper class for all spring bean XML serialization
 */
public class PSSpringBeanUtils
{
   /**
    * Enforce static use.
    */
   private PSSpringBeanUtils()
   {
      
   }
   
   /**
    * Restore the bean from its XML representation
    * 
    * @param className The bean class name, may not be <code>null</code> or 
    * empty.
    * @param data The source element of the bean, may not be <code>null</code>.
    * 
    * @return The bean, never <code>null</code>.
    * @throws PSInvalidXmlException
    */
   public static IPSBeanConfig createBean(String className, Element data) 
      throws PSInvalidXmlException
   {
      IPSBeanConfig bean;
      try
      {
         bean = (IPSBeanConfig) Class.forName(className).newInstance();
         bean.fromXml(data);
         
         return bean;
      }
      catch (PSInvalidXmlException e)
      {
         throw (PSInvalidXmlException) e.fillInStackTrace();
      }
      catch (Exception e)
      {
         String[] args = new String[3];
         args[0] = className;
         args[1] = e.getLocalizedMessage();
         args[2] = PSXmlDocumentBuilder.toString(data);
         
         throw new PSInvalidXmlException(IPSXmlErrors.XML_RESTORE_ERROR, args, 
            e);
      }
   }
   
   /**
    * Get the name of the bean from the supplied bean element.
    * 
    * @param src The element may not be <code>null</code>.
    * 
    * @return The name, never <code>null</code> or empty.
    * 
    * @throws PSInvalidXmlException If the name attribute is not found or has
    * an empty value. 
    */
   public static String getBeanName(Element src) throws PSInvalidXmlException
   {
      return PSXmlUtils.checkAttribute(src, BEAN_ID_ATTR, true);
   }
   
   /**
    * Get the class name of the bean from the supplied bean element.
    * 
    * @param src The element may not be <code>null</code>.
    * 
    * @return The class name, never <code>null</code> or empty.
    * 
    * @throws PSInvalidXmlException If the class name attribute is not found or
    * has an empty value.
    */
   public static String getClassName(Element src) throws PSInvalidXmlException
   {
      
      return mapMovedClasses(PSXmlUtils.checkAttribute(src, BEAN_CLASSNAME_ATTR, true));
   }
   
   /**
    * Append a property element to the supplied element using the supplied name 
    * and value.
    *  
    * @param root The root element of the bean, usually obtained by a call to
    * {@link #createBeanRootElement(IPSBeanConfig, Document)}, may not be
    * <code>null</code>.
    * @param name The name of the property, may not be <code>null</code> or 
    * empty.
    * @param value The value of the property, may be <code>null</code> or empty.
    */
   public static void addBeanProperty(Element root, String name, 
      String value)
   {
      if (root == null)
         throw new IllegalArgumentException("root may not be null");
      
      Document doc = root.getOwnerDocument();
      if (doc == null)
         throw new IllegalArgumentException(
            "root must be associated with a document");
      
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      if (value == null)
         value = "";
      
      Element propEl = createPropElement(doc, root, name);
      propEl.setAttribute(BEAN_PROP_VAL_ATTR, value);
   }
   
   /**
    * Append a property element to the supplied element using the supplied name 
    * and a map for the value.
    *  
    * @param root The root element of the bean, usually obtained by a call to
    * {@link #createBeanRootElement(IPSBeanConfig, Document)}, may not be
    * <code>null</code>.
    * @param name The name of the property, may not be <code>null</code> or 
    * empty.
    * @param value The map to use as the value, may not be <code>null</code>, 
    * may be empty.
    */
   public static void addBeanProperty(Element root, String name, 
      Map<String, String> value)
   {
      if (root == null)
         throw new IllegalArgumentException("root may not be null");
      
      Document doc = root.getOwnerDocument();
      if (doc == null)
         throw new IllegalArgumentException(
            "root must be associated with a document");
      
      if (value == null)
         throw new IllegalArgumentException("value may not be null");
      
      Element propEl = createPropElement(doc, root, name);
      Element mapEl = PSXmlDocumentBuilder.addEmptyElement(doc, propEl, 
         BEAN_PROP_VAL_MAP);
      
      for (Map.Entry<String, String> entry : value.entrySet())
      {
         Element entryEl = PSXmlDocumentBuilder.addEmptyElement(doc, mapEl, 
            BEAN_PROP_VAL_MAP_ENTRY);
         entryEl.setAttribute(BEAN_PROP_VAL_MAP_KEY_ATTR, entry.getKey());
         
         PSXmlDocumentBuilder.addElement(doc, entryEl, 
            BEAN_PROP_VAL_MAP_VAL, entry.getValue());
      }
   }
   
   /**
    * Append a property element to the supplied element using the supplied name 
    * and a list of beans for the value.
    *  
    * @param root The root element of the bean, usually obtained by a call to
    * {@link #createBeanRootElement(IPSBeanConfig, Document)}, may not be
    * <code>null</code>.
    * @param name The name of the property, may not be <code>null</code> or 
    * empty.
    * @param value The list to use as the value, may not be <code>null</code>, 
    * may be empty.
    */
   public static void addBeanProperty(Element root, String name, 
      List<? extends IPSBeanConfig> value)
   {
      if (root == null)
         throw new IllegalArgumentException("root may not be null");
      
      Document doc = root.getOwnerDocument();
      if (doc == null)
         throw new IllegalArgumentException(
            "root must be associated with a document");
      
      if (value == null)
         throw new IllegalArgumentException("value may not be null");
      
      Element propEl = createPropElement(doc, root, name);
      Element listEl = PSXmlDocumentBuilder.addEmptyElement(doc, propEl, 
         BEAN_PROP_VAL_LIST);
      
      for (IPSBeanConfig config : value)
      {
         Element beanEl = config.toXml(doc);
         listEl.appendChild(beanEl);
      }
      
   }
   
   /**
    * Append a property element to the supplied element using the supplied name 
    * and value.
    *  
    * @param root The root element of the bean, usually obtained by a call to
    * {@link #createBeanRootElement(IPSBeanConfig, Document)}, may not be
    * <code>null</code>.
    * @param name The name of the property, may not be <code>null</code> or 
    * empty.
    * @param ref The name of the bean to reference, may not be <code>null</code>
    * or empty.
    */   
   public static void addBeanRef(Element root, String name, String ref)
   {
      if (root == null)
         throw new IllegalArgumentException("root may not be null");
      
      Document doc = root.getOwnerDocument();
      if (doc == null)
         throw new IllegalArgumentException(
            "root must be associated with a document");
      
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      if (StringUtils.isBlank(ref))
         throw new IllegalArgumentException("ref may not be null or empty");
      
      Element propEl = createPropElement(doc, root, name);
      Element refEl = PSXmlDocumentBuilder.addEmptyElement(doc, propEl, 
         BEAN_REF);
      refEl.setAttribute(BEAN_REF_NAME_ATTR, ref);      
   }
   
   
   /**
    * Creates an empty property element with the specified name attribute.
    * 
    * @param doc The doc to use, assumed not <code>null</code>.
    * @param root The element to which the property element is added, assumed 
    * not <code>null</code>. 
    * @param name The name of the property, assumed not <code>null</code> or 
    * empty.
    * 
    * @return The element, never <code>null</code>.
    */
   private static Element createPropElement(Document doc, Element root, 
      String name)
   {
      Element propEl = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         BEAN_PROPERTY);
      propEl.setAttribute(BEAN_PROP_NAME_ATTR, name);
      
      return propEl;
   }
   
   /**
    * Gets the next property element of the supplied root element with an 
    * expected property name.
    * 
    * @param root The root element, may not be <code>null</code>.
    * @param curProp The current property element.  The next sibling property
    * element is the one to return.  May be <code>null</code> to get the first
    * child property element of the root.
    * @param name The expected property name, may not be <code>null</code> or 
    * empty.
    * 
    * @return The element, never <code>null</code>.
    * @throws PSInvalidXmlException If the supplied element is invalid or if the
    * expected property cannot be found. 
    */
   public static Element getNextPropertyElement(Element root, Element curProp, 
      String name) throws PSInvalidXmlException
   {
      if (root == null)
         throw new IllegalArgumentException("root may not be null");
      
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(root);
      int flag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      if (curProp != null)
      {
         tree.setCurrent(curProp);
         flag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      }
      
      Element nextPropEl = tree.getNextElement(BEAN_PROPERTY, flag);
      if (nextPropEl == null)
      {
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            BEAN_PROPERTY);
      }
      else if (!name.equals(getBeanPropertyName(nextPropEl)))
      {
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_INVALID_ATTR, 
            new String[] {BEAN_PROPERTY, BEAN_PROP_NAME_ATTR, name});
      }
      
      return nextPropEl;
   }
   
   /**
    * Gets the next property element of the supplied root element.
    * 
    * @param root The root element, may not be <code>null</code>.
    * @param curProp The current property element.  The next sibling property
    * element is the one to return.  May be <code>null</code> to get the first
    * child property element of the root.
    * 
    * @return The element, <code>null</code> if no more properties are found.
    */
   public static Element getNextPropertyElement(Element root, Element curProp) 
   {
      if (root == null)
         throw new IllegalArgumentException("root may not be null");
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(root);
      int flag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      if (curProp != null)
      {
         tree.setCurrent(curProp);
         flag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      }
      
     return tree.getNextElement(BEAN_PROPERTY, flag);
   }   
   
   /**
    * Gets the element name of a simple bean property
    * 
    * @param source The source element, may not be <code>null</code>.
    * 
    * @return The property name, never <code>null</code> or empty.
    * 
    * @throws PSInvalidXmlException If the value cannot be located 
    */
   public static String getBeanPropertyName(Element source) 
   throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      return PSXmlUtils.checkAttribute(source, BEAN_PROP_NAME_ATTR, true);
   }
   
   /**
    * Get the element value of a bean property as a map.
    * 
    * @param source The source element, may not be <code>null</code>.
    * 
    * @return The map, never <code>null</code>, may be empty.
    * @throws PSInvalidXmlException If the supplied element is invalid.
    */
   public static Map<String, String> getBeanPropertyValueMap(Element source) 
      throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      Map<String, String> map = new HashMap<String, String>();
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
      Element mapEl = tree.getNextElement(BEAN_PROP_VAL_MAP, 
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (mapEl == null)
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
            BEAN_PROP_VAL_MAP);
      
      Element entryEl = tree.getNextElement(BEAN_PROP_VAL_MAP_ENTRY, 
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      while (entryEl != null)
      {
         String key = PSXmlUtils.checkAttribute(entryEl, 
            BEAN_PROP_VAL_MAP_KEY_ATTR, true);
         
         Element valEl = tree.getNextElement(BEAN_PROP_VAL_MAP_VAL, 
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (valEl == null)
            throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING,
               BEAN_PROP_VAL_MAP_VAL);
         String value = PSXmlUtils.getElementData(valEl, BEAN_PROP_VAL_MAP_VAL, 
            true);
         
         map.put(key, value);
         
         tree.setCurrent(entryEl);
         entryEl = tree.getNextElement(BEAN_PROP_VAL_MAP_ENTRY, 
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      
      return map;
   }
   
   /**
    * Used to traverse the elements of a list property value.  Use
    * {@link #getBeanPropertyValueList(Element)} if all list values can be
    * restored as beans.
    * 
    * @param root The root element, may not be <code>null</code> and must
    * contain a list element. 
    * @param curEl The last property value element returned from the list, 
    * <code>null</code> to get the first one, otherwise used to get the next one 
    * in the list.
    * 
    * @return The next element, may be <code>null</code> if the list does not
    * contain any more elements.
    *  
    * @throws PSInvalidXmlException If the supplied root does not contain a 
    * list element. 
    */
   public static Element getNextPropertyListElement(Element root, Element curEl) 
      throws PSInvalidXmlException
   {
      if (root == null)
         throw new IllegalArgumentException("root may not be null");

      PSXmlTreeWalker tree = new PSXmlTreeWalker(root);
      int flags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      if (curEl == null)
      {
         Element listEl = tree.getNextElement(BEAN_PROP_VAL_LIST, 
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (listEl == null)
            throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
               BEAN_PROP_VAL_LIST);
      }
      else
      {
         tree.setCurrent(curEl);
         flags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      }
      
      return tree.getNextElement(flags);
   }
   
   /**
    * Get the element value of a bean property as a list of beans.
    * 
    * @param source The source element, may not be <code>null</code>.
    * 
    * @return The list, never <code>null</code>, may be empty.
    * 
    * @throws PSInvalidXmlException If the supplied element is invalid.
    */
   public static List<IPSBeanConfig> getBeanPropertyValueList(Element source) 
      throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      List<IPSBeanConfig> list = new ArrayList<IPSBeanConfig>();
      Element entryEl = getNextPropertyListElement(source, null);
      while (entryEl != null)
      {
         if (!entryEl.getNodeName().equals(IPSBeanConfig.BEAN_NODE_NAME))
         {
            throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING, 
               IPSBeanConfig.BEAN_NODE_NAME);
         }
         
         list.add(createBean(getClassName(entryEl), entryEl));
         entryEl = getNextPropertyListElement(source, entryEl);
      }
      
      return list;
   }
   
   /**
    * Gets the element value of a simple bean property
    * 
    * @param source The source element, may not be <code>null</code>.
    * @param required <code>true</code> if a non-empty value must be found,
    * <code>false</code> if not.
    * 
    * @return The string value, never <code>null</code>, may be empty.
    * 
    * @throws PSInvalidXmlException If the value cannot be located 
    */
   public static String getBeanPropertyValue(Element source, boolean required) 
   throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      return PSXmlUtils.checkAttribute(source, BEAN_PROP_VAL_ATTR, required);
   }

   /**
    * Creates a standard bean root element for the supplied bean config.
    * Generally called from the {@link IPSBeanConfig#toXml(Document)} method of
    * a bean config implementation passing <code>this</code> and the supplied
    * document as parameters.
    * 
    * @param config The bean config, may not be <code>null</code>.
    * @param doc The doc to use, may not be <code>null</code>.
    * 
    * @return The root element.
    */
   public static Element createBeanRootElement(IPSBeanConfig config, 
      Document doc)
   {
      if (config == null)
         throw new IllegalArgumentException("config may not be null");
      
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      
      Element root = doc.createElement(IPSBeanConfig.BEAN_NODE_NAME);
      root.setAttribute(BEAN_ID_ATTR, config.getBeanName());
      root.setAttribute(BEAN_CLASSNAME_ATTR, config.getClassName());
      
      return root;
   }
   
   /**
    * Checks that the supplied element defines the expected bean root.
    * 
    * @param beanName The expected bean name, may not be <code>null</code> or empty.
    * @param className The expected bean class name, may not be <code>null</code> or empty. 
    * @param source The element to validate, may not be <code>null</code>.
    * 
    * @throws PSInvalidXmlException If the root is not valid for the specified
    * criteria.
    */
   public static void validateBeanRootElement(String beanName, String className, 
      Element source) throws PSInvalidXmlException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      
      if (StringUtils.isBlank(beanName))
         throw new IllegalArgumentException(
            "beanName may not be null or empty");
      
      if (StringUtils.isBlank(className))
         throw new IllegalArgumentException(
            "className may not be null or empty");
      
      if (!source.getNodeName().equals(IPSBeanConfig.BEAN_NODE_NAME))
         throw new IllegalArgumentException("Invalid source element");
      
      String test; 
      test = getBeanName(source);
      if (!beanName.equals(test))
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_INVALID_ATTR, 
            new String[] {IPSBeanConfig.BEAN_NODE_NAME, BEAN_ID_ATTR, test});
      
      test = getClassName(source);
      if (!className.equals(test))
         throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_INVALID_ATTR, 
            new String[] {IPSBeanConfig.BEAN_NODE_NAME, BEAN_CLASSNAME_ATTR, 
            test});
   }
   
   private static String mapMovedClasses(String className)
   {
      String mappedClass = movedClasses.get(className);
      return mappedClass==null ? className : mappedClass;
   }
   
   private static final Map<String,String> movedClasses = new HashMap<>();
   static {
      movedClasses.put("com.percussion.services.datasource.PSDatasourceResolver", PSDatasourceResolver.class.getName());
      movedClasses.put("com.percussion.services.datasource.PSDatasourceConfig", PSDatasourceConfig.class.getName());
   }
   
   private static final String BEAN_ID_ATTR = "id";
   private static final String BEAN_CLASSNAME_ATTR = "class";
   private static final String BEAN_PROPERTY = "property";
   private static final String BEAN_PROP_NAME_ATTR = "name";
   private static final String BEAN_PROP_VAL_ATTR = "value";
   private static final String BEAN_PROP_VAL_MAP = "map";
   private static final String BEAN_PROP_VAL_MAP_ENTRY = "entry";
   private static final String BEAN_PROP_VAL_MAP_VAL = "value";
   private static final String BEAN_PROP_VAL_MAP_KEY_ATTR = "key";
   private static final String BEAN_PROP_VAL_LIST = "list";
   private static final String BEAN_REF = "ref";
   private static final String BEAN_REF_NAME_ATTR = "bean";
}

