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

package com.percussion.rest;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description ="The supported object types on the system.")
public enum ObjectTypeEnum {
    /**
     * Used for internal objects that require a unique id, but are not exposed
     * through the service interface.
     */
    INTERNAL,
    /**
     * Indicates a JSR-170 item is referenced. The object could be a node or a
     * property. (Not used in Rhino, consider this a placeholder)
     */
    ITEM,
    /**
     * Indicates a JSR-170 node definition is referenced (aka a content type id)
     */
    NODEDEF,
    /**
     * Indicates a JSR-170 property definition is referenced.
     */
    PROPERTYDEF,
    /**
     * Indicates a template is referenced.
     */
    TEMPLATE,
    /**
     * Indicates a template slot is referenced.
     */
    SLOT,
    /**
     * Indicates an item filter.
     */
    ITEM_FILTER,
    /**
     * Indicates an item filter rule def.
     */
    ITEM_FILTER_RULE_DEF,
    /**
     * Indicates a site object.
     */
    SITE,
    /**
     * A location scheme for url and location assembly.
     */
    LOCATION_SCHEME,
    /**
     * Site child table that holds properties, uses just the UUID.
     */
    SITE_PROPERTY,
    /**
     * Location scheme child table that holds properties, uses just the UUID.
     */
    LOCATION_PROPERTY,
    /**
     * A community definition.
     */
    COMMUNITY_DEF,
    /**
     * A keyword definition.
     */
    KEYWORD_DEF,
    /**
     * A search definition.
     */
    SEARCH_DEF,
    /**
     * A role value
     */
    ROLE,

    /**
     * An ACL
     */
    ACL,

    /**
     * A view definition.
     */
    VIEW_DEF,

    /**
     * A relationship object.
     */
    RELATIONSHIP,

    /**
     * A relationship config name object.
     */
    RELATIONSHIP_CONFIGNAME,

    /**
     * A content list
     */
    CONTENT_LIST,

    /**
     * A workflow definition.
     */
    WORKFLOW,

    /**
     * A workflow notification definition.
     */
    WORKFLOW_NOTIFICATION,

    /**
     * A workflow role definition.
     */
    WORKFLOW_ROLE,

    /**
     * A workflow state definition.
     */
    WORKFLOW_STATE,

    /**
     * A workflow transition definition.
     */
    WORKFLOW_TRANSITION,

    /**
     * An item history entry.
     */
    ITEM_HISTORY,

    /**
     * A shared property.
     */
    SHARED_PROPERTY,

    /**
     * An object lock.
     */
    OBJECT_LOCK,

    /**
     * Display format definition
     */
    DISPLAY_FORMAT,

    /**
     * Identifies a hierarchy node as used with the workbench.
     */
    HIERARCHY_NODE,

    /**
     * Identifies a hierarchy node property as used with the workbench.
     */
    HIERARCHY_NODE_PROPERTY,

    /**
     * Identifies a configuration that can be loaded, edited, and saved through
     * the workbench.
     */
    CONFIGURATION,

    /**
     * Identifies the set of all auto translation definitions.
     */
    AUTO_TRANSLATIONS,

    /**
     * Identifies the scheduled tasks. It is only used by the task scheduler
     * subsystem. The task object is saved by Quartz (table prefixed with PSX_Q)
     */
    SCHEDULED_TASK,

    /**
     * Identifies the event log for scheduled tasks.
     */
    SCHEDULE_TASK_LOG,

    /**
     * Identifies the Notification Template for the message of scheduled tasks.
     */
    SCHEDULE_NOTIFICATION_TEMPLATE,

    /**
     * A locale object, i.e. {@link com.percussion.i18n.PSLocale}.
     */
    LOCALE,

    /**
     * A content item from the legacy content repository. Will be deprecated in
     * the future.
     */
    LEGACY_CONTENT,

    /**
     * A content item's child from the legacy content repository. Will be
     * deprecated in the future.
     */
    LEGACY_CHILD,

    /**
     * A child content type from the item def manager. Will be deprecated in the
     * future.
     */
    LEGACY_CHILD_CONTENT_TYPE,

    /**
     * Rhythmyx extension.
     */
    EXTENSION,

    /**
     * Design objects used only by the workbench for managing a user-defined
     * folder structure.
     */
    WORKBENCH_FILE,

    /**
     * A menu action.
     */
    ACTION,

    /**
     * A menu mode.
     */
    MENU_MODE,

    /**
     * A menu context.
     */
    MENU_CONTEXT,

    /**
     * A publishing edition.
     */
    EDITION,

    /**
     * Uid for publishing records.
     */
    PUB_REFERENCE_ID,

    /**
     * Delivery types.
     */
    DELIVERY_TYPE,

    /**
     * Assembly and publishing location contexts.
     */
    CONTEXT,

    /**
     * Edition to content list association.
     */
    EDITION_CONTENT_LIST,

    /**
     * Edition task definition.
     */
    EDITION_TASK_DEF,

    /**
     * Deployer Package id.
     */
    DEPLOYER_PACKAGE_ID,

    /**
     * Deployer descriptor id.
     */
    DEPLOYER_DESCRIPTOR_ID,

    /**
     * Package Information object.
     */
    PACKAGE_INFO,

    /**
     * Package Element object.
     */
    PACKAGE_ELEMENT,

    /**
     * Package Element Dependency object.
     */
    PACKAGE_ELEMENT_DEPENDENCY,

    /**
     * Package Information object.
     */
    PACKAGE_CONFIG_INFO,

    /**
     * Application.
     */
    APPLICATION,

    /**
     * Auth type.
     */
    AUTH_TYPE,

    /**
     * Component.
     */
    COMPONENT,

    /**
     * Component slot.
     */
    COMPONENT_SLOT,

    /**
     * Image file.
     */
    IMAGE_FILE,

    /**
     * Content type-template definition.
     */
    CONTENT_TYPE_TEMPLATE_DEF,

    /**
     * Control.
     */
    CONTROL,

    /**
     * Custom object.
     */
    CUSTOM,

    /**
     * Table data.
     */
    TABLE_DATA,

    /**
     * Database function definition.
     */
    DATABASE_FUNCTION_DEF,

    /**
     * Folder.
     */
    FOLDER,

    /**
     * Folder contents.
     */
    FOLDER_CONTENTS,

    /**
     * Folder translations.
     */
    FOLDER_TRANSLATIONS,

    /**
     * Folder tree.
     */
    FOLDER_TREE,

    /**
     * Loadable handler.
     */
    LOADABLE_HANDLER,

    /**
     * Action menu category.
     */
    ACTION_CATEGORY,

    /**
     * Shared group.
     */
    SHARED_GROUP,

    /**
     * Table schema.
     */
    TABLE_SCHEMA,

    /**
     * Stylesheet.
     */
    STYLESHEET,

    /**
     * Support file.
     */
    SUPPORT_FILE,

    /**
     * System definition.
     */
    SYSTEM_DEF,

    /**
     * Template community definition.
     */
    TEMPLATE_COMMUNITY_DEF,

    /**
     * User dependency.
     */
    USER_DEPENDENCY,

    /**
     * Content.
     */
    CONTENT,

    /**
     * Content assembler.
     */
    CONTENT_ASSEMBLER,

    /**
     * Used by the temporary IDs table
     */
    TEMP_IDS,

    /**
     * Used for the ID of Notification}
     */
    WORKFLOW_TRANS_NOTIFICATION,

    /**
     * This is used to get the system-wide, unique and increased sort-rank number
     */
    SORT_RANK,

    /**
     * Publishing server.
     */
    PUBLISHING_SERVER,

    /**
     * Server child table that holds properties, uses just the UUID.
     */
    SERVER_PROPERTY,

    /**
     * Integrity task id.
     */
    INTEGRITY_TASK,

    /**
     * Integrity task property id
     */
    INTEGRITY_TASK_PROPERTY,

    /**
     * An invalid type, only used for testing purposes.
     */
    INVALID
}
