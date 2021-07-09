/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

import java.security.Principal;
import java.util.Collection;
import java.util.List;

import javax.security.auth.Subject;

/**
 * A subject cataloger is used by Rhythmyx to find any or all subjects from a
 * source, and also to obtain specific information about a named subject. For
 * example, an implementation of this interface may be used by the Rhyhtmyx
 * server to catalog the subjects by name and then obtain their email addresses.
 * 
 * {@link #findUsers(List)} is used to obtain a list of matching subjects. To
 * retrieve specific attributes from the returned subjects,
 * {@link Subject#getPrincipals(java.lang.Class) 
 * Subject.getPrincipals(IPSPrincipalAttribute)} is called against each subject
 * to retrieve principals that specify the attribute value(s), and
 * {@link IPSPrincipalAttribute#getAttributeType()} is called on each returned
 * principal to determine the type of attribute.
 */
public interface IPSSubjectCataloger
{
   /**
    * Get the name of the cataloger instance.  This is set at runtime by the 
    * system with a call to {@link #setName(String)}.  
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getName();
   
   /**
    * Sets the name of the cataloger instance.  This is called at runtime by the
    * system.   
    * 
    * @param name The name, may not be <code>null</code> or empty.  This is the
    * name specfied when registering the cataloger with the system.
    */
   public void setName(String name);
   
   /**
    * Get the optional description of the cataloger instance.  This is set at 
    * runtime by the system with a call to {@link #setDescription(String)}.
    * 
    * @return The description, may be <code>null</code> or empty.
    */
   public String getDescription();
   
   /**
    * Sets the description of the cataloger instance.  This is called at runtime 
    * by the system.   
    * 
    * @param desc The description, may be <code>null</code> or empty.  This is 
    * the description specfied when registering the cataloger with the system.
    */
   public void setDescription(String desc);
   
   /**
    * Returns the subjects for users that match the specified names. The
    * returned subject's principals may contain various attributes of the
    * subject, such as the name that was used to match on the subject, or the
    * subject's email address. See {@link IPSPrincipalAttribute} for details on
    * returning attributes in a subjects collection of principals.  The 
    * principal attribute of type <code>SUBJECT_NAME</code> must always be
    * returned.
    * 
    * @param names The names of the subjects to find. Each would match the name
    * supplied to authenticate the subject. May be <code>null</code> or empty
    * to return all possible subjects. Names may contain wildcards to support
    * filtering. Possible wildcards characters are '_' for match one character,
    * and '%' for match any.
    * 
    * @return The matching subjects, never <code>null</code>. Only subjects
    * for which a match was found based on the supplied names are returned, and
    * an entry with wildcards may match multiple subjects, so there is no
    * guarantee that this list will be the same size as the supplied list of
    * names, and it may be empty.  
    * 
    * @throws PSSecurityCatalogException if there are any errors.
    */
   public List<Subject> findUsers(List<String> names)
      throws PSSecurityCatalogException;
   
   /**
    * Determine if subjects returned by {@link #findUsers(List)} may contain an
    * {@link IPSPrincipalAttribute} of the specified attribute type.
    * 
    * @param attrType The type of attribute to check, may not be
    * <code>null</code>.
    * 
    * @return <code>true</code> if subjects may contain an
    * {@link IPSPrincipalAttribute} of the specified attribute type,
    * <code>false</code> if that type is not supported by this cataloger. Note
    * that a result of <code>true</code> does not guarantee all subjects
    * returned will have an attribute of the specified type, only that they
    * might.
    */
   public boolean supportsAttributeType(
      IPSPrincipalAttribute.PrincipalAttributes attrType);

   /**
    * Determine if cataloging group names is supported.  Used primarily by the
    * Admin Client user interface to determine if groups can be cataloged. 
    * 
    * @return <code>true</code> if {@link #findGroups(String)} may be called to
    * obtain lists of groups, <code>false</code> if not.
    */
   public boolean supportsGroups();
   
   /**
    * Returns the principals representing groups whose names match the specified
    * filter pattern. {@link #supportsGroups()} should be called before calling
    * this method to determine if groups may be cataloged.
    * 
    * @param pattern The pattern to use to match the names of the groups to
    * find. May be <code>null</code> or empty to return all possible group
    * names. Possible wildcards characters are '_' for match one character, and
    * '%' for match any.
    * 
    * @return The list of group as principals for which a match was found based
    * on the supplied pattern. May be empty if no groups match or if
    * {@link #supportsGroups()} would return <code>false</code>, never
    * <code>null</code>.
    * 
    * @throws PSSecurityCatalogException if there are any errors.
    */
   public List<Principal> findGroups(String pattern) 
      throws PSSecurityCatalogException;
   
   /**
    * Returns the members of the supplied groups. Any group that can be resolved
    * should be removed from the supplied list of groups. Any groups that cannot
    * be resolved should be left in the supplied list.
    * <p>
    * Ideally the cataloger should handle resolving nested groups to some number
    * of levels and only principals representing users should be returned, but
    * this is not required. All principals returned will be treated as users for
    * such purposes as notification and community filtering.
    * 
    * @param groups A list of principals representing group names, may not be
    * <code>null</code>. Any of the groups resolved by this call should be
    * removed from the list before the method returns.
    * 
    * @return A list of group members, may be empty if there are no members or
    * if no groups can be resolved by this cataloger. If the cataloger cannot
    * support this operation, it should return an emtpy list and not modify the
    * supplied list of groups.
    */
   public List<IPSTypedPrincipal> getGroupMembers(
      Collection<? extends Principal> groups);
   
   /**
    * Returns the list of groups of which the user is a member.
    * 
    * @param user The user to check, may not be <code>null</code>.
    * 
    * @return The list of groups, may be empty if the user is not a member of
    * any groups, or if this cataloger cannot support this operation.
    */
   public List<Principal> getUserGroups(Principal user);
}

