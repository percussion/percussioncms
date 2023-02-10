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
package com.percussion.security;

import com.percussion.design.objectstore.PSRoleProvider;
import com.percussion.design.objectstore.PSServerConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;


/**
 * A role cataloger using the backend role/subject data as directory 
 * source.
 */
public class PSBackendRoleCataloger extends PSBackendCataloger 
   implements IPSInternalRoleCataloger
{
   /**
    * Convenience constructor that calls 
    * {@link #PSBackendRoleCataloger(Properties, PSServerConfiguration)} with
    * <code>null<code> for the server configuration.
    */
   public PSBackendRoleCataloger(Properties properties)
   {
      this(properties, null);
   }

   /**
    * Constructs a new backend role cataloger.
    * 
    * @param properties required parameter for instantiation only, may be 
    *    <code>null</code> or empty.
    * @param config required parameter for instantiation only, may be
    *    <code>null</code> or empty.
    * @throws PSSecurityException if the referenced role provider was not
    *    found.
    */
   public PSBackendRoleCataloger(Properties properties, 
      PSServerConfiguration config) throws PSSecurityException
   {
      m_properties = properties;
      m_config = config;
      
      // avoid eclipse warnings
      if (m_properties == null);
      if (m_config == null);
   }

   /** @see IPSInternalRoleCataloger */
   public List getRoles(String subjectName, int subjectType)
   {
      return getRhythmyxRoles(subjectName, subjectType);
   }
   
   /** @see IPSInternalRoleCataloger */
   public PSRoleProvider getProvider()
   {
      return new PSRoleProvider("sys_backendrolecataloger", 
         PSRoleProvider.TYPE_BACKEND, (String) null);
   }

   /** @see IPSInternalRoleCataloger */
   public Set getSubjects(String roleName, String subjectNameFilter)
   {
      return getSubjects(roleName, subjectNameFilter, 0, null, true);
   }
   
   /** @see IPSInternalRoleCataloger */
   public Set getSubjects(String roleName, String subjectNameFilter, 
      int subjectType, String attributeNameFilter, boolean includeEmpty)
   {
      HashMap filters = new HashMap();

      if (null != roleName && roleName.trim().length() > 0)
         filters.put(FILTER_ROLE_NAME, roleName);

      if (null != subjectNameFilter && subjectNameFilter.trim().length() > 0)
         filters.put(FILTER_SUBJECT_NAME, subjectNameFilter);

      if (null != attributeNameFilter && attributeNameFilter.trim().length() > 0)
         filters.put(FILTER_ATTRIBUTE_NAME, attributeNameFilter);

      if (subjectType != 0)
         filters.put("sys_subjectType", String.valueOf(subjectType));
      
      List results = new ArrayList();
      results.addAll(getSubjects(filters, null));
      
      return new HashSet(results);
   }
   
   /**
    * Role cataloger configuration properties supplied at construction time. 
    * Currently not used.
    */
   private Properties m_properties = null;
   
   /**
    * The rhythmyx server configuration supplied at construction time. Currently
    * not used.
    */
   private PSServerConfiguration m_config = null;
}
