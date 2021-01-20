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
package com.percussion.rx.services.deployer;

import javax.xml.bind.annotation.XmlElement;


/**
 * This class represents the community and the associated packages.
 * @author bjoginipally
 *
 */
public class PSCommunityPackage
{
   /**
    * ctor
    *
    */
   PSCommunityPackage()
   {
      
   }
   
   /**
    * ctor
    * @param name @see {@link #setCommunity(String)} for details.
    * @param packages @see {@link #setPackages(String)} for details.
    */ 
   PSCommunityPackage(String name, String packages)
   {
      m_community = name;
      m_packages = packages;
   }
   
   /**
    * @return the community never <code>null</code>
    */
   @XmlElement(name = "community")
   public String getCommunity()
   {
      return m_community;
   }
   
   /**
    * 
    * @param community must not be <code>blank</code>.
    */
   public void setCommunity(String community)
   {
      if (community == null || community.trim().length()<1)
         throw new IllegalArgumentException("community must not be blank");
      m_community = community;
   }
   
   /**
    * 
    * @return packages associated with the community, never <code>null</code>, may be
    * empty. {@link PSPackageService#NAME_SEPARATOR} separated list.
    */
   public String getPackages()
   {
      return m_packages;
   }

   /**
    * @param packages, May be <code>null</code> or empty. If
    * <code>null</code> sets it to empty string. If set must be a
    * {@link PSPackageService#NAME_SEPARATOR} separated list.
    */
   public void setPackages(String packages)
   {
      m_packages = packages;
   }
   
   
   private String m_community;
   private String m_packages;
   

}
