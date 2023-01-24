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
package com.percussion.rx.design.impl;

import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The design model manages {@link IPSLocationScheme} objects.
 * The service of the model is wired by Spring framework.
 * <p>
 * Note, caller must set the Context ID ({@link #setContextId(IPSGuid)}) before
 * calls {@link #nameToGuid(String)} or {@link #createScheme(String)}.
 *
 * @see PSDesignModel
 * @author YuBingChen
 */
public class PSLocationSchemeModel extends PSDesignModel
{
   @Override
   public void delete(IPSGuid id) throws PSNotFoundException {
      IPSLocationScheme scheme = getSiteMgr().loadSchemeModifiable(id);
      getSiteMgr().deleteScheme(scheme);
   }
   
   @Override
   public Object load(IPSGuid guid) throws PSNotFoundException {
      return getSiteMgr().loadScheme(guid);
   }

   @Override
   public Object loadModifiable(IPSGuid guid) throws PSNotFoundException {
      return getSiteMgr().loadSchemeModifiable(guid);
   }

   @Override
   public IPSGuid nameToGuid(String name)
   {
      List<IPSLocationScheme> locs = findAllSchemes();
      for (IPSLocationScheme loc  : locs)
      {
         if (name.equalsIgnoreCase(loc.getName()))
            return loc.getGUID();
      }
      return null;
   }
   
   @Override
   public Collection<String> findAllNames()
   {
      List<String> names = new ArrayList<>();
      for (IPSLocationScheme scheme : findAllSchemes())
      {
         names.add(scheme.getName());
      }
      return names;
   }

   @Override
   public void save(Object scheme, @SuppressWarnings("unused")
   List<IPSAssociationSet> associationSets)
   {
      getSiteMgr().saveScheme((IPSLocationScheme)scheme);
   }
   
   /**
    * Returns all Location Schemes for the current Context.
    * @return all Location Schemes, never <code>null</code>, but may be empty.
    */
   public List<IPSLocationScheme> findAllSchemes()
   {
      return getSiteMgr().findSchemesByContextId(getContextId());      
   }
   
   /**
    * Creates a Location Scheme with the specified name and the current Context.
    * Note, {{@link #setContextId(IPSGuid)} must be called before this.
    * 
    * @return the created Location Scheme, never <code>null</code>.
    */
   public IPSLocationScheme createScheme(String schemeName)
   {
      IPSLocationScheme loc = getSiteMgr().createScheme();
      loc.setName(schemeName);
      loc.setContextId(getContextId());
      return loc;
   }
   
   /**
    * Returns the Context ID.
    * @return the Context ID, never <code>null</code>
    */
   private IPSGuid getContextId()
   {
      if (m_contextId == null)
         throw new IllegalStateException("Context ID must be set.");
      
      return m_contextId;
   }
   
   /**
    * Sets the Context ID as the parent ID of the managed Location Schemes.
    * 
    * @param contextId the new Context ID, never <code>null</code>.
    */
   public void setContextId(IPSGuid contextId)
   {
      if (contextId == null)
         throw new IllegalArgumentException("contextId may not be null.");
      
      m_contextId = contextId;
   }
   
   /**
    * Returns the Site Manager service, which is wired by the Spring framework.
    * @return the service object, never <code>null</code>.
    */
   private IPSSiteManager getSiteMgr()
   {
      return (IPSSiteManager) getService();
   }
   
   /**
    * The Context ID of the Location Scheme that the model manages. It may be
    * <code>null</code> if not set yet.
    */
   private IPSGuid m_contextId;
}
