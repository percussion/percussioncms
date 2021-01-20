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
package com.percussion.webservices.system;

import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSInvalidLocaleException;
import com.percussion.webservices.PSUserNotMemberOfCommunityException;

import java.util.List;
import java.util.Map;

/**
 * This interface defines all system related webservices.
 * <p>
 * The relationship methods in this interface are made public for advanced uses
 * only (and the relationships should be used read-only, the
 * {@link #saveRelationships(List)} method must be used w/ special care.)
 * Generally, there are specific methods for each relationship type that should
 * be used for creating/editing each type of relationship that can be found in
 * the {@link IPSContentWs} interface. 
 * <p>
 * If the implementer has created a new relationship type that has properties
 * beyond those supported by the standard interfaces, then these methods may
 * prove useful. 
 * <p>
 * The following table shows the preferred create methods for each type of 
 * relationship:
 * <table>
 *   <th>
 *      <td>Relationship category</td><td>Method</td>
 *   </th>
 *   <tr>
 *      <td>Active Assembly</td>
 *      <td>{@link IPSContentWs#addContentRelations(
 *      IPSGuid, List, String, String, int)}</td>
 *   </tr>
 *   <tr>
 *      <td>New Copy</td>
 *      <td>{@link IPSContentWs#newCopies(
 *      List, List, String, boolean)}
 *      </td>
 *   </tr>
 *   <tr>
 *      <td>Promotable Version</td>
 *      <td>{@link IPSContentWs#newPromotableVersions(
 *      List, List, String, boolean)}</td>
 *   </tr>
 *   <tr>
 *      <td>Translation</td>
 *      <td>{@link IPSContentWs#newTranslations(
 *      List, List, String, boolean)}</td>
 *   </tr>
 * </table>    
 */
public interface IPSSystemWs
{
   /**
    * Switch the user to a new community. The user can only switch to 
    * communities to which he belongs. Use the list returned with the login 
    * response.
    * 
    * @param name the community name to which the user will be switched, not 
    *    <code>null</code> or empty. Must be the an existing community.
    *    
    * @throws PSUserNotMemberOfCommunityException If the user does not belong
    * to the specified community. 
    */
   public void switchCommunity(String name) 
      throws PSUserNotMemberOfCommunityException;
   
   /**
    * Switch to the specified locale. The user can only switch to installed 
    * and enabled locales. Use the list received with the login response.
    * 
    * @param code the code of the locale to which the user wants to switch, 
    *    e.g. <code>en-us</code>, not <code>null</code> or empty, must be an 
    *    installed and enabled locale.
    *    
    * @throws PSInvalidLocaleException If the specified locale does not exist
    * or is inactive. 
    */
   public void switchLocale(String code) throws PSInvalidLocaleException;
   
   /**
    * Creates a new relationship for the specified parameters. The created
    * relationship is persisted to the repository. This method should not
    * generally be used by implementers unless they have defined custom
    * relationship types that have properties not supported by the standard
    * creation methods. See the class description for the proper way to create
    * relationships.
    * 
    * @param name the name or the relationship type definition to create, not
    * <code>null</code> or empty, must be an existing relationship type.
    * 
    * @param ownerId the id of the relationship owner, not <code>null</code>,
    * must be an id to an existing object. This must be a content guid obtained
    * from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * If the relationship type uses owner revision, then the revision of this id
    * must be either <code>-1</code> or the head revision (which is edit
    * revision if the item is checked out or current revision if the item is not
    * checked out).
    * 
    * @param depenentId the id of the relationship dependent, not
    * <code>null</code>, must be the id to an existing object. This must be a
    * content guid obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * If the relationship type uses dependent revision, then the revision of
    * this id must be either <code>-1</code> or the head revision (which is
    * edit revision if the item is checked out or current revision if the item
    * is not checked out).
    * 
    * @return a new relationship between the specified onwer and dependent of
    * the requested type, never <code>null</code>. All relationship
    * properties are initialized with the defaults as specified in the
    * relationship type definition. The owner revision will be the head (edit or
    * current) revision of the owner item if the relationship type uses the
    * owner revision. The dependent revision will be the head (edit or current)
    * revision of the dependent item if the relationship type uses the dependent
    * revision.
    * 
    * @throws PSErrorException if any error occurs.
    */
   public PSRelationship createRelationship(String name, IPSGuid ownerId, 
      IPSGuid depenentId) throws PSErrorException;
   
   /**
    * Load all relationships for the specified relationship filter. Note that a
    * system may have thousands of relationships defined and the perfomance may
    * be poor if the supplied filter is too general.
    * <p>
    * In general, this method should not be used for relationships whose
    * category is ActiveAssembly because there are rules associated with that
    * relationship type that are not enforced by this API. Instead, the
    * {@link IPSContentWs#loadContentRelations( PSRelationshipFilter, boolean)
    * loadContentRelations} method should be used.
    * 
    * @param filter defines the parameters by which to filter the returned
    * relationships, may be <code>null</code>. If not supplied or no filter
    * parameters are specified, all relationships will be returned.
    * 
    * @return a list with all loaded relationships ordered by id, never
    * <code>null</code>, may be empty.
    * 
    * @throws PSErrorException if failed to load the relationships.
    */
   public List<PSRelationship> loadRelationships(PSRelationshipFilter filter)
      throws PSErrorException;
   
   /**
    * Saves the supplied relationships to the repository.
    * <p>
    * This method should never be used for relationships whose category is
    * ActiveAssembly because there are rules associated with that relationship
    * type that are not enforced by this API. Instead, the
    * {@link IPSContentWs#saveContentRelations(List)
    * saveContentRelations} method should be used.
    * 
    * @param relationships a list with all relationships to be saved to the
    * repository, not <code>null</code> or empty.
    * 
    * @throws PSErrorsException if failed to save specified relationships.
    */
   public void saveRelationships(List<PSRelationship> relationships)
      throws PSErrorsException;
   
   /**
    * Deletes all relationships specified through the supplied ids. Deletes
    * cannot be reverted. Implementers should not use this method.
    * <p>
    * For ActiveAssembly relationships, use the {@link 
    * IPSContentWs#deleteContentRelations(List) deleteContentRelations}.
    * For all other types of relationships, they will be deleted when the owner
    * or dependent is deleted.
    * 
    * @param ids a list with all relationship ids to be deleted, not
    * <code>null</code> or empty. We ignore cases where a relationship of a
    * supplied id does not exist.
    * 
    * @throws PSErrorsException if failed to remove any specified relationships.
    * @throws PSErrorException if any error occurs.
    */
   public void deleteRelationships(List<IPSGuid> ids) 
      throws PSErrorsException, PSErrorException;
   
   /**
    * Find all relationship dependents for the specified owner and filter.
    * 
    * @param contentId the owner id for which to find the dependents, not
    * <code>null</code>. This must be a content guid obtained from one of the
    * IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager},
    * and must be the id of a valid object.
    * 
    * @param filter defines the parameters by which to filter the returned
    * children, may be <code>null</code>. If not supplied or no filter
    * parameters are specified, all children will be returned. Note, the owner
    * of the filter will be ignored if specified.
    * 
    * @return a list of object ids for all children found for the specified
    * object and filter parameters, never <code>null</code>, may be empty.
    * 
    * @throws PSErrorException if any error occurs.
    */
   public List<IPSGuid> findDependents(IPSGuid contentId,
         PSRelationshipFilter filter) throws PSErrorException;
   
   /**
    * Find all relationship owners for the specified dependent and filter.
    * 
    * @param contentId the dependent id for which to find the owners, not
    * <code>null</code>. This must be a content guid obtained from one of the
    * IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager},
    * and must be the id of a valid object.
    * 
    * @param filter defines the parameters by which to filter the returned
    * parents, may be <code>null</code>. If not supplied or no filter
    * parameters are specified, all parents will be returned. Note, the
    * dependents of the filter will be ignored if specified.
    * 
    * @return a list of object ids for all parents found for the specified
    * object and filter, never <code>null</code>, may be empty.
    * 
    * @throws PSErrorException if any error occurs.
    */
   public List<IPSGuid> findOwners(IPSGuid contentId,
         PSRelationshipFilter filter) throws PSErrorException;
   
   /**
    * Loads all relationship types for the supplied parameters in read-only 
    * mode.
    * 
    * @param name the relationship type name to load, may be <code>null</code> 
    *    or empty, asterisk wildcards are accepted. If not supplied or empty, 
    *    all relationship types will be loaded.
    * @param category the relationship category for which to load the 
    *    configurations, may be <code>null</code> but not empty. Must be an 
    *    existing category. All relationship configurations will be loaded if 
    *    not supplied.
    *    
    * @return a list with all loaded relationship configurations in read-only 
    *    mode, never <code>null</code>, may be empty, alpha ordered by name.
    *    
    * @throws PSErrorException if an error occurs while loading the relationship
    *    types.
    */
   public List<PSRelationshipConfig> loadRelationshipTypes(String name, 
      String category) throws PSErrorException;
   
   /**
    * Loads the workflow audit trails for each supplied item.
    * 
    * @param ids a list of item ids for which to get the audit trails, not
    * <code>null</code> or empty. It is an error if no item exists for any of
    * the supplied ids. This must be a content guid obtained from one of the
    * IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}
    * and the revision of the id is ignored.
    * 
    * @return a list of audit trails, grouped by the specified item id.
    * 
    * @throws PSErrorException if any item does not exist.
    */
   public Map<IPSGuid, List<PSContentStatusHistory>> loadAuditTrails(
      List<IPSGuid> ids) throws PSErrorException;
   
   /**
    * @deprecated Use {@link #transitionItems(List, String)}
    */
   @Deprecated
   public List<String> transitionItems(List<IPSGuid> ids, String transition, 
      String user) throws PSErrorsException, PSErrorException;
   
   /**
    * Transition all items identified through the supplied item ids and
    * transition name.
    * 
    * @param ids a list of ids for all items to be trasitioned, not
    * <code>null</code> or empty. This must be a content guid obtained from
    * one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * It is an error if no item exists for any of the supplied ids. All items
    * must be in checked in mode or they must be checked out by the requested
    * user. The revision of an id (in the specified ids) may be <code>-1</code>;
    * otherwise it must be the current revision of the item.
    * 
    * @param transition the name or the transition to use for each item, not
    * <code>null</code> or empty. It is an error if no transition exists for
    * any of the specified items current state or if the supplied transition
    * does not exist.
    * 
    * @return a list of state names to which the requested items were
    * transitioned to in the same order as requested, never <code>null</code>
    * or empty.
    * 
    * @throws PSErrorsException if failed to transition a specified item.
    * @throws PSErrorException if any other error occurs.
    */
   public List<String> transitionItems(List<IPSGuid> ids, String transition) 
      throws PSErrorsException, PSErrorException;

   /**
    * @deprecated Use {@link #transitionItems(List, String, String, List)}
    */
   @Deprecated
   public List<String> transitionItems(List<IPSGuid> ids, String transition, 
      String comment, List<String> adhocUsers, String user) 
      throws PSErrorsException, PSErrorException;
   
   /**
    * Transition all items identified through the supplied item ids and
    * transition name.
    * 
    * @param ids a list of ids for all items to be trasitioned, not
    * <code>null</code> or empty. This must be a content guid obtained from
    * one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * It is an error if no item exists for any of the supplied ids. All items
    * must be in checked in mode or they must be checked out by the requested
    * user. The revision of an id (in the specified ids) may be <code>-1</code>;
    * otherwise it must be the current revision of the item.
    * 
    * @param transition the name or the transition to use for each item, not
    * <code>null</code> or empty. It is an error if no transition exists for
    * any of the specified items current state or if the supplied transition
    * does not exist.
    * 
    * @param comment the comment for this transition. It may be 
    * <code>null</code> or empty.
    *  
    * @param adhocUsers a list of add hoc users for this transition. It may be 
    * <code>null</code> or empty.
    * 
    * @return a list of state names to which the requested items were
    * transitioned to in the same order as requested, never <code>null</code>
    * or empty.
    * 
    * @throws PSErrorsException if failed to transition a specified item.
    * @throws PSErrorException if any other error occurs.
    */
   public List<String> transitionItems(List<IPSGuid> ids, String transition, 
      String comment, List<String> adhocUsers) 
      throws PSErrorsException, PSErrorException;
   
   
   /**
    * Loads all workflows for the supplied name in read-only mode.
    * 
    * @param name the name or the workflow to load, may be <code>null</code> 
    *    or empty, asterisk wildcards are accepted. All workflows will be 
    *    loaded if <code>null</code> or empty.
    * @return a list with all loaded workflows in read-only mode, never 
    *    <code>null</code>, may be empty, alpha ordered by name.
    */
   public List<PSWorkflow> loadWorkflows(String name);
   
   /**
    * Get the list of allowed transition names for the current user and supplied
    * item ids.
    * 
    * @param ids The list of ids, may not be <code>null</code>. This must be
    * a content guid obtained from one of the IPS*Ws interfaces or from the
    * {@link com.percussion.services.guidmgr.IPSGuidManager IPSGuidManager}.
    * 
    * @return A map of allowed transition names, never <code>null</code>,
    * might be empty. Key is the transition name, value is the label. If
    * multiple ids are supplied, then the intersection of each of their allowed
    * transitions is returned.
    */
   public Map<String, String> getAllowedTransitions(List<IPSGuid> ids);
}

