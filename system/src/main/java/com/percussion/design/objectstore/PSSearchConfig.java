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

package com.percussion.design.objectstore;

import com.percussion.server.PSServer;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This object represents the configuration of the search engine in the server.
 * It encompases information including whether convera is configured, where the
 * server is located and what the configuration file is.
 * <p>Certain properties are accessed using {@link #getCustomProp(String)}.
 * Constants are provided for property names known to this class in the form
 * <code><i>TYPE</i>_KEY</code>. In an ideal world, these keys would not be
 * here because they are implementation specific and this object attempts to
 * be unaware of the implementation.
 */
public class PSSearchConfig extends PSComponent
{
   /**
    * This is the name of the custom property that contains the default value
    * for the synonym expansion. The presence of this property is optional.
    * If present and has a value of yes then the query is expanded with synonyms
    * before searching otherwise not.
    */
   public final static String SYNONYM_EXPANSION = "synonym_expansion";
   
   /**
    * This is the name of the custom property that contains the directory
    * of lucene index files. The presence of this property is
    * required if the search engine is enabled.
    */
   public final static String INDEXROOTDIR_KEY = "indexRootDir";
      
   /**
    * Key to properties for the delay time
    */
   public final static String PROPAGATION_DELAY = "propagation_delay";
   
   /**
    * This is the name of the custom property that contains a flag indicating
    * whether the content types should be indexed during server startup.  A 
    * value of yes or true will cause the content to be indexed on startup.
    * This property will be set to yes by default for new installs, but will be
    * set to no for upgrades.
    */
   public final static String INDEX_ON_STARTUP = "index_on_startup";
   
   /**
    * This is the name of the custom property that specifies a content type name that should 
    * be indexed during server startup.  A value matching a content type name will cause the 
    * content to be indexed on startup, an empty or invalid value will not index any content.
    * Once indexed, the property is cleared. 
    */
   public final static String INDEX_TYPE_ON_STARTUP = "index_type_on_startup";
   
   /**
    * Default constructor. The following defaults are set:
    * <table>
    *    <tr>
    *       <th>Property</th>
    *       <th>Default value</th>
    *    </tr>
    *    <tr>
    *       <td>full text search enabled</td>
    *       <td>false</td>
    *    </tr>
    *    <tr>
    *       <td>admin master</td>
    *       <td>false</td>
    *    </tr>
    *    <tr>
    *       <td>max search result</td>
    *       <td>-1</td>
    *    </tr>
    *    <tr>
    *       <td>custom props</td>
    *       <td>none</td>
    *    </tr>
    * </table>
    */
   public PSSearchConfig()
   {
      initDefaults();
   }

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param sourceNode the XML element node to construct this object from.
    * Never <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object.
    * May be <code>null</code>.
    * @param parentComponents the parent objects of this object. May be 
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not 
    * conform to the required dtd.
    */
   public PSSearchConfig(
      Element sourceNode,
      IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this(); // Setup defaults
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Copy constructor.
    * @param config object to copy, must never be <code>null</code>
    */
   public PSSearchConfig(PSSearchConfig config)
   {
      copyFrom(config);
   }

   /**
    * Generates an element that conforms to the following dtd. All optional
    * and implied values are added when the node is created, but they are not
    * required when calling the {@link #fromXml(Element, IPSDocument, ArrayList) 
    * fromXml} method.
    * <p>See the interface for further details.
    * 
    * <pre>
    * &lt;!ENTITY % True "yes"&gt;
    * &lt;!ENTITY % False "no"&gt;
    * &lt;!ENTITY % Boolean "(yes|no)"&gt;
    * 
    * &lt;!ELEMENT PSXSearchConfig (Properties?)&gt;
    * &lt;!ATTLIST PSXSearchConfig
    *    fullTextSearchEnabled %Boolean% %False%
    *    masterAdminServer %Boolean% %True%
    *    traceEnabled %Boolean% %False%
    *    maxSearchResult CDATA #OPTIONAL
    *    &gt;
    * &lt;!ELEMENT Properties (Property+)&gt;
    * &lt;!ELEMENT Property #PCDATA&gt;
    * &lt;!ATTLIST Property
    *    name CDATA #REQUIRED
    *    &gt;
    * </pre>
    */
   public Element toXml(Document doc)
   {
      Element search = doc.createElement(XML_NODE_NAME);

      search.setAttribute(TRACE_ENABLED_ATTR, 
            BOOLEAN_VALUE[m_traceEnabled ? 0 : 1]);
      search.setAttribute(SEARCH_ENABLED_ATTR, 
            BOOLEAN_VALUE[m_ftsEnabled ? 0 : 1]);
      search.setAttribute(ADMIN_MASTER_ATTR, 
            BOOLEAN_VALUE[m_adminMaster ? 0 : 1]);
      search.setAttribute(MAX_SEARCH_RESULT_ATTR, Integer
            .toString(m_maxSearchResult));

      if (!m_properties.isEmpty())
      {
         Element propsEl = doc.createElement(PROPERTIES_ELEM);
         search.appendChild(propsEl);
         Iterator propNames = m_properties.keySet().iterator();
         while (propNames.hasNext())
         {
            String propName = (String) propNames.next();
            Element prop = PSXmlDocumentBuilder.addElement(doc, propsEl, 
                  PROPERTY_ELEM, (String) m_properties.get(propName));
            prop.setAttribute(NAME_ATTR, propName);
         }
      }
      Element resultProcExEl = doc.createElement(RESULTPROCESSINGEXITS_ELEM);
      search.appendChild(resultProcExEl);
      resultProcExEl.appendChild(m_resultProcessingExitSet.toXml(doc));

      //Add analyzers element
      search.appendChild(getAnalyzersElement(doc));
      //Add text converters element 
      search.appendChild(getTextConvertersElement(doc));
      
      return search;
   }
   
   /**
    * Convenient method to create a text converter element.
    * 
    * @param doc The parent document assumed not <code>null</code>.
    * @return Text converters element never <code>null</code>.
    */
   private Element getTextConvertersElement(Document doc)
   {
      Element textConvertersEl = doc.createElement(TEXTCONVERTERS_ELEM);
      Iterator<String> iter = m_textConverters.keySet().iterator();
      while (iter.hasNext())
      {
         String name = iter.next();
         Element mtypeEl = doc.createElement(MIMETYPE_ELEM);
         mtypeEl.setAttribute(NAME_ATTR, name);
         mtypeEl.appendChild(m_textConverters.get(name).toXml(doc));
         textConvertersEl.appendChild(mtypeEl);
      }
      return textConvertersEl;

   }

   /**
    * Convenient method to create an analyzers element.
    * 
    * @param doc The parent document assumed not <code>null</code>.
    * @return Analyzers element never <code>null</code>.
    */
   private Element getAnalyzersElement(Document doc)
   {
      Element analyzersEl = doc.createElement(ANALYZERS_ELEM);
      Iterator<String> iter = m_analyzers.keySet().iterator();
      while (iter.hasNext())
      {
         String name = iter.next();
         Element localeEl = doc.createElement(LOCALE_ELEM);
         localeEl.setAttribute(NAME_ATTR, name);
         localeEl.appendChild(m_analyzers.get(name).toXml(doc));
         analyzersEl.appendChild(localeEl);
      }
      return analyzersEl;

   }

   /**
    * See interface for full description. If the enabled attribute is not
    * affirmative, the supplied xml is ignored and all properties are set to
    * default values using {@link #initDefaults()}.
    * 
    * @param sourceNode Expected to comply w/ the dtd defined in the {@link 
    * #toXml(Document) toXml} method.
    */
   public void fromXml(
      Element sourceNode,
      IPSDocument parentDoc,
      ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      String searchEnabled = sourceNode.getAttribute(SEARCH_ENABLED_ATTR);
      if (searchEnabled != null
            && searchEnabled.trim().equalsIgnoreCase(BOOLEAN_VALUE[0]))
      {
         m_ftsEnabled = true;
      }
      else
      {
         m_ftsEnabled = false;
      }

      String adminMaster = sourceNode.getAttribute(ADMIN_MASTER_ATTR);
      String traceEnabled = sourceNode.getAttribute(TRACE_ENABLED_ATTR);
      String nsMaxSearchResult = sourceNode
            .getAttribute(MAX_SEARCH_RESULT_ATTR);

      if (adminMaster != null
            && adminMaster.trim().equalsIgnoreCase(BOOLEAN_VALUE[0]))
      {
         m_adminMaster = true;
      }
      else
      {
         m_adminMaster = false;
      }
      if (traceEnabled != null
            && traceEnabled.trim().equalsIgnoreCase(BOOLEAN_VALUE[0]))
      {
         m_traceEnabled = true;
      }
      else
      {
         m_traceEnabled = false;
      }
      if (nsMaxSearchResult != null && nsMaxSearchResult.trim().length() > 0)
      {
         try
         {
            m_maxSearchResult = Integer.parseInt(nsMaxSearchResult);
            if (m_maxSearchResult < 0)
               m_maxSearchResult = -1;
         }
         catch (NumberFormatException nfe)
         {
            Object args[] = { "PSXSearchConfiguration",
                  MAX_SEARCH_RESULT_ATTR, nsMaxSearchResult };
            throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }

      m_properties.clear();
      Element propsEl = walker.getNextElement(PROPERTIES_ELEM);
      if (null != propsEl)
      {
         PSXmlTreeWalker propsWalker = new PSXmlTreeWalker(propsEl);
         Element propEl = null;
         do
         {
            if (null != propEl)
            {
               String propName = propEl.getAttribute(NAME_ATTR);
               if (null == propName || propName.trim().length() == 0)
               {
                  String[] args = { propEl.getNodeName(), NAME_ATTR, propName };
                  throw new PSUnknownNodeTypeException(
                        IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
               }
               String value = PSXmlTreeWalker.getElementData(propEl);
               m_properties.put(propName, value);
            }
            propEl = propsWalker.getNextElement(PROPERTY_ELEM);
         } while (null != propEl);
      }

      walker.setCurrent(sourceNode);
      m_resultProcessingExitSet.clear();
      Element resultProcessingExitsEl = walker
            .getNextElement(RESULTPROCESSINGEXITS_ELEM);
      if (null != resultProcessingExitsEl)
      {
         Element exSetEl = walker
               .getNextElement(PSExtensionCallSet.ms_NodeType);
         if (null != exSetEl)
            m_resultProcessingExitSet.fromXml(exSetEl, null, null);
      }

      walker.setCurrent(sourceNode);
      m_analyzers.clear();
      Element analyzersEl = walker.getNextElement(ANALYZERS_ELEM);
      if (analyzersEl != null)
      {
         PSXmlTreeWalker aWalker = new PSXmlTreeWalker(analyzersEl);
         Element mtypeEl = null;
         do
         {
            if (mtypeEl != null)
            {
               Element ext = aWalker
                     .getNextElement(PSExtensionCall.ms_NodeType);
               m_analyzers.put(mtypeEl.getAttribute(NAME_ATTR),
                     new PSExtensionCall(ext, null, null));
            }
            mtypeEl = aWalker.getNextElement(LOCALE_ELEM);
         } while (mtypeEl != null);
      }

      walker.setCurrent(sourceNode);
      m_textConverters.clear();
      Element textconvertersEl = walker.getNextElement(TEXTCONVERTERS_ELEM);
      if (textconvertersEl != null)
      {
         PSXmlTreeWalker tWalker = new PSXmlTreeWalker(textconvertersEl);
         Element localeEl = null;
         do
         {
            if (localeEl != null)
            {
               Element ext = tWalker
                     .getNextElement(PSExtensionCall.ms_NodeType);
               m_textConverters.put(localeEl.getAttribute(NAME_ATTR),
                     new PSExtensionCall(ext, null, null));
            }
            localeEl = tWalker.getNextElement(MIMETYPE_ELEM);
         } while (localeEl != null);
      }
   }

   /**
    * Copy search configuration to this object. A deep copy is made of all
    * mutable members.
    *  
    * @param config Config to copy, must never be <code>null</code>
    */
   public void copyFrom(PSSearchConfig config)
   {
      if (config == null)
      {
         throw new IllegalArgumentException("config must never be null");
      }
      super.copyFrom(config);
      setFtsEnabled(config.isFtsEnabled());
      
      setAdminMaster(config.isAdminMaster());
      setMaxSearchResult(config.getMaxSearchResult());
      m_properties = config.getCustomProps();
      m_resultProcessingExitSet = (PSExtensionCallSet) config
      .getSearchResultProcessingExtensions().clone();
      Map<String,PSExtensionCall> amap = config.getAnalyzers();
      Iterator<String> aiter=amap.keySet().iterator();
      while(aiter.hasNext())
      {
          String key = aiter.next();
          m_analyzers.put(key, (PSExtensionCall)amap.get(key).clone());
      }
      Map<String,PSExtensionCall> tmap = config.getAnalyzers();
      Iterator<String> titer=tmap.keySet().iterator();
      while(titer.hasNext())
      {
          String key = titer.next();
          m_textConverters.put(key, (PSExtensionCall)tmap.get(key).clone());
      }
   }

   //see base class
   public Object clone()
   {
      return new PSSearchConfig(this);
   }

   /**
    * See base class for more details. 
    * <p>The id of this object is not considered in <code>hashCode</code> or in 
    * this method. The custom property names and values are considered case-
    * sensitive. 
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSSearchConfig))
      {
         return false;
      }      

      PSSearchConfig test = (PSSearchConfig) o;

      if (m_ftsEnabled != test.m_ftsEnabled)
         return false;
         
      if (m_adminMaster != test.m_adminMaster)
         return false;

      if (m_maxSearchResult != test.m_maxSearchResult)
         return false;
      
      if (m_properties.size() != test.m_properties.size())
         return false;
      else if (!((HashMap) m_properties).equals(test.m_properties))
         return false;
      
      if(!m_resultProcessingExitSet.equals(test.m_resultProcessingExitSet))
         return false;

      if(!m_analyzers.equals(test.m_analyzers))
          return false;

      if(!m_textConverters.equals(test.m_textConverters))
          return false;

      return true;
   }

   /**
    * See base class for more details. 
    * <p>The id of this object is not considered in <code>equals</code> or in 
    * this method. 
    */
   public int hashCode()
   {
      String concat = "" + m_adminMaster + m_ftsEnabled;
      return concat.hashCode() + m_properties.hashCode()
                + m_resultProcessingExitSet.hashCode()
                + m_analyzers.hashCode()
                + m_textConverters.hashCode();
   }

   /**
    * See {@link #setFtsEnabled(boolean)} for details.
    * 
    * @return The flag used to enable/disable full text searching capability.
    */
   public boolean isFtsEnabled()
   {
      return m_ftsEnabled;
   }

   /**
    * See (@link #setMaxSearchResult(int) for details.
    * 
    * @return the max rows returned from the search result.
    */
   public int getMaxSearchResult()
   {
      return m_maxSearchResult;
   }
   
   /**
    * Get the propagation delay value from the configuration in milliseconds.
    * Defaults to 5 seconds (5000 millis) if not specified
    * @return the value of the {@link #PROPAGATION_DELAY} property converted
    * to an integer, or 5000 if incorrectly specified or not specified
    */
   public int getPropagationDelay()
   {
      String str = getCustomProp(PROPAGATION_DELAY);
      if (str != null && str.trim().length() != 0)
      {
         try
         {
            return Integer.parseInt(str);
         }
         catch (Exception e)
         {
            // Ignore
         }
      }
      return 5000;
   }

   /**
    * See {@link #setTraceEnabled(boolean)} for details.
    */
   public boolean isTraceEnabled()
   {
      return m_traceEnabled;
   }

   /**
    * A flag to indicate whether the search engine should output additional
    * information to aid debugging. Defaults to <code>false</code>.
    * 
    * @param enable <code>true</code> to enable, <code>false</code> to disable.
    */
   public void setTraceEnabled(boolean enable)
   {
      m_traceEnabled = enable;
   }

   /**
    * See {@link #setAdminMaster(boolean)} for details.
    * 
    * @return <code>true</code> if this server controls the search engine,
    * <code>false</code> otherwise.
    */
   public boolean isAdminMaster()
   {
      return m_adminMaster;
   }

   /**
    * See {@link #addCustomProp(String,String)} for details.
    * 
    * @param name The property name, never <code>null</code> or empty.
    * 
    * @return The value associated with the supplied name (never <code>null
    * </code>) or <code>null</code> if there is no property by that name.
    */
   public String getCustomProp(String name)
   {
      if (null == name || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name cannot be null or empty");
      }      
      return (String) m_properties.get(name);
   }

   /**
    * See {@link #addCustomProp(String,String)} for details.
    * 
    * @return Each entry has a <code>String</code> key, which is the property
    * name, and <code>String</code> value. Never <code>null</code>, may be 
    * empty. Caller takes ownership of the returned object.
    */
   public Map getCustomProps()
   {
      //since keys and values are immutable, we don't need to do anything else
      return (Map) ((HashMap) m_properties).clone();
   }

   /**
    * Custom properties are known only by the search engine implementation.
    * They are passed thru by the Rx server unmodified. Properties are order-
    * insensitive. 
    * 
    * @param name The property identifier, never <code>null</code> or empty,
    * case-sensitive.
    * 
    * @param value May be <code>null</code> or empty. <code>null</code> is
    * stored as an empty string.
    */
   public void addCustomProp(String name, String value)
   {
      if (null == name || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name cannot be null or empty");
      }
      if (null == value)
         value = "";
      m_properties.put(name, value);
   }

   /**
    * Removes the specified property from the current set of properties.
    * 
    * @param name Never <code>null</code> or empty. Case-sensitive.
    * 
    * @return the value of the removed property, may be <code>null</code>.
    */
   public String removeCustomProp(String name)
   {
      if (null == name || name.trim().length() == 0)
      {
         throw new IllegalArgumentException("name cannot be null or empty");
      }      
      return (String) m_properties.remove(name);
   }
   
   /**
    * Clears the set of custom properties currently held by this object.
    */
   public void removeAllCustomProps()
   {
      m_properties.clear();
   }

   /**
    * In a system that contains multiple Rx servers, only one of them can be
    * the search master administrator. The search master is responsible for 
    * maintaning the search engine configuration such that it matches the 
    * current master's configuration (namely content editor defs) and 
    * controlling the search engine processes.
    *  
    * @param isMaster Set to <code>true</code> to make this system the master, 
    * <code>false</code> otherwise. See {@link #PSSearchConfig()} for the 
    * default setting.
    */
   public void setAdminMaster(boolean isMaster)
   {
      m_adminMaster = isMaster;
   }
   
   /**
    * A flag to control whether to use the advanced search engine or not. 
    * Regardless of the setting, the brand code must have the FTS flag set in 
    * order for search to be usable. Defaults to <code>true</code>.
    * 
    * @param isEnabled <code>true</code> to turn on advanced search,
    * <code>false</code> otherwise. See {@link #PSSearchConfig()} for the 
    * default setting.
    */
   public void setFtsEnabled(boolean isEnabled)
   {
      m_ftsEnabled = isEnabled;
   }

   /**
    * Set the maximum rows returned from a search result.
    * 
    * @param maxSearchResult the new max search result, <code>-1</code> if 
    *    unlimited.
    */
   public void setMaxSearchResult(int maxSearchResult)
   {
      if (m_maxSearchResult < 0)
         m_maxSearchResult = -1;
      else
         m_maxSearchResult = maxSearchResult;
   }
   
   
   /**
    * Convenient method to get the absolute location of the root directory of
    * the search engine indexes. It gets the value from the custom property
    * {@link #INDEXROOTDIR_KEY}. Returns <code>null</code> if the property
    * does not exist, otherwise returns the absolute location of the index
    * directory. Adds the trailing slashes if does not exist.
    * 
    * @return Absolute location of index root directory or <code>null</code>,
    * if the property {@link #INDEXROOTDIR_KEY} is not set.
    */
   public String getIndexDirectory()
   {
      String irDir = getCustomProp(INDEXROOTDIR_KEY);
      if(irDir == null)
         return null;
      //normalize the path
      irDir = irDir.replace('\\', '/');
      File irDirFile = new File(irDir);
      if (!irDirFile.isAbsolute())
      {
         if (irDir.startsWith("/"))
            irDir = irDir.substring(1);
         String rxroot = PSServer.getRxDir().getAbsolutePath().replace('\\',
               '/');
         if(!rxroot.endsWith("/"))
            rxroot+="/";
         irDir = rxroot + irDir;
      }
      // Remove the trailing slashes.
      if (!irDir.endsWith("/"))
         irDir += "/";
      return irDir;
   }
   
   /**
    * @return <code>true</code> if a custom property with name
    * {@link #SYNONYM_EXPANSION} exists with a value of "yes" or "true",
    * otherwise <code>false</code>.
    */
   public boolean isSynonymExpansionRequired()
   {
      String prop = getCustomProp(SYNONYM_EXPANSION);
      if (prop != null
            && (prop.toLowerCase().equals("yes") || prop.toLowerCase().equals(
                  "true")))
         return true;
      return false;
   }
   
   /**
    * Disables the engine and sets all properties to reasonable values. 
    */
   private void initDefaults()
   {
      // Must configure to enable full-text search
      m_ftsEnabled = false;
      m_adminMaster = false;
      m_properties = new HashMap();
   }

   /**
    * Get the extensions to process the search results.  
    * 
    * @return extension call set, Never <code>null</code>, may be empty.
    */
   public PSExtensionCallSet getSearchResultProcessingExtensions()
   {
      return m_resultProcessingExitSet;
   }

   /**
    * Get the analyzers.
    * 
    * @return map of locale string and extension call, Never <code>null</code>,
    * may be empty.
    */
   public Map<String, PSExtensionCall> getAnalyzers()
   {
      return m_analyzers;
   }

   /**
    * Get the text converters.
    * 
    * @return Map of mimetype and extension call, Never <code>null</code>,
    * may be empty.
    */
   public Map<String, PSExtensionCall> getTextConverters()
   {
      return m_textConverters;
   }

   /**
    * See {@link #setFtsEnabled(boolean)} and {@link #PSSearchConfig()} for 
    * details.
    */
   private boolean m_ftsEnabled;
   
   /**
    * See {@link #setAdminMaster(boolean)} and {@link #PSSearchConfig()} for 
    * details. 
    */
   private boolean m_adminMaster;
   
   /**
    * See {@link #setTraceEnabled(boolean)} and {@link #PSSearchConfig()} 
    * for details.
    */
   private boolean m_traceEnabled;
   
   /**
    * See (@link #setMaxSearchResult(int) and {@link #PSSearchConfig()}for
    * details. Default to <code>-1</code>.
    */
   private int m_maxSearchResult = -1;
   
   /**
    * Contains the custom properties. Each entry has a <code>String</code> key
    * and a <code>String</code> value. Set in ctor, then never <code>
    * null</code>. Modified by {@link #addCustomProp(String, String)}, 
    * {@link #removeCustomProp(String)} and {@link #removeAllCustomProps()}.
    */
   private Map m_properties;

   /**
    * Search results processing extension set. Never <code>null</code> may be
    * empty.
    */
   PSExtensionCallSet m_resultProcessingExitSet = new PSExtensionCallSet();
   
   /**
    * Search results processing extension set. Never <code>null</code> may be
    * empty.
    */
   Map <String, PSExtensionCall> m_analyzers = 
      new HashMap<>();

   /**
    * Search results processing extension set. Never <code>null</code> may be
    * empty.
    */
   Map <String, PSExtensionCall> m_textConverters = 
      new HashMap<>();

   /*
    * Values for the element/attribute names used in the configuration file
    */
   public static final String XML_NODE_NAME = "PSXSearchConfig";
   private static final String SEARCH_ENABLED_ATTR = "fullTextSearchEnabled";
   private static final String ADMIN_MASTER_ATTR = "adminMaster";
   private static final String TRACE_ENABLED_ATTR = "traceEnabled";
   private static final String PROPERTIES_ELEM = "Properties";
   private static final String PROPERTY_ELEM = "Property";
   private static final String NAME_ATTR = "name";
   private static final String RESULTPROCESSINGEXITS_ELEM =
      "ResultProcessingExits";
   private static final String ANALYZERS_ELEM = "Analyzers";
   private static final String TEXTCONVERTERS_ELEM = "TextConverters";
   private static final String MIMETYPE_ELEM = "Mimetype";
   private static final String LOCALE_ELEM = "Locale";
   private static final String MAX_SEARCH_RESULT_ATTR = "maxSearchResult";
      
   /**
    * Contains the strings used as values when writing boolean properties to
    * the xml format. Index 0 is for <code>true</code> values, index 1 for 
    * <code>false</code> values.
    */
   private static final String[] BOOLEAN_VALUE = {"yes", "no"};

}
