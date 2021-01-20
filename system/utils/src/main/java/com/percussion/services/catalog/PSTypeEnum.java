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
package com.percussion.services.catalog;


import org.apache.commons.lang.StringUtils;

/**
 * This enum lists all types in the system that participate in cataloging. The
 * ordinal value of these values is used when creating guids. Ordinal values
 * <b>must</b> be unique.
 * 
 * @author dougrand
 */
public enum PSTypeEnum {
   /**
    * Used for internal objects that require a unique id, but are not exposed
    * through the service interface.
    */
   INTERNAL(0),
   /**
    * Indicates a JSR-170 item is referenced. The object could be a node or a
    * property. (Not used in Rhino, consider this a placeholder)
    */
   ITEM(1),
   /**
    * Indicates a JSR-170 node definition is referenced (aka a content type id)
    */
   NODEDEF(2, "Content Type", "CONTENTTYPES"),
   /**
    * Indicates a JSR-170 property definition is referenced.
    */
   PROPERTYDEF(3),
   /**
    * Indicates a template is referenced.
    */
   TEMPLATE(4, "Template", "variantid"),
   /**
    * Indicates a template slot is referenced.
    */
   SLOT(5, "Slot", "slotid"),
   /**
    * Indicates an item filter.
    */
   ITEM_FILTER(7, "Item Filter", "PSItemFilter"),
   /**
    * Indicates an item filter rule def.
    */
   ITEM_FILTER_RULE_DEF(8),
   /**
    * Indicates a site object.
    */
   SITE(9, "PSSite", "Site", "newsiteid"),
   /**
    * A location scheme for url and location assembly.
    */
   LOCATION_SCHEME(10, "PSLocationScheme", "Location Scheme", "schemeid"),
   /**
    * Site child table that holds properties, uses just the UUID.
    */
   SITE_PROPERTY(11, "PSSiteProperty", null, "variableid"),
   /**
    * Location scheme child table that holds properties, uses just the UUID.
    */
   LOCATION_PROPERTY(12, "PSLocationSchemeParameter", null, "schemeparamid"),
   /**
    * A community definition.
    */
   COMMUNITY_DEF(13, "Community", "communityid"),
   /**
    * A keyword definition.
    */
   KEYWORD_DEF(14, "Keyword", "RXLOOKUP"),
   /**
    * A search definition.
    */
   SEARCH_DEF(15, "Search", "searchid"),
   /**
    * A role value
    */
   ROLE(16, "Role", "Role"),

   /**
    * An ACL
    */
   ACL(17),

   /**
    * A view definition.
    */
   VIEW_DEF(18, "View", "searchid"),

   /**
    * A relationship object.
    */
   RELATIONSHIP(19, "Relationship", "RXRELATEDCONTENT"),

   /**
    * A relationship config name object.
    */
   RELATIONSHIP_CONFIGNAME(20, "Relationship Type",
         "PSX_RELATIONSHIPCONFIGNAME"),

   /**
    * A content list
    */
   CONTENT_LIST(21, "Content List", "contentlistid"),

   /**
    * A workflow definition.
    */
   WORKFLOW(23, null, "Workflow"),

   /**
    * A workflow notification definition.
    */
   WORKFLOW_NOTIFICATION(24, null, "NOTIFICATIONS"),

   /**
    * A workflow role definition.
    */
   WORKFLOW_ROLE(25, null, "ROLES"),

   /**
    * A workflow state definition.
    */
   WORKFLOW_STATE(26, null, "STATES"),

   /**
    * A workflow transition definition.
    */
   WORKFLOW_TRANSITION(27, null, "TRANSITIONS"),

   /**
    * An item history entry.
    */
   ITEM_HISTORY(28, null, "CONTENTSTATUSHISTORY"),

   /**
    * A shared property.
    */
   SHARED_PROPERTY(29),

   /**
    * An object lock.
    */
   OBJECT_LOCK(30),

   /**
    * Display format definition
    */
   DISPLAY_FORMAT(31, "Display Format", "displayformatid"),

   /**
    * Identifies a hierarchy node as used with the workbench.
    */
   HIERARCHY_NODE(32),

   /**
    * Identifies a hierarchy node property as used with the workbench.
    */
   HIERARCHY_NODE_PROPERTY(33),

   /**
    * Identifies a configuration that can be loaded, edited, and saved through
    * the workbench.
    */
   CONFIGURATION(34, "Configuration"),

   /**
    * Identifies the set of all auto translation definitions.
    */
   AUTO_TRANSLATIONS(35, "Translation Settings"),

   /**
    * Identifies the scheduled tasks. It is only used by the task scheduler
    * subsystem. The task object is saved by Quartz (table prefixed with PSX_Q)
    */
   SCHEDULED_TASK(36, "Scheduled Task", "PSX_SCH_TASK"),

   /**
    * Identifies the event log for scheduled tasks.
    */
   SCHEDULE_TASK_LOG(37, "Scheduled Task Log", "PSX_SCH_TASK_LOG"),

   /**
    * Identifies the Notification Template for the message of scheduled tasks.
    */
   SCHEDULE_NOTIFICATION_TEMPLATE(38, "Task Notification Template",
         "PSX_SCH_NOTIF_TEMPLATE"),

   /**
    * A locale object, i.e. {@link com.percussion.i18n.PSLocale}.
    */
   LOCALE(100, "Locale", "localeid"),

   /**
    * A content item from the legacy content repository. Will be deprecated in
    * the future.
    */
   LEGACY_CONTENT(101, "Content Item/Folder"),

   /**
    * A content item's child from the legacy content repository. Will be
    * deprecated in the future.
    */
   LEGACY_CHILD(102, "Child Item"),

   /**
    * A child content type from the item def manager. Will be deprecated in the
    * future.
    */
   LEGACY_CHILD_CONTENT_TYPE(104, "Content Type"),

   /**
    * Rhythmyx extension.
    */
   EXTENSION(105, "Extension"),

   /**
    * Design objects used only by the workbench for managing a user-defined
    * folder structure.
    */
   WORKBENCH_FILE(106),

   /**
    * A menu action.
    */
   ACTION(107, "Menu Action", "actionid"),

   /**
    * A menu mode.
    */
   MENU_MODE(108),

   /**
    * A menu context.
    */
   MENU_CONTEXT(109),

   /**
    * A publishing edition.
    */
   EDITION(110, "Edition", "editionid"),

   /**
    * Uid for publishing records.
    */
   PUB_REFERENCE_ID(111),

   /**
    * Delivery types.
    */
   DELIVERY_TYPE(112),

   /**
    * Assembly and publishing location contexts.
    */
   CONTEXT(113, "Context", "contextid"),

   /**
    * Edition to content list association.
    */
   EDITION_CONTENT_LIST(114, "Edition Content List", "editionclistid"),

   /**
    * Edition task definition.
    */
   EDITION_TASK_DEF(115, "Edition Task Definition", "PSX_EDITION_TASK"),

   /**
    * Deployer Package id.
    */
   DEPLOYER_PACKAGE_ID(116),

   /**
    * Deployer descriptor id.
    */
   DEPLOYER_DESCRIPTOR_ID(117),

   /**
    * Package Information object.
    */
   PACKAGE_INFO(118),

   /**
    * Package Element object.
    */
   PACKAGE_ELEMENT(119),

   /**
    * Package Element Dependency object.
    */
   PACKAGE_ELEMENT_DEPENDENCY(120),

   /**
    * Package Information object.
    */
   PACKAGE_CONFIG_INFO(121),

   /**
    * Application.
    */
   APPLICATION(122),

   /**
    * Auth type.
    */
   AUTH_TYPE(123),

   /**
    * Component.
    */
   COMPONENT(124),

   /**
    * Component slot.
    */
   COMPONENT_SLOT(125),

   /**
    * Image file.
    */
   IMAGE_FILE(126),

   /**
    * Content type-template definition.
    */
   CONTENT_TYPE_TEMPLATE_DEF(127),

   /**
    * Control.
    */
   CONTROL(128),

   /**
    * Custom object.
    */
   CUSTOM(129),

   /**
    * Table data.
    */
   TABLE_DATA(130),

   /**
    * Database function definition.
    */
   DATABASE_FUNCTION_DEF(131),

   /**
    * Folder.
    */
   FOLDER(132),

   /**
    * Folder contents.
    */
   FOLDER_CONTENTS(133),

   /**
    * Folder translations.
    */
   FOLDER_TRANSLATIONS(134),

   /**
    * Folder tree.
    */
   FOLDER_TREE(135),

   /**
    * Loadable handler.
    */
   LOADABLE_HANDLER(136),

   /**
    * Action menu category.
    */
   ACTION_CATEGORY(137),

   /**
    * Shared group.
    */
   SHARED_GROUP(138),

   /**
    * Table schema.
    */
   TABLE_SCHEMA(139),

   /**
    * Stylesheet.
    */
   STYLESHEET(140),

   /**
    * Support file.
    */
   SUPPORT_FILE(141),

   /**
    * System definition.
    */
   SYSTEM_DEF(142),

   /**
    * Template community definition.
    */
   TEMPLATE_COMMUNITY_DEF(143),

   /**
    * User dependency.
    */
   USER_DEPENDENCY(144),

   /**
    * Content.
    */
   CONTENT(145),

   /**
    * Content assembler.
    */
   CONTENT_ASSEMBLER(146),

   /**
    * Used by the temporary IDs table
    */
   TEMP_IDS(147, "Temporary Ids", "PSX_TEMPIDS"),

   /**
    * Used for the ID of
    * {@link com.percussion.services.workflow.data.PSNotification}
    */
   WORKFLOW_TRANS_NOTIFICATION(150, null, "TRANSITIONNOTIFICATIONS"),

   /**
    * This is used to get the system-wide, unique and increased sort-rank number 
    */
   SORT_RANK(151, null, "SORT_RANK"),
   
   /**
    * Publishing server.
    */
   PUBLISHING_SERVER(152, null, "pubserver"),
   
   /**
   * Server child table that holds properties, uses just the UUID.
   */
   SERVER_PROPERTY(153, null, "pubserver_property"),
   
   /**
   * Integrity task id.
   */
   INTEGRITY_TASK(154, null, "integrity_task"),
   
   /**
    * Integrity task property id
    */
   INTEGRITY_TASK_PROPERTY(155, null, "integrity_task_property"),
   
   /**
    * An invalid type, only used for testing purposes.
    */
   INVALID(0xFF);

   /**
    * Ordinal value, initialized in the ctor, and never modified.
    */
   private short mi_ordinal;

   /**
    * Key value, initialized for legacy types in the ctor, never modified, may
    * be <code>null</code>, never empty.
    */
   private String mi_key = null;

   /**
    * The display name of the type, may be <code>null</code> or empty after
    * construction.
    */
   private String mi_displayName;

   /**
    * The class if registered.
    */
   private String mi_className;

   /**
    * Returns the ordinal value for the enumeration. This ordinal is used as an
    * part of the {@link com.percussion.utils.guid.IPSGuid} id, and can be used
    * as part of the cataloging process
    * 
    * @return the ordinal
    */
   public short getOrdinal()
   {
      return mi_ordinal;
   }

   /**
    * For legacy types this enum stores the key from the next number table that
    * is used to lookup the next block of ids.
    * 
    * @return the key, will be <code>null</code> for types that use true guids.
    */
   public String getKey()
   {
      return mi_key;
   }

   /**
    * Get the name of this type to use suitable for display to an end user. This
    * value is not necessarily unique among types and should not be used other
    * than for display purposes.
    * 
    * @return The name, never <code>null</code> or empty.
    */
   public String getDisplayName()
   {
      return StringUtils.isBlank(mi_displayName) ? name() : mi_displayName;
   }

   /**
    * Ctor
    * 
    * @param ord
    */
   private PSTypeEnum(int ord)
   {
      this(ord, null, (String) null);
   }

   /**
    * Ctor
    * 
    * @param ord
    * @param displayName
    */
   private PSTypeEnum(int ord, String displayName)
   {
      this(ord, displayName, (String) null);
   }

   /**
    * Ctor
    * 
    * @param ord
    * @param clazz
    * @param displayName
    */
   private PSTypeEnum(int ord, String displayName, Class clazz)
   {
      this(ord, clazz, displayName, null);
   }

   /**
    * Ctor
    * 
    * @param ord
    * @param displayName
    * @param keyvalue
    */
   private PSTypeEnum(int ord, String displayName, String keyvalue)
   {
      this(ord, (String)null, displayName, keyvalue);
   }

   /**
    * Construct a type enum.
    * 
    * @param ord The ordinal value to use
    * @param className The className associated with the ordinal, may be
    *           <code>null</code>
    * @param displayName The display name, if <code>null</code> or empty, the
    *           name is used.
    * @param keyvalue Optional key value to use for id generation, specifies a
    *           key for the NEXTNUMBER table. If <code>null</code> or empty, the
    *           default guid manager key generation is used instead of the
    *           NEXTNUMBER table.
    */
   private PSTypeEnum(int ord, String className, String displayName, String keyvalue)
   {

      if (ord > Short.MAX_VALUE)
      {
         throw new IllegalArgumentException("Ordinal value too large");
      }

      mi_ordinal = (short) ord;

      mi_displayName = displayName;

      mi_className = className;

      if (keyvalue != null)
      {
         if (keyvalue.trim().length() == 0)
            throw new IllegalArgumentException("keyvalue may not be empty");

         mi_key = keyvalue;
      }
   }

   /**
    * Construct a type enum.
    *
    * @param ord The ordinal value to use
    * @param clazz The class associated with the ordinal, may be
    *           <code>null</code>
    * @param displayName The display name, if <code>null</code> or empty, the
    *           name is used.
    * @param keyvalue Optional key value to use for id generation, specifies a
    *           key for the NEXTNUMBER table. If <code>null</code> or empty, the
    *           default guid manager key generation is used instead of the
    *           NEXTNUMBER table.
    */
   private PSTypeEnum(int ord, Class clazz, String displayName, String keyvalue)
   {
      this(ord,clazz.getSimpleName(),displayName,keyvalue);
   }
   /**
    * Lookup enum value by ordinal. Ordinals should be unique. If they are not
    * unique, then the first enum value with a matching ordinal is returned.
    * 
    * @param s ordinal value
    * @return an enumerated value or <code>null</code> if the ordinal does not
    *         match
    * @throws IllegalArgumentException
    */
   public static PSTypeEnum valueOf(int s) throws IllegalArgumentException
   {
      PSTypeEnum types[] = values();
      for (int i = 0; i < types.length; i++)
      {
         if (types[i].getOrdinal() == s)
            return types[i];
      }
      return null;
   }

   /**
    * Lookup enum value by class. This will only return values for enum values
    * that have an associated class.
    * 
    * @param clazz the class, never <code>null</code>
    * @return an enumerated value or <code>null</code> if the class isn't
    *         registered
    */
   public static PSTypeEnum valueOf(Class clazz)
   {
      PSTypeEnum types[] = values();
      for (int i = 0; i < types.length; i++)
      {
         if (types[i].mi_className == clazz.getSimpleName())
            return types[i];
      }
      return null;
   }
}
