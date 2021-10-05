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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipSet;

import java.util.List;

/**
 * The relationship processor defines the methods needed to manage relationships
 * between objects defined in the Object table. It only manages relationships,
 * not the objects referenced by those relationships. In other words, if your
 * business rule for an object called for deleting the child when the
 * relationship was removed, you may need to enforce that yourself (unless the
 * relationship has an effect that implements the business rule).
 * <p>On the server, all processing is performed by the relationship handler.
 * <p>When we have more time, this needs to be re-implemented using
 * PSCmsRelationship objects. This interface is currently limited in its
 * generality because it doesn't allow types or properties. These attributes
 * will be handled by the relationship object.
 * <p>Currently, these methods are only useful for folder processing.
 *
 * @author Paul Howard
 * @version 1.0
 */
public interface IPSRelationshipProcessor
{
   /**
    * Creates a new relationship between the supplied parent and each
    * supplied child of the type specified by <code>relationshipType</code> and
    * stores it in the database.
    * <p>The process is transaction safe, meaning it either completes
    * successfully, or the database is unchanged.
    *
    * @param relationshipType The internal name of the relationship type you
    *    wish to create. May not be empty or <code>null</code>. The name is
    *    case-insensitive.
    *
    * @param children  Each entry must be a valid PSKey, which is a key
    *    that references an existing object in the database. Never <code>null
    *    </code>. If empty, this method returns immediately without exception.
    *
    * @param targetParent  A valid key referencing the object that will own the
    *    generated relationships. A valid key is one that references an
    *    existing object in the database. Never <code>null</code>.
    *    If the type of the children is not supported by the target parent,
    *    an exception is thrown.
    *
    * @throws PSCmsException If the processor for the supplied type is not
    *    found or cannot be instantiated, or any problems occur while adding
    *    the relationship(s).
    */
   public void add(
      String relationshipType,
      List children,
      PSLocator targetParent)
      throws PSCmsException;

   /**
    * Creates a new relationship between the supplied parent and each
    * supplied child of the type specified by <code>relationshipType</code> and
    * stores it in the database.
    * <p>The process is transaction safe, meaning it either completes
    * successfully, or the database is unchanged.
    *
    * @param componentType One of the supported names for relationship
    *    processing. Never <code>null</code> or empty.
    *
    * @param relationshipType The internal name of the relationship type you
    *    wish to create. May not be empty or <code>null</code>. The name is
    *    case-insensitive.
    *
    * @param children  Each entry must be a valid PSLocator, which is a key
    *    that references an existing object in the database. Never <code>null
    *    </code>. If empty, this method returns immediately without exception.
    *
    * @param targetParent  A valid key referencing the object that will own the
    *    generated relationships. A valid key is one that references an
    *    existing object in the database. Never <code>null</code>.
    *    If the type of the children is not supported by the target parent,
    *    an exception is thrown.
    *
    * @throws PSCmsException If the processor for the supplied type is not
    *    found or cannot be instantiated, or any problems occur while adding
    *    the relationship(s).
    * 
    * @deprecated use {@link #add(String, List, PSLocator)} instead.
    */
   public void add(
      String componentType,
      String relationshipType,
      List children,
      PSKey targetParent)
      throws PSCmsException;

   /**
    * Permanently removes the relationship type specified between each child 
    * and the source parent. Provide <code>null</code> for relationship type 
    * and dependents to delete all relationships for the supplied owner. 
    * Whether the child is deleted is dependent on the effects associated with 
    * the relationship type being removed.
    * <p>
    * The process is not transaction safe. If the process fails, the
    * caller should requery the source parent to determine which relationships
    * were actually removed.
    *
    * @param relationshipType the relationship type to delete, may be 
    *    <code>null</code> but not empty, in which case the dependents of all 
    *    relationship types will be deleted. An exception is thrown if the 
    *    requested type was not found in the relationship configuration.
    *
    * @param sourceParent  A valid key that references the current owner of
    *    the relationships to all the supplied children. A valid key is one
    *    that references an existing object in the database. Never
    *    <code>null</code>.
    *
    * @param children  Each entry must be a non-<code>null</code> PSLocator.
    *    If the key does not reference a child of the supplied parent, it is
    *    skipped. Never <code>null</code>. If empty, this method returns
    *    immediately without exception.
    *
    * @throws PSCmsException If the suppied relationship type (if supplied) 
    *    is not found or cannot be instantiated, or any problems occur while 
    *    removing the relationship(s).
    */
   public void delete(
      String relationshipType,
      PSKey sourceParent,
      List children)
      throws PSCmsException;

   /**
    * This method can be used to catalog related objects.
    *
    * @param componentType  The value returned by the {@link
    *    IPSDbComponent#getComponentType()} method of the component being
    *    processed. Never empty or <code>null</code>. If the name is not found
    *    in the config, an exception is thrown.
    *
    * @param parent  A valid key that references the component for which you
    *    wish to obtain the child relationships. A valid key is one
    *    that references an existing object in the database. Never
    *    <code>null</code>.
    *
    * @return An array with 0 or more entries. Each entry is a valid summary.
    *
    * @throws PSCmsException If the processor for the supplied type is not
    *    found or cannot be instantiated, or any problems occur while removing
    *    the relationship(s).
    * 
    * @deprecated use {@link #getSummaries(PSRelationshipFilter, boolean)} method
    *    with  <code>false</code> for the second parameter.
    */
   public PSComponentSummary[] getChildren(String componentType, PSKey parent)
      throws PSCmsException;

   /**
    * Just like {@link #getChildren(String, PSKey) getChildren}, except the
    * relationship type is specified by the caller.
    *
    * @param relationshipType  The requested relationship type name. It may not
    *    be <code>null</code> or empty.
    * 
    * @deprecated use {@link #getSummaries(PSRelationshipFilter, boolean)} method
    *    with  <code>false</code> for the second parameter.
    */
   public PSComponentSummary[] getChildren(
      String componentType,
      String relationshipType,
      PSKey parent)
      throws PSCmsException;
   
   /**
    * Get the relationship configuration object given the relationship type 
    * name.
    * @param relationshipTypeName name of the relationship name. Must 
    * correspond to one of the relationships configured probably in the 
    * workbench. 
    * @return Relationship Configuration object, may be <code>null</code> if 
    * one with the name supplied is not found.
    * @throws PSCmsException if the method cannot extract the configuraion 
    * object for the given name for any reason.
    */
   public PSRelationshipConfig getConfig(String relationshipTypeName)
      throws PSCmsException;

   /**
    * This method can be used to catalog related parent objects. It is similar
    * with {@link #getChildren(String, String, PSKey) getChildren}, except it
    * catalog the parent objects for a given locator.
    * 
    * @deprecated use {@link #getSummaries(PSRelationshipFilter, boolean)} method
    *    with  <code>true</code> for the second parameter.
    */
   public PSComponentSummary[] getParents(
      String componentType,
      String relationshipType,
      PSKey locator)
      throws PSCmsException;

   /**
    * Catalogs all relationships for a given object, or possibly only those
    * relationships that have a given name.
    *
    * @param relationshipType The internal name or category of the relationship
    *    type you wish to limit your request to. If empty or <code>null
    *    </code>, all types are returned. The name is case-insensitive.
    *    Relationship types are checked first, then categories.
    *
    * @param locator The unique identifier for the object for which you want
    *    the relationships. Never <code>null</code>. If no revision is
    *    supplied, all relationships are returned, otherwise only those
    *    relationships for the requested revision, plus all relationships that
    *    don't use revisions are returned.
    *
    * @param owner If <code>true</code>, relationships which are owned by
    *    the object are returned, otherwise relationships for which the
    *    requested object is a dependent are returned.
    *
    * @return A valid array of 0 or more relationship objects, in no particular
    *    order.
    *
    * @throws PSCmsException If any problems occur trying to get the
    *    relationships, such as a db failure.
    */
   public PSRelationshipSet getRelationships(
      String relationshipType,
      PSLocator locator,
      boolean owner)
      throws PSCmsException;

   /**
    * Catalogs all relationships based on the filter object supplied.
    *
    * @param filter relationship filer, must not be <code>null</code>.
    *
    * @return A set of 0 or more relationship objects, in no particular order.
    *
    * @throws PSCmsException If any problems occur trying to get the 
    * relationships, such as a db failure.
    */
   public PSRelationshipSet getRelationships(PSRelationshipFilter filter)
      throws PSCmsException;

   /**
    * Permanently removes the specified relationships from the CMS. Whether 
    * the children (or the owners) are deleted is dependent on the effects 
    * associated with the relationship type of each each relationship being 
    * removed.
    * <p>
    * @param relationships list of relationships to be removed from the 
    * system, must not be <code>null</code> or empty.
    * @throws PSCmsException if delete fails for any reason. 
    */
   public void delete(PSRelationshipSet relationships) throws PSCmsException;

   /**
    * Saves all supplied relationships to the CMS.
    *
    * @param relationships relationship set to save, must not be <code>null</code> or empty.
    *
    * @throws PSCmsException If any problems occur trying to save the relationships.
    */
   public void save(PSRelationshipSet relationships) throws PSCmsException;

   /**
    * This method can be used to get the summaries for the owner or dependent 
    * objects in the relationships filetred by the supplied filter.
    *
    * @param filter relationship filer, must not be <code>null</code>.
    * 
    * @param owner specify <code>true</code> to get the summaries of owners of 
    * the relationships or specify <code>false</code> to get summaries of the 
    * dependnents. 
    *
    * @return A component summaries object containing summary for each 
    * (unique) dependent/owner of the relationship. Never <code>null</code>, 
    * may be empty.
    *
    * @throws PSCmsException If the processor cannot obtain required 
    *    information basedon the parameters supplied.
    */
   public PSComponentSummaries getSummaries(
      PSRelationshipFilter filter,
      boolean owner)
      throws PSCmsException;

   /**
    * Changes the parent of each relationship to the supplied children from
    * the source parent to the target parent.
    * <p>The process is transaction safe, meaning it either completes
    * successfully, or the database is unchanged.
    * 
    * @param relationshipType A releationship type name known to the system, 
    * may be <code>null</code> or empty in which case all relationship 
    * irrespective of the relationship type will be moved. Otherwise only the 
    * relationships of this type will be considered for moving.
    *
    * @param sourceParent  A valid key that references the current owner of 
    * the relationships to all the supplied children. A valid key is one that 
    * references an existing object of the correct type in the database. 
    * Never <code>null</code>.
    *
    * @param children  Each entry must be a valid PSLocator, which is a key 
    * that references an existing object in the database. Never 
    * <code>null</code>. If empty, this method returns immediately without 
    * exception. If the type of the children are not supported by the target 
    * parent, an exception is thrown.
    *
    * @param targetParent  A valid key referencing the object that will own 
    * the relationships after the move. A valid key is one that references an 
    * existing object in the database. Never <code>null</code>.
    *
    * @throws PSCmsException If the processor for the supplied type is not 
    * found or cannot be instantiated, or any problems occur while moving 
    * the relationship(s).
    */
   public void move(
      String relationshipType,
      PSLocator sourceParent,
      List children,
      PSLocator targetParent)
      throws PSCmsException;

   /**
    * Changes the parent of each relationship to the supplied children from
    * the source parent to the target parent.
    * <p>The process is transaction safe, meaning it either completes
    * successfully, or the database is unchanged.
    *
    * @param relationshipType A releationship type name known to the system, 
    * may be <code>null</code> or empty in which case all relationship 
    * irrespective of the relationship type will be moved. Otherwise only the 
    * relationships of this type will be considered for moving.
    *
    * @param sourceParent  A valid key that references the current owner of 
    * the relationships to all the supplied children. A valid key is one that 
    * references an existing object of the correct type in the database. 
    * Never <code>null</code>.
    *
    * @param children  Each entry must be a valid PSLocator, which is a key 
    * that references an existing object in the database. Never 
    * <code>null</code>. If empty, this method returns immediately without 
    * exception. If the type of the children are not supported by the target 
    * parent, an exception is thrown.
    *
    * @param targetParent  A valid key referencing the object that will own 
    * the relationships after the move. A valid key is one that references 
    * an existing object in the database. Never <code>null</code>.
    *
    * @throws PSCmsException If the processor for the supplied type is not 
    * found or cannot be instantiated, or any problems occur while moving 
    * the relationship(s).
    * 
    * @deprecated use {@link #move(String, PSLocator, List, PSLocator)} 
    * instead.
    */
   public void move(
      String relationshipType,
      PSKey sourceParent,
      List children,
      PSKey targetParent)
      throws PSCmsException;

   /**
    * Locate the object and get the component summary given fully qualified 
    * path and relationship type name. A full qualified path has following 
    * syntax.
    * <p>
    * <em>/root/child_1/child_2/child_3/../../child_n</em>
    * <p>
    * In this root, child_1, child_2 etc are the sys_title fields of the 
    * objects (Content Item or Folder). The method makes sure the root object 
    * with sys_title field value 'root' indeed is a root, i.e. ha no parent 
    * or owner. The component summary returned will refer to the right most 
    * object (child_n) in the above path. Any two consecutive objects in the 
    * path are related by the supplied relationship type.  
    * 
    * @param componentType  The value returned by the {@link
    *    IPSDbComponent#getComponentType()} method of the component being
    *    processed. Never empty or <code>null</code>. If the name is not found
    *    in the config, an exception is thrown.
    *
    * @param path fully qualified relationship path as explained above, must 
    *    not be <code>null</code> or empty.
    *    
    * @param relationshipTypeName name of the relationship that any two 
    *    consecutive objects related by, Must be one of the relationship types 
    *    known to the system (which is configured in the workbench).
    * 
    * @return Component summary for the leaf object (referred by child_n 
    *    above) in the path, can be<code>null</code> if there is no such 
    *    relationship path exists in the system. 
    *    
    * @throws PSCmsException
    */
   public PSComponentSummary getSummaryByPath(
      String componentType,
      String path,
      String relationshipTypeName)
      throws PSCmsException;

   /**
    * Creates a new relationship of specified relationship type for each 
    * child linking the child to the target parent. Whether the child is 
    * cloned is dependent on the effects associated with the relationship 
    * type.
    * <p>
    * The process is not transaction safe. If the process fails, the caller
    * should requery the target parent to determine which relationships were
    * actually created.
    *
    * @param relationshipType  Must be one of the relationship types 
    *    configured in the system.
    *
    * @param children  Each entry must be a valid PSKey, which is a key
    *    that references an existing object in the database. Never <code>null
    *    </code>. If empty, this method returns immediately without exception.
    *    If the type of the children is not supported by the target parent,
    *    an exception is thrown.
    *
    * @param targetParent  A valid key referencing the object that will own the
    *    newly created relationships. A valid key is one that references an
    *    existing object in the database. Never <code>null</code>.
    *
    * @throws PSCmsException If any problems occur while copying the 
    *    relationship(s).
    */
   public void copy(
      String relationshipType,
      List children,
      PSKey targetParent)
      throws PSCmsException;
      
   /**
    * Get the relationship owner paths for a given item. The relationship 
    *    owner path is generated the following way:
    * <ol>
    * <li>Get the owner(s) of the current item and record the sys_title field 
    *    of this item</li>
    * <li>For each owner found walk to its parent in a recursive way until 
    *    there is no owner found</li>
    * <li>Generate the path for every for every route recursed in the syntax: 
    *    <p>sys_title_owner1/sys_title_owner2/.../sys_title_owner_last</p></li> 
    * </ol>
    * <p>
    * <b>Note that the paths generated could be multidimensional when an item in 
    *    the path has more than one owner. However, this implementation is 
    *    suitable ONLY folder relationship or a relationship that allows only 
    *    one parent for an object. The exception is that the first object can 
    *    have multiple parents and then parents have only one parent each. 
    *    This means the use case of an item that exists in multiple folders is 
    *    very well covered by this implementation since one folder can exist 
    *    in only one folder.</b>   
    * 
    * @param componentType  The value returned by the {@link
    *    IPSDbComponent#getComponentType()} method of the component being
    *    processed. Never empty or <code>null</code>. If the name is not found
    *    in the config, an exception is thrown.
    *
    * @param locator Loctaor of the item for which the owner paths are to be 
    *    generated, must not be <code>null</code>.
    *    
    * @param relationshipTypeName name of the relationship that any two 
    *    consecutive objects related by, Must be one of the relationship types 
    *    known to the system (which is configured in the workbench).
    * 
    * @return An array of paths per one recursive route explained above. Never
    *    <code>null</code>. Will be empty if the object with supplied locator 
    *    has no owner by the relationship supplied.
    * 
    * @throws PSCmsException if path generation fails for any reason. 
    */
   public String[] getRelationshipOwnerPaths(
      String componentType,
      PSLocator locator,
      String relationshipTypeName)
      throws PSCmsException;

   /**
    * Method to test if a given object is a descendent of specified parent 
    * object. The test succeeds if the parent specified matches with any object
    * between the object above the child and the root in the ancestor tree, 
    * which means the parent is an immediate parent or a grand parent via the 
    * relationship type supplied.
    * <p>
    * This method assumes that the an object will never have more than one 
    * parent and dependent is revision insensitive, which means this is NOT 
    * applicable to the relationship type categories that allow multiple parents 
    * for an item/object, e.g. 
    * {@link PSRelationshipConfig#CATEGORY_ACTIVE_ASSEMBLY}.
    * 
    * @param componentType  The value returned by the {@link
    * IPSDbComponent#getComponentType()} method of the component being
    * processed. Never empty or <code>null</code>. If the name is not found in
    * the config, an exception is thrown.
    *
    * @param parent Locator of the parent object, must not be <code>null</code>.
    * 
    * @param child Locator of the child object, must not be <code>null</code>.
    * 
    * @param relationshipTypeName name of the relationship between the two 
    * objects, must not be <code>null</code> or empty.
    * 
    * @return <code>true</code> if parent is related to child via the 
    * relationshipTypeName and child is indeed descendent of parent, 
    * <code>false</code> otherwise. 
    * 
    * @throws PSCmsException if the check could not be performed for any reason.
    */
   public boolean isDescendent(
      String componentType,
      PSLocator parent,
      PSLocator child,
      String relationshipTypeName)
      throws PSCmsException;
   
   
   /**
    * Method to retrieve all descendents of an object
    * @param componentType  The value returned by the {@link
    *    IPSDbComponent#getComponentType()} method of the component being
    *    processed. Never empty or <code>null</code>. If the name is not found
    *    in the config, an exception is thrown.
    * @param relationshipType The requested relationship type name. It may not
    *    be <code>null</code> or empty.
    * @param parent  A valid key that references the component for which you
    *    wish to obtain the child relationships. A valid key is one
    *    that references an existing object in the database. Never
    *    <code>null</code>.
    * @return An array with 0 or more entries. Each entry is a valid locator.
    * @throws PSCmsException PSCmsException If the processor for the supplied type is not
    *    found or cannot be instantiated, or any problems occur.
    */
   public PSKey[] getDescendentsLocators(
            String componentType,
            String relationshipType,
            PSKey parent)
      throws PSCmsException;
}
