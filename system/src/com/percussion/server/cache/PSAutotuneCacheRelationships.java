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

package com.percussion.server.cache;

import java.util.HashMap;
import java.util.Map;

import com.percussion.server.command.PSConsoleCommandAutotuneCache;

/**
 * This class provides a mapping between specific database tables and fields in
 * ehcache.xml. The {@link PSConsoleCommandAutotuneCache} class is to update the
 * maxElementsInMemory in ehcache.xml based on the count(*) result from the
 * referenced database tables.
 * 
 * <br/>
 * 
 * @author chriswright
 */
public class PSAutotuneCacheRelationships
{
   /**
    * default constructor
    */
   private PSAutotuneCacheRelationships()
   {
      super();
   }

   public static final Map<String, String> DBTABLES_AND_CACHEFIELDS = new HashMap<String, String>()
   {
      /**
       * 
       */
      private static final long serialVersionUID = 3272532891197809345L;

      {
         put("CONTENTSTATUS", "PSComponentSummary");
         put("RXLOCALE", "PSLocale");
         put("WORKFLOWAPPS", "workflow");
         put("PSX_TEMPLATE", "PSAssemblyTemplate");
         put("PSX_OBJECTS", "PSCmsObject");
         put("PSX_LOCKS", "PSObjectLock");
         put("PSX_SLOT_FINDER_PARAM", "PSSlotContentFinderParam");
         put("PSX_TEMPLATE_BINDING", "PSTemplateBinding");
         put("RXSLOTTYPE", "PSTemplateSlot");
         put("PSX_AUTOTRANSLATION", "PSAutoTranslation");
         put("PSX_PROPERTIES", "PSFolderProperty");
         put("RXLOOKUP", "PSKeyword");
         put("CONTENTTYPES", "PSNodeDefinition");
         put("PSX_ITEM_FILTER", "PSItemFilter");
         put("PSX_ITEM_FILTER_RULE", "PSItemFilterRuleDef");
         put("PSX_RECENT", "PSRecent");
         put("PSX_RXCONFIGURATIONS", "PSConfig");
         put("PSX_CONFIG_STATUS", "PSConfigStatus");
         put("PSX_PKG_INFO", "PSPkgInfo");
         put("PSX_PKG_ELEMENT", "PSPkgElement");
         put("PSX_PKG_DEPENDENCY", "PSPkgDependency");
         put("CONTENTSTATUSHISTORY", "PSContentStatusHistory");
         put("RXASSEMBLERPROPERTIES", "PSSiteProperty");
         put("RXSITES", "PSSite");
         put("RXLOCATIONSCHEME", "PSLocationScheme");
         put("RXCOMMUNITY", "PSCommunity");
         put("PSX_ROLES", "PSBackEndRole");
         put("PSX_SCH_TASK_LOG", "PSScheduledTaskLog");
         put("PSX_SCH_NOTIF_TEMPLATE", "PSNotificationTemplate");
         put("PSX_PUBSERVER_PROPERTIES", "PSPubServerProperty");
         put("PSX_PUBSERVER", "PSPubServer");
         put("PSX_CONTENTLIST_EXPANDER_PARAM", "PSTemplateExpanderParam");
         put("PSX_CONTENTLIST_GEN_PARAM", "PSContentListGeneratorParam");
         put("PSX_EDITION_TASK", "PSEditionTaskDef");
         put("RXEDITIONCLIST", "PSEditionContentList");
         put("RXEDITION", "PSEdition");
         put("PSX_DELIVERY_TYPE", "PSDeliveryType");
         put("RXCONTENTLIST", "PSContentList");
         put("PSX_PUBLICATION_SITE_ITEM", "PSSiteItem");
         put("PSX_PUBLICATION_DOC", "pubstatus");
         put("PSX_OBJECTRELATIONSHIPPROP", "PSRelationshipPropertyData");
         put("PSX_PERSISTEDPROPERTYVALUES", "PSPersistentProperty");
         put("PSX_PERSISTEDPROPERTYMETA", "PSPersistentPropertyMeta");
         put("RXSLOTCONTENT", "PSTemplateTypeSlotAssociation");
         put("PSX_ITEM_FILTER_RULE_PARAM", "PSItemFilterRuleParam");
         put("PSX_GUID_DATA", "PSGuidGeneratorData");
         put("PSX_RELATIONSHIPCONFIGNAME", "PSRelationshipConfigName");
         put("PSX_OBJECTRELATIONSHIP", "PSRelationshipData");
         put("RXSYSCOMPONENT", "PSUIComponent");
         put("PSX_SUBJECTS", "PSBackEndSubject");
         put("RXCOMMUNITYROLE", "PSCommunityRoleAssociation");
         put("PSX_CONTENTCHANGEEVENT", "PSContentChangeEvent");
         put("PSX_CONTENTTYPE_TEMPLATE", "PSContentTemplateDesc");
         put("PSX_CONTENTTYPE_WORKFLOW", "PSContentTypeWorkflow");
         put("PSX_WB_HIERARCHY_NODE", "PSHierarchyNode");
         put("PSX_WB_HIERARCHY_NODE_PROP", "PSHierarchyNodeProperty");
         put("PSX_ID_NAME", "PSIdName");
         put("PSX_IMPORTLOGENTRY", "PSImportLogEntry");
         put("PSX_INTEGRITYSTATUS", "PSIntegrityStatus");
         put("PSX_INTEGRITYTASK", "PSIntegrityTask");
         put("PSX_INTEGRITY_TASK_PROPERTIES", "PSIntegrityTaskProperty");
         put("RXLOCATIONSCHEMEPARAMS", "PSLocationSchemeParameter");
         put("PSX_MANAGEDLINK", "PSManagedLink");
         put("PSX_METADATA", "PSMetadata");
         put("RXCONTEXT", "PSPublishingContext");
         put("PSX_SHARED_PROPERTIES", "PSSharedProperty");
         put("PSX_SITEIMPORTSUMMARY", "PSSiteImportSummary");
         put("RXSYSCOMPONENTPROPERTY", "PSUIComponentProperty");
         put("PSX_USERITEM", "PSUserItem");
         put("PSX_WIDGETBUILDERDEFINITION", "PSWidgetBuilderDefinition");
      }
   };

   /**
    * Maintains the list of large table names which
    * must be traversed in order as they are presented.
    * Contentstatus should be cached first, then statushistory,
    * pssiteitem, and pubstatus.  First the smaller tables are
    * allocated in cache and then these larger tables. Remaining memory
    * is then allocated for the larger tables after the small ones as
    * they can grow infinitely big.
    */
   public static final String[] LARGE_TABLE_NAMES =
   {"PSComponentSummary", "PSContentStatusHistory", "PSRelationshipData", "PSSiteItem", "pubstatus"};

   /**
    * Maintains the mapping of large tables to their equivalent
    * ehcache average item size.  There is no easy decision as to
    * check for the average cache sizes dynamically using the 
    * {@link PSCacheStatisticsSnapshot} class because if the cache
    * was recently flushed it may not be accurate.
    */
   public static final Map<String, Double> LARGE_TABLES = new HashMap<String, Double>()
   {
      /**
       * 
       */
      private static final long serialVersionUID = 2554253050884012589L;

      {
         put("PSComponentSummary", 1.6);
         put("PSContentStatusHistory", 1.5);
         put("pubstatus", 1.7);
         put("PSSiteItem", 1.4);
         put("PSRelationshipPropertyData", 1.4);
      }
   };

   /**
    * Maintains the mapping of small tables to their equivalent
    * ehcache average item size.  There is no easy decision as to
    * check for the average cache sizes dynamically using the 
    * {@link PSCacheStatisticsSnapshot} class because if the cache
    * was recently flushed it may not be accurate.
    */
   public static final Map<String, Double> SMALL_TABLES = new HashMap<String, Double>()
   {
      /**
       * 
       */
      private static final long serialVersionUID = 4626103100667474371L;

      {
         put("workflow", 11.0);
         put("PSAssemblyTemplate", 2.6);
         put("PSGuidGeneratorData", 1.1);
         put("PSLocale", 1.3);
         put("PSCmsObject", 1.3);
         put("PSContentListGeneratorParam", 1.4);
         put("PSObjectLock", 0.0); // TODO: find this one
         put("PSSlotContentFinderParam", 0.0); // TODO: find this one
         put("PSTemplateBinding", 1.3);
         put("PSItemFilterRuleParam", 0.0); // TODO: find this one
         put("PSRelationshipConfigName", 1.3);
         put("PSTemplateSlot", 1.4);
         put("PSWidgetBuilderDefinition", 0.0); // TODO: find this one
         put("PSPersistentPropertyMeta", 1.3);
         put("PSAutoTranslation", 0.0); // TODO: find this one
         put("PSFolderProperty", 1.2);
         put("PSKeyword", 1.3);
         put("PSUserItem", 1.3);
         put("PSNodeDefinition", 1.5);
         put("PSItemFilter", 1.3);
         put("PSItemFilterRuleDef", 1.4);
         put("PSRecent", 1.3);
         put("PSConfig", 39.4);
         put("PSConfigStatus", 63.3);
         put("PSRelationshipPropertyData", 0.0); // TODO: find this one
         put("PSPersistentProperty", 1.3);
         put("PSPkgInfo", 1.5);
         put("PSPkgElement", 1.3);
         put("PSPkgDependency", 1.3);
         put("PSSiteProperty", 1.4);
         put("PSSite", 1.5);
         put("PSSiteImportSummary", 0.0); // TODO: find this one
         put("PSLocationScheme", 1.4);
         put("PSCommunity", 1.3);
         put("PSBackEndRole", 1.3);
         put("PSLocationSchemeParameter", 0.0); // TODO: find this one
         put("PSScheduledTaskLog", 0.0); // TODO: find this one
         put("PSNotificationTemplate", 2.0);
         put("PSPubServerProperty", 1.2);
         put("PSPubServer", 1.3);
         put("PSTemplateExpanderParam", 0.0); // TODO: find this one
         put("PSEditionTaskDef", 1.3);
         put("PSEditionContentList", 4.7);
         put("PSEdition", 1.4);
         put("PSDeliveryType", 1.3);
         put("PSContentList", 1.6);
         put("PSTemplateTypeSlotAssociation", 4.7);
         put("PSRelationshipData", 1.3);
         put("PSUIComponent", 1.2);
         put("PSBackEndSubject", 1.2);
         put("PSCommunityRoleAssociation", 4.5);
         put("PSContentChangeEvent", 0.0); // TODO: find this one
         put("PSContentTemplateDesc", 1.3);
         put("PSContentTypeWorkflow", 1.3);
         put("PSHierarchyNode", 0.0); // TODO: find this one
         put("PSHierarchyNodeProperty", 0.0); // TODO: find this one
         put("PSIdName", 1.2);
         put("PSImportLogEntry", 0.0); // TODO: find this one
         put("PSIntegrityStatus", 0.0); // TODO: find this one
         put("PSIntegrityTask", 0.0); // TODO: find this one
         put("PSIntegrityTaskProperty", 0.0); // TODO: find this one
         put("PSManagedLink", 1.3);
         put("PSMetadata", 1.2);
         put("PSPublishingContext", 1.3);
         put("PSSharedProperty", 0.0); // TODO: find this one
         put("PSUIComponentProperty", 1.2);
      }
   };
}
