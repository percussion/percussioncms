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
package com.percussion.security;

import java.util.List;
import java.util.Set;

import com.percussion.design.objectstore.PSRoleProvider;

/**
 * This interface provides cataloging of roles and role membership.
 *
 * @author Paul Howard
 * @version 5
 */
public interface IPSInternalRoleCataloger
{
   /**
    * Returns a list with all roles to which the supplied subject belongs.
    *
    * @param subjectName the name of the subject to search for. If
    *    <code>null</code>, all roles are returned regardless of whether they
    *    contain any subjects, ignoring the other parameter values when
    *    searching. If empty, this property is ignored, but roles are still
    *    searched using the other parameter values.
    * @param subjectType one of the PSSubject.SUBJECT_TYPE_xxx types, or 0 to
    *    ignore this property.
    * @return a valid list of 0 or more Strings, each naming a role. The list
    *    will not contain duplicates.
    */
   public List getRoles(String subjectName, int subjectType);

   /**
    * Convenience method which calls 
    * {@link #getSubjects(String, String, int, String, boolean)}.
    */
   public Set getSubjects(String roleName, String subjectNameFilter);

   /**
    * Gets a filtered set of subjects associated with a specific role.
    *
    * @param roleName The name of the role for which you wish to get the
    *    member list. If <code>null</code> or empty, all subjects matching the
    *    subjectNameFilter are returned. If a role by this name doesn't exist,
    *    an empty list is returned.
    * @param subjectNameFilter a filter to limit the subject lookup.
    *    If not provided (<code>null</code> or empty), all members of the role
    *    are returned. The filter uses SQL LIKE syntax ('%' matches 0 or more
    *    chars, '_' matches any single char).
    * @param subjectType one of the <code>PSSubject.SUBJECT_TYPE_xxx</code> 
    *    flags to filter the results by type, provide 0 to return all types.
    * @param attributeNameFilter an attribute name by which to filter the
    *    returned subjects, <code>null</code> to ignore this filter.
    * @param includeEmpty <code>true</code> to include subjects with an empty
    *    attribute list, <code>false</code> otherwise.
    * @return A valid set with 0 or more PSSubject objects. The caller takes 
    *    ownership of the set and set contents. The subjects are ordered in 
    *    ascending alpha order by their name.
    *
    * @throws PSSecurityException If any problems occur while trying to get
    *    the requested data.
    */
   public Set getSubjects(String roleName, String subjectNameFilter, 
      int subjectType, String attributeNameFilter, boolean includeEmpty);
   
   /**
    * Get the role provider definition.
    * 
    * @return the role provider definition, never <code>null</code>.
    */
   public PSRoleProvider getProvider();
}
