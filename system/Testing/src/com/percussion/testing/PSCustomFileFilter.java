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
package com.percussion.testing;

import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Object representation of the XML that defines an Ant custom filter using the
 * <code>PSJunitFileSelector</code> class.  Allows selection of test classes
 * using include/exclude patterns.  See {@link #toXml(Document)} for format 
 * of the XML.  
 */
public class PSCustomFileFilter
{
   /**
    * Construct an empty filter.  
    */
   public PSCustomFileFilter()
   {
      // default to std classpath param value
      m_filterClassPath = FILTER_CLASS_PATH_VALUE;
   }
   
   /**
    * Construct a file filter from its Xml representation.  See 
    * {@link #toXml(Document)} for format of the XML.
    *   
    * @param src The root element of the Xml representation, may not be
    * <code>null</code>.
    */
   public PSCustomFileFilter(Element src)
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(src);
      Element paramEl = tree.getNextElement(PARAM_EL, 
         tree.GET_NEXT_ALLOW_CHILDREN);
      while (paramEl != null)
      {
         extractParam(paramEl);
         paramEl = tree.getNextElement(PARAM_EL, tree.GET_NEXT_ALLOW_SIBLINGS);
      }
   }
   
   /**
    * Serializes this object to its XML representation.  The format is:
    * <pre><code>
    * &lt;!--
    * Contains the custom filter parameters, specifies the custom filter class.
    * 
    * Attributes:
    *    classname - the fully qualified classname of the filter class to use.
    *    classpathref - the classpath ref for the filter to use, currently 
    *       ignored by Ant due to bugs in Ant 1.54.  
    * -->
    * &lt;!ELEMENT custom (param*)>
    * &lt;!ATTLIST custom
    *    classname CDATA #REQUIRED
    *    classpathref CDATA #IMPLIED
    * >
    * 
    * &lt;!--
    *    Defines a parameter to be passed to the filter.  There are two basic
    *    types of parameters: the classpath and filters.  
    *       classpath - The classpath is specified with name=filterClasspath and 
    *          type=path. Only one isexpected, and the last one encountered is 
    *          used.  This is used to define a classloader to overcome the Ant
    *          bug that ignores the classpathref defined on the Custom element.
    *       filter - The filter definitions to use for selection.  There are 
    *          three different filters, and all three may be specified with a
    *          type as include or exclude.  Any of the three may be specified
    *          multiple times with different values, and the result is the
    *          union of the includes intersected with the intersection of the
    *          excludes.  The three filter names supported are packageFilter, 
    *          classNameFilter, classImplFilter.  See name doc below for more
    *          detials.     
    *    
    *    Attributes:
    *       name - the name of the parameter.  Possible values are:
    *          filterClassPath - defines the classpath to use when the selector
    *             class loads classes.  Must be used with a type=path or it is
    *             ignored. 
    *          
    *          packageFilter - defines a which packages are included or exluded.
    *             May include "?" and "*" as wildcards.

    *          classNameFilter - defines a which class names are included or 
    *             exluded. May include "?" and "*" as wildcards.
    *
    *          classImplFilter - defines a classes are included or exluded.  A
    *             class matches this filter if it is an "instanceof" the 
    *             specifed classname.  Must specified a fully qualified class
    *             name, no wildcards are supported.
    * 
    *       type - The type of parameter specified.  Possible values are:
    *          path - Defines a path, currently only the filterClassPath path
    *             is supported.
    * 
    *          exclude - defines an exclude filter.  May be used with any of the 
    *             packageFilter, classNameFilter, classImplFilter names.
    *
    *          include - defines an include filter.  May be used with any of the 
    *             packageFilter, classNameFilter, classImplFilter names.
    *
    *       value - The value of the parameter.  For type=path, it is the path
    *          definition as it would be specified in an environment variable or
    *          system property.  For filters, it depends on which filter is
    *          specified.
    * -->
    * &lt;!ELEMENT param EMPTY>
    * &lt;!ATTLIST param
    *    name (filterClasspath | packageFilter | classNameFilter | 
    *       classImplFilter) #REQUIRED
    *    type (exclude | include | path) #REQUIRED
    *    value CDATA #REQUIRED
    * >
    * <code/><pre/>
    * 
    * @param doc The doc to use, may not be <code>null</code>.
    * 
    * @return The root element of the Xml, never <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(CUSTOM_EL);
      root.setAttribute(CUST_CLASS_NAME_ATTR, 
         "com.percussion.ant.PSJunitFileSelector");
      root.setAttribute(CUST_CLASSPATH_REF_ATTR, "antExt.class.path");
      
      // add classpath if defined
      if (m_filterClassPath != null)
         appendParams(doc, root, PSIteratorUtils.iterator(m_filterClassPath), 
            FILTER_CLASS_PATH_ATTR, TYPE_PATH);
      
      // add package filters
      appendParams(doc, root, m_pkgIncludes.iterator(), PACKAGE_FILTER_ATTR, 
         TYPE_INCLUDE);
      appendParams(doc, root, m_pkgExcludes.iterator(), PACKAGE_FILTER_ATTR, 
         TYPE_EXCLUDE);
      
      // add class name filters
      appendParams(doc, root, m_classIncludes.iterator(), 
         CLASS_NAME_FILTER_ATTR, TYPE_INCLUDE);
      appendParams(doc, root, m_classExcludes.iterator(), 
         CLASS_NAME_FILTER_ATTR, TYPE_EXCLUDE);
         
      // add class instance filters
      appendParams(doc, root, m_instanceIncludes.iterator(), 
         CLASS_IMPL_FILTER_ATTR, TYPE_INCLUDE);
      appendParams(doc, root, m_instanceExcludes.iterator(), 
         CLASS_IMPL_FILTER_ATTR, TYPE_EXCLUDE);
         
      return root;
   }
   
   /**
    * Appends a param for each value specified to the supplied root element.
    * 
    * @param values The list of values to append, assumed not <code>null</code>, 
    * may be empty.
    * @param name The name of the param, assumed not <code>null</code> or empty.
    * @param type The type of param, assumed not <code>null</code> or empty.
    */ 
   private void appendParams(Document doc, Element root, Iterator values, 
      String name, String type)
   {
      while (values.hasNext())
      {
         String filter = (String)values.next();
         Element param = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
            PARAM_EL);
         param.setAttribute(NAME_ATTR, name);
         param.setAttribute(TYPE_ATTR, type);
         param.setAttribute(VALUE_ATTR, filter);
      }      
   }
   
   /**
    * Extracts a parameter from the supplied element and adds it to the 
    * appropriate list.
    * 
    * @param paramEl The element from which the parameter is extracted.  See
    * {@link #toXml(Document)} for a description of the <code>param</code>
    * element format expected.  Assumed not <code>null</code>.
    */
   private void extractParam(Element paramEl)
   {
      String name = paramEl.getAttribute(NAME_ATTR);
      String type = paramEl.getAttribute(TYPE_ATTR);
      String value = paramEl.getAttribute(VALUE_ATTR);
      
      boolean isInclude = true;
      boolean isPath = false;
      if (TYPE_PATH.equalsIgnoreCase(type))
      {
         if (name.equalsIgnoreCase(FILTER_CLASS_PATH_ATTR))
            isPath = true;
         else         
            throw new RuntimeException("Unsupported param name (" + name + 
               ") for type: " + type);            
      }
      else if (TYPE_EXCLUDE.equalsIgnoreCase(type))
      {
         isInclude = false;
      }            
      else if (!TYPE_INCLUDE.equalsIgnoreCase(type))
      {
         throw new RuntimeException("Invalid parameter type: " + type);         
      }
      
      if (value.trim().length() == 0)
         throw new RuntimeException("Invalid param element, empty value");
               
      if (isPath)
      {
         m_filterClassPath = value;
      }
      else if (name.equalsIgnoreCase(PACKAGE_FILTER_ATTR))
      {
         addPackageFilter(value, isInclude);
      }
      else if (name.equalsIgnoreCase(CLASS_NAME_FILTER_ATTR))
      {
         addClassNameFilter(value, isInclude);
      }
      else if (name.equalsIgnoreCase(CLASS_IMPL_FILTER_ATTR))
      {
         addInstanceFilter(value, isInclude);
      }
      else
      {
         throw new RuntimeException("Invalid param element, name=" + name);
      }
   }
   
   /**
    * Add a package filter.  See {@link #toXml(Document)} for a description
    * of how filters are used.
    * 
    * @param filter The value of the filter to add, may not be <code>null</code> 
    * or empty.
    * @param isInclude <code>true</code> to specify an include filter, 
    * <code>false</code> to specify an exclude.
    */
   public void addPackageFilter(String filter, boolean isInclude)
   {
      if (filter == null || filter.trim().length() == 0)
         throw new IllegalArgumentException("filter may not be null or empty");

      if (isInclude)
         m_pkgIncludes.add(filter);
      else
         m_pkgExcludes.add(filter);
   }

   /**
    * Add a class name filter.  See {@link #toXml(Document)} for a description
    * of how filters are used.
    * 
    * @param filter The value of the filter to add, may not be <code>null</code> 
    * or empty.
    * @param isInclude <code>true</code> to specify an include filter, 
    * <code>false</code> to specify an exclude.
    */
   public void addClassNameFilter(String filter, boolean isInclude)
   {
      if (filter == null || filter.trim().length() == 0)
         throw new IllegalArgumentException("filter may not be null or empty");

      if (isInclude)
         m_classIncludes.add(filter);
      else
         m_classExcludes.add(filter);
   }

   /**
    * Add a class impl filter.  See {@link #toXml(Document)} for a description
    * of how filters are used.
    * 
    * @param filter The value of the filter to add, may not be <code>null</code> 
    * or empty.
    * @param isInclude <code>true</code> to specify an include filter, 
    * <code>false</code> to specify an exclude.
    */   
   public void addInstanceFilter(String filter, boolean isInclude)
   {
      if (filter == null || filter.trim().length() == 0)
         throw new IllegalArgumentException("filter may not be null or empty");

      if (isInclude)
         m_instanceIncludes.add(filter);
      else
         m_instanceExcludes.add(filter);
   }
   
   /**
    * Get the defined package include filters.
    * 
    * @return An iterator over zero or more <code>String</code> objects, 
    * never <code>null</code>.
    */
   public Iterator getPackageIncludes()
   {
      return m_pkgIncludes.iterator();
   }
   
   /**
    * Get the defined package exclude filters.
    * 
    * @return An iterator over zero or more <code>String</code> objects, 
    * never <code>null</code>.
    */
   public Iterator getPackageExcludes()
   {
      return m_pkgExcludes.iterator();      
   }

   /**
    * Get the defined class name include filters.
    * 
    * @return An iterator over zero or more <code>String</code> objects, 
    * never <code>null</code>.
    */   
   public Iterator getClassNameIncludes()
   {
      return m_classIncludes.iterator();
   }
   
   /**
    * Get the defined class name exclude filters.
    * 
    * @return An iterator over zero or more <code>String</code> objects, 
    * never <code>null</code>.
    */   
   public Iterator getClassNameExcludes()
   {
      return m_classExcludes.iterator();
   }

   /**
    * Get the defined instance/impl include filters.
    * 
    * @return An iterator over zero or more <code>String</code> objects, 
    * never <code>null</code>.
    */   
   public Iterator getInstanceIncludes()
   {
      return m_instanceIncludes.iterator();
   }
   
   /**
    * Get the defined instance/impl exclude filters.
    * 
    * @return An iterator over zero or more <code>String</code> objects, 
    * never <code>null</code>.
    */   
   public Iterator getInstanceExcludes()
   {
      return m_instanceExcludes.iterator();
   }

   /**
    * Clears all filter lists.  
    */   
   public void clearAll()
   {
      m_pkgIncludes.clear();
      m_pkgExcludes.clear();
      m_classIncludes.clear();
      m_classExcludes.clear();
      m_instanceIncludes.clear();
      m_instanceExcludes.clear();
   }
   
   /**
    * List of package include filters, never null, may be empty.  Packages are
    * added by {@link #PSCustomFileFilter(Element)}, and the list is modified
    * by {@link #addPackageFilter(String, boolean)} and {@link #clearAll()}.
    */
   private List m_pkgIncludes = new ArrayList();
   private List m_pkgExcludes = new ArrayList();
   private List m_classIncludes = new ArrayList();
   private List m_classExcludes = new ArrayList();
   private List m_instanceIncludes = new ArrayList();
   private List m_instanceExcludes = new ArrayList();
   
   /**
    * Specifies the classpath to provide as a param in the filter Xml output.
    * May be <code>null</code> if constructed using 
    * {@link #PSCustomFileFilter(Element)} and no path param is specified.  If
    * constructed using {@link #PSCustomFileFilter()}, then it is initialized to
    * {@link #FILTER_CLASS_PATH_VALUE}.  Never emtpy, immutable after 
    * construction.
    */
   private String m_filterClassPath = null;
   
   /**
    * Default filter classpath value.
    */
   private static final String FILTER_CLASS_PATH_VALUE = "${filter.classpath}";
   
   // xml constants
   private static final String PACKAGE_FILTER_ATTR = "packageFilter";
   private static final String CLASS_NAME_FILTER_ATTR = "classNameFilter";
   private static final String CLASS_IMPL_FILTER_ATTR = "classImplFilter";
   private static final String FILTER_CLASS_PATH_ATTR = "filterClasspath";   
   private static final String TYPE_INCLUDE = "include";
   private static final String TYPE_EXCLUDE = "exclude";
   private static final String TYPE_PATH = "path";
   private static final String CUSTOM_EL = "custom";
   private static final String CUST_CLASS_NAME_ATTR = "classname";
   private static final String CUST_CLASSPATH_REF_ATTR = "classpathref";
   private static final String PARAM_EL = "param";
   private static final String NAME_ATTR = "name";
   private static final String TYPE_ATTR = "type";
   private static final String VALUE_ATTR = "value";

}