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
package com.percussion.services.system;

import com.percussion.services.system.data.PSConfigurationTypes;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.services.system.data.PSDependency;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.services.system.data.PSSharedProperty;
import com.percussion.services.system.data.PSUIComponent;
import com.percussion.services.system.impl.PSEmailMessageHandler;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.workflow.mail.IPSMailMessageContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * This interface defines various services for CRUD operations for system
 * objects.
 */
public interface IPSSystemService
{
   /**
    * Finds total number of items having the new content activities between 
    * the specified begin and end date for a given set of items, where the 
    * content activity is represented by the work-flow state.
    * <p> 
    * For example, this can be used to find out the total number of new published  
    * items since a specified date, where the <code>stateName<code> may be LIVE.
    * 
    * @param cids the IDs of the content items in question, not
    * <code>null</code>.
    * @param beginDate the begin date (inclusive) in question, not <code>null</code>.
    * @param endDate the end date (exclusive) in question, not <code>null</code>.
    * @param stateName the state name of a work-flow, not blank.
    * 
    * @return the total number of items having the new activities.
    */
   int findNewContentActivities(Collection<Integer> cids, Date beginDate, Date endDate, String stateName);
   
   /**
    * Find the total number of content activities between the specified dates
    * for the specified content items, where the content activity is
    * represented by the work-flow state.
    * 
    * @param cids the IDs of the content items in question, not
    * <code>null</code>.
    * @param beginDate the specified begin date (inclusive), not <code>null</code>.
    * @param endDate the specified end date (exclusive), not <code>null</code>.
    * @param stateName the state name of the work-flow, not blank.
    * @param transitionName the name of the transition used to transition 
    * items to above state. It may be <code>null</code> if the transition 
    * parameter is ignored for this activity.
    * 
    * @return the total number of the specified content activities.
    */
   int findNumberContentActivities(Collection<Integer> cids,
         Date beginDate, Date endDate, String stateName, String transitionName);

   /**
    * Find list of ids of content activities between the specified dates
    * for the specified content items, where the content activity is
    * represented by the work-flow state.
    * 
    * @param cids the IDs of the content items in question, not
    * <code>null</code>.
    * @param beginDate the specified begin date (inclusive), not <code>null</code>.
    * @param endDate the specified end date (exclusive), not <code>null</code>.
    * @param stateName the state name of the work-flow, not blank.
    * @param transitionName the name of the transition used to transition 
    * items to above state. It may be <code>null</code> if the transition 
    * parameter is ignored for this activity.
    * 
    * @return the list of ids for the specified content activities.
    */
   List<Long> findPageIdsContentActivities(Collection<Integer> cids,
         Date beginDate, Date endDate, String stateName, String transitionName);
   
   /**
    * Finds total number of published items from the specified item IDs and
    * within the specified date range.
    * 
    * @param cids the ID of the items in question, not <code>null</code>.
    * @param beginDate the begin date range (inclusive), not <code>null</code>.
    * @param endDate the end date range (exclusive), not <code>null</code>.
    * @param pubStateName the state name used when items are transitioned after
    *           they are published, not blank.
    * @param archiveStateName the state name used when items are transitioned
    *           before they are unpublished, not blank.
    * 
    * @return the total number of published items.
    */
   int findPublishedItems(Collection<Integer> cids, Date beginDate, Date endDate,
         String pubStateName, String archiveStateName);
   
   /**
    * Finds published items from the specified item IDs.
    * 
    * @param cids the ID of the items in question, not <code>null</code>.
    * @param pubStateName the state name used when items are transitioned after
    *           they are published, not blank.
    * @param archiveStateName the state name used when items are transitioned
    *           before they are unpublished, not blank.
    * 
    * @return the IDs of the published items, never <code>null</code>, may be empty.
    */
   Collection<Long> findPublishedItems(Collection<Integer> cids, String pubStateName, String archiveStateName);
   
   /**
    * Find all shared properties for the supplied name.
    * 
    * @param name the name of the property to find, may be <code>null</code>
    *    or empty in which case all shared properties are returned. SQL type (%)
    *    wildcards are supported.
    * @return a list with all shared properties found for the supplied name,
    *    never <code>null</code>, may be empty, ascending sorted by property 
    *    name.
    */
   public List<PSSharedProperty> findSharedPropertiesByName(String name);
   
   /**
    * Load the shared property for the specified id.
    * 
    * @param id the id of the shared property to load, not <code>null</code>.
    * @return the loaded shared property, never <code>null</code>.
    * @throws PSSystemException if no shared property exists for the 
    *    supplied id. 
    */
   public PSSharedProperty loadSharedProperty(IPSGuid id) 
      throws PSSystemException;
   
   /**
    * Save the supplied property to the repository.
    * 
    * @param property the property to save, not <code>null</code>.
    */
   public void saveSharedProperty(PSSharedProperty property);
   
   /**
    * Delete the property for the supplied id from the repository.
    * 
    * @param id the id of the property to delete, not <code>null</code>. We
    *    ignore the case where a property does not exist for the supplied id.
    */
   public void deleteSharedProperty(IPSGuid id);
   
   /**
    * For each specified GUID, return a dependency object which may specify
    * zero or more dependent objects.
    * 
    * @param ids The GUIDs of the specified design objects, may not be 
    * <code>null</code>, may be empty.  
    * 
    * @return A list containing a dependency object for each id supplied, in the
    * same order, never <code>null</code>.
    */
   public List<PSDependency> findDependencies(List<IPSGuid> ids);
   
   /**
    * For each composite GUID (which is an array of GUIDs), return a dependency 
    * object which may specify zero or more dependent objects.
    * 
    * @param ids A list of composite GUIDs, not <code>null</code>, may be empty.
    * Each composite GUID is an array of design object GUIDs, may not be 
    * <code>null</code> and must contain at least two elements.   
    * 
    * @return A list containing a dependency object for each composite GUID 
    * supplied, in the same order, never <code>null</code>.  The first GUID in
    * the array is used as the id for the dependency object.
    */
   public List<PSDependency> findCompositeDependencies(List<IPSGuid[]> ids);   
   
   /**
    * Loads the specified configuration from the file system.
    * 
    * @param type The type, may not be <code>null</code>.
    * 
    * @return The configuration, never <code>null</code>.
    * 
    * @throws FileNotFoundException If the matching configuration file is 
    * missing.
    */
   public PSMimeContentAdapter loadConfiguration(PSConfigurationTypes type) 
      throws FileNotFoundException;
   
   /**
    * Save the specified configuration.
    * 
    * @param config The content to save, may not be <code>null</code>.  
    * {@link PSMimeContentAdapter#getName()} must match one of the supported
    * types specified by {@link PSConfigurationTypes}, and 
    * {@link PSMimeContentAdapter#getContent()} may not return 
    * <code>null</code>.
    * @throws IOException if the matching configuration file is 
    * missing or cannot be written to. 
    */
   public void saveConfiguration(PSMimeContentAdapter config) 
      throws IOException;
   
   /**
    * Get the path to a configuration file, used for testing purposes, should 
    * not be used to access the file directly under normal circumstances.
    * 
    * @param type The type of the config, may not be <code>null</code>, and must
    * be supported.
    * 
    * @return The file, never <code>null</code>.
    */
   public File getConfigurationFile(PSConfigurationTypes type);
   
   /**
    * Find all content status history objects for the specified item.
    * 
    * @param id The item id, never <code>null</code>, must be an instance of 
    * {@link com.percussion.services.guidmgr.data.PSLegacyGuid}, the revision
    * component is ignored.
    *  
    * @return The list of histories, never <code>null</code>, may be empty if
    * none were found.
    */
   public List<PSContentStatusHistory> findContentStatusHistory(IPSGuid id);
   
   /**
    * Save the specified content history entry. A new ID will be set if the ID 
    * is less than <code>0</code>.
    * 
    * @param entity the to be saved content history entry, not <code>null</code>.
    */
   public void saveContentStatusHistory(PSContentStatusHistory entity);
   
   /**
    * Deletes the specified content history entry.
    * 
    * @param entity the to be deleted content history entry, not <code>null</code>
    */
   public void deleteContentStatusHistory(PSContentStatusHistory entity);
   
   /**
    * Find the last history for check in or out actions for a specified item.
    *  
    * @param id the item ID, never <code>null</code>.
    * 
    * @return the last check in/out history of the specified item. It may be
    * <code>null</code> if cannot find one.
    */
   public PSContentStatusHistory findLastCheckInOut(IPSGuid id);
   
   /**
    * Load a ui component by name
    * 
    * @param name the ui component's name, never <code>null</code> or empty,
    *           may not contain wildcards
    * @return the ui component or <code>null</code> if the ui component is not
    *         found, if multiples are found, the first is returned
    */
   public PSUIComponent findComponentByName(String name);
   
   /**
    * Get the assignment types for the given content ids. The length and order
    * of the returned assignment types will match the length and order of the
    * ids passed to this method.
    * 
    * @param ids the ids of interest, never <code>null</code>
    * @param user the user's name, never <code>null</code> or empty
    * @param roles the user's roles, never <code>null</code>
    * @param community the user's current community
    * @return a list of assignment types, never <code>null</code> and always
    *         the exact length of <code>ids</code>
    * @throws PSSystemException if there is an error obtaining information to
    *            calculate the assignment type
    */
   public List<PSAssignmentTypeEnum> getContentAssignmentTypes(
         List<IPSGuid> ids, String user, 
         List<String> roles, int community)
         throws PSSystemException;
   
   /**
    * The same as {@link #getContentAssignmentTypes(List, String, List, int)}
    * except the user, roles and community are retrieved from current user
    * session.
    * 
    * @param ids the IDs of items in question, not <code>null</code> or empty.
    * 
    * @return the assignment type list, in the same order as the IDs parameter.
    * 
    * @throws PSSystemException if error occurs.
    */
   public List<PSAssignmentTypeEnum> getContentAssignmentTypes(List<IPSGuid> ids)
         throws PSSystemException;

   /**
    * This method queues a message to be sent. The message is persisted to
    * a jms queue and removed one by one by the handler for processing. See
    * the handler, {@link PSEmailMessageHandler} for details of the 
    * delivery process.
    * 
    * @param emailContext the email context to deliver, never <code>null</code>
    */
   public void sendEmail(IPSMailMessageContext emailContext);

   /**
    * Get the list of roles to use for adhoc assignment.
    * 
    * @param contentId The id of the item being transitioned, may not be
    * <code>null</code>.
    * @param transitionId The id of the transition being executed, may not be
    * <code>null</code>.
    * 
    * @return The list of role names, never <code>null</code>, may be empty.
    * Will contain any roles in the transition's to-state that are adhoc
    * enabled, and if any allow anonymous, then all role names defined on the
    * server are returned.
    */
   public List<String> getAdhocRoles(IPSGuid contentId, IPSGuid transitionId);
   
   /**
    * Search for role members to use for adhoc assignment.
    * 
    * @param contentId The id of the item being transitioned, may not be
    * <code>null</code>.
    * @param transitionId The id of the transition being executed, may not be
    * <code>null</code>.
    * @param roleName The name of the role to search in, may not be 
    * <code>null</code> or empty.
    * @param nameFilter An optional name filter, may be <code>null</code> or 
    * empty, use sql type wildcards ('%').
    * 
    * @return The list of role members, never <code>null</code>, may be empty.
    * Members are filtered by the item's community unless the to-state of the 
    * specified transition allows anonymous ad-hoc 
    * assignment, in which case all members are returned.
    */
   public List<String> getAdhocRoleMembers(IPSGuid contentId, 
      IPSGuid transitionId, String roleName, String nameFilter);

   boolean isOracle();

   boolean isMySQL();

   boolean isMsSQL();

   boolean isDB2();

   boolean isDerby();
}

