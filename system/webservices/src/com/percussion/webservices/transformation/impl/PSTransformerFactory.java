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
package com.percussion.webservices.transformation.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemRelatedItem;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.design.objectstore.PSContentEditorSharedDef;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRole;
import com.percussion.error.PSExceptionUtils;
import com.percussion.i18n.PSLocale;
import com.percussion.search.objectstore.PSWSSearchField;
import com.percussion.search.objectstore.PSWSSearchParams;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.data.PSTemplateBinding;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSContentTypeSummary;
import com.percussion.services.content.data.PSContentTypeSummaryChild;
import com.percussion.services.content.data.PSFieldDescription;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.content.data.PSKeywordChoice;
import com.percussion.services.content.data.PSSearchSummary;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.locking.data.PSObjectLockSummary;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.security.data.PSCommunityVisibility;
import com.percussion.services.security.data.PSLogin;
import com.percussion.services.system.data.PSDependency;
import com.percussion.services.system.data.PSDependent;
import com.percussion.services.system.data.PSMimeContentAdapter;
import com.percussion.services.system.data.PSSharedProperty;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.services.workflow.data.PSAgingTransition;
import com.percussion.services.workflow.data.PSAssignedRole;
import com.percussion.services.workflow.data.PSNotification;
import com.percussion.services.workflow.data.PSNotificationDef;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSTransition;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.services.workflow.data.PSWorkflowRole;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.assembly.data.OutputFormatType;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateBindingsBinding;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;
import com.percussion.webservices.assembly.data.PSTemplateSlotType;
import com.percussion.webservices.assembly.data.PublishType;
import com.percussion.webservices.assembly.data.TemplateType;
import com.percussion.webservices.assembly.data.TemplateUsageType;
import com.percussion.webservices.content.PSChildEntry;
import com.percussion.webservices.content.PSContentEditorDefinition;
import com.percussion.webservices.content.PSContentType;
import com.percussion.webservices.content.PSFieldDescriptionDataType;
import com.percussion.webservices.content.PSRelatedItem;
import com.percussion.webservices.faults.PSError;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSLockFault;
import com.percussion.webservices.transformation.converter.PSAaRelationshipConverter;
import com.percussion.webservices.transformation.converter.PSAclEntryImplConverter;
import com.percussion.webservices.transformation.converter.PSAclImplConverter;
import com.percussion.webservices.transformation.converter.PSActionConverter;
import com.percussion.webservices.transformation.converter.PSAgingTransitionConverter;
import com.percussion.webservices.transformation.converter.PSArrayConverter;
import com.percussion.webservices.transformation.converter.PSArrayToListConverter;
import com.percussion.webservices.transformation.converter.PSArrayToSetConverter;
import com.percussion.webservices.transformation.converter.PSAssemblyTemplateWsConverter;
import com.percussion.webservices.transformation.converter.PSAssignedRoleConverter;
import com.percussion.webservices.transformation.converter.PSAutoTranslationConverter;
import com.percussion.webservices.transformation.converter.PSCommunityConverter;
import com.percussion.webservices.transformation.converter.PSCommunityVisibilityConverter;
import com.percussion.webservices.transformation.converter.PSConnectorTypesConverter;
import com.percussion.webservices.transformation.converter.PSContentEditorDefinitionConverter;
import com.percussion.webservices.transformation.converter.PSContentTemplateDescConverter;
import com.percussion.webservices.transformation.converter.PSContentTypeConverter;
import com.percussion.webservices.transformation.converter.PSContentTypeSummaryChildConverter;
import com.percussion.webservices.transformation.converter.PSContentTypeSummaryConverter;
import com.percussion.webservices.transformation.converter.PSConverter;
import com.percussion.webservices.transformation.converter.PSDimensionEnumConverter;
import com.percussion.webservices.transformation.converter.PSDisplayFormatConverter;
import com.percussion.webservices.transformation.converter.PSErrorResultsExceptionConverter;
import com.percussion.webservices.transformation.converter.PSErrorsExceptionConverter;
import com.percussion.webservices.transformation.converter.PSFieldConverter;
import com.percussion.webservices.transformation.converter.PSFieldDescriptionConverter;
import com.percussion.webservices.transformation.converter.PSFieldSourceTypeConverter;
import com.percussion.webservices.transformation.converter.PSFieldTransferEncodingConverter;
import com.percussion.webservices.transformation.converter.PSFieldTypeConverter;
import com.percussion.webservices.transformation.converter.PSFolderConverter;
import com.percussion.webservices.transformation.converter.PSGlobalTemplateUsageConverter;
import com.percussion.webservices.transformation.converter.PSHierarchyNodeConverter;
import com.percussion.webservices.transformation.converter.PSItemChildEntryConverter;
import com.percussion.webservices.transformation.converter.PSItemConverter;
import com.percussion.webservices.transformation.converter.PSItemDataTypeConverter;
import com.percussion.webservices.transformation.converter.PSItemFieldValueTypeConverter;
import com.percussion.webservices.transformation.converter.PSItemFilterConverter;
import com.percussion.webservices.transformation.converter.PSItemStatusConverter;
import com.percussion.webservices.transformation.converter.PSItemSummaryConverter;
import com.percussion.webservices.transformation.converter.PSKeywordChoiceConverter;
import com.percussion.webservices.transformation.converter.PSKeywordConverter;
import com.percussion.webservices.transformation.converter.PSListToArrayConverter;
import com.percussion.webservices.transformation.converter.PSLocaleConverter;
import com.percussion.webservices.transformation.converter.PSMimeContentAdapterConverter;
import com.percussion.webservices.transformation.converter.PSNodeTypeConverter;
import com.percussion.webservices.transformation.converter.PSNotificationConverter;
import com.percussion.webservices.transformation.converter.PSNotificationDefConverter;
import com.percussion.webservices.transformation.converter.PSObjectSummaryConverter;
import com.percussion.webservices.transformation.converter.PSObjectTypeEnumConverter;
import com.percussion.webservices.transformation.converter.PSOperationEnumConverter;
import com.percussion.webservices.transformation.converter.PSOperatorTypesConverter;
import com.percussion.webservices.transformation.converter.PSOutputFormatConverter;
import com.percussion.webservices.transformation.converter.PSPublishWhenConverter;
import com.percussion.webservices.transformation.converter.PSRelatedItemActionConverter;
import com.percussion.webservices.transformation.converter.PSRelatedItemConverter;
import com.percussion.webservices.transformation.converter.PSRelationshipConfigConverter;
import com.percussion.webservices.transformation.converter.PSRelationshipConverter;
import com.percussion.webservices.transformation.converter.PSRoleConverter;
import com.percussion.webservices.transformation.converter.PSSearchConverter;
import com.percussion.webservices.transformation.converter.PSSearchFieldConverter;
import com.percussion.webservices.transformation.converter.PSSearchParamsConverter;
import com.percussion.webservices.transformation.converter.PSSearchSummaryConverter;
import com.percussion.webservices.transformation.converter.PSSearchViewConverter;
import com.percussion.webservices.transformation.converter.PSSlotTypeConverter;
import com.percussion.webservices.transformation.converter.PSStateConverter;
import com.percussion.webservices.transformation.converter.PSTemplateSlotConverter;
import com.percussion.webservices.transformation.converter.PSTemplateTypeConverter;
import com.percussion.webservices.transformation.converter.PSTransitionConverter;
import com.percussion.webservices.transformation.converter.PSWorkflowConverter;
import com.percussion.webservices.transformation.converter.PSWorkflowRoleConverter;
import com.percussion.webservices.ui.data.NodeType;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This factory is used to transform server objects to axis generated
 * web services objects and vice versa.
 */
public class PSTransformerFactory
{
   protected static final Logger log = LogManager.getLogger(IPSConstants.WEBSERVICES_LOG);

   /**
    * Get the transformation factory.
    *
    * @return the transformation factory, never <code>null</code>.
    */
   public static PSTransformerFactory getInstance()
   {
      if (ms_instance == null)
         ms_instance = new PSTransformerFactory();

      return ms_instance;
   }

   /**
    * Get the bean utils which has all custom converters registered.
    *
    * @return the bean utils, never <code>null</code>.
    */
   public BeanUtilsBean getBeanUtils()
   {
      return ms_beanUtils;
   }

   /**
    * Get the converter for the supplied class. A default converter will be
    * returned if no specific converter is registered for the supplied class.
    *
    * @param type the class for which to get the converter, not
    *    <code>null</code>.
    * @return the converter found for the supplied class, never
    *    <code>null</code>.
    */
   public Converter getConverter(Class type)
   {
      if (type == null)
         throw new IllegalArgumentException("type cannot be null");

      Converter converter = ms_beanUtils.getConvertUtils().lookup(type);
      if (converter == null)
         return new PSConverter(ms_beanUtils);

      return converter;
   }

   /**
    * Gets a mapper that maps server side types (as the map keys) to client
    * side types (as the map values).
    *
    * @return the mapper described above, never <code>null</code>.
    */
   public Map<Class, Class> getTypeMapper()
   {
      return m_typeMappings;
   }

   /**
    * Constructs the singleton and registers all converters required for the
    * webservices.
    */
   private PSTransformerFactory()
   {
      ms_beanUtils = new BeanUtilsBean();

      // register all custom converters
      ConvertUtilsBean convertUtils = ms_beanUtils.getConvertUtils();

      // register all fault converters
      convertUtils.register(new PSConverter(ms_beanUtils),
         PSErrorException.class);
      convertUtils.register(new PSConverter(ms_beanUtils),
         PSError.class);
      convertUtils.register(new PSConverter(ms_beanUtils),
         PSLockErrorException.class);
      convertUtils.register(new PSConverter(ms_beanUtils),
         PSLockFault.class);

      registerListToArrayConverter(PSDependency.class,
         com.percussion.webservices.system.PSDependency.class);
      registerListToArrayConverter(PSDependent.class,
         com.percussion.webservices.system.PSDependent.class);

      // register array to collection converters
      convertUtils.register(new PSArrayToListConverter(ms_beanUtils),
         Collection.class);
      convertUtils.register(new PSArrayToListConverter(ms_beanUtils),
         List.class);
      convertUtils.register(new PSArrayToSetConverter(ms_beanUtils),
         Set.class);

      // PSObjectSummary converter
      register(PSObjectSummaryConverter.class, PSObjectSummary.class,
            com.percussion.webservices.common.PSObjectSummary.class);

      // PSObjectLockSummary converter
      convertUtils.register(new PSConverter(ms_beanUtils),
         PSObjectLockSummary.class);
      convertUtils.register(new PSConverter(ms_beanUtils),
         com.percussion.webservices.common.PSObjectSummaryLocked.class);

      // PSLogin converter
      convertUtils.register(new PSConverter(ms_beanUtils),
         PSLogin.class);
      convertUtils.register(new PSConverter(ms_beanUtils),
         com.percussion.webservices.security.data.PSLogin.class);

      // PSCommunity converter
      register(PSCommunityConverter.class, PSCommunity.class,
            com.percussion.webservices.security.data.PSCommunity.class);

      // PSRole converter
      register(PSRoleConverter.class, PSRole.class,
            com.percussion.webservices.security.data.PSRole.class);

      // PSLocale converter
      register(PSLocaleConverter.class, PSLocale.class,
            com.percussion.webservices.content.PSLocale.class);
      register(PSLocaleConverter.class, PSLocale.class,
         com.percussion.webservices.security.data.PSLocale.class);

      // PSAssemblyTemplate converter
      register(PSAssemblyTemplateWsConverter.class, PSAssemblyTemplateWs.class,
            com.percussion.webservices.assembly.data.PSAssemblyTemplate.class);

      // PSTemplateBinding converter
      register(PSConverter.class, PSTemplateBinding.class,
            PSAssemblyTemplateBindingsBinding.class);

      // IPSAssemblyTemplate.GlobalTemplateUsage converter
      registerConverter(PSGlobalTemplateUsageConverter.class,
            IPSAssemblyTemplate.GlobalTemplateUsage.class,
            TemplateUsageType.class);

      // IPSAssemblyTemplate.OutputFormat converter
      registerConverter(PSOutputFormatConverter.class,
            IPSAssemblyTemplate.OutputFormat.class,
            OutputFormatType.class);

      // IPSAssemblyTemplate.TemplateType converter
      registerConverter(PSTemplateTypeConverter.class,
            IPSAssemblyTemplate.TemplateType.class,
            TemplateType.class);

      // IPSAssemblyTemplate.PublishWhen converter
      registerConverter(PSPublishWhenConverter.class,
            IPSAssemblyTemplate.PublishWhen.class,
            PublishType.class);

      // PSTemplateSlot converter
      register(PSTemplateSlotConverter.class, PSTemplateSlot.class,
            com.percussion.webservices.assembly.data.PSTemplateSlot.class);

      // IPSTemplateSlot.SlotType converter
      registerConverter(PSSlotTypeConverter.class,
            IPSTemplateSlot.SlotType.class,
            PSTemplateSlotType.class);

      // PSErrorResultsException converter
      registerConverter(PSErrorResultsExceptionConverter.class,
            PSErrorResultsException.class,
            PSErrorResultsFault.class);

      // PSFieldDescription.PSFieldTypeEnum converter
      registerConverter(PSFieldTypeConverter.class,
            PSFieldDescription.PSFieldTypeEnum.class,
            PSFieldDescriptionDataType.class);

      // PSFieldDescriptionConverter converter
      register(PSFieldDescriptionConverter.class, PSFieldDescription.class,
            com.percussion.webservices.content.PSFieldDescription.class);

      // PSContentTypeSummaryChildConverter converter
      register(PSContentTypeSummaryChildConverter.class, PSContentTypeSummaryChild.class,
            com.percussion.webservices.content.PSContentTypeSummaryChild.class);

      // PSContentTypeSummaryConverter converter
      register(PSContentTypeSummaryConverter.class, PSContentTypeSummary.class,
            com.percussion.webservices.content.PSContentTypeSummary.class);

      // PSErrorsException converter
      registerConverter(PSErrorsExceptionConverter.class,
            PSErrorsException.class,
            PSErrorsFault.class);

      // ItemDef converter
      register(PSContentTypeConverter.class, PSItemDefinition.class,
            PSContentType.class);

      // PSAction converter
      register(PSActionConverter.class, PSAction.class,
            com.percussion.webservices.ui.data.PSAction.class);

      // PSDisplayFormat converter
      register(PSDisplayFormatConverter.class, PSDisplayFormat.class,
            com.percussion.webservices.ui.data.PSDisplayFormat.class);

      // PSSearch converter
      register(PSSearchViewConverter.class, PSSearch.class,
            com.percussion.webservices.ui.data.PSSearchDef.class);
      register(PSSearchViewConverter.class, PSSearch.class,
            com.percussion.webservices.ui.data.PSViewDef.class);

      // PSRelationshipConifg converter
      register(PSRelationshipConfigConverter.class, PSRelationshipConfig.class,
            com.percussion.webservices.system.PSRelationshipConfig.class);
      register(PSRelationshipConfigConverter.class, PSRelationshipConfig.class,
            com.percussion.webservices.system.RelationshipConfigSummary.class);

      register(PSItemStatusConverter.class, PSItemStatus.class,
            com.percussion.webservices.content.PSItemStatus.class);

      // PSHierarchyNode.NodeType converter
      registerConverter(PSNodeTypeConverter.class,
            PSHierarchyNode.NodeType.class,
            NodeType.class);

      // PSHierarchyNode converter
      register(PSHierarchyNodeConverter.class, PSHierarchyNode.class,
            com.percussion.webservices.ui.data.PSHierarchyNode.class);

      // PSMimeContentAdapter converter
      register(PSMimeContentAdapterConverter.class, PSMimeContentAdapter.class,
         com.percussion.webservices.system.PSMimeContentAdapter.class);

      // PSContentTemplateDescConverter
      register(PSContentTemplateDescConverter.class,
         PSContentTemplateDesc.class,
         com.percussion.webservices.content.PSContentTemplateDesc.class);

      // PSKeyword converter
      register(PSKeywordConverter.class, PSKeyword.class,
            com.percussion.webservices.content.PSKeyword.class);

      // PSKeywordChoice converter
      register(PSKeywordChoiceConverter.class, PSKeywordChoice.class,
            com.percussion.webservices.content.PSKeywordChoice.class);

      // workflow classes
      register(PSWorkflowConverter.class, PSWorkflow.class,
         com.percussion.webservices.system.PSWorkflow.class);

      register(PSNotificationDefConverter.class, PSNotificationDef.class,
         com.percussion.webservices.system.PSNotificationDef.class);

      register(PSWorkflowRoleConverter.class, PSWorkflowRole.class,
         com.percussion.webservices.system.PSWorkflowRole.class);

      register(PSNotificationConverter.class, PSNotification.class,
         com.percussion.webservices.system.PSNotification.class);

      register(PSTransitionConverter.class, PSTransition.class,
         com.percussion.webservices.system.PSTransition.class);

      register(PSAgingTransitionConverter.class, PSAgingTransition.class,
         com.percussion.webservices.system.PSAgingTransition.class);

      register(PSAssignedRoleConverter.class, PSAssignedRole.class,
         com.percussion.webservices.system.PSAssignedRole.class);

      register(PSStateConverter.class, PSState.class,
         com.percussion.webservices.system.PSState.class);

      // PSAutoTranslations converter
      register(PSAutoTranslationConverter.class, PSAutoTranslation.class,
         com.percussion.webservices.content.PSAutoTranslation.class);

      // PSContentEditorSystemDef converter
      register(PSContentEditorDefinitionConverter.class,
         PSContentEditorSystemDef.class,
         PSContentEditorDefinition.class);

      // PSContentEditorSharedDef converter
      register(PSContentEditorDefinitionConverter.class,
         PSContentEditorSharedDef.class,
         PSContentEditorDefinition.class);

      // PSSharedProperty
      register(PSConverter.class, PSSharedProperty.class,
         com.percussion.webservices.system.PSSharedProperty.class);

      register(PSAclEntryImplConverter.class, PSAclEntryImpl.class,
         com.percussion.webservices.system.PSAclEntryImpl.class);

      register(PSAclImplConverter.class, PSAclImpl.class,
         com.percussion.webservices.system.PSAclImpl.class);

      // PSCommunityVisibility
      register(PSCommunityVisibilityConverter.class, PSCommunityVisibility.class,
         com.percussion.webservices.security.data.PSCommunityVisibility.class);

      // PSItemFilter
      register(PSItemFilterConverter.class, PSItemFilter.class,
         com.percussion.webservices.system.PSItemFilter.class);

      register(PSAaRelationshipConverter.class,
            PSAaRelationship.class,
            com.percussion.webservices.content.PSAaRelationship.class);

      register(PSRelationshipConverter.class,
            PSRelationship.class,
            com.percussion.webservices.system.PSRelationship.class);

      register(PSFolderConverter.class,
            PSFolder.class,
            com.percussion.webservices.content.PSFolder.class);

      // PSItem
      register(PSItemConverter.class, PSCoreItem.class,
         com.percussion.webservices.content.PSItem.class);

      // PSFieldConverter
      register(PSFieldConverter.class, PSItemField.class,
         com.percussion.webservices.content.PSField.class);

      // PSDimensionEnumConverter
      register(PSDimensionEnumConverter.class, PSField.PSDimensionEnum.class,
         com.percussion.webservices.content.PSFieldDimension.class);
      
      // PSItemDataTypeConverter
      register(PSItemDataTypeConverter.class, Integer.class,
         com.percussion.webservices.content.PSFieldDataType.class);
      
      // PSItemFieldValueTypeConverter
      register(PSItemFieldValueTypeConverter.class, Integer.class,
         com.percussion.webservices.content.PSFieldFieldValueType.class);
      
      // PSFieldSourceTypeConverter
      register(PSFieldSourceTypeConverter.class, Integer.class,
         com.percussion.webservices.content.PSFieldSourceType.class);
      
      // PSFieldTransferEncodingConverter
      register(PSFieldTransferEncodingConverter.class, Integer.class,
         com.percussion.webservices.content.PSFieldTransferEncoding.class);
      
      // PSItemChildEntryConverter
      register(PSItemChildEntryConverter.class, PSItemChildEntry.class, 
         PSChildEntry.class);
      
      // PSRelatedItemConverter
      register(PSRelatedItemConverter.class, PSItemRelatedItem.class, 
         PSRelatedItem.class);
      
      // PSRelatedItemActionConverter
      register(PSRelatedItemActionConverter.class, 
         PSItemRelatedItem.PSRelatedItemAction.class,
         com.percussion.webservices.content.PSRelatedItemAction.class);

      // PSConnectorTypesConverter
      register(PSConnectorTypesConverter.class, 
         PSWSSearchField.PSConnectorEnum.class,
         com.percussion.webservices.common.ConnectorTypes.class);

      // PSOperatorTypesConverter
      register(PSOperatorTypesConverter.class, 
         PSWSSearchField.PSOperatorEnum.class,
         com.percussion.webservices.common.OperatorTypes.class);

      // PSSearchFieldConverter
      register(PSSearchFieldConverter.class, 
         PSWSSearchField.class,
         com.percussion.webservices.content.PSSearchField.class);

      // PSSearchParamsConverter
      register(PSSearchParamsConverter.class, 
         PSWSSearchParams.class,
         com.percussion.webservices.content.PSSearchParams.class);

      // PSSearchConverter
      register(PSSearchConverter.class, 
         PSWSSearchRequest.class,
         com.percussion.webservices.content.PSSearch.class);

      // PSOperationEnumConverter
      register(PSOperationEnumConverter.class, 
         PSItemSummary.OperationEnum.class,
         com.percussion.webservices.content.PSItemSummaryOperation.class);

      // PSObjectTypeEnumConverter
      register(PSObjectTypeEnumConverter.class, 
         PSItemSummary.ObjectTypeEnum.class,
         com.percussion.webservices.common.ObjectType.class);

      // PSItemSummaryConverter
      register(PSItemSummaryConverter.class, 
         PSItemSummary.class,
         com.percussion.webservices.content.PSItemSummary.class);

      // PSSearchSummaryConverter
      register(PSSearchSummaryConverter.class, 
         PSSearchSummary.class,
         com.percussion.webservices.content.PSSearchResults.class);
   }

   /**
    * Register a pair of supplied classes for the supplied converter and
    * the {@link PSListToArrayConverter} class.
    *
    * @param converterClass the converter class which is able to handle the
    *   conversion of the supplied classes, assumed not <code>null</code>.
    * @param serverClass one of the to be converted classes, assumed not
    *   <code>null</code>.
    * @param clientClass one of the to be converted classes, assumed not
    *   <code>null</code>.
    */
   public void register(Class converterClass, Class serverClass,
         Class clientClass)
   {
      registerConverter(converterClass, serverClass, clientClass);
      registerListToArrayConverter(serverClass, clientClass);
   }

   /**
    * Register a converter that is able to convert between the instances of the
    * supplied classes.
    *
    * @param converterClass the class of the registered converter, assumed not
    *   <code>null</code>.
    * @param serverClass one of the to be converted classes, assumed not
    *   <code>null</code>.
    * @param clientClass one of the to be converted classes, assumed not
    *   <code>null</code>.
    */
   private void registerConverter(Class converterClass, Class serverClass,
         Class clientClass)
   {
      try
      {
         // register converters for single objects
         Constructor constructor = converterClass.getConstructor(
            new Class[] { BeanUtilsBean.class });
         ms_beanUtils.getConvertUtils().register(
            (Converter) constructor.newInstance(ms_beanUtils), clientClass);
         ms_beanUtils.getConvertUtils().register(
            (Converter) constructor.newInstance(ms_beanUtils), serverClass);

         // register converters for array of objects
         ms_beanUtils.getConvertUtils().register(
            new PSArrayConverter(ms_beanUtils),
            Array.newInstance(serverClass, 0).getClass());
         ms_beanUtils.getConvertUtils().register(
            new PSArrayConverter(ms_beanUtils),
            Array.newInstance(clientClass, 0).getClass());
      }
      catch (Exception e)
      {
         log.error(PSExceptionUtils.getMessageForLog(e));
         throw new RuntimeException(e);
      }
   }

   /**
    * Register a pair of classes for the {@link PSListToArrayConverter}
    * converter Assumed the conversion between the pair classes can be handled
    * by {@link PSConverter} or an registered converter.
    * 
    * @param serverClass the server class, asssumed not <code>null</code>.
    * Assumed the instances of this class are contained in the list when
    * converting from list to array.
    * @param clientClass the client class, assumed not <code>null</code>.
    * Assumed the instances of this class are contained in the array when
    * converting from list to array.
    */
   private void registerListToArrayConverter(Class serverClass,
         Class clientClass)
   {
      ms_beanUtils.getConvertUtils().register(
            new PSListToArrayConverter(ms_beanUtils),
            Array.newInstance(clientClass, 0).getClass());

      // has to map clientClass to serverClass because there are more than one
      // clientClasses map to the same serverClass,
      // such as both PSSearchDef & PSViewDef map to PSSearch
      m_typeMappings.put(clientClass, serverClass);
   }

   /**
    * This bean utils instance will hold all registed custom converters
    * required for the various translations. Initialized during construction,
    * never <code>null</code> or changed after that.
    */
   private static BeanUtilsBean ms_beanUtils = null;

   /**
    * The one and only transformation factory, initialized with the first call
    * to {@link #getInstance()}, never <code>null</code> or changed after that.
    */
   private static PSTransformerFactory ms_instance = null;

   /**
    * This map specifies the array element type mappings. The map key specifies
    * the server side types while the map value specified the client side
    * types.
    */
   private Map<Class, Class> m_typeMappings = new HashMap<Class, Class>();

}

