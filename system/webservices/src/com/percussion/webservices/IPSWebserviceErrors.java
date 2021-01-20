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
package com.percussion.webservices;

/**
 * Error numbers for use with the error string bundle 
 * <code>PSWebserviceErrorStringBundle.properties</code>.
 */
public interface IPSWebserviceErrors
{
   /**
    * The service was called with parameters not complying to the specified
    * contract. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The service name.</td></tr>
    * <tr><td>1</td><td>The contract violation error message.</td></tr>
    * </table>
    */
   public static final int INVALID_CONTRACT = 1;

   /**
    * The design object for the specified id does not exist. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The name of the design object type.</td></tr>
    * <tr><td>1</td><td>The requested id.</td></tr>
    * </table>
    */
   public static final int OBJECT_NOT_FOUND = 2;

   /**
    * An invalid rhythmyx session was received. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The received rhythmyx session id.</td></tr>
    * </table>
    */
   public static final int INVALID_SESSION = 3;

   /**
    * The rhythmyx session header was missing. 
    */
   public static final int MISSING_SESSION = 4;
   
   /**
    * Could not lock the requested design object. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The type of the object tried to lock.</td></tr>
    * <tr><td>1</td><td>The id of the object tried to lock.</td></tr>
    * <tr><td>2</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int CREATE_LOCK_FAILED = 5;
   
   /**
    * Could not save the supplied design object. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The type of the object tried to save.</td></tr>
    * <tr><td>1</td><td>The id of the object tried to save.</td></tr>
    * <tr><td>2</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int SAVE_FAILED = 6;
   
   /**
    * Could not delete the supplied design object. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The type of the object tried to delete.</td></tr>
    * <tr><td>1</td><td>The id of the object tried to delete.</td></tr>
    * <tr><td>2</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int DELETE_FAILED = 7;
   
   /**
    * Cannot save an object because it is not locked. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The type of the object tried to lock.</td></tr>
    * <tr><td>1</td><td>The id of the object tried to lock.</td></tr>
    * </table>
    */
   public static final int OBJECT_NOT_LOCKED = 8;
   
   /**
    * Cannot save an object because it is not locked by the requesting user. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The type of the object tried to lock.</td></tr>
    * <tr><td>1</td><td>The id of the object tried to lock.</td></tr>
    * <tr><td>2</td><td>The user who has it locked.</td></tr>
    * <tr><td>3</td><td>The remining lock time in minutes.</td></tr>
    * </table>
    */
   public static final int OBJECT_NOT_LOCKED_FOR_REQUESTOR = 9;
   
   /**
    * Could not create or extend a lock. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The id of the object tried to lock.</td></tr>
    * <tr><td>1</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int CREATE_EXTEND_LOCK_FAILED = 10;
   
   /**
    * The design object for the specified id already exists (used for violating
    * unique naming rules on creation). 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The name of the design object type.</td></tr>
    * <tr><td>1</td><td>The specified name.</td></tr>
    * </table>
    */
   public static final int OBJECT_ALREADY_EXISTS = 11;   
   
   /**
    * No hierarchy node found for the specified name and parent. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The name of the hierarchy node.</td></tr>
    * <tr><td>1</td><td>The parent id.</td></tr>
    * </table>
    */
   public static final int MISSING_HIERARCHY_NODE_FOR_PARENT = 12;   
   
   /**
    * Duplicate hierarchy node found for the specified name and parent. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The name of the hierarchy node.</td></tr>
    * <tr><td>1</td><td>The parent id.</td></tr>
    * </table>
    */
   public static final int DUPLICATE_HIERARCHY_NODE_FOR_PARENT = 13;   
   
   /**
    * Delete failed due to existence of dependents 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The type of the object tried to delete.</td></tr>
    * <tr><td>1</td><td>The id of the object tried to delete.</td></tr>
    * <tr><td>2</td><td>A list of the types of dependent objects found.</td></tr>
    * </table>
    */
   public static final int DELETE_FAILED_DEPENDENTS = 14;     

   /**
    * Failed to load a design object due to an error.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The type of the object tried to load.</td></tr>
    * <tr><td>1</td><td>The id of the object tried to load.</td></tr>
    * <tr><td>2</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int LOAD_FAILED = 15;

   /**
    * Failed to find a design object due to an error.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The type of the object tried to save.</td></tr>
    * <tr><td>1</td><td>The id of the object tried to save.</td></tr>
    * <tr><td>2</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FIND_FAILED = 16;

   /**
    * Failed to load all relationship configurations.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_LOAD_REL_CONFIGS = 17;

   /**
    * Failed to lookup the community visibility.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The community id.</td></tr>
    * <tr><td>1</td><td>The object type.</td></tr>
    * <tr><td>2</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_COMMUNITY_VISIBILITY_LOOKUP = 18;

   /**
    * Unsupported type for community visibility lookup.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The object type.</td></tr>
    * </table>
    */
   public static final int UNSUPPORTD_COMMUNITY_VISIBILITY_LOOKUP_TYPE = 19;
   
   /**
    * Failed to load a workflow with a specified workflow id
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The workflow id.</td></tr>
    * </table>
    */
   public static final int FAILED_LOAD_WORKFLOW = 20;
   
   /**
    * Cannot find a specified state in a specified workflow.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The state id.</td></tr>
    * <tr><td>1</td><td>The workflow id.</td></tr>
    * <tr><td>2</td><td>The workflow name.</td></tr>
    * </table>
    */
   public static final int CANNOT_FIND_WORKFLOW_STATE_ID = 21;
   
   /**
    * The current state of the item does not match its to-state after 
    * the prepareForEdit operation.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The current state id.</td></tr>
    * <tr><td>0</td><td>The current state name.</td></tr>
    * <tr><td>1</td><td>The to-state id.</td></tr>
    * <tr><td>2</td><td>The to-state name.</td></tr>
    * </table>
    */
   public static final int CURR_STATE_NOT_MATCH = 22;
   
   /**
    * Cannot find a transition from the specified public state to
    * a quick-edit state in the specified workflow.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The id of the public state.</td></tr>
    * <tr><td>1</td><td>The name of the public state.</td></tr>
    * <tr><td>2</td><td>The workflow id.</td></tr>
    * <tr><td>3</td><td>The workflow name.</td></tr>
    * </table>
    */
   public static final int CANNOT_FIND_TRANS_TO_QE_STATE = 23;
   
   /**
    * Cannot find a transition from the specified from-state to the specified
    * to-state in the specified workflow.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The id of the from-state.</td></tr>
    * <tr><td>1</td><td>The name of the from-state.</td></tr>
    * <tr><td>2</td><td>The id of the to-state.</td></tr>
    * <tr><td>3</td><td>The name of the to-state.</td></tr>
    * <tr><td>4</td><td>The workflow id.</td></tr>
    * <tr><td>5</td><td>The workflow name.</td></tr>
    * </table>
    */
   public static final int CANNOT_FIND_TRANS_4_STATE_2_STATE = 24;
   
   /**
    * Failed to transition the specified item.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The id of the item.</td></tr>
    * <tr><td>1</td><td>The trigger name of the transition.</td></tr>
    * </table>
    */
   public static final int FAILED_TRANSITION_ITEM = 25;

   /**
    * Failed to check out the specified item.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The id of the item.</td></tr>
    * <tr><td>1</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_CHECK_OUT_ITEM = 26;

   /**
    * Failed to check in the specified item.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The id of the item.</td></tr>
    * <tr><td>1</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_CHECK_IN_ITEM = 27;
   
   /**
    * Failed to save relationship instances.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_SAVE_RELATIONSHIPS = 28;

   /**
    * Failed to load the specified relationship instance.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The id of the relationship.</td></tr>
    * <tr><td>1</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_LOAD_RELATIONSHIP = 29;
   
   /**
    * Cannot find the specified relationship instance.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The id of the relationship.</td></tr>
    * </table>
    */
   public static final int CANNOT_FIND_RELATIONSHIP = 30;
   
   /**
    * Failed to delete relationship instances.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_DELETE_RELATIONSHIPS = 31;
   
   /**
    * Operation for an object failed due to lack of permissions in the object's
    * acl for the current user.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The object guid </TD></TR>
    * <TR><TD>1</TD><TD>The action attempted</TD></TR>
    * </table>
    */
   public static final int ACCESS_CONTROL_ERROR = 32;
   
   /**
    * Failed to load objects with the specified criteria (filter or ids).
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The object type.</td></tr>
    * <tr><td>1</td><td>The specified criteria.</td></tr>
    * <tr><td>2</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int LOAD_OBJECTS_ERROR = 33;
   
   /**
    * The design object for the specified name does not exist. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The name of the design object type.</td></tr>
    * <tr><td>1</td><td>The requested name.</td></tr>
    * </table>
    */
   public static final int OBJECT_NOT_FOUND_BY_NAME = 34;

   /**
    * There is no folder path for the specified folder id. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The folder id.</td></tr>
    * </table>
    */
   public static final int NO_FOLDER_PATH_FOR_FOLDERID = 35;

   /**
    * Error occurred while loading the folder path for the specified folder id. 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The folder id.</td></tr>
    * <tr><td>1</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_LOAD_FOLDER_PATH = 36;

   /**
    * Item must be checked out by the current user.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The object guid </TD></TR>
    * <TR><TD>1</TD><TD>The action attempted</TD></TR>
    * </table>
    */
   public static final int ITEM_NOT_CHECKED_OUT = 37;

   /**
    * Child entry guid specifies invalid child id
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The child entry id</TD></TR>
    * <TR><TD>1</TD><TD>The child fieldset name</TD></TR>
    * <TR><TD>2</TD><TD>The parent item id</TD></TR>
    * </table>
    */
   public static final int INVALID_CHILD_ID = 38;
   
   /**
    * Child entry not found in specified field.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The child entry id</TD></TR>
    * <TR><TD>1</TD><TD>The child fieldset name</TD></TR>
    * <TR><TD>2</TD><TD>The parent item id</TD></TR>
    * </table>
    */
   public static final int CHILD_ENTRY_NOT_FOUND = 39;
   
   /**
    * Child entry already exists for the specified field.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The child entry id</TD></TR>
    * <TR><TD>1</TD><TD>The child fieldset name</TD></TR>
    * <TR><TD>2</TD><TD>The parent item id</TD></TR>
    * </table>
    */
   public static final int CHILD_ENTRY_ALREADY_EXISTS = 40;
   
   /**
    * An unknown content type was used with a webservice.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The service name</TD></TR>
    * <TR><TD>1</TD><TD>The unknown content type name</TD></TR>
    * <TR><TD>2</TD><TD>The underlying error message</TD></TR>
    * </table>
    */
   public static final int UNKNOWN_CONTENT_TYPE = 41;
   
   /**
    * Error occurred while looking up for an item or folder from a 
    * specified path.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The folder path</TD></TR>
    * <TR><TD>1</TD><TD>The underlying error message</TD></TR>
    * </table>
    */
   public static final int FAILED_FIND_ID_FROM_PATH = 42;

   /**
    * An specified path does not exist.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The specified folder path</TD></TR>
    * </table>
    */
   public static final int PATH_NOT_EXIST = 43;
   
   /**
    * Unknown community id.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The community id</TD></TR>
    * </table>
    */
   public static final int UNKNOWN_COMMUNITY_ID = 44;
   
   /**
    * Failed to add a list of child items or folders to the specified parent 
    * folder.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The parent folder id</TD></TR>
    * <TR><TD>1</TD><TD>The list of child ids</TD></TR>
    * <TR><TD>2</TD><TD>The underlying error message</TD></TR>
    * </table>
    */
   public static final int FAILED_ADD_FOLDER_CHILDREN = 45;
   
   /**
    * Failed to find child items or folders for the specified parent folder.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The parent folder id</TD></TR>
    * <TR><TD>1</TD><TD>The underlying error message</TD></TR>
    * </table>
    */
   public static final int FAILED_FIND_FOLDER_CHILDREN = 46;
   
   /**
    * Failed to find child items for the specified parent item.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The parent item id</TD></TR>
    * <TR><TD>1</TD><TD>The underlying error message</TD></TR>
    * </table>
    */
   public static final int FAILED_FIND_CHILD_ITEMS = 47;
   
   /**
    * Failed to find parent items for the specified child item.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The child item id</TD></TR>
    * <TR><TD>1</TD><TD>The underlying error message</TD></TR>
    * </table>
    */
   public static final int FAILED_FIND_PARENT_ITEMS = 48;
   
   /**
    * Validation failed due to the specified item has not checked out by 
    * specified user.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The item id</TD></TR>
    * <TR><TD>1</TD><TD>The user name</TD></TR>
    * </table>
    */
   public static final int ITEM_NOT_CHECKOUT_BY_USER = 49;
   

   /**
    * The specified item revision is invalid for the current context.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The content id </TD></TR>
    * <TR><TD>1</TD><TD>The revision</TD></TR>
    * <TR><TD>2</TD><TD>The username</TD></TR>
    * </table>
    */
   public static final int INVALID_EDIT_REVISION = 50;
   
   /**
    * The specified item revision is invalid for the current context.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The content id </TD></TR>
    * <TR><TD>1</TD><TD>The revision</TD></TR>
    * <TR><TD>2</TD><TD>The username</TD></TR>
    * </table>
    */
   public static final int INVALID_CURRENT_REVISION = 51;
   
   /**
    * The user has specified a community of which they are not a member.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The community </TD></TR>
    * </table>
    */
   public static final int USER_NOT_MEMBER_COMMUNITY = 52;
   
   /**
    * The user has specified a locale which does not exist or is not active.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The locale code</TD></TR>
    * </table>
    */
   public static final int INVALID_LOCALE = 53;   
   
   /**
    * The specified folder does not contain a specified child id.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The specified folder parent id.</td></tr>
    * <tr><td>1</td><td>The specified child id.</td></tr>
    * </table>
    */
   public static final int INVALID_FOLDER_CHILD = 54;
   
   /**
    * Error occurred while looking up the folder path for an item or folder.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The id of an item or folder</TD></TR>
    * <TR><TD>1</TD><TD>The underlying error message</TD></TR>
    * </table>
    */
   public static final int FAILED_FIND_PATH_FROM_ID = 55;
   
   /**
    * Save item failed.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The item id.</td></tr>
    * <tr><td>1</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_SAVE_ITEM = 56;
   
   /**
    * Load item failed.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The item id.</td></tr>
    * <tr><td>1</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_LOAD_ITEM = 57;
   
   /**
    * Item must be checked in by the current user.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The object guid </TD></TR>
    * <TR><TD>1</TD><TD>The action attempted</TD></TR>
    * </table>
    */
   public static final int ITEM_NOT_CHECKED_IN = 58;

   /**
    * Item must be checked in by the current user.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The object guid </TD></TR>
    * <TR><TD>1</TD><TD>The action attempted</TD></TR>
    * <TR><TD>2</TD><TD>The workflow of the item</TD></TR>
    * <TR><TD>3</TD><TD>The state of the item</TD></TR>
    * </table>
    */   
   public static final int INAVLID_ACTION_FOR_STATE = 59;
   
   /**
    * Failed to create a new copy.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The id of the item to create a new copy for.</TD></TR>
    * <TR><TD>1</TD><TD>The underlying error message.</TD></TR>
    * </table>
    */
   public static final int NEWCOPY_FAILED = 60;
   
   /**
    * Failed to create a new promotable version.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The id of the item to create a new promotable version for.</TD></TR>
    * <TR><TD>1</TD><TD>The underlying error message.</TD></TR>
    * </table>
    */
   public static final int NEWPROMOTABLEVERSION_FAILED = 61;
   
   /**
    * Failed to create a new translation.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <TR><TD>0</TD><TD>The id of the item to create a new translation for.</TD></TR>
    * <TR><TD>1</TD><TD>The underlying error message.</TD></TR>
    * </table>
    */
   public static final int NEWTRANSLATION_FAILED = 62;
   
   /**
    * View item failed.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The item id.</td></tr>
    * <tr><td>1</td><td>The underlying error message.</td></tr>
    * </table>
    */
   public static final int FAILED_VIEW_ITEM = 63;
   
   /**
    * Cannot convert to server item because an unknown field was supplied for
    * that contenttype.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The item id.</td></tr>
    * <tr><td>1</td><td>The field name.</td></tr>
    * </table>
    */
   public static final int UNKNOWN_FIELD_NAME = 64;
   
   /**
    * A system or shared definition failed to validate against one or more
    * content types with regards to things like overrides and/or duplicate field 
    * names.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The error message</td></tr>
    * <tr><td>1</td><td>The list of content type names for which validation 
    * generated the same error message</td></tr>
    * </table>
    */   
   public static final int FAILED_SYS_SHARED_DEF_VALIDATION = 65;
   
   /**
    * Delete of object association failed due to existence of dependents 
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The type of the association owner.</td></tr>
    * <tr><td>1</td><td>The id of the association owner.</td></tr>
    * <tr><td>2</td><td>The type of the association dependent.</td></tr>
    * <tr><td>3</td><td>The list of ids of the association dependents.</td></tr>
    * <tr><td>4</td><td>A list of the types of dependent objects found.</td></tr>
    * </table>
    */
   public static final int DELETE_ASSOCIATION_FAILED_DEPENDENTS = 66;
   
   /**
    * Failed to load a folder from a specified id or path.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The id or path string for which the load was executed.</td></tr>
    * <tr><td>1</td><td>The underlying error message.</td></tr>
    * </table>
    */   
   public static final int FAILED_LOAD_FOLDER = 67;
   
   /**
    * An operation failed with error.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The description of the operation.</td></tr>
    * <tr><td>1</td><td>The underlying error message.</td></tr>
    * </table>
    */   
   public static final int OPERATION_FAILED_ERROR = 68;
   
   /**
    * An unexpected error.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The unexpected error message.</td></tr>
    * </table>
    */   
   public static final int UNEXPECTED_ERROR = 69;

   /**
    * Unable to save the shared def due to validation errors.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The error message</td></tr>
    * </table>
    */   
   public static final int UNABLE_SAVE_SHARED_DEF_VALIDATION = 70;   

   /**
    * Failed to rename the ACL entries for communities.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The object type string</td></tr>
    * <tr><td>1</td><td>A string collection with all underlying errors</td></tr>
    * </table>
    */   
   public static final int FAILED_RENAMING_ACLS = 71;   

   /**
    * User not authorized to perform requested operation.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The users name</td></tr>
    * <tr><td>1</td><td>Requested operation</td></tr>
    * <tr><td>2</td><td>The underlying error message</td></tr>
    * </table>
    */   
   public static final int NOT_AUTHORIZED = 72;   
   
   /**
    * Hierarchy node path could not be obtained from object Id.
    * <p>
    * The arguments passed in for this message are:
    * <table border="1">
    * <tr><th>Arg</th><th>Description</th></tr>
    * <tr><td>0</td><td>The underlying error message</td></tr>
    * </table>
    */   
   public static final int FAILED_TO_OBTAIN_PATH_FROM_OBJECT_ID = 73;   
   
}



