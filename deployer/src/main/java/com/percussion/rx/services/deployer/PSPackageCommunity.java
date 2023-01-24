/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
