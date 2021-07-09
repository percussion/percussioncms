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
package com.percussion.services.contentmgr;

import com.percussion.utils.jsr170.IPSPropertyInterceptor;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This configuration object controls the content manager's behavior. It is
 * primarily used by the assembly service to adjust the retrieved property
 * values from the configuration manager. 
 * 
 * @see PSContentMgrOption for details on the options that can be provided
 * 
 * @author dougrand
 * 
 */
public class PSContentMgrConfig
{
   /**
    * The options to the content manager control various aspects of retrieving
    * content
    */
   private Set<PSContentMgrOption> m_options = new HashSet<>();

   /**
    * if specified, this filters all access to body fields
    * takes a {@link Property} argument
    */
   private IPSPropertyInterceptor m_bodyAccess = null;

   /**
    * If specified, this class will be instantiated and called for properties
    * that have the <i>cleanupNamespaces</i> property set on the given field.
    */
   private IPSPropertyInterceptor m_namespaceCleanup = null;
   
   /**
    * If specified, then this filters out any div tags whose
    * class attribute value equals 'rxbodyfield'. The children
    * of the div tag will still remain. 
    */
   private IPSPropertyInterceptor m_divTagCleanup = null;

   /**
    * Empty ctor
    */
   public PSContentMgrConfig() {
      addOption(PSContentMgrOption.LOAD_MINIMAL);
   }

   /**
    * @return Returns the bodyAccessClass.
    */
   public IPSPropertyInterceptor getBodyAccess()
   {
      return m_bodyAccess;
   }

   /**
    * @param bodyAccess The bodyAccess to set.
    */
   public void setBodyAccess(IPSPropertyInterceptor bodyAccess)
   {
      m_bodyAccess = bodyAccess;
   }

   /**
    * @return Returns the options.
    */
   public Set<PSContentMgrOption> getOptions()
   {
      return m_options;
   }

   /**
    * Add the given option
    * 
    * @param option an option, never <code>null</code>
    */
   public void addOption(PSContentMgrOption option)
   {
      if (option == null)
      {
         throw new IllegalArgumentException("option may not be null");
      }
      m_options.add(option);
   }

   /**
    * Removes the given option
    * 
    * @param option an option, never <code>null</code>
    */
   public void removeOption(PSContentMgrOption option)
   {
      if (option == null)
      {
         throw new IllegalArgumentException("option may not be null");
      }
      m_options.remove(option);
   }

   /**
    * @return Returns the textCleanup.
    */
   public IPSPropertyInterceptor getNamespaceCleanup()
   {
      return m_namespaceCleanup;
   }   

   /**
    * If this is set to a non-<code>null</code> value, then it will be called
    * when accessing fields that have the 
    * @param nsCleanup The textCleanup to set.
    */
   public void setNamespaceCleanup(IPSPropertyInterceptor nsCleanup)
   {
      m_namespaceCleanup = nsCleanup;
   }
   
   /**
    * @return The div tag cleanup class, may be <code>null</code>.
    * see {@link #m_divTagCleanup} for details.
    */
   public IPSPropertyInterceptor getDivTagCleanup()
   {
      return m_divTagCleanup;
   }
   
   /**
    * See {@link #m_divTagCleanup} for details.
    * @param divCleanup may be <code>null</code>.
    */
   public void setDivTagCleanup(IPSPropertyInterceptor divCleanup)
   {
      m_divTagCleanup = divCleanup;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(this, obj);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

}
