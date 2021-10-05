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
