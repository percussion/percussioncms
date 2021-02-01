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

package com.percussion.services.security;

import com.percussion.security.IPSDirectoryCataloger;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.services.security.data.PSCatalogerConfig;
import com.percussion.security.IPSPrincipalAttribute;
import com.percussion.security.IPSRoleCataloger;
import com.percussion.security.IPSSubjectCataloger;
import com.percussion.security.PSSecurityCatalogException;
import com.percussion.security.IPSPrincipalAttribute.PrincipalAttributes;
import com.percussion.utils.xml.PSInvalidXmlException;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.xml.sax.SAXException;

/**
 * Provides cataloging services for roles and subjects defined by both internal 
 * and external catalogers.  Also provides persistence services for external
 * cataloger configurations.
 */
public interface IPSRoleMgr
{
   /**
    * Type constant for subject catalogers.  
    */
   public static final String SUBJECT_CATALOGER_TYPE = "subjectCataloger";

   /**
    * Convenience method that calls 
    * {@link #findUsers(List, String, String) findUsers(names, null, null)}.
    */
   public List<Subject> findUsers(List<String> names) 
      throws PSSecurityCatalogException;

   /**
    * Convenience method that calls {@link #findUsers(List, String, String, Set) 
    * findUsers(names, catalogerName, type, null)}
    */
   public List<Subject> findUsers(List<String> names, String catalogerName, 
      String type) throws PSSecurityCatalogException;
   
   /**
    * Convenience method that calls {@link #findUsers(List, String, String, Set) 
    * findUsers(names, catalogerName, type, supportedTypes, false)}    */
   public List<Subject> findUsers(List<String> names, String catalogerName, 
      String type, Set<PrincipalAttributes> supportedTypes) 
      throws PSSecurityCatalogException;
   
   /**
    * Find all matching subjects by querying all subject catalogers and
    * directory providers. See {@link IPSSubjectCataloger#findUsers(List)} for
    * more info.
    * 
    * @param names The names of the subjects to find. Each would match the name
    * supplied to authenticate the subject. May be <code>null</code> or empty
    * to return all possible subjects. Names may contain wildcards to support
    * filtering. Possible wildcards characters are '_' for match one character,
    * and '%' for match any.
    * @param catalogerName The name of the subject cataloger to query, may be
    * <code>null</code> or empty to query all subject and directory
    * catalogers.
    * @param type The cataloger type, may not be <code>null</code> or empty if
    * <code>catalogerName</code> is supplied, and must match an existing
    * cataloger type. For subject catalogers, use
    * {@link #SUBJECT_CATALOGER_TYPE}, otherwise it's the
    * {@link IPSDirectoryCataloger#getCatalogerType()}. Ignored if
    * <code>catalogerName</code> is not supplied.
    * @param supportedTypes Only catalogers that support all types specified in
    * the set will be queried. For subject catalogers,
    * {@link IPSSubjectCataloger#supportsAttributeType(IPSPrincipalAttribute.PrincipalAttributes)}
    * is called. For directory catalogers,
    * {@link PrincipalAttributes#SUBJECT_NAME} is always supported, and for,
    * {@link PrincipalAttributes#EMAIL_ADDRESS},
    * {@link IPSDirectoryCataloger#getEmailAddressAttributeName()} must return a
    * non-emtpy value. May be <code>null</code> or emtpy to disregard this
    * parameter.
    * 
    * @param throwCatalogerExceptions if true, throws a cataloger exception as it occurs, if results 
    * are expected from multiple catalogers, set it to false, so that it loops rest of the
    * catalogers even if one fails.
    * 
    * 
    * @return A list of subjects, never <code>null</code>, may be empty.
    * Internal directory catalogers are queried for defined attributes - an
    * attribute must be defined in the directory's "return attributes", or in
    * the directory set's "required attribute" list to be included in the
    * returned subjects. Note that attributes returned by the Backend-table and
    * OS security providers are automatically exposed by their respective
    * directory catalogers. External cataloger implementations define what
    * attributes they will expose.
    * 
    * @throws PSSecurityCatalogException If there are any errors.
    */
   public List<Subject> findUsers(List<String> names, String catalogerName, 
      String type, Set<PrincipalAttributes> supportedTypes, boolean throwCatalogerExceptions) 
      throws PSSecurityCatalogException; 
   

   /**
    * Catalog the members of the specified role.
    *
    * @param roleName The name of the role, may not be <code>null</code> or
    * empty.
    * 
    * @return A set of members, never <code>null</code>, may be empty if no
    * matching role is found or if the role has no members. Principles returned
    * with {@link 
    * IPSTypedPrincipal.PrincipalTypes#UNDEFINED}
    * are ignored.
    * 
    * @throws PSSecurityCatalogException if there are any errors.
    */
   public Set<IPSTypedPrincipal> getRoleMembers(String roleName)
      throws PSSecurityCatalogException;
   
   /**
    * Catalog the members of the specified role of the specified type.
    *
    * @param roleName The name of the role, may not be <code>null</code> or
    * empty.
    * @param type The type of principal to return, may not be <code>null</code>
    * and may not be {@link 
    * IPSTypedPrincipal.PrincipalTypes#UNDEFINED}.
    * 
    * @return A set of members, never <code>null</code>, may be empty if no
    * matching role is found or if the role has no members. Principles returned
    * with {@link 
    * IPSTypedPrincipal.PrincipalTypes#UNDEFINED}
    * are ignored.
    * 
    * @throws PSSecurityCatalogException if there are any errors.
    */
   public Set<IPSTypedPrincipal> getRoleMembers(String roleName, 
      IPSTypedPrincipal.PrincipalTypes type) throws PSSecurityCatalogException;   

   /**
    * Get the list of roles that the specified user is a member of, only
    * checking the default back-end role cataloger.
    * 
    * @param user The user or group to check, may be <code>null</code> or
    * emtpy to get all role names defined.
    * 
    * @return A set of role names, never <code>null</code>, may be empty if
    * the specified user is not in any roles, if a matching user is not known to
    * the cataloger, or if no roles are defined.
    * 
    * @throws PSSecurityCatalogException if there are any errors.
    */
   public Set<String> getDefaultUserRoles(IPSTypedPrincipal user)
      throws PSSecurityCatalogException;

   /**
    * Get the list of Rhythmyx roles that the specified user is a member of.
    * 
    * @param user The user or group to check, may be <code>null</code> to get
    * all role names defined.
    * 
    * @return A set of role names, never <code>null</code>, may be empty if
    * the specified user is not in any roles, if a matching user is not known to
    * any catalogers, or if no roles are defined.  The set of role names is
    * limited to Rhythmyx roles.
    * 
    * @throws PSSecurityCatalogException if there are any errors.
    */
   public Set<String> getUserRoles(IPSTypedPrincipal user)
      throws PSSecurityCatalogException;
   
   /**
    * Returns the list of groups of which the user is a member.
    * 
    * @param user The user to check, may not be <code>null</code>.
    * 
    * @return The list of groups, may be empty if the user is not a member of
    * any groups, or if no cataloger can support this operation.
    */
   public Set<Principal> getUserGroups(IPSTypedPrincipal user);

   /**
    * Returns the principals representing groups whose names match the specified
    * filter pattern. {@link #supportsGroups(String, String)} may be called before
    * calling this method to determine if groups may be cataloged.
    * 
    * @param pattern The pattern to use to match the names of the groups to
    * find. May be <code>null</code> or empty to return all possible group
    * names. Possible wildcards characters are '_' for match one character, and
    * '%' for match any.
    * @param catalogerName The name of the subject cataloger to query, may be
    * null or empty to query all. 
    * @param type The cataloger type, may not be <code>null</code> or empty, and
    * must match an existing cataloger type.  For subject catalogers, use
    * {@link #SUBJECT_CATALOGER_TYPE}, otherwise it's the 
    * {@link IPSDirectoryCataloger#getCatalogerType()}.  Ignored if 
    * <code>catalogerName</code> is not supplied.
    * 
    * @return The list of group as principals for which a match was found based
    * on the supplied pattern. May be empty if no groups match or if {@link
    * #supportsGroups(String, String)} would return <code>false</code>.
    * 
    * @throws PSSecurityCatalogException if there are any errors.
    */
   public List<Principal> findGroups(String pattern, String catalogerName, 
      String type) throws PSSecurityCatalogException;

   /**
    * Gets all defined cataloger configurations.
    * 
    * @return A list of cataloger configurations, never <code>null</code>, may
    * be empty.
    * 
    * @throws SAXException If the spring config file is malformed. 
    * @throws IOException If there is an error loading the spring config file.
    * @throws PSInvalidXmlException If the spring config xml is invalid.
    */
   public List<PSCatalogerConfig> getCatalogerConfigs() 
      throws PSInvalidXmlException, IOException, SAXException;

   /**
    * Saves the list of cataloger configurations, replacing any that are
    * currently defined.  The server must be restarted for these configurations
    * to become active.
    * 
    * @param configs The configurations to save, may be empty, never 
    * <code>null</code>.
    * 
    * @throws SAXException If the spring config file is malformed. 
    * @throws IOException If there is an error loading the spring config file.
    * @throws PSInvalidXmlException If the spring config xml is invalid.
    */
   public void saveCatalogerConfigs(List<PSCatalogerConfig> configs) 
      throws PSInvalidXmlException, IOException, SAXException;

   /**
    * Returns the members of the supplied groups. Any group that can be resolved
    * will be removed from the supplied list of groups. Any groups that cannot
    * be resolved will be left in the supplied list. All principals returned may
    * be treated as users for such purposes as notification and community
    * filtering.
    * 
    * @param groups A list of principals representing group names, may be empty,
    * never <code>null</code>. Any of the groups resolved by this call will
    * be removed from the list before the method returns.
    * 
    * @return A list of group members, may be empty if there are no members, if
    * no groups can be resolved, or if the supplied list is empty.
    */
   public List<IPSTypedPrincipal> getGroupMembers(
      Collection<? extends Principal> groups);

   /**
    * Get the list of active external subject catalogers. Does not include 
    * internal directory catalogers.
    * 
    * @return The list of external subject catalogers, never <code>null</code>,
    * may be empty.  Caller owns the list; modifications to it do not affect the
    * state of this object. 
    */
   public List<IPSSubjectCataloger> getSubjectCatalogers();

   /**
    * Get the names of internal directory catalogers. 
    * 
    * @return The list of directory catalogers, never <code>null</code>,
    * may be empty. 
    */
   public List<IPSDirectoryCataloger> getDirectoryCatalogers();
   
   /**
    * Determine if the supplied cataloger is the default backend cataloger.
    * @param cataloger The cataloger to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if it is the default cataloger, 
    * <code>false</code> otherwise.
    */
   public boolean isDefaultCataloger(IPSDirectoryCataloger cataloger);
   
   /**
    * Determines if the specfied external subject cataloger supports group
    * cataloging.
    * 
    * @param catalogerName The name of the subject cataloger to check for group
    * support, may not be <code>null</code> or empty. 
    * @param type The type of cataloger, may not be <code>null</code> or empty
    * and must match an existing cataloger type.  For subject catalogers, use
    * {@link #SUBJECT_CATALOGER_TYPE}, otherwise it's the 
    * {@link IPSDirectoryCataloger#getCatalogerType()}. 
    * 
    * @return <code>true</code> if groups are supported, <code>false</code> if 
    * not.
    */
   public boolean supportsGroups(String catalogerName, String type);

   /**
    * Get a list of the names of roles defined within Rhythmyx.
    * 
    * @return The role names, never <code>null</code>, may be empty.
    */
   public List<String> getDefinedRoles();

   /**
    * Gets attributes of a role defined within the Rhythmyx backend.
    * 
    * @param roleName The name of the role, may not be <code>null</code> or
    * empty.
    * 
    * @return A set of attributes, never <code>null</code>, may be empty if the
    * role does not have attributes defined, or if the specified role cannot be
    * found.
    */
   public Set<IPSPrincipalAttribute> getRoleAttributes(String roleName);
   
   /**
    * Sets the list of role catalogers to use.
    * 
    * @param catalogers The list, never <code>null</code>, may be empty.
    */
   public void setRoleCatalogers(List<IPSRoleCataloger> catalogers);
   
   /**
    * Sets the list of subject catalogers to use.
    * 
    * @param catalogers The list, never <code>null</code>, may be empty.
    */
   public void setSubjectCatalogers(List<IPSSubjectCataloger> catalogers);
}
