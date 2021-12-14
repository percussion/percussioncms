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
package com.percussion.i18n.ui;

import com.percussion.error.IPSBeansErrors;
import com.percussion.error.PSBeansException;
import com.percussion.i18n.PSTmxUnit;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.util.PSXMLDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is used to access key and values generated from the Rhythmyx
 * Language Tool.  The keys and values are used for the localization of
 * exception messages as well as labels and other GUI entities.  This class is
 * a Singleton.
 */
@SuppressWarnings("unchecked")
public class PSI18NTranslationKeyValues
{
   private PSI18NTranslationKeyValues()
   {

   }

   /**
    * For the correct dtd see {@link #toXml(Document) toXml(Document)}
    * Populates this object with data from the sourceNode.  All existing keys
    * and values will be replaced by the data in this <code>Element</code>.
    *
    * @param sourceNode  must not be <code>null</code> and must constrain to
    * said dtd.
    */
   public void fromXml(Element sourceNode) throws PSBeansException
   {
      if(sourceNode == null)
         throw new IllegalArgumentException("sourceNode must not be null");

      // clear existing values per contract:
      m_keyValueMap.clear();

      NodeList nl = sourceNode.getElementsByTagName(ELEM_KEYVALUE);

      Node keyEl = null;
      String key = "";
      String value = "";
      String mnemonic = null;
      String tooltip = null;

      try
      {
         for(int i=0; i < nl.getLength(); i++)
         {
            PSTmxUnit unit = null;
            keyEl = nl.item(i);
            key = PSXMLDomUtil.checkAttribute((Element)keyEl, ATTR_KEY, true);
            mnemonic = PSXMLDomUtil.checkAttribute((Element)keyEl, ATTR_MNEMONIC, false);
            tooltip = PSXMLDomUtil.checkAttribute((Element)keyEl, ATTR_TOOLTIP, false);
            value = PSXMLDomUtil.getElementData(keyEl);
            int mnemonicvalue = 0;
            if (mnemonic.length() > 0)
            {
               mnemonicvalue = Integer.parseInt(mnemonic);
            }
            unit = new PSTmxUnit(value, mnemonicvalue, tooltip);
            populateMap(key, unit);
         }
      }
      catch (Exception e)
      {
         throw new PSBeansException(
            IPSBeansErrors.XML_PROCESSING_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Returns an element populated with data from this object.  The dtd to
    * which this objects document will be constrained is as follows:
    * <pre>
    * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
    * &lt;!ELEMENT KeyValue (#PCDATA)&gt;
    * &lt;!ATTLIST KeyValue key CDATA #REQUIRED&gt;
    * &lt;!ELEMENT KeyValues (KeyValue+)&gt;
    * </pre>
    * @param doc must not be <code>null</code>
    * @return not <code>null</code>
    */
   public Element toXml(Document doc)
   {
      if(doc == null)
         throw new IllegalArgumentException("doc must not be null");

      // create root and its attributes
      Element root = doc.createElement(ELEM_KEYVALUES);

      // iterate through the map and create keys attribute and values
      Iterator it = m_keyValueMap.entrySet().iterator();
      Map.Entry me = null;
      Element keyEl = null;
      Text valueTextNode = null;
      while(it.hasNext())
      {
         me = (Map.Entry)it.next();
         PSTmxUnit unit = (PSTmxUnit) me.getValue();
         keyEl = doc.createElement(ELEM_KEYVALUE);
         valueTextNode = doc.createTextNode(unit.getValue());
         keyEl.appendChild(valueTextNode);
         
         keyEl.setAttribute(ATTR_KEY, (String)me.getKey());
         if (unit.getMnemonic() != 0)
         {
            StringBuilder buf = new StringBuilder(1);
            buf.append(Integer.toString(unit.getMnemonic()));
            keyEl.setAttribute(ATTR_MNEMONIC, buf.toString());
         }
         
         if (unit.getTooltip() != null)
         {
            keyEl.setAttribute(ATTR_TOOLTIP, unit.getTooltip());
         }

         root.appendChild(keyEl);
      }

      doc.appendChild(root);

      return root;
   }

   /**
    * This method loads data from the application found at the end of the
    * {@link #LOOKUP_APP_RESOURCE_URL LOOKUP_APP_RESOURCE_URL} given a
    * <code>IPSRemoteRequester</code>.  This serves as a convience method and
    * calls the {@link #fromXml(Element) fromXml(Element)} with the
    * document retrieved by the request to the url.
    *
    * @param requester with the must not be <code>null</code>.
    */
   public void load(IPSRemoteRequester requester)
      throws IOException, SAXException, PSBeansException
   {
      if(requester == null)
         throw new IllegalArgumentException("requester must not be null");

      Map<String,Object> parameters = new HashMap<>();
      if (m_packages.size() > 0)
      {
         parameters.put("sys_package", m_packages);         
      }
      
      Document doc = requester.getDocument(LOOKUP_APP_RESOURCE_URL, parameters);

      if(doc != null)
         fromXml(doc.getDocumentElement());

   }

   /**
    * Populates the instance of this class with the keys and values supplied
    * in the <code>keyValueMap</code>.
    *
    * @param keyValueMap the data that will be used to populate the instance of
    * this class.  All existing keys and values will be replaced by the data
    * in this <code>Map</code>.  Both the keys and values must be of type
    * <code>String</code> or a ClassCastExceptionException will be thrown.
    * Must not be <code>null</code>.
    */
   public void fromMap(Map keyValueMap)
   {
      if(keyValueMap == null)
         throw new IllegalArgumentException("keyValueMap must not be null.");

      // clear old values:
      m_keyValueMap.clear();

      Iterator it = keyValueMap.entrySet().iterator();
      Map.Entry me = null;
      while(it.hasNext())
      {
         me = (Map.Entry)it.next();
         populateMap((String)me.getKey(), (PSTmxUnit)me.getValue());
      }
   }

   /**
    * Returns the value that corresponds to the provided key.
    *
    * @param key, is treated as  case-sensitive key.  Must not be
    * <code>null</code> or empty.
    * @return the substring of the <code>key</code> (after the "@" if it is
    * in the key) if not found.  Never <code>null</code>.
    */
   public String getTranslationValue(String key)
   {
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key must not be null or empty.");

      if(m_keyValueMap.containsKey(key))
      {
         return ((PSTmxUnit)m_keyValueMap.get(key)).getValue();
      }
      else
      {
         // never return empty, but only after@:
         int atLoc = key.indexOf("@");

         // is key has an @ grab all after:
         if( atLoc > 0)
            // get just member part of key not class name:
            key = key.substring(atLoc + 1);

         return key;
      }
   }
   
   /**
    * Returns the tooltip that corresponds to the provided key.
    *
    * @param key, is treated as  case-sensitive key.  Must not be
    * <code>null</code> or empty.
    * @return the tooltip defined, or <code>null</code> if no tooltip exists
    */
   public String getTooltip(String key)
   {
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key must not be null or empty.");

      if(m_keyValueMap.containsKey(key))
      {
         return ((PSTmxUnit)m_keyValueMap.get(key)).getTooltip();
      }
      
      return null;
   }
   
   /**
    * @param key the key to lookup, must never be <code>null</code> or empty
    * @return the mnemonic or <code>0</code> if the key is not found or the
    * mnemonic was not defined
    */
   public int getMnemonic(String key)
   {
      if(key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key must not be null or empty.");

      if(m_keyValueMap.containsKey(key))
      {
         return ((PSTmxUnit)m_keyValueMap.get(key)).getMnemonic();
      }

      return 0;
   }   

   /**
    * Get the singleton instance of this class.
    *
    * @return The singleton instance of this class, never <code>null</code>.
    */
   public static PSI18NTranslationKeyValues getInstance()
   {
      if(ms_theInstance == null)
         ms_theInstance = new PSI18NTranslationKeyValues();

      return ms_theInstance;
   }

   /**
    * Populates the key value map of this class.  If either key or value
    * are <code>null</code> or empty neither will be added.
    *
    * @param key the key to be used in the map.  If <code>null</code>
    * or empty will not be added to map.
    * @param value the value to be used in the map.  If <code>null
    * </code> or empty it will not be added to map.
    */
   private void populateMap(String key, PSTmxUnit value)
   {
      if(key ==  null || key.trim().length() == 0 || value == null)
         return;

      m_keyValueMap.put(key, value);
   }

   /**
    * The keys and values in of this object.  Its keys and values will be
    * only <code>Strings</code>.  Populated by
    * {@link #populateMap(String, String) populateMap(String, String)}.
    * Never <code>null</code>.  Invariant.
    */
   private Map m_keyValueMap = new HashMap();

   /**
    * If this has any values, use them to limit the packages that should
    * be loaded from the server.
    */
   private List<String> m_packages = Collections.EMPTY_LIST;
   
   /**
    * Limit the package names to retrieve keys from the server for
    * @param packageNames the package names, may be <code>null</code> or
    * empty, which means to load all packages.
    */
   public void setPackages(List<String> packageNames)
   {
      if (packageNames == null)
         m_packages = Collections.EMPTY_LIST;
      else
         m_packages = packageNames;
   }

   /**
    * The single instance allowed.  Instantiated by
    * {@link #getInstance() getInstance()}, once instantiated never <code>null
    * </code>
    */
   private static PSI18NTranslationKeyValues ms_theInstance = null;

  /**
   * The root element name.
   * @see toXml(Document) toXml(Document)
   */
   public static final String ELEM_KEYVALUES = "KeyValues";

  /**
   * The child element name that will hold the key attribute.
   * @see toXml(Document) toXml(Document)
   */
   public static final String ELEM_KEYVALUE = "KeyValue";

  /**
   * Attribute name of the element <code>ELEM_KEYVALUE</code> representing the
   * key of the translation value.
   * @see toXml(Document) toXml(Document)
   */
   public static final String ATTR_KEY = "key";
   
   /**
    * Attribute name that represents the mnemonic value for the lookup
    */
   public static final String ATTR_MNEMONIC = "mnemonic";

   /**
    * Attribute name that represents the tooltip value for the lookup
    */
   public static final String ATTR_TOOLTIP = "tooltip";

   /**
    * The url path to the resource for this class.  This resource outputs
    * xml that conforms to the dtd for this class.
    * @see toXml(Document) toXml(Document doc)
    */
   public static final String LOOKUP_APP_RESOURCE_URL =
      "sys_i18nSupport/translationkeyvaluelookup.xml";


}
