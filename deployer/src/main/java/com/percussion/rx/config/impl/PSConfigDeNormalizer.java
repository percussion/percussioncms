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
package com.percussion.rx.config.impl;

import com.percussion.utils.types.PSPair;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Creates default configuration document that conforms to
 * <code>localConfig.xsd</code> schema and returns it as string. This is a
 * singleton class.
 * 
 */
public class PSConfigDeNormalizer
{

   /**
    * Get singleton instance of the <code>PSConfigDeNormalizer</code>.
    * 
    * @return the instance, never <code>null</code>.
    */
   public static PSConfigDeNormalizer getInstance()
   {
      if (ms_instance == null)
         ms_instance = new PSConfigDeNormalizer();
      return ms_instance;
   }

   /**
    * Private ctor to inhibit direct instantiation.
    */
   private PSConfigDeNormalizer()
   {

   }

   /**
    * Creates default configuration document with the supplied publisher prefix,
    * publisher name and solution names. Adds the properties as per the
    * <code>localConfig.xsd</code> schema.
    * 
    * @param props The properties map from which the default config property
    * fragments needs to be created, must not be <code>null</code>, may be
    * empty.
    * @param errors List of error strings that will be written to the output xml
    * as comments, may be <code>null</code>.
    * @param publisherPrefix The publisher prefix must not be empty. It will be
    * added to the root element as a publisherPrefix attribute. The replacement
    * names that do not start with this name are not processed.
    * @param publisherName The publisher name is added to the default
    * configuration file as an attribute to the root element, must not be empty.
    * @param solutionName The solution name is added as value of name attribute
    * of element SolutionConfig, must not be empty.
    * @return String representation of default config xml document, that gets
    * built as per the <code>localConfig.xsd</code> schema.
    */
   public String getDeNormalizedXml(Map<String, Object> props,
         List<String> errors, String publisherName, String publisherPrefix,
         String solutionName)
   {
      if (props == null)
         throw new IllegalArgumentException("props must not be null");
      if (StringUtils.isBlank(publisherPrefix))
         throw new IllegalArgumentException(
               "publisherPrefix must not be empty");
      if (StringUtils.isBlank(publisherName))
         throw new IllegalArgumentException("publisherName must not be empty");
      if (StringUtils.isBlank(solutionName))
         throw new IllegalArgumentException("solutionName must not be empty");
      String prefix = publisherPrefix + "." + solutionName + ".";

      Document configDoc = PSXmlDocumentBuilder.createXmlDocument(
            ELEM_SOLUTION_CONFIGURATIONS, null, null);
      Element rootElem = configDoc.getDocumentElement();
      rootElem.setAttribute("xmlns:xsi",
            "http://www.w3.org/2001/XMLSchema-instance");
      rootElem
            .setAttribute("xsi:noNamespaceSchemaLocation", "localConfig.xsd");
      rootElem.setAttribute(ATTR_PUBLISHER_PREFIX, publisherPrefix);
      rootElem.setAttribute(ATTR_PUBLISHER_NAME, publisherName);
      Element solConfig = configDoc.createElement(ELEM_SOLUTION_CONFIG);
      solConfig.setAttribute(ATTR_NAME, solutionName);
      rootElem.appendChild(solConfig);
      // Build the rest of the elements from the map of properties here.
      Map<String, Object> sortedProps = sortMap(props);
      boolean hasFixMe = false;
      for (String name : sortedProps.keySet())
      {
         hasFixMe = hasFixMe
               | handleProperties(configDoc, solConfig, prefix, name,
                     sortedProps.get(name), true);
      }
      if (hasFixMe)
      {
         if (errors == null)
            errors = new ArrayList<String>(1);
         errors.add(0, ERROR_FIXME);
      }
      addComments(rootElem, errors);
      return PSXmlDocumentBuilder.toString(configDoc);
   }

   /**
    * Add comment element to the document. This will include generated date and
    * any errors that occurred.
    * 
    * @param parent assumed not <code>null</code>.
    * @param errors may be <code>null</code> or empty.
    */
   private void addComments(Element parent, List<String> errors)
   {
      Document doc = parent.getOwnerDocument();
      StringBuilder buff = new StringBuilder();
      buff.append(NEWLINE);
      buff.append(NEWLINE);
      buff.append("Generated on ");
      SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
      buff.append(formatter.format(new Date()));
      buff.append(NEWLINE);
      if (errors != null && !errors.isEmpty())
      {
         buff.append(NEWLINE);
         buff.append("Errors Occurred:");
         buff.append(NEWLINE);
         buff.append("================");
         buff.append(NEWLINE);
         for (String err : errors)
         {
            buff.append("- ");
            buff.append(err);
            buff.append(NEWLINE);
            buff.append(NEWLINE);
         }
      }
      Comment comment = doc.createComment(buff.toString());
      parent.insertBefore(comment, parent.getFirstChild());
   }

   /**
    * Recursively handle transforming the passed in properties into the
    * appropriate document elements. Only top level property names are supposed
    * to be fixed and if this method gets called recursively then the local
    * calls must pass <code>false</code> for nameTobeFixed parameter.
    * 
    * @param doc assumed not <code>null</code>.
    * @param parent the parent to which elements will be appended to. Assumed
    * not <code>null</code>.
    * @param name may be <code>null</code>.
    * @param prop assumed not <code>null</code>.
    * @param nameTobeFixed, a boolean flag to indicate whether the name to be
    * fixed or not. If false, the name gets used as is.
    * @return <code>true</code> if a FIXME exists in the output.
    */
   @SuppressWarnings("unchecked")
   private boolean handleProperties(final Document doc, final Element parent,
         final String prefix, final String name, final Object prop,
         boolean nameTobeFixed)
   {
      boolean hasFixMe = false;
      Element el = null;
      if (prop == null)
      {
         String fixedName = nameTobeFixed ? fixName(name, prefix) : name;
         hasFixMe = fixedName.startsWith(FIXME_INVALID_PREFIX);
         el = doc.createElement(ELEM_PROPERTY);
         el.setAttribute(ATTR_NAME, fixedName);
      }
      else if (prop instanceof Map)
      {
         Map<String, Object> sortedProps = sortMap((Map<String, Object>) prop);
         el = doc.createElement(ELEM_PROPERTY_SET);
         if (name != null)
         {
            String fixedName = nameTobeFixed ? fixName(name, prefix) : name;
            hasFixMe = fixedName.startsWith(FIXME_INVALID_PREFIX);
            el.setAttribute(ATTR_NAME, fixedName);
         }
         for (String key : sortedProps.keySet())
         {
            hasFixMe = hasFixMe
                  | handleProperties(doc, el, prefix, key, sortedProps
                        .get(key), false);
         }
      }
      else if (prop instanceof Collection)
      {
         String fixedName = nameTobeFixed ? fixName(name, prefix) : name;
         hasFixMe = fixedName.startsWith(FIXME_INVALID_PREFIX);
         el = doc.createElement(ELEM_PROPERTY);
         el.setAttribute(ATTR_NAME, fixedName);
         Iterator iter = ((Collection) prop).iterator();
         Element values = null;
         // Create a ELEM_PVALUES element if there are collection is empty, in
         // which case it creates an empty pvalues element or if the instance of
         // elements is String. Otherwise a propertySet element is created in
         // the next recursive call.
         if(((Collection) prop).isEmpty() || iter.next() instanceof String)
         {
            values = doc.createElement(ELEM_PVALUES);
            el.appendChild(values);
         }
         else
         {
            values = el;
         }
         for (Object obj : (Collection) prop)
         {
            hasFixMe = hasFixMe
                  | handleProperties(doc, values, prefix, null, obj, false);
         }
      }
      else if (prop instanceof PSPair)
      {
         PSPair pair = (PSPair) prop;
         el = doc.createElement(ELEM_PAIR);
         el.setAttribute(ATTR_VALUE + "1", toString(pair.getFirst()));
         el.setAttribute(ATTR_VALUE + "2", toString(pair.getSecond()));
      }
      else if (parent.getNodeName().equals(ELEM_PVALUES))
      {
         el = doc.createElement(ELEM_PVALUE);
         el.appendChild(doc.createTextNode(toString(prop)));
      }
      else
      {
         String fixedName = nameTobeFixed ? fixName(name, prefix) : name;
         hasFixMe = fixedName.startsWith(FIXME_INVALID_PREFIX);
         el = doc.createElement(ELEM_PROPERTY);
         el.setAttribute(ATTR_NAME, fixedName);
         el.setAttribute(ATTR_VALUE, toString(prop));
      }
      parent.appendChild(el);
      return hasFixMe;
   }

   /**
    * Fix the name by removing prefix and adding a Fix me note if the prefix is
    * invalid.
    * 
    * @param name
    * @param prefix
    * @return
    */
   private String fixName(String name, String prefix)
   {
      if (name.startsWith(prefix))
         return name.substring(prefix.length());
      return FIXME_INVALID_PREFIX + name;
   }

   /**
    * Handle Enum oddities by using the <code>name</code> method if it is an
    * Enum.
    * 
    * @param obj
    * @return
    */
   @SuppressWarnings("unchecked")
   private String toString(Object obj)
   {
      if (obj instanceof Enum)
         return ((Enum) obj).name();
      return obj.toString();

   }

   /**
    * Sort a map in alpha ascending order. Passes back an underlying
    * <code>TreeMap</code> to maintain sort order.
    * 
    * @return sorted <code>Map</code>, never <code>null</code>.
    */
   private Map<String, Object> sortMap(Map<String, Object> map)
   {
      return new TreeMap<String, Object>(map);
   }

   /**
    * Singleton instance for this class. Initialized in {@link #getInstance()}.
    */
   private static PSConfigDeNormalizer ms_instance;

   /**
    * Name of the root element of the default configuration file.
    */
   private static final String ELEM_SOLUTION_CONFIGURATIONS = "SolutionConfigurations";

   /**
    * Constant for the <code>pair</code> element.
    */
   private static final String ELEM_PAIR = "pair";

   /**
    * Constant for the <code>pvalue</code> element.
    */
   private static final String ELEM_PVALUE = "pvalue";

   /**
    * Constant for the <code>pvalues</code> element.
    */
   private static final String ELEM_PVALUES = "pvalues";

   /**
    * Constant for the <code>property</code> element.
    */
   private static final String ELEM_PROPERTY = "property";

   /**
    * Constant for the <code>propertySet</code> element.
    */
   private static final String ELEM_PROPERTY_SET = "propertySet";

   /**
    * Name of the solution config element name.
    */
   private static final String ELEM_SOLUTION_CONFIG = "SolutionConfig";

   /**
    * Publisher prefix attribute name.
    */
   private static final String ATTR_PUBLISHER_PREFIX = "publisherPrefix";

   /**
    * Publisher name attribute name
    */
   private static final String ATTR_PUBLISHER_NAME = "publisherName";

   /**
    * Generic attribute with name as name
    */
   private static final String ATTR_NAME = "name";

   /**
    * Constant for the <code>value</code> attribute.
    */
   private static final String ATTR_VALUE = "value";

   /**
    * Newline constant.
    */
   private static final String NEWLINE = "\n";

   /**
    * Constant for the fix me marker.
    */
   private static final String FIXME_INVALID_PREFIX = "FIXME(Invalid Prefix) ";

   /**
    * Fix me Error constant.
    */
   private static final String ERROR_FIXME = "One or more name attributes have invalid prefixes. "
         + "Search for FIXME to find the occurences to be fixed.";

   /**
    * Generated date format constant.
    */
   private static final String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss";

}
