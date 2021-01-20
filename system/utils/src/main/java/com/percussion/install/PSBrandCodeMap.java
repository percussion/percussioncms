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

package com.percussion.install;

import com.percussion.util.IPSBrandCodeConstants.ServerTypes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * In memory representation of the component map xml file.
 */
@SuppressWarnings("unchecked")
public class PSBrandCodeMap implements IPSBrandCodeMap
{

 /*************************************************************************
  * IPSBrandCodeMap functions
  *************************************************************************/

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
   public boolean isValidBrandCodeMapVersion(int bcmv)
   {
      return m_brandCodeMapVersions.containsKey(new Integer(bcmv));
   }

   
   public boolean supportsExtendedProductInfo(int brandCodeMapVersion) 
      throws CodeException
   {
      Integer ver = verifyBrandCodeMapVersion(brandCodeMapVersion);
      PSBrandCodeMapVersion bcmv = m_brandCodeMapVersions.get(ver);
      
      return bcmv.supportsExtendedProductInfo();
   }

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
   public Iterator<PSBrandCodeMapVersion> getBrandCodeMapVersions()
   {
      return m_brandCodeMapVersions.values().iterator();
   }

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
   public int getBrandCodeMapVersion(String rxVersion,
      int buildFrom, int buildTo) throws CodeException
   {
      if ((rxVersion == null) || (rxVersion.trim().length() < 1))
         throw new IllegalArgumentException(
            "rxVersion may not be null or empty");

      int bcmVer = -1;

      Iterator<Entry<Integer, PSBrandCodeMapVersion>> it = 
         m_brandCodeMapVersions.entrySet().iterator();
      while (it.hasNext() && (bcmVer == -1))
      {
         Entry<Integer, PSBrandCodeMapVersion> item = it.next();
         PSBrandCodeMapVersion bcmv = item.getValue();
         if (bcmv.supportsRhythmyxVersion(rxVersion, buildFrom, buildTo))
            bcmVer = bcmv.getVersion();
      }

      if (bcmVer == -1)
      {
         throw new CodeException(
            "Failed to find brand code map for Rhythmyx version : " +
            rxVersion + " and buildFrom : " + buildFrom + " and buildTo : "
            + buildTo);
      }

      return bcmVer;
   }

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
  public Map getLicenses(int brandCodeMapVersion)
      throws CodeException
   {
      Integer ver = verifyBrandCodeMapVersion(brandCodeMapVersion);
      PSBrandCodeMapVersion bcmv =
         m_brandCodeMapVersions.get(ver);
      return bcmv.getLicenses();
   }

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
   public String getLicenseName(int brandCodeMapVersion, int licenseId)
      throws CodeException
   {
      Integer ver = verifyBrandCodeMapVersion(brandCodeMapVersion);
      PSBrandCodeMapVersion bcmv =
         m_brandCodeMapVersions.get(ver);
      return bcmv.getLicenseName(licenseId);
   }
   
   public List<ServerTypes> getLimitedServerTypes(int brandCodeMapVersion,
      int licenseId) throws CodeException
   {
      Integer ver = verifyBrandCodeMapVersion(brandCodeMapVersion);
      PSBrandCodeMapVersion bcmv =
         m_brandCodeMapVersions.get(ver);
      return bcmv.getLicenseServerTypes(licenseId);
   }

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
   public String getPartName(int brandCodeMapVersion, int partId)
      throws CodeException
   {
      Integer ver = verifyBrandCodeMapVersion(brandCodeMapVersion);
      PSBrandCodeMapVersion bcmv =
         m_brandCodeMapVersions.get(ver);
      return bcmv.getPartName(partId);
   }

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
   public Map getComponents(int brandCodeMapVersion, List partsIdList)
      throws CodeException
   {
      if (partsIdList == null)
         throw new IllegalArgumentException("partsIdList may not be null");
      Integer ver = verifyBrandCodeMapVersion(brandCodeMapVersion);
      PSBrandCodeMapVersion bcmv =
         m_brandCodeMapVersions.get(ver);
      return bcmv.getComponents(partsIdList, m_componentList);
   }

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
   public Map getParts(int brandCodeMapVersion, int licenseId, int partsType)
      throws CodeException
   {
      Integer ver = verifyBrandCodeMapVersion(brandCodeMapVersion);
      PSBrandCodeMapVersion bcmv =
         m_brandCodeMapVersions.get(ver);
      return bcmv.getParts(licenseId, partsType);
   }

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
   public Map<String, String> getProperties()
      throws CodeException
   {
      String[] attrNames = new String[]
      {
         IPSBrandCodeMap.ATTR_ID, IPSBrandCodeMap.ATTR_NAME
      };
      List attrValuesList =
         m_propertiesList.getAttributeList(attrNames, true);
      Map<String, String> propertiesMap = new HashMap<String, String>();
      for (int i = 0; i < attrValuesList.size(); i++)
      {
         String[] attrValues = (String[])attrValuesList.get(i);
         propertiesMap.put(attrValues[0], attrValues[1]);
      }
      return propertiesMap;
   }

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
   public List getLicenseProperties(int brandCodeMapVersion, int licenseId)
      throws CodeException
   {
      Integer ver = verifyBrandCodeMapVersion(brandCodeMapVersion);
      PSBrandCodeMapVersion bcmv =
         m_brandCodeMapVersions.get(ver);
      List<String> allProperties = new ArrayList<String>(getProperties().keySet());
      return bcmv.getLicenseProperties(licenseId, allProperties);
   }

   /**
    * @see com.percussion.install.IPSBrandCodeMap
    */
   public List getRhythmyxVersions()
      throws CodeException
   {
      return m_currentVersionsList.getAttributeList(
         IPSBrandCodeMap.ATTR_RHYTHMYX_VERSION);
   }

 /*************************************************************************
  * PSBrandCodeMap functions
  *************************************************************************/

   /**
    * Returns the singleton instance of the <code>PSBrandCodeMap</code> object.
    * @return the singleton instance, never <code>null</code>
    * @throws CodeException if any error occurs parsing the component map
    * Xml document
    */
   public static synchronized PSBrandCodeMap newInstance()
      throws CodeException
   {
      if (ms_instance == null)
      {
         try
         {
            ms_instance = new PSBrandCodeMap();
         }
         catch (ParserConfigurationException pce)
         {
            ms_instance = null;
            throw new CodeException(pce.getLocalizedMessage());
         }
         catch (IOException ioe)
         {
            ms_instance = null;
            throw new CodeException(ioe.getLocalizedMessage());
         }
         catch (SAXException saxe)
         {
            ms_instance = null;
            throw new CodeException(saxe.getLocalizedMessage());
         }
      }
      return ms_instance;
   }

   /**
    * Constructor
    * @throws IllegalArgumentException if any error occurs parsing the
    * ComponentMap.xml file
    * @throws CodeException if any error occurs constructing the object from
    * the Xml representation or if a failure occurs while opening an input
    * stream to file ComponentMap.xml
    * @throws CodeException
    * @throws ParserConfigurationException
    * @throws IOException
    * @throws SAXException
    */
   private PSBrandCodeMap()
      throws CodeException, ParserConfigurationException,
            IOException, SAXException
   {
      InputStream stream = null;
      try
      {
         stream = PSBrandCodeMap.class.getResourceAsStream(
            IPSBrandCodeMap.COMPONENT_MAP_FILE);
         if (stream == null)
         {
            throw new CodeException(
               "Failed to open an input stream from file ComponentMap.xml");
         }
         Document componentMapDoc =
            PSBrandCodeUtil.createXmlDocument(stream);
         Element componentMapEl = componentMapDoc.getDocumentElement();
         fromXml(componentMapEl);
      }
      finally
      {
         if (stream != null)
         {
            try
            {
               stream.close();
            }
            catch (Exception e)
            {
               // no-op
            }
         }
      }
   }

   /**
    * Restore this object from an Xml representation.
    * @param sourceNode the document element of Component Map Document. Its
    * tag name should equal <code>EL_COMPONENT_MAP</code>
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    * or its tag name does not equal <code>EL_COMPONENT_MAP</code>
    * @throws CodeException if any error occurs constructing the object from
    * the Xml representation.
    */
   public void fromXml(Element sourceNode)
      throws CodeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
      if (!(sourceNode.getTagName().equals(IPSBrandCodeMap.EL_COMPONENT_MAP)))
         throw new IllegalArgumentException("invalid sourceNode element");

      Element el = null;
      String str = null;

      // create the list of current versions for different Rhythmyx versions
      el = PSBrandCodeUtil.getRequiredChildElement(sourceNode,
         IPSBrandCodeMap.EL_CURRENT_VERSIONS);
      m_currentVersionsList = new PSBrandCodeElementList(el,
         IPSBrandCodeMap.EL_CURRENT_VERSION,
         IPSBrandCodeMap.REQ_ATTRIBUTES_EL_CURRENT_VERSION, null);

      // create the list of components
      el = PSBrandCodeUtil.getRequiredChildElement(sourceNode,
         IPSBrandCodeMap.EL_COMPONENTS);
      m_componentList = new PSBrandCodeElementList(el, IPSBrandCodeMap.EL_COMPONENT,
         IPSBrandCodeMap.REQ_ATTRIBUTES_EL_COMPONENT, null);

      // create the list of properties
      el = PSBrandCodeUtil.getRequiredChildElement(sourceNode,
         IPSBrandCodeMap.EL_PROPERTIES);
      m_propertiesList = new PSBrandCodeElementList(el, IPSBrandCodeMap.EL_PROPERTY,
         IPSBrandCodeMap.REQ_ATTRIBUTES_EL_PROPERTY, null);

      // create the hash map of brand code map versions
      NodeList nl = sourceNode.getElementsByTagName(IPSBrandCodeMap.EL_MAP);
      if ((nl == null) || (nl.getLength() == 0))
      {
         throw new CodeException("Failed to find child element : " +
            IPSBrandCodeMap.EL_MAP + " under the parent element : " +
            sourceNode.getTagName());
      }
      for (int i = 0; i < nl.getLength(); i++)
      {
         el = (Element)nl.item(i);
         // get the version of the map
         str = PSBrandCodeUtil.getAttributeValue(el,
            IPSBrandCodeMap.ATTR_VERSION, true);

         String errMsg = "Illegal version specified for map version : " + str;
         int version = PSBrandCodeUtil.toInt(str, errMsg);
         if (version < 1)
            throw new CodeException(errMsg);

         PSBrandCodeMapVersion mapVersion =
            new PSBrandCodeMapVersion(version, el);
         m_brandCodeMapVersions.put(new Integer(version), mapVersion);
      }
   }

   /**
    * Serializes this object's state to Xml.
    * @param doc The document to use when creating elements, may not be
    * <code>null</code>
    * @return the element containing this object's state,
    * never <code>null</code>
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
      // create the root element - componentmap
      Element   root = doc.createElement(IPSBrandCodeMap.EL_COMPONENT_MAP);

      Element el = null;

      // current versions
      el = m_currentVersionsList.toXml(doc);
      root.appendChild(el);

      // list of components
      el = m_componentList.toXml(doc);
      root.appendChild(el);

      // list of properties
      el = m_propertiesList.toXml(doc);
      root.appendChild(el);

      // brand code map versions
      Iterator it = m_brandCodeMapVersions.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry item = (Map.Entry)it.next();
         PSBrandCodeMapVersion bcmv = (PSBrandCodeMapVersion)item.getValue();
         el = bcmv.toXml(doc);
         root.appendChild(el);
      }

      return root;
    }

   /**
    * Returns an <code>Integer</code> object wrapping the input parameter
    * brandCodeMapVersion.
    * @param brandCodeMapVersion the brand code map version to verify if it is
    * defined in the component map Xml document.
    * @return an <code>Integer</code> object wrapping the input parameter
    * brandCodeMapVersion, never <code>null</code>
    * @throws CodeException if brandCodeMapVersion is less than 1 or no brand
    * code map is defined for the specified version.
    */
   private Integer verifyBrandCodeMapVersion(int brandCodeMapVersion)
      throws CodeException
   {
      if (brandCodeMapVersion > 0)
      {
         Integer ret = new Integer(brandCodeMapVersion);
         if (m_brandCodeMapVersions.containsKey(ret))
            return ret;
      }
      throw new CodeException(
         "Invalid current brand code map version : " + brandCodeMapVersion);
   }

   /**
    * The singleton <code>PSBrandCodeMap</code> instance, initialized in the
    * <code>newInstance</code> method.
    */
   private static PSBrandCodeMap ms_instance = null;

   /**
    * The brand code map version to use, set in the
    * <code>setBrandCodeMapVersion</code> method.
    */
   //private int m_brandCodeMapVersion = 0;

   /**
    * Stores the current version of the component map for different Rhythmyx
    * versions, initialized in the <code>fromXml</code> method,
    * never <code>null</code> after initialization.
    */
   private PSBrandCodeElementList m_currentVersionsList = null;

   /**
    * List of components, initialized in the <code>fromXml</code> method,
    * never <code>null</code> after initialization.
    */
   private PSBrandCodeElementList m_componentList = null;

   /**
    * List of properties, initialized in the <code>fromXml</code> method,
    * never <code>null</code> after initialization.
    */
   private PSBrandCodeElementList m_propertiesList = null;

   /**
    * Map containg the brand code map versions. The version number is used
    * as key and <code>PSBrandCodeMapVersion</code> object as value,
    * never <code>null</code>, populated in the <code>fromXml</code> method.
    */
   private Map<Integer, PSBrandCodeMapVersion> m_brandCodeMapVersions = 
      new LinkedHashMap<Integer, PSBrandCodeMapVersion>();

}


