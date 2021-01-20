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

/**
 * This class represents the packages and associated communities.
 * 
 * @author bjoginipally
 * 
 */
public class PSPackageCommunity
{

   /**
    * ctor
    * 
    */
   PSPackageCommunity()
   {
      super();

   }

   /**
    * Ctor
    * 
    * @param pkg must not be blank.
    * @param communities @see #setCommunities(String) for details.
    */
   PSPackageCommunity(String pkg, String communities)
   {
      m_package = pkg;
      setCommunities(communities);
   }

   /**
    * 
    * @return communties for a package, never <code>null</code>, may be
    * empty.
    */
   public String getCommunities()
   {
      return m_communities;
   }

   /**
    * @param communities, May be <code>null</code> or empty. If
    * <code>null</code> sets it to empty string. If set must be a
    * {@link PSPackageService#NAME_SEPARATOR} separated list.
    */
   public void setCommunities(String communities)
   {
      if (communities == null)
         communities = "";
      m_communities = communities;
   }

   /**
    * @return The name of the package, never <code>null</code>, or empty.
    */
   public String getPackage()
   {
      return m_package;
   }

   /**
    * @param pkg must not be blank.
    */
   public void setPackage(String pkg)
   {
      if (pkg == null || pkg.trim().length() < 1)
         throw new IllegalArgumentException("pkg must not be blank");
      m_package = pkg;
   }

   private String m_communities;

   private String m_package;

}
