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

import com.percussion.util.IPSBrandCodeConstants.EvalTypes;
import com.percussion.util.IPSBrandCodeConstants.ServerTypes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class stores the data for generating the brand code.
 */
@SuppressWarnings("unchecked")
public class PSBrandCodeData
{
   /**
    * Constructor
    * @throws CodeException if any error occurs getting the brand code map
    */
   public PSBrandCodeData()
      throws CodeException
   {
      m_brandCodeMap = PSBrandCodeMap.newInstance();
   }

   /**
    * Gets the version number of the brand code map to use for generating the
    * brand code.
    * @return the the version number of the brand code map to use for generating
    * the brand code.
    */
   public int getBrandCodeMapVersion()
   {
      return m_brandCodeMapVersion;
   }

   /**
    * Sets the version number of the brand code map to use for generating the
    * brand code.
    * @param version the version number of the brand code map, should be
    * greater than 0.
    * @throws IllegalArgumentException if version is not greater than 0.
    */
   public void setBrandCodeMapVersion(int version)
   {
      if (version < 1)
         throw new IllegalArgumentException("Illegal brand code version number");
      m_brandCodeMapVersion = version;
   }

   /**
    * Returns the type of server.
    * 
    * @return the type, never <code>null</code>.
    */
   public ServerTypes getServerType()
   {
      return m_serverType;
   }

   /**
    * Sets the type of server
    * 
    * @param type The type, may not be <code>null</code>.
    */
   public void setServerType(ServerTypes type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      m_serverType = type;
   }


   /**
    * Returns the product expiry period.
    * 
    * @return the eval type, never <code>null</code> 
    */
   public EvalTypes getProductExpires()
   {
      return m_productExpires;
   }

   /**
    * Sets the product expiry period.
    * 
    * @param type The eval type, may not be <code>null</code>.
    */
   public void setProductExpires(EvalTypes type)
   {
      if (type == null)
         throw new IllegalArgumentException("type may not be null");
      
      m_productExpires = type;
   }

   /**
    * Returns the date on which the brand code expires.
    * @return the date on which the brand code expires, never <code>null</code>
    */
   public Date getCodeExpires()
   {
      return m_codeExpires;
   }

   /**
    * Sets the date on which the brand code expires.
    * @param expire the date on which the brand code expires, may not be
    * <code>null</code>
    * @throws IllegalArgumentException if expire is <code>null</code>
    */
   public void setCodeExpires(Date expire)
   {
      if (expire == null)
         throw new IllegalArgumentException("expire may not be null");
      m_codeExpires = expire;
   }

   /**
    * Gets the value of the license id.
    * @return the value of the license id, always non-negative.
    */
   public int getLicenseId()
   {
      return m_licenseId;
   }

   /**
    * Returns the name of the license.
    * @return the name of the license, never <code>null</code> or empty
    */
   public String getLicenseName()
   {
      try
      {
         return m_brandCodeMap.getLicenseName(m_brandCodeMapVersion, m_licenseId);
      }
      catch (CodeException cex)
      {
         throw new RuntimeException(cex.getLocalizedMessage());
      }
   }

   /**
    * Sets the value of the license id.
    * @param licenseId the value of the license id, should be non-negative.
    * @throws IllegalArgumentException if license id is less than 0.
    */
   public void setLicenseId(int licenseId)
   {
      if (licenseId < 0)
         throw new IllegalArgumentException("Illegal license id.");
      m_licenseId = licenseId;
   }

   /**
    * Returns the list containing the names of selected parts.
    * @return the list containing the names of selected parts, never
    * <code>null</code>
    */
   public List getPartsName()
   {
      List partsName = new ArrayList();
      try
      {
         Iterator it = m_partsId.iterator();
         while (it.hasNext())
         {
            String strPartId = (String)it.next();
            int partId = Integer.parseInt(strPartId);
            String partName =
                   m_brandCodeMap.getPartName(m_brandCodeMapVersion, partId);
            partsName.add(partName);
         }
         return partsName;
      }
      catch (CodeException cex)
      {
         throw new RuntimeException(cex.getLocalizedMessage());
      }
   }

   /**
    * Returns the list containing the part ids for selected parts.
    * @return the list containing the part ids for selected parts.
    */
   public List getPartsId()
   {
      return m_partsId;
   }

   /**
    * Sets the list containing the part ids for selected parts.
    * @param partsId the the list containing the part ids for selected parts, may
    * not be <code>null</code>
    * @throws IllegalArgumentException if parts is <code>null</code>
    */
   public void setPartsId(List partsId)
   {
      if (partsId == null)
         throw new IllegalArgumentException("partsId may not be null");
      m_partsId = partsId;
   }

   /**
    * Returns the map containing the property id and quantity of each property.
    * @return the map containing the property id and quantity of each property.
    */
   public Map getProperties()
   {
      return m_propertiesMap;
   }

   /**
    * Sets the map containing the property id and quantity of each property.
    * @param properties the map containing the property id and quantity of
    * each property.
    * @throws IllegalArgumentException if properties is <code>null</code>
    */
   public void setProperties(Map properties)
   {
      if (properties == null)
         throw new IllegalArgumentException("properties may not be null");
      m_propertiesMap = properties;
   }

   /**
    * Returns <code>true</code> if the component corresponding to the input
    * componentId parameter is licensed, <code>false</code> otherwise.
    * @param componentId the id of the component, should be one of the constant
    * values from <code>com.percussion.util.IPSBrandCodeConstants</code>
    * interface, should be greater than 0.
    * @return <code>true</code> if the component corresponding to the input
    * componentId parameter is licensed, <code>false</code> otherwise.
    * @throws IllegalArgumentException if componentId is less than 1
    * @throws CodeException if any error occurs retrieving the list of
    * licensed components
    */
   public boolean isComponentLicensed(int componentId)
      throws CodeException
   {
      if (componentId < 1)
         throw new IllegalArgumentException("Invalid component id");
      Map licCompMap = getLicensedComponents();
      if (licCompMap.containsKey("" + componentId))
         return true;
      return false;
   }

   /**
    * Returns a map containing the licensed component's id as key and component
    * name as value.
    * @return map containing the licensed component's id as key and component
    * name as value
    * @throws CodeException if any error occurs retrieving the list of
    * licensed components
    */
   public Map getLicensedComponents()
      throws CodeException
   {
      return m_brandCodeMap.getComponents(
         m_brandCodeMapVersion, m_partsId);
   }

   /**
    * Returns the value of the property corresponding to the input
    * propertyId parameter.
    * @param propertyId the id of the property whose value is required,
    * should be one of the constant values from
    * <code>com.percussion.util.IPSBrandCodeConstants</code> interface,
    * should be non-negative.
    * @return the value of the property corresponding to the input
    * propertyId parameter.
    * @throws IllegalArgumentException if componentId is less than 0
    */
   public int getPropertyValue(int propertyId)
   {
      if (propertyId < 0)
         throw new IllegalArgumentException("Invalid property id");

      if (!m_propertiesMap.containsKey("" + propertyId))
         return 0;
      String propValue = (String)m_propertiesMap.get("" + propertyId);
      return Integer.parseInt(propValue);
   }

   /**
    * Determine whether the brand code has expired or not.
    * @return <code>true</code> if the brand code has expired,
    * <code>false</code> otherwise.
    */
   public boolean hasExpired()
   {
      GregorianCalendar now = new GregorianCalendar();
      GregorianCalendar expire = new GregorianCalendar();
      expire.setTime(getCodeExpires());

      now.clear(Calendar.HOUR);
      now.clear(Calendar.MINUTE);
      now.clear(Calendar.SECOND);
      now.clear(Calendar.MILLISECOND);
      expire.clear(Calendar.HOUR);
      expire.clear(Calendar.MINUTE);
      expire.clear(Calendar.SECOND);
      expire.clear(Calendar.MILLISECOND);

      if(expire.before(now))
         return(true);
      return(false);
   }

   /**
    * The version of the brand code map to use for generating the brand code.
    */
    private int m_brandCodeMapVersion = 1;

   /**
    * type of server 
    */
   private ServerTypes m_serverType = ServerTypes.PRODUCTION;

   /**
    * product expiry period 
    */
   private EvalTypes m_productExpires = EvalTypes.NOT_EVAL;

   /**
    * Date on which the brand code expires.
    */
   private Date m_codeExpires = null;

    /**
     * license id.
     */
   private int m_licenseId = 0;

   /**
    * list for storing the part ids for selected parts.
    */
   private List m_partsId = null;

   /**
    * map for storing the property id and quantity of each property. For example,
    * the property id of "Number of Processors" (0) and its quantity.
    */
   private Map m_propertiesMap = null;

   /**
    * In memory representation of the Component Map Xml, initialized in the
    * constructor, never <code>null</code> after initialization.
    */
   private IPSBrandCodeMap m_brandCodeMap = null;

}



