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
package com.percussion.webservices.assembly.data;

import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.utils.guid.IPSGuid;

import java.util.Map;

/**
 * The class that contains a template and a list of associated sites.
 */
public class PSAssemblyTemplateWs implements IPSCatalogSummary 
{
   /**
    * Creates an instance from the specified template and site refereneces.
    * 
    * @param template the template, never <code>null</code>.
    * @param sites the sites (reference) associated with the template.
    *   <code>null</code>, may be empty.
    */
   public PSAssemblyTemplateWs(IPSAssemblyTemplate template,
         Map<IPSGuid, String> sites)
   {
      if (template == null)
         throw new IllegalArgumentException("template may not be null.");
      if (sites == null)
         throw new IllegalArgumentException("sites may not be null.");
      
      m_template = template;
      m_sites = sites;
   }

   /**
    * Gets the site references (id and name) that associated with the template.
    * 
    * @return the associated sites, never <code>null</code>, may be empty.
    */
   public Map<IPSGuid, String> getSites()
   {
      return m_sites;
   }

   /**
    * @return the template, never <code>null</code>.
    */
   public IPSAssemblyTemplate getTemplate()
   {
      return m_template;
   }
   
   /**
    * Get the template's guid
    * 
    * @return The guid, never <code>null</code>.
    */
   public IPSGuid getGUID()
   {
      return m_template.getGUID();
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object b)
   {
      if (!(b instanceof PSAssemblyTemplateWs))
         return false;
      
      PSAssemblyTemplateWs other = (PSAssemblyTemplateWs) b;
      
      return m_template.equals(other.getTemplate())
            && m_sites.equals(other.m_sites);
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   public int hashCode()
   {
      return m_template.hashCode() + m_sites.hashCode();
   }
   
   /**
    * Returns corresponding template property.
    * Created to satisfy implemented interface {@link IPSCatalogSummary}.
    * @see com.percussion.services.catalog.IPSCatalogSummary#getName()
    */
   public String getName()
   {
      return m_template.getName();
   }

   /**
    * Returns corresponding template property.
    * Created to satisfy implemented interface {@link IPSCatalogSummary}.
    * @see com.percussion.services.catalog.IPSCatalogSummary#getName()
    */
   public String getLabel()
   {
      return m_template.getLabel();
   }

   /**
    * Returns corresponding template property.
    * Created to satisfy implemented interface {@link IPSCatalogSummary}.
    * @see com.percussion.services.catalog.IPSCatalogSummary#getName()
    */
   public String getDescription()
   {
      return m_template.getDescription();
   }

   /**
    * The template, initialized by ctor, never <code>null</code> after that.
    */
   private final IPSAssemblyTemplate m_template;
   
   /**
    * The sites that associated with the template, init by ctor, never 
    * <code>null</code> after that.
    */
   private final Map<IPSGuid, String> m_sites;
}
