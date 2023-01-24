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
package com.percussion.services.contentmgr;

import com.percussion.utils.jsr170.IPSPropertyInterceptor;

import java.util.HashSet;
import java.util.Objects;
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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSContentMgrConfig)) return false;
      PSContentMgrConfig that = (PSContentMgrConfig) o;
      return Objects.equals(m_options, that.m_options) && Objects.equals(m_bodyAccess, that.m_bodyAccess) && Objects.equals(m_namespaceCleanup, that.m_namespaceCleanup) && Objects.equals(m_divTagCleanup, that.m_divTagCleanup);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_options, m_bodyAccess, m_namespaceCleanup, m_divTagCleanup);
   }
}
