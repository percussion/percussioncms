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

package com.percussion.install;

import com.percussion.util.IPSBrandCodeConstants.ServerTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents a single version of the brand code map.
 */
@SuppressWarnings("unchecked")
public class PSBrandCodeMapVersion
{
   /**
    * Constructor
    * @param version brand code map version, should be greater than 0.
    * @param sourceNode the Xml representation of this object, may not
    * be <code>null</code>
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    * or if version is not greater than 0.
    * @throws CodeException if any error occurs constructing the object from
    * the Xml representation.
    */
   public PSBrandCodeMapVersion(int version, Element sourceNode)
      throws CodeException
   {
      if (version < 1)
         throw new IllegalArgumentException(
            "Invalid brand code map version specified : " + version);
      m_version = version;
      fromXml(sourceNode);
   }

   /**
    * Returns the brand code map version, always greater than 0.
    * @return the brand code map version.
    */
   public int getVersion()
   {
      return m_version;
   }
   
   /**
    * Determine if the map version supports extended product info.  This 
    * means that additional server types and evaluation periods may be 
    * specified.
    * 
    * @return <code>true</code> if so, <code>false</code> otherwise.
    */
   public boolean supportsExtendedProductInfo()
   {
      return m_supportsExtendedProductInfo;
   }

   /**
    * Returns an array containing the mininum and maximum build numbers
    * supported for the specified Rhythmyx version.
    * Returns <code>null</code> if the specified Rhythmyx version is not
    * supported by this version of the brand code map.
    *
    * @param rxVersion the Rhythmyx version, may not be <code>null</code> and
    * non-empty.
    *
    * @return an array containing <code>2</code> elements if the specified
    * Rhythmyx version is supported by this version of the brand code map,
    * <code>null</code> otherwise. First member of the array is the minimum
    * build number supported, second member is the maximum build number
    * supported.
    *
    * @throws CodeException if any error occurs
    *
    * @throws IllegalArgumentException if <code>rxVersion</code> is
    * <code>null</code> or empty
    */
   public int[] getRhythmyxBuildNumbers(String rxVersion)
      throws CodeException
   {
      if ((rxVersion == null) || (rxVersion.trim().length() < 1))
         throw new IllegalArgumentException(
            "rxVersion may not be null or empty");

      int [] buildNumbers = null;

      PSBrandCodeElement bce =
         m_supportedRxVersionsList.getBrandCodeElement(
            IPSBrandCodeMap.ATTR_VALUE, rxVersion, false);

      if (bce != null)
      {
         String strBuildFrom = bce.getAttributeValue(
            IPSBrandCodeMap.ATTR_BUILD_FROM, true);
         String strBuildTo = bce.getAttributeValue(
            IPSBrandCodeMap.ATTR_BUILD_TO, true);

         String buildFromErrMsg = "Invalid buildFrom attribute value : " +
            strBuildFrom + " for Rhythmyx Version : " + rxVersion;
         String buildToErrMsg = "Invalid buildTo attribute value : " +
            strBuildTo + " for Rhythmyx Version : " + rxVersion;

         int buildFrom = PSBrandCodeUtil.toInt(strBuildFrom, buildFromErrMsg);
         int buildTo = PSBrandCodeUtil.toInt(strBuildTo, buildToErrMsg);

         buildNumbers = new int[2];
         buildNumbers[0] = buildFrom;
         buildNumbers[1] = buildTo;
      }

      return buildNumbers;
   }

   /**
    * Returns <code>true</code> if the specified Rhythmyx version and build
    * number is supported by this version of the brand code map.
    *
    * @param rxVersion the Rhythmyx version, may not be <code>null</code> and
    * non-empty.
    * @param buildFrom the minimum supported build number, should either
    * be <code>-1</code> or a 8 digit number
    * @param buildTo the maximum supported build number, should either be
    * <code>-1</code> or a 8 digit number. If not <code>-1</code>, then
    * should be greater than <code>buildFrom</code>
    *
    * @return <code>true</code> if the specified Rhythmyx version and build
    * number is supported by this version of the brand code map,
    * <code>false</code> otherwise.
    *
    * @throws CodeException if any supportedVersions/rxversion element exists
    * for this map which has invalid buildFrom or buildTo attribute value
    *
    * @throws IllegalArgumentException if <code>rxVersion</code> is
    * <code>null</code> or empty
    */
   public boolean supportsRhythmyxVersion(String rxVersion, int buildFrom,
      int buildTo) throws CodeException
   {
      if ((rxVersion == null) || (rxVersion.trim().length() < 1))
         throw new IllegalArgumentException(
            "rxVersion may not be null or empty");

      boolean supported = false;

      PSBrandCodeElement bce =
         m_supportedRxVersionsList.getBrandCodeElement(
            IPSBrandCodeMap.ATTR_VALUE, rxVersion, false);
      if (bce != null)
      {
         String strBuildFrom = bce.getAttributeValue(
            IPSBrandCodeMap.ATTR_BUILD_FROM, true);
         String strBuildTo = bce.getAttributeValue(
            IPSBrandCodeMap.ATTR_BUILD_TO, true);

         String buildFromErrMsg = "Invalid buildFrom attribute value : " +
            strBuildFrom + " for Rhythmyx Version : " + rxVersion;
         String buildToErrMsg = "Invalid buildTo attribute value : " +
            strBuildTo + " for Rhythmyx Version : " + rxVersion;

         int versionBuildFrom =
            PSBrandCodeUtil.toInt(strBuildFrom, buildFromErrMsg);
         int versionBuildTo = PSBrandCodeUtil.toInt(strBuildTo, buildToErrMsg);

         if ((buildFrom == versionBuildFrom) && (buildTo == versionBuildTo))
            supported = true;
      }
      return supported;
   }

   /**
    * Returns the map of licenses, the id of the license is the key and the
    * license name is the value.
    * @return a map containing the id of the license as key and license name
    * as value, never <code>null</code>
    * @throws CodeException if any license element does not have name or id
    * attribute defined.
    */
   public Map getLicenses()
      throws CodeException
   {
      String[] attrNames = new String[]
      {
         IPSBrandCodeMap.ATTR_ID, IPSBrandCodeMap.ATTR_NAME
      };
      List attrValuesList =
         m_licensesList.getAttributeList(attrNames, true);
      Map licenseMap = new HashMap();
      for (int i = 0; i < attrValuesList.size(); i++)
      {
         String[] attrValues = (String[])attrValuesList.get(i);
         licenseMap.put(attrValues[0], attrValues[1]);
      }
      return licenseMap;
   }

   /**
    * Returns the name of the license specified by the licenseId parameter.
    * @param licenseId the id of the license whose name is required
    * @return the name of license, never <code>null</code> or empty
    * @throws CodeException if any error occurs obtaining the name of the
    * specified license.
    */
   public String getLicenseName(int licenseId)
      throws CodeException
   {
      PSBrandCodeElement bce = m_licensesList.getBrandCodeElement(
         IPSBrandCodeMap.ATTR_ID, "" + licenseId, true);
      return bce.getAttributeValue(IPSBrandCodeMap.ATTR_NAME, true);
   }

   /**
    * Returns the name of the part specified by the partId parameter.
    * @param partId the id of the part whose name is required
    * @return the name of part, never <code>null</code> or empty
    * @throws CodeException if any error occurs obtaining the name of the
    * specified part.
    */
   public String getPartName(int partId)
      throws CodeException
   {
      PSBrandCodeElement bce = m_partsList.getBrandCodeElement(
         IPSBrandCodeMap.ATTR_ID, "" + partId, true);
      return bce.getAttributeValue(IPSBrandCodeMap.ATTR_NAME, true);
   }
   
   /**
    * Determines if the specified part is deprecated
    * 
    * @param partId The ID of the part to check.
    * 
    * @return <code>true</code> if deprecated, <code>false</code> if not.
    * 
    * @throws CodeException if any error occurs locating this property of the
    * specified part.
    */
   public boolean isPartDeprecated(int partId) throws CodeException
   {
      PSBrandCodeElement bce = m_partsList.getBrandCodeElement(
         IPSBrandCodeMap.ATTR_ID, String.valueOf(partId), true);
      
      return bce.getAttributeValue(IPSBrandCodeMap.ATTR_DEPRECATED, false) != 
         null;
   }

   /**
    * Returns a map containing components id as key and components name
    * as value.
    * @param partsIdList list containing the id of parts, may not be
    * <code>null</code>
    * @param componentList list of components, may not be <code>null</code>
    * @return map containing the components id as key and component
    * name as value
    * @throws CodeException if any error occurs retrieving the list of
    * components
    * @throws IllegalArgumentException if partsIdList or componentList is
    * <code>null</code>
    */
   public Map getComponents(List partsIdList, 
      PSBrandCodeElementList componentList) throws CodeException
   {
      if (partsIdList == null)
         throw new IllegalArgumentException("partsIdList may not be null");
      if (componentList == null)
         throw new IllegalArgumentException("componentList may not be null");

      int componentId = 0;
      Iterator it = partsIdList.iterator();
      while (it.hasNext())
      {
         String partId = (String)it.next();
         PSBrandCodeElement bcePart = m_partsList.getBrandCodeElement(
            IPSBrandCodeMap.ATTR_ID, "" + partId, true);
         String strComponentId = bcePart.getAttributeValue(
            IPSBrandCodeMap.ATTR_COMPONENT_ID, true);
         int compId = Integer.parseInt(strComponentId);
         componentId |= compId;
      }
      List idList = componentList.getAttributeList(IPSBrandCodeMap.ATTR_ID);
      Map compMap = new HashMap();
      it = idList.iterator();
      while (it.hasNext())
      {
         String strCompId = (String)it.next();
         int compId = Integer.parseInt(strCompId);
         if ((componentId & compId) == compId)
         {
            PSBrandCodeElement bceComp = componentList.getBrandCodeElement(
               IPSBrandCodeMap.ATTR_ID, strCompId, true);
            String componentName = bceComp.getAttributeValue(
               IPSBrandCodeMap.ATTR_NAME, true);
            compMap.put(strCompId, componentName);
         }
      }
      return compMap;
   }


   /**
    * Returns the map of parts for a specified license, the id of the
    * part is the key and the part name is the value.
    *
    * @param licenseId the license for which the map of parts is to be returned.
    *
    * @param partsType the type of part, must be one of the following values:
    * <code>PARTS_TYPE_REQUIRED</code>
    * <code>PARTS_TYPE_OPTIONAL_SELECTED</code>
    * <code>PARTS_TYPE_OPTIONAL_UNSELECTED</code>
    * <code>PARTS_TYPE_OPTIONAL</code>
    *
    * @return a map containing the id of the part as key and part name
    * as value, never <code>null</code>
    *
    * @throws CodeException if the specified license element could not be found
    * @throws IllegalArgumentException if <code>partsType</code> is invalid
    */
   public Map getParts(int licenseId, int partsType)
      throws CodeException
   {
      PSBrandCodeElement bce = m_licensesList.getBrandCodeElement(
         IPSBrandCodeMap.ATTR_ID, "" + licenseId, true);

      String errMsg = "Invalid required or optional part id specified for " +
         "license id : " + licenseId;

      int reqPartId = PSBrandCodeUtil.toInt(bce.getAttributeValue(
         IPSBrandCodeMap.ATTR_REQUIRED_PART_ID, true), errMsg);

      int selOptPartId = PSBrandCodeUtil.toInt(bce.getAttributeValue(
         IPSBrandCodeMap.ATTR_SELECTED_OPTIONAL_PART_ID, true), errMsg);

      int unselOptPartId = PSBrandCodeUtil.toInt(bce.getAttributeValue(
         IPSBrandCodeMap.ATTR_UNSELECTED_OPTIONAL_PART_ID, true), errMsg);

      int reqoptPartId = 0;
      switch (partsType)
      {
         case IPSBrandCodeMap.PARTS_TYPE_ALL:
         case IPSBrandCodeMap.PARTS_TYPE_REQUIRED:
            reqoptPartId  += reqPartId;
            if (partsType == IPSBrandCodeMap.PARTS_TYPE_REQUIRED)
               break;

         case IPSBrandCodeMap.PARTS_TYPE_OPTIONAL:
         case IPSBrandCodeMap.PARTS_TYPE_OPTIONAL_SELECTED:
            reqoptPartId  += selOptPartId;
            if (partsType == IPSBrandCodeMap.PARTS_TYPE_OPTIONAL_SELECTED)
               break;

         case IPSBrandCodeMap.PARTS_TYPE_OPTIONAL_UNSELECTED:
            reqoptPartId  += unselOptPartId;
            break;

         default:
            throw new IllegalArgumentException("Invalid parts type");
      }

      Map allPartsMap = getAllParts();
      Map partsMap = new HashMap();
      Iterator it = allPartsMap.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry item = (Map.Entry)it.next();
         String strPartId = (String)item.getKey();
         String partName = (String)item.getValue();
         int partId = Integer.parseInt(strPartId);
         if ((reqoptPartId & partId) == partId)
         {
            partsMap.put(strPartId, partName);
         }
      }
      return partsMap;
   }

   /**
    * Returns the map of all the parts, the id of the part is the key and the
    * part name is the value.
    * @return a map containing the id of the part as key and part name
    * as value, never <code>null</code>
    * @throws CodeException if any part element does not have name or id
    * attribute defined.
    */
   public Map getAllParts()
      throws CodeException
   {
      String[] attrNames = new String[]
      {
         IPSBrandCodeMap.ATTR_ID, IPSBrandCodeMap.ATTR_NAME
      };
      
      List attrValuesList =
         m_partsList.getAttributeList(attrNames, true);
      Map partsMap = new HashMap();
      for (int i = 0; i < attrValuesList.size(); i++)
      {
         String[] attrValues = (String[])attrValuesList.get(i);
         int id = Integer.parseInt(attrValues[0]);
         if (isPartDeprecated(id))
            continue;
         partsMap.put(attrValues[0], attrValues[1]);
      }
      return partsMap;
   }

   /**
    * Returns a list of property ids corresponding to the properties supported
    * by the licese specified by <code>licenseId</code>
    *
    * @param licenseId the id of the license whose properties is to be obtained
    *
    * @param allProperties list of all the property ids supported by the
    * brand code map, may not be <code>null</code> or empty
    *
    * @return the list of property ids (<code>String</code>),
    * never <code>null</code>, may be empty
    *
    * @throws CodeException if the specified license element could not be found
    * @throws IllegalArgumentException if <code>allProperties</code> is
    * <code>null</code> or empty
    */
   public List getLicenseProperties(int licenseId, List allProperties)
      throws CodeException
   {
      if ((allProperties == null) || (allProperties.isEmpty()))
         throw new IllegalArgumentException(
            "allProperties may not be null or empty");

      PSBrandCodeElement bce = m_licensesList.getBrandCodeElement(
         IPSBrandCodeMap.ATTR_ID, "" + licenseId, true);
      String propIdVal = bce.getAttributeValue(
         IPSBrandCodeMap.ATTR_PROPERTIES_ID, true);
      
      Set<String> propIds = new HashSet<String>();
      for (String strPropId : propIdVal.split(","))
      {
         propIds.add(strPropId);
      }

      List retList = new ArrayList();
      Iterator it = allProperties.iterator();
      while (it.hasNext())
      {
         String strListPropId = (String)it.next();
        
         if (propIds.contains(strListPropId))
            retList.add(strListPropId);
      }
      return retList;
   }
   
   /**
    * Get the list of types for which the specified license to be limited to.
    * 
    * @param licenseId The license id.
    * 
    * @return A list of limited types, empty if the license should be available
    * to all server types, never <code>null</code>.
    * 
    * @throws CodeException if the specified license is not valid.
    */
   public List<ServerTypes> getLicenseServerTypes(int licenseId) 
      throws CodeException
   {
      PSBrandCodeElement bce = m_licensesList.getBrandCodeElement(
         IPSBrandCodeMap.ATTR_ID, "" + licenseId, true);
      String limitedTypes = bce.getAttributeValue(
         IPSBrandCodeMap.ATTR_LIMITED_SERVER_TYPES, false);
      
      List<ServerTypes> result = new ArrayList<ServerTypes>();
      if (limitedTypes != null)
      {
         String[] types = limitedTypes.split(",");
         for (String type : types)
         {
            result.add(ServerTypes.valueOf(Integer.parseInt(type)));
         }
      }
      
      return result;
   }

   /**
    * Restore this object from an Xml representation.
    * @param sourceNode the element which represents a particular brand code
    * map version. Its tag name should equal <code>EL_MAP</code>
    * @throws IllegalArgumentException if sourceNode is <code>null</code>
    * or its tag name does not equal <code>EL_MAP</code>
    * @throws CodeException if any error occurs constructing the object from
    * the Xml representation.
    */
   public void fromXml(Element sourceNode)
      throws CodeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
      if (!(sourceNode.getTagName().equals(IPSBrandCodeMap.EL_MAP)))
         throw new IllegalArgumentException("invalid sourceNode element");

      m_supportsExtendedProductInfo = "y".equalsIgnoreCase(
         PSBrandCodeUtil.getAttributeValue(sourceNode,
            IPSBrandCodeMap.ATTR_SUPPORTS_EXTENDED_PRODUCT_INFO, false));
      
      Element el = null;
      // set the list of versions of Rhythmyx that this version of brand code
      // map supports
      el = PSBrandCodeUtil.getRequiredChildElement(sourceNode,
         IPSBrandCodeMap.EL_SUPPORTED_VERSIONS);
      m_supportedRxVersionsList = new PSBrandCodeElementList(el,
         IPSBrandCodeMap.EL_RX_VERSION,
         IPSBrandCodeMap.REQ_ATTRIBUTES_EL_RX_VERSION, null);

      // set the list of parts that this version of brand code map supports
      el = PSBrandCodeUtil.getRequiredChildElement(sourceNode,
         IPSBrandCodeMap.EL_PARTS);
      m_partsList = new PSBrandCodeElementList(el,
         IPSBrandCodeMap.EL_PART, IPSBrandCodeMap.REQ_ATTRIBUTES_EL_PART,
         IPSBrandCodeMap.OPT_ATTRIBUTES_EL_PART);

      // set the list of licenses that this version of brand code map supports
      el = PSBrandCodeUtil.getRequiredChildElement(sourceNode,
         IPSBrandCodeMap.EL_LICENSES);
      m_licensesList = new PSBrandCodeElementList(el,
         IPSBrandCodeMap.EL_LICENSE,
         IPSBrandCodeMap.REQ_ATTRIBUTES_EL_LICENSE,
         IPSBrandCodeMap.OPT_ATTRIBUTES_EL_LICENSE);
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
      // create the root element
      Element   root = doc.createElement(IPSBrandCodeMap.EL_MAP);
      root.setAttribute(IPSBrandCodeMap.ATTR_VERSION, "" + m_version);
      root.setAttribute(IPSBrandCodeMap.ATTR_SUPPORTS_EXTENDED_PRODUCT_INFO, 
         m_supportsExtendedProductInfo ? "y" : "n");
      
      Element el = null;
      // supprted Rhythmyx versions
      el = m_supportedRxVersionsList.toXml(doc);
      root.appendChild(el);

      // parts
      el = m_partsList.toXml(doc);
      root.appendChild(el);

      // licenses
      el = m_licensesList.toXml(doc);
      root.appendChild(el);

      return root;
   }

   /**
    * List of supported Rhythmyx versions, initialized in the
    * <code>fromXml</code> method,
    * never <code>null</code> after initialization.
    */
   private PSBrandCodeElementList m_supportedRxVersionsList = null;

   /**
    * List of parts, initialized in the <code>fromXml</code> method,
    * never <code>null</code> after initialization.
    */
   private PSBrandCodeElementList m_partsList = null;

   /**
    * List of licenses, initialized in the <code>fromXml</code> method,
    * never <code>null</code> after initialization.
    */
   private PSBrandCodeElementList m_licensesList = null;

   /**
    * brand code map version, its value is set in the constructor.
    */
   private int m_version = 0;
   
   /**
    * <code>true</code> if extended product info is supported,
    * <code>false</code> if not.
    */
   private boolean m_supportsExtendedProductInfo;
}


