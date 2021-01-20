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
package com.percussion.rx.design.impl;

import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.services.sitemgr.IPSLocationScheme;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
   public void delete(IPSGuid id)
   {
      IPSLocationScheme scheme = getSiteMgr().loadSchemeModifiable(id);
      getSiteMgr().deleteScheme(scheme);
   }
   
   @Override
   public Object load(IPSGuid guid)
   {
      return getSiteMgr().loadScheme(guid);
   }

   @Override
   public Object loadModifiable(IPSGuid guid)
   {
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
      List<String> names = new ArrayList<String>();
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
