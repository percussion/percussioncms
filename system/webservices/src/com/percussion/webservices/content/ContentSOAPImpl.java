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
package com.percussion.webservices.content;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.server.PSBinaryFileValue;
import com.percussion.cms.objectstore.server.PSPurgableFileValue;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.search.objectstore.PSWSSearchRequest;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSSearchSummary;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.util.IOTools;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSBaseSOAPImpl;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;
import com.percussion.webservices.PSInvalidStateException;
import com.percussion.webservices.PSUnknownChildException;
import com.percussion.webservices.PSUnknownContentTypeException;
import com.percussion.webservices.PSWebserviceErrors;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorsFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.faults.PSUnknownChildFault;
import com.percussion.webservices.faults.PSUnknownContentTypeFault;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Server side implementations for web services defined in
 * <code>rhythmyx.wsdl</code> for operations defined in the
 * <code>contentSOAP</code> bindings.
 */
public class ContentSOAPImpl extends PSBaseSOAPImpl implements Content
{
   /* (non-Javadoc)
    * @see Content#loadTranslationSettings()
    */
   public PSAutoTranslation[] loadTranslationSettings() 
      throws RemoteException, PSInvalidSessionFault
   {
      try
      {
         authenticate();

         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<com.percussion.services.content.data.PSAutoTranslation> ats = 
            service.loadTranslationSettings();
         
         PSAutoTranslation[] result = (PSAutoTranslation[]) convert(
            PSAutoTranslation[].class, ats);
         
         return result;
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, 
               "loadTranslationSettings", e.getLocalizedMessage()), 
               ExceptionUtils.getFullStackTrace(e));
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#loadContentTypes(LoadContentTypesRequest)
    */
   public PSContentTypeSummary[] loadContentTypes(
      LoadContentTypesRequest loadContentTypesRequest) throws RemoteException,
      PSInvalidSessionFault, PSNotAuthorizedFault
   {
      try
      {
         authenticate();

         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<com.percussion.services.content.data.PSContentTypeSummary> sums = 
            service.loadContentTypes(loadContentTypesRequest.getName());
         
         PSContentTypeSummary[] result = (PSContentTypeSummary[]) convert(
            PSContentTypeSummary[].class, sums);
         return result;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadContentTypes");
      }

      return null; // never happen here, used to turn off compiling error
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#loadKeywords(LoadKeywordsRequest)
    */
   @SuppressWarnings("unused")
   public PSKeyword[] loadKeywords(LoadKeywordsRequest loadKeywordsRequest)
      throws RemoteException, PSInvalidSessionFault, PSNotAuthorizedFault
   {
      String serviceName = "loadKeywords";
      try
      {
         authenticate();

         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List keywords = service.loadKeywords(
            loadKeywordsRequest.getName());

         return (PSKeyword[]) convert(PSKeyword[].class, keywords);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }

      return null; // never happen here, used to turn off compiling error
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#loadLocales(LoadLocalesRequest)
    */
   public PSLocale[] loadLocales(LoadLocalesRequest loadLocalesRequest)
      throws RemoteException, PSInvalidSessionFault
   {
      try
      {
         authenticate();

         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<com.percussion.i18n.PSLocale> locales = 
            service.loadLocales(loadLocalesRequest.getCode(), 
               loadLocalesRequest.getName());
         
         PSLocale[] result = (PSLocale[]) convert(
            PSLocale[].class, locales);
         
         return result;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadLocales");
         return null;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#addContentRelations(AddContentRelationsRequest)
    */
   public PSAaRelationship[] addContentRelations(
      AddContentRelationsRequest req)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSNotAuthorizedFault, PSErrorResultsFault
   {
      String serviceName = "addContentRelations";
      try
      {
         authenticate();
         
         // get data from request
         PSLegacyGuid ownerId = new PSLegacyGuid(req.getId());
         List<IPSGuid> relatedIds = new ArrayList<IPSGuid>(req.getRelatedId().length);
         for (long relatedId : req.getRelatedId())
            relatedIds.add(new PSLegacyGuid(relatedId));
         int index = (req.getIndex() == null) ? -1 : req.getIndex().intValue();  

         // create and save relationships
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         List<com.percussion.cms.objectstore.PSAaRelationship> relationships = 
            service.addContentRelations(ownerId, relatedIds, req.getSlot(), 
               req.getTemplate(), req.getRelationshipConfig(), index);
         
         // convert the saved relationships
         PSAaRelationship[] result = (PSAaRelationship[]) convert(
               PSAaRelationship[].class, relationships);
         
         return result;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#addFolder(AddFolderRequest)
    */
   public AddFolderResponse addFolder(AddFolderRequest request)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault
   {
      authenticate();
      
      try
      {
         if (StringUtils.isBlank(request.getName()))
            throw new IllegalArgumentException(
                  "The folder name must not be null or empty.");
         if (StringUtils.isBlank(request.getPath()))
            throw new IllegalArgumentException(
               "The parent folder path must not be null or empty.");
               
         IPSContentWs service = PSContentWsLocator.getContentWebservice();

         com.percussion.cms.objectstore.PSFolder folder = service.addFolder(
               request.getName(), request.getPath());
         
         PSFolder result = (PSFolder) convert(PSFolder.class, folder);
         
         AddFolderResponse response = new AddFolderResponse();
         response.setPSFolder(result);
         
         return response;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "addFolder");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }

      return null;
   }

   /**
    * Validates the given folder reference.
    * 
    * @param ref the folder reference in question, must not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if both id and path are specified 
    *    (or both are not <code>null</code>).
    */
   private void validateFolderRef(FolderRef ref)
   {
      if (ref == null)
         throw new IllegalArgumentException(
         "Folder reference, ref, must not be null.");
      
      if (ref.getId() != null && (!StringUtils.isBlank(ref.getPath())))
         throw new IllegalArgumentException(
               "Cannot specified both folder id and path. Either id or paths must be null or empty.");
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see Content#addFolderChildren(AddFolderChildrenRequest)
    */
   public void addFolderChildren(
      AddFolderChildrenRequest request)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault
   {
      authenticate();

      try
      {
         validateFolderRef(request.getParent());
         if (request.getChildIds() == null || request.getChildIds().length == 0)
            throw new IllegalArgumentException("ChildIds must not be null or empty");
         
         // convert long to IPSGuid
         List<IPSGuid> childIds = PSWebserviceUtils
               .getLegacyGuidFromLong(request.getChildIds());
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         if (request.getParent().getId() != null)
         {
            IPSGuid parentId = new PSLegacyGuid(request.getParent().getId());
            service.addFolderChildren(parentId, childIds); 
         }
         else
         {
            service.addFolderChildren(request.getParent().getPath(), childIds);
         }
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "addFolderChildren");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#addFolderTree(AddFolderTreeRequest)
    */
   public PSFolder[] addFolderTree(AddFolderTreeRequest request)
      throws RemoteException, PSInvalidSessionFault, PSErrorResultsFault,
      PSContractViolationFault
   {
      String serviceName = "addFolderTree";
      authenticate();
      
      try
      {
         if (StringUtils.isBlank(request.getPath()))
            throw new IllegalArgumentException(
               "The parent folder path must not be null or empty.");
               
         IPSContentWs service = PSContentWsLocator.getContentWebservice();

         List<com.percussion.cms.objectstore.PSFolder> folders = 
            service.addFolderTree(request.getPath());
         
         PSFolder[] result;
         if (folders.isEmpty())
            result = new PSFolder[0];
         else
            result = (PSFolder[]) convert(PSFolder[].class, folders);
         
         return result;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }      
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }

      return null;
   }

   /* (non-Javadoc)
    * @see Content#checkinItems(CheckinItemsRequest)
    */
   public void checkinItems(CheckinItemsRequest checkinItemsRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault
   {
      String serviceName = "checkinItems";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         service.checkinItems(PSWebserviceUtils.getLegacyGuidFromLong(
            checkinItemsRequest.getId()), checkinItemsRequest.getComment());
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
   }

   /* (non-Javadoc)
    * @see Content#checkoutItems(CheckoutItemsRequest)
    */
   public void checkoutItems(CheckoutItemsRequest checkoutItemsRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault
   {
      String serviceName = "checkoutItems";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         service.checkoutItems(PSWebserviceUtils.getLegacyGuidFromLong(
            checkoutItemsRequest.getId()), checkoutItemsRequest.getComment());
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
   }

   /* (non-Javadoc)
    * @see Content#createChildEntries(CreateChildEntriesRequest)
    */
   public PSChildEntry[] createChildEntries(
      CreateChildEntriesRequest createChildEntriesRequest)
      throws RemoteException, PSInvalidSessionFault, PSUnknownChildFault,
      PSContractViolationFault
   {
      String serviceName = "createChildEntries";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         String childName = createChildEntriesRequest.getName();
         int count = createChildEntriesRequest.getCount() == null ? 1 : 
            createChildEntriesRequest.getCount();
         PSLegacyGuid id = new PSLegacyGuid(createChildEntriesRequest.getId());
         
         return (PSChildEntry[]) convert(PSChildEntry[].class, 
            service.createChildEntries(id, childName, count));
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSUnknownChildException e)
      {
         throw new PSUnknownChildFault(e.getCode(), e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSInvalidStateException e)
      {
         throw new PSContractViolationFault(e.getCode(), 
            e.getLocalizedMessage(), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());         
      }
      
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#createItems(CreateItemsRequest)
    */
   public PSItem[] createItems(CreateItemsRequest createItemsRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault,
      PSUnknownContentTypeFault, PSNotAuthorizedFault
   {
      String serviceName = "createItems";
      try
      {
         authenticate();
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         String contentType = createItemsRequest.getContentType();
         Integer count = createItemsRequest.getCount();
         if (count == null)
            count = Integer.valueOf(1);
         
         List<PSCoreItem> items = service.createItems(contentType, 
            count.intValue());
         
         return convertItems(items, null, null, null);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSUnknownContentTypeException e)
      {
         int code = IPSWebserviceErrors.UNKNOWN_CONTENT_TYPE;
         throw new PSUnknownContentTypeFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               createItemsRequest.getContentType(),
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
   }
   
   /**
    * Convert the supplied items. Can't use the list to array converter 
    * because the returned list may contain server items and we can't 
    * register the core and server item at the same time. This also converts
    * the items id into a client side legacy GUID with the revision set to -1.
    * 
    * @param items the list of items to convert, assumed not <code>null</code>,
    *    may be empty.
    * @param fieldNames a list of names for all parent fields to be returned 
    *    with the converted items, may be <code>null</code> to return all 
    *    fields or empty to return no fields.
    * @param childNames a list of names for all child fieldsets to be returned 
    *    with the converted items, may be <code>null</code> to return all 
    *    children or empty to return no children.
    * @param slotNames a list of names for all slots for which to return the 
    *    related content with the converted items, may be <code>null</code> to 
    *    return all related content or empty to return no related content.
    *    
    * @return an array with all items converted, never <code>null</code>, may
    *    be empty. If any of the items contains related items, then the related
    *    items will not contain their related items (if there is any), they 
    *    will also not contain binary data (if there is any).
    */
   private PSItem[] convertItems(List<PSCoreItem> items, 
      List<String> fieldNames, List<String> childNames, List<String> slotNames) 
   {
      PSItem[] convertedItems = new PSItem[items.size()];
      
      int index = 0;
      for (PSCoreItem item : items)
      {
         PSItem convertedItem = (PSItem) convert(PSItem.class, item);
         filterFields(convertedItem, fieldNames);
         filterChildren(convertedItem, childNames);
         filterSlots(convertedItem, slotNames);
         
         convertedItems[index++] = convertedItem;
      }
      
      return convertedItems;
   }
   
   /**
    * Filter the supplied item for all requested fields.
    * 
    * @param item the item to filter, assumed not <code>null</code>.
    * @param fieldNames a list of field names for which to filter the item,
    *    may be <code>null</code> to skip the filter or empty to filter all
    *    fields.
    */
   private void filterFields(PSItem item, List<String> fieldNames)
   {
      if (fieldNames == null)
         return;
      
      if (fieldNames.isEmpty())
      {
         item.setFields(null);
         return;
      }

      int count = 0;
      PSField[] filteredFields = new PSField[fieldNames.size()];
      for (PSField field : item.getFields())
      {
         if (fieldNames.contains(field.getName()))
            filteredFields[count++] = field;
      }
      
      item.setFields(filteredFields);
   }
   
   /**
    * Filter the supplied item for all requested children.
    * 
    * @param item the item to filter, assumed not <code>null</code>.
    * @param childNames a list of child names for which to filter the item,
    *    may be <code>null</code> to skip the filter or empty to filter all 
    *    children.
    */
   private void filterChildren(PSItem item, List<String> childNames)
   {
      if (childNames == null)
         return;
      
      if (childNames.isEmpty())
      {
         item.setChildren(null);
         return;
      }

      int count = 0;
      PSItemChildren[] filteredChildren = new PSItemChildren[childNames.size()];
      for (PSItemChildren child : item.getChildren())
      {
         if (childNames.contains(child.getName()))
            filteredChildren[count++] = child;
      }
      
      item.setChildren(filteredChildren);
   }
   
   /**
    * Filter the supplied item for all requested slots.
    * 
    * @param item the item to filter, assumed not <code>null</code>.
    * @param slotNames a list of slot names for which to filter the item,
    *    may be <code>null</code> or empty to skip the filter.
    */
   private void filterSlots(PSItem item, List<String> slotNames)
   {
      if (slotNames == null)
         return;

      if (slotNames.isEmpty())
      {
         item.setSlots(null);
         return;
      }
         
      int count = 0;
      PSItemSlots[] filteredSlots = new PSItemSlots[slotNames.size()];
      for (PSItemSlots slot : item.getSlots())
      {
         if (slotNames.contains(slot.getName()))
            filteredSlots[count++] = slot;
      }
      
      item.setSlots(filteredSlots);
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#deleteChildEntries(DeleteChildEntriesRequest)
    */
   public void deleteChildEntries(
      DeleteChildEntriesRequest deleteChildEntriesRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSUnknownChildFault, PSContractViolationFault
   {
      String serviceName = "deleteChildEntries";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         IPSGuid id = new PSLegacyGuid(deleteChildEntriesRequest.getId());
         String name = deleteChildEntriesRequest.getName();
         List<IPSGuid> childIds = new ArrayList<IPSGuid>();
         for (long childid : deleteChildEntriesRequest.getChildId())
         {
            childIds.add(new PSLegacyGuid(childid));
         }
         
         service.deleteChildEntries(id, name, childIds);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSUnknownChildException e)
      {
         throw new PSUnknownChildFault(e.getCode(), e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSInvalidStateException e)
      {
         throw new PSContractViolationFault(e.getCode(), 
            e.getLocalizedMessage(), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#deleteContentRelations(long[])
    */
   public void deleteContentRelations(long[] guidIds)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      String serviceName = "deleteContentRelations";
      try
      {
         authenticate();

         // get data from request
         List<IPSGuid> ids = getGuidFromLong(guidIds, PSTypeEnum.RELATIONSHIP);

         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         service.deleteContentRelations(ids);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#deleteFolders(DeleteFoldersRequest)
    */
   public void deleteFolders(DeleteFoldersRequest request)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault
   {
      String serviceName = "deleteFolders";
      try
      {
         authenticate();

         // get data from request
         List<IPSGuid> ids = PSWebserviceUtils.getLegacyGuidFromLong(request.getId());
         boolean isPurgeItem = request.getPurgItems() == null ? false : request
               .getPurgItems().booleanValue();

         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         service.deleteFolders(ids, isPurgeItem);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
   }
   
   /**
    * Converts the specified Guid values to a list of Guids.
    *  
    * @param longIds a list of Guid values, it may not be <code>null</code> or 
    *    empty. 
    * @param type the type of the to be created Guid, assumed not 
    *    <code>null</code>.
    * 
    * @return the converted Guids, never <code>null</code> or empty.
    */
   List<IPSGuid> getGuidFromLong(long[] longIds, PSTypeEnum type)
   {
      if (longIds == null || longIds.length == 0)
         throw new IllegalArgumentException("longIds must not be null or empty.");
      
      List<IPSGuid> ids = new ArrayList<IPSGuid>(longIds.length);
      for (long guidId : longIds)
         ids.add(new PSGuid(type, guidId));
      
      return ids;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see Content#deleteItems(long[])
    */
   public void deleteItems(long[] deleteItemsRequest) throws RemoteException,
      PSInvalidSessionFault, PSErrorsFault, PSContractViolationFault
   {
      String serviceName = "deleteItems";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toLegacyGuidList(deleteItemsRequest);
         
         service.deleteItems(ids);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#findChildItems(FindChildItemsRequest)
    */
   public PSItemSummary[] findChildItems(
      FindChildItemsRequest request) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();
      
      try
      {
         PSRelationshipFilter filter;
         PSLegacyGuid ownerId;
         if (request.getPSAaRelationshipFilter() != null)
         {
            request.getPSAaRelationshipFilter().setOwner(request.getId());
            filter = getRelationshipFilter(request.getPSAaRelationshipFilter());
            // the revision of the owner may have been modified if it was -1
            // get the owner id from the filter
            ownerId = new PSLegacyGuid(filter.getOwner());
         }
         else
         {
            filter = getRelationshipFilter(null);
            ownerId = new PSLegacyGuid(request.getId());
         }
         
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         List<com.percussion.services.content.data.PSItemSummary> children = 
            service.findDependents(ownerId, filter, request.isLoadOperations());
         
         return getItemSummaries(children);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadContentRelations");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#findFolderChildren(FindFolderChildrenRequest)
    */
   public PSItemSummary[] findFolderChildren( FindFolderChildrenRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();

      try
      {
         validateFolderRef(request.getFolder());
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         List<com.percussion.services.content.data.PSItemSummary> children;
         if (request.getFolder().getId() != null )
         {
            IPSGuid parentId = new PSLegacyGuid(request.getFolder().getId());
            children = service.findFolderChildren(parentId, request
                  .isLoadOperations()); 
         }
         else
         {
            children = service.findFolderChildren(
                  request.getFolder().getPath(), request.isLoadOperations());
         }
         return getItemSummaries(children);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "findFolderChildren");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }

      return null;
   }
   
   /**
    * Converts the specified item summaries from server to client format.
    * 
    * @param srcList the to be converted objects, assumed not <code>null</code>.
    * 
    * @return the converted object, never <code>null</code>, may be empty.
    */
   private PSItemSummary[] getItemSummaries(
      List<com.percussion.services.content.data.PSItemSummary> srcList) 
   {
      return (PSItemSummary[]) convert(PSItemSummary[].class, srcList);
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#findFolderPath(FindFolderPathRequest)
    */
   public FindFolderPathResponse findFolderPath(FindFolderPathRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();

      try
      {
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         IPSGuid id = new PSLegacyGuid(request.getId());
         String[] paths = service.findFolderPaths(id);
         FindFolderPathResponse response = new FindFolderPathResponse(paths);
         return response;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "findFolderPath");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }      
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#findItems(FindItemsRequest)
    */
   public PSSearchResults[] findItems(FindItemsRequest findItemsRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      String serviceName = "findItems";
      try
      {
         authenticate();

         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         PSWSSearchRequest search = (PSWSSearchRequest) convert(
            PSWSSearchRequest.class, findItemsRequest.getPSSearch());
         boolean loadOperations = extractBooleanValue(
            findItemsRequest.isLoadOperations(), false);
         
         List<PSSearchSummary> results = service.findItems(search, 
            loadOperations);
         
         return (PSSearchResults[]) convert(PSSearchResults[].class, results);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
         return null;
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#findParentItems(FindParentItemsRequest)
    */
   public PSItemSummary[] findParentItems(
      FindParentItemsRequest request) throws RemoteException,
      PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();
      
      try
      {
         PSRelationshipFilter filter = getRelationshipFilter(
               request.getPSAaRelationshipFilter());
         PSLegacyGuid dependentId = new PSLegacyGuid(request.getId());
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<com.percussion.services.content.data.PSItemSummary> parents = 
            service.findOwners(dependentId, filter, request.isLoadOperations());
   
         return getItemSummaries(parents);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "findParentItems");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#findPathIds(FindPathIdsRequest)
    */
   public FindPathIdsResponse findPathIds(FindPathIdsRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      authenticate();

      try
      {
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         List<IPSGuid> ids = service.findPathIds(request.getPath());
         FindPathIdsResponse response = new FindPathIdsResponse(
            PSWebserviceUtils.getLongsFromGuids(ids));
         return response;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "findFolderPath");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }      
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#findRevisions(long[])
    */
   public PSRevisions[] findRevisions(long[] findRevisionsRequest)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      String serviceName = "findRevisions";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<IPSGuid> guids = PSGuidUtils.toLegacyGuidList(
            findRevisionsRequest);
         List<com.percussion.services.content.data.PSRevisions> revisionsList = 
            service.findRevisions(guids);
         
         com.percussion.webservices.content.PSRevisions[] results = 
            new com.percussion.webservices.content
            .PSRevisions[revisionsList.size()];

         for (int i = 0; i < results.length; i++)
         {
            com.percussion.services.content.data.PSRevisions revisions = 
               revisionsList.get(i);
            PSComponentSummary sum = revisions.getSummary();
            int contentId = sum.getContentId();
            int editRev = sum.getEditLocator().getRevision();
            int curRev = sum.getCurrentLocator().getRevision();
            
            List<PSContentStatusHistory> revList = revisions.getRevisions();
            PSRevision[] revArr = new PSRevision[revList.size()];
            for (int j = 0; j < revArr.length; j++)
            {
               PSContentStatusHistory hist = revList.get(j);
               int rev = hist.getRevision();
               PSLegacyGuid guid = new PSLegacyGuid(contentId, rev);
               Calendar time = Calendar.getInstance();
               time.setTime(hist.getEventTime());
               
               revArr[j] = new PSRevision(guid.longValue(), rev, 
                  (rev == curRev), (rev == editRev), time, hist.getActor(), 
                  hist.getTransitionComment());
            }
            
            results[i] = new PSRevisions(editRev, curRev, revArr);
         }
         
         return results;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#getAssemblyUrls(GetAssemblyUrlsRequest)
    */
   public GetAssemblyUrlsResponse getAssemblyUrls(GetAssemblyUrlsRequest request)
         throws RemoteException, PSInvalidSessionFault,
         PSContractViolationFault
   {
      String serviceName = "getAssemblyUrls";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<IPSGuid> guids = PSGuidUtils.toLegacyGuidList(request.getId());
         List<String> urls = service.getAssemblyUrls(guids, 
               request.getTemplate(), request.getContext(), 
               request.getItemFilter(), request.getSite(), 
               request.getFolderPath());
         
         String[] results = new String[urls.size()];
         return new GetAssemblyUrlsResponse(urls.toArray(results));
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }      

      return null;
   }

   /* (non-Javadoc)
    * @see Content#loadChildEntries(LoadChildEntriesRequest)
    */
   public PSChildEntry[] loadChildEntries(
      LoadChildEntriesRequest loadChildEntriesRequest) throws RemoteException,
      PSErrorResultsFault, PSInvalidSessionFault, PSUnknownChildFault,
      PSContractViolationFault
   {
      String serviceName = "loadChildEntries";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         String childName = loadChildEntriesRequest.getName();
         PSLegacyGuid id = new PSLegacyGuid(loadChildEntriesRequest.getId());
         boolean includeBinary = extractBooleanValue(
            loadChildEntriesRequest.getIncludeBinaries(), false);
         boolean attachBinaries = extractBooleanValue(
            loadChildEntriesRequest.getAttachBinaries(), false);
         
         List<PSItemChildEntry> childEntries = service.loadChildEntries(id, 
            childName, includeBinary);
         
         if (includeBinary && attachBinaries)
         {
            for (PSItemChildEntry childEntry : childEntries)
               addAttachments(childEntry.getAllFields());
         }
         
         return (PSChildEntry[]) convert(PSChildEntry[].class, childEntries);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSUnknownChildException e)
      {
         throw new PSUnknownChildFault(e.getCode(), e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#loadContentRelations(LoadContentRelationsRequest)
    */
   public PSAaRelationship[] loadContentRelations(
      LoadContentRelationsRequest req)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault, PSNotAuthorizedFault
   {
      authenticate();
      
      try
      {
         PSRelationshipFilter filter = getRelationshipFilter(
               req.getPSAaRelationshipFilter());
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         List<com.percussion.cms.objectstore.PSAaRelationship> relationships = 
            service.loadContentRelations(filter, req.isLoadReferenceInfo());
         
         // convert the saved relationships
         PSAaRelationship[] result = (PSAaRelationship[]) convert(
               PSAaRelationship[].class, relationships);
         
         return result;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "loadContentRelations");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }

      return null;
   }

   /**
    * Converts the specified AA Relationship Filter to
    * {@link PSRelationshipFilter}.
    * <p>
    * Note, the owner revision will be <code>-1</code> if the 
    * isLimitToOwnerRevisions of the source filter is <code>false</code>;
    * otherwise, the owner revision will be the Edit (or Tip) revision if the
    * item is checked out by the current user; otherwise the owner
    * revision is the current revision of the owner item.
    *
    * @param src the to be converted AA Filter, may be <code>null</code>.
    * @return the converted filter, never <code>null</code>.
    *
    * @throws PSErrorException if any of properties in the source filter
    *    is invalid.
    */
   private PSRelationshipFilter getRelationshipFilter(
      PSAaRelationshipFilter src) 
      throws PSErrorException
   {
      PSRelationshipFilter filter = PSWebserviceUtils.getRelationshipFilter(
            src);
      
      if (src == null)
      {
         filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
         return filter;
      }
      
      // set slot id property
      if (! StringUtils.isBlank(src.getSlot()))
      {
         IPSTemplateSlot slot = 
            (IPSTemplateSlot) PSWebserviceUtils.getSlotOrTemplateFromName(
               src.getSlot(), true);
         filter.setProperty(IPSHtmlParameters.SYS_SLOTID, String.valueOf(
               slot.getGUID().longValue()));
      }

      // set template id property
      if (! StringUtils.isBlank(src.getTemplate()))
      {
         IPSAssemblyTemplate template = 
            (IPSAssemblyTemplate) PSWebserviceUtils.getSlotOrTemplateFromName(
               src.getTemplate(), false);
         filter.setProperty(IPSHtmlParameters.SYS_VARIANTID, String.valueOf(
               template.getGUID().longValue()));
      }

      // set site id property
      if (! StringUtils.isBlank(src.getSite()))
      {
         IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();
         IPSSite site;
         try
         {
            site = sitemgr.loadSite(src.getSite());
         }
         catch (PSNotFoundException e)
         {
            throw new IllegalArgumentException(e); // cannot find site.
         }
         filter.setProperty(IPSHtmlParameters.SYS_SITEID,
               String.valueOf(site.getGUID().longValue()));
      }

      // set folder id property
      if (! StringUtils.isBlank(src.getFolderPath()))
      {
         try
         {
            PSRelationshipProcessor processor = 
               PSWebserviceUtils.getRelationshipProcessor();
            int id = processor.getIdByPath(
                  PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
                  src.getFolderPath(),
                  PSRelationshipConfig.TYPE_FOLDER_CONTENT);
            filter.setProperty(IPSHtmlParameters.SYS_FOLDERID,
                  String.valueOf(id));
         }
         catch (PSCmsException e)
         {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
         }
      }

      return filter;
   }


   /*
    * (non-Javadoc)
    * 
    * @see Content#loadFolders(LoadFoldersRequest)
    */
   public PSFolder[] loadFolders(LoadFoldersRequest request)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      String serviceName = "loadFolders";

      authenticate();

      try
      {
         if (request.getId() != null && request.getId().length > 0 && 
            request.getPath() != null && request.getPath().length > 0)
            throw new IllegalArgumentException("Cannot load folders by both " +
               "ids and paths. Either ids or paths must be null or empty.");
               
         List<com.percussion.cms.objectstore.PSFolder> ats = null;
         List<IPSGuid> ids = null;
         if (request.getId() != null && request.getId().length > 0)
            ids = PSWebserviceUtils.getLegacyGuidFromLong(request.getId());
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();

         if (ids != null)
            ats = service.loadFolders(ids);
         else
            ats = service.loadFolders(request.getPath());
         
         PSFolder[] result = (PSFolder[]) convert(PSFolder[].class, ats);
         
         return result;
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#loadItems(LoadItemsRequest)
    */
   @SuppressWarnings("unchecked")
   public PSItem[] loadItems(LoadItemsRequest loadItemsRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      String serviceName = "loadItems";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toLegacyGuidList(
            loadItemsRequest.getId());
         
         List<String> fieldNames = null;
         if (loadItemsRequest.getFieldName() != null && 
            loadItemsRequest.getFieldName().length > 0)
            fieldNames = Arrays.asList(loadItemsRequest.getFieldName());
         
         boolean includeBinary = extractBooleanValue(
            loadItemsRequest.getIncludeBinary(), false);
         boolean attachBinaries = extractBooleanValue(
            loadItemsRequest.getAttachBinaries(), false);
         
         boolean includeChildren = extractBooleanValue(
            loadItemsRequest.getIncludeChildren(), false);
         List<String> childNames = null;
         if (includeChildren && loadItemsRequest.getChildName() != null)
            childNames = Arrays.asList(loadItemsRequest.getChildName());
         else if (!includeChildren)
            childNames = new ArrayList<String>();
         
         boolean includeRelated = extractBooleanValue(
            loadItemsRequest.getIncludeRelated(), false);
         List<String> slotNames = null;
         if (includeRelated && loadItemsRequest.getSlotName() != null)
            slotNames = Arrays.asList(loadItemsRequest.getSlotName());
         else if (!includeRelated)
            slotNames = new ArrayList<String>();
         
         boolean includeFolderPath = extractBooleanValue(
            loadItemsRequest.getIncludeFolderPath(), false);

         // always load both AA relationships and the related (or dependent)
         // items together; otherwise the returned items may fail to be 
         // converted by {@link PSRelatedItemConverter}
         List<PSCoreItem> items = service.loadItems(ids, includeBinary, 
            includeChildren, includeRelated, includeFolderPath, includeRelated);

         if (includeBinary && attachBinaries)
            attachBinaryFields(items);

         return convertItems(items, fieldNames, childNames, slotNames);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      
      return new PSItem[0];
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#moveFolderChildren(MoveFolderChildrenRequest)
    */
   public void moveFolderChildren(MoveFolderChildrenRequest request)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault
   {
      String serviceName = "moveFolderChildren";
      try
      {
         authenticate();
         // validating, cannot specified both id & path for source/target
         validateFolderRef(request.getSource());
         validateFolderRef(request.getTarget());
         
         PSLegacyGuid sourceId = request.getSource().getId() == null ? null
               : new PSLegacyGuid(request.getSource().getId());
         PSLegacyGuid targetId = request.getTarget().getId() == null ? null
               : new PSLegacyGuid(request.getTarget().getId());
         String sourcePath = request.getSource().getPath();
         String targetPath = request.getTarget().getPath();
         
         if (sourceId != null && (!StringUtils.isBlank(sourcePath)))
            throw new IllegalArgumentException(
               "Cannot specified both source id and source path.");
         if (targetId != null && (!StringUtils.isBlank(targetPath)))
            throw new IllegalArgumentException(
               "Cannot specified both target id and target path.");
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         List<IPSGuid> childIds = null;
         if (request.getChildId() != null && request.getChildId().length > 0)
            childIds = PSWebserviceUtils.getLegacyGuidFromLong(request.getChildId());
         if (sourceId != null)
            service.moveFolderChildren(sourceId, targetId, childIds);
         else
            service.moveFolderChildren(sourcePath, targetPath, childIds);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }   
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#newCopies(NewCopiesRequest)
    */
   public PSItem[] newCopies(NewCopiesRequest newCopiesRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      String serviceName = "newCopies";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toLegacyGuidList(
            newCopiesRequest.getIds());
         
         List<String> paths = getPaths(newCopiesRequest.getPaths());
         
         String relationshipType = newCopiesRequest.getType();

         boolean enableRevisions = extractBooleanValue(
            newCopiesRequest.getEnableRevisions(), false);
         
         List<PSCoreItem> items = service.newCopies(ids, paths, 
            relationshipType, enableRevisions);
         
         return convertItems(items, null, null, null);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return null;
   }

   /**
    * Creates a list of {@link String} from the specified array of
    * {@link String}.
    *  
    * @param paths the specified array of string; must not be <code>null</code> 
    *    or empty.
    *      
    * @return the created list, never <code>null</code> or empty.
    * 
    * @throw IllegalArgumentException if paths is <code>null</code> or empty.
    */
   private List<String> getPaths(String[] paths)
   {
      if (paths != null && paths.length > 0)
      {
         return Arrays.asList(paths);
      }
      else 
      {
         throw new IllegalArgumentException("paths must not be null or empty.");
      }
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see Content#newPromotableVersions(NewPromotableVersionsRequest)
    */
   public PSItem[] newPromotableVersions(
      NewPromotableVersionsRequest newPromotableVersionsRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      String serviceName = "newPromotableVersions";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toLegacyGuidList(
            newPromotableVersionsRequest.getIds());
         
         List<String> paths = getPaths(newPromotableVersionsRequest.getPaths());
         
         String relationshipType = newPromotableVersionsRequest.getType();

         boolean enableRevisions = extractBooleanValue(
            newPromotableVersionsRequest.getEnableRevisions(), false);
         
         List<PSCoreItem> items = service.newPromotableVersions(ids, paths, 
            relationshipType, enableRevisions);
         
         return convertItems(items, null, null, null);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#newTranslations(NewTranslationsRequest)
    */
   @SuppressWarnings("unchecked")
   public PSItem[] newTranslations(NewTranslationsRequest newTranslationsRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      String serviceName = "newTranslations";
      try
      {
         authenticate();
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toLegacyGuidList(
            newTranslationsRequest.getIds());
         
         List<com.percussion.services.content.data.PSAutoTranslation> autoTranslations = null;
         if (newTranslationsRequest.getAutoTranslations() != null)
            autoTranslations = (List<com.percussion.services.content.data.PSAutoTranslation>) convert(List.class, 
               newTranslationsRequest.getAutoTranslations());
         
         String relationshipType = newTranslationsRequest.getType();

         boolean enableRevisions = extractBooleanValue(
            newTranslationsRequest.getEnableRevisions(), false);
         
         List<PSCoreItem> items = service.newTranslations(ids, autoTranslations, 
            relationshipType, enableRevisions);
         
         return convertItems(items, null, null, null);
      }
      catch (IllegalArgumentException e)
      {
         int code = IPSWebserviceErrors.INVALID_CONTRACT;
         throw new PSContractViolationFault(code, 
            PSWebserviceErrors.createErrorMessage(code, serviceName, 
               e.getLocalizedMessage()), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
      
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#prepareForEdit(long[])
    */
   public PSItemStatus[] prepareForEdit(long[] ids)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      String serviceName = "prepareForEdit";
      try
      {
         authenticate();

         List<IPSGuid> idList = new ArrayList<IPSGuid>();
         for (long id : ids)
            idList.add(new PSLegacyGuid(id));
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         List<com.percussion.services.content.data.PSItemStatus> status = 
            service.prepareForEdit(idList);

         return (PSItemStatus[]) convert(PSItemStatus[].class, status);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#promoteRevisions(PromoteRevisionsRequest)
    */
   public void promoteRevisions(long[] promoteRevisionsRequest) 
   	  throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault, 
   	  PSContractViolationFault
   {
      String serviceName = "promoteRevisions";
      try
      {
         authenticate();
         
         List<IPSGuid> ids = null;
         if (promoteRevisionsRequest != null && 
            promoteRevisionsRequest.length > 0)
            ids = PSWebserviceUtils.getLegacyGuidFromLong(promoteRevisionsRequest);
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         service.promoteRevisions(ids);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#releaseFromEdit(ReleaseFromEditRequest)
    */
   @SuppressWarnings("unchecked")
   public void releaseFromEdit(ReleaseFromEditRequest req)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault
   {
      String serviceName = "releaseFromEdit";
      try
      {
         authenticate();

         List<com.percussion.services.content.data.PSItemStatus> status = (List<com.percussion.services.content.data.PSItemStatus>) convert(List.class, 
               req.getPSItemStatus());

         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         service.releaseFromEdit(status, req.isCheckInOnly());
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#removeFolderChildren(RemoveFolderChildrenRequest)
    */
   public void removeFolderChildren(RemoveFolderChildrenRequest request)
      throws RemoteException, PSInvalidSessionFault, PSContractViolationFault
   {
      String serviceName = "removeFolderChildren";
      try
      {
         authenticate();
         validateFolderRef(request.getParent());
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         List<IPSGuid> childIds = null;
         if (request.getChildIds() != null && request.getChildIds().length > 0)
            childIds = PSWebserviceUtils.getLegacyGuidFromLong(request
                  .getChildIds());
         boolean purgeItem = false;
         if (request.getPurgeItems() != null)
            purgeItem = request.getPurgeItems().booleanValue();
         if (request.getParent().getId() != null)
         {
            PSLegacyGuid parentId = new PSLegacyGuid(request.getParent()
                  .getId());
            service.removeFolderChildren(parentId, childIds, purgeItem);
         }
         else
         {
            service.removeFolderChildren(request.getParent().getPath(),
                  childIds, purgeItem);
         }
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#reorderChildEntries(ReorderChildEntriesRequest)
    */
   public void reorderChildEntries(
      ReorderChildEntriesRequest reorderChildEntriesRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSUnknownChildFault, PSContractViolationFault
   {
      String serviceName = "reorderChildEntries";
      try
      {
         authenticate();
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         IPSGuid id = new PSLegacyGuid(reorderChildEntriesRequest.getId());
         String name = reorderChildEntriesRequest.getName();
         List<IPSGuid> childIds = new ArrayList<IPSGuid>();
         for (long childid : reorderChildEntriesRequest.getChildId())
         {
            childIds.add(new PSLegacyGuid(childid));
         }
         
         service.reorderChildEntries(id, name, childIds);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSUnknownChildException e)
      {
         throw new PSUnknownChildFault(e.getCode(), e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSInvalidStateException e)
      {
         throw new PSContractViolationFault(e.getCode(), 
            e.getLocalizedMessage(), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#reorderContentRelations(ReorderContentRelationsRequest)
    */
   public void reorderContentRelations(ReorderContentRelationsRequest req)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSContractViolationFault
   {
      try
      {
         if (req.getId() == null || req.getId().length == 0)
            throw new IllegalArgumentException("ids must not be null or empty.");
         
         authenticate();

         // get data from request
         List<IPSGuid> relatedIds = getGuidFromLong(req.getId(), 
            PSTypeEnum.RELATIONSHIP);
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         int index = (req.getIndex() == null) ? -1 : req.getIndex().intValue();
         service.reorderContentRelations(relatedIds, index);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, "reorderContentRelations");
      }
      catch (PSErrorException e)
      {
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#saveChildEntries(SaveChildEntriesRequest)
    */
   @SuppressWarnings("unchecked")
   public void saveChildEntries(SaveChildEntriesRequest saveChildEntriesRequest)
      throws RemoteException, PSInvalidSessionFault, PSErrorsFault,
      PSUnknownChildFault, PSContractViolationFault
   {
      String serviceName = "saveChildEntries";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         IPSGuid id = new PSLegacyGuid(saveChildEntriesRequest.getId());
         String name = saveChildEntriesRequest.getName();
         List<PSItemChildEntry> entries = (List<PSItemChildEntry>) 
            convert(List.class, saveChildEntriesRequest.getPSChildEntry());
         
         service.saveChildEntries(id, name, entries);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSUnknownChildException e)
      {
         throw new PSUnknownChildFault(e.getCode(), e.getLocalizedMessage(), 
            ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSInvalidStateException e)
      {
         throw new PSContractViolationFault(e.getCode(), 
            e.getLocalizedMessage(), ExceptionUtils.getFullStackTrace(e));
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }
      catch (PSErrorException e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#saveContentRelations(data.PSAaRelationship[])
    */
   @SuppressWarnings("unchecked")
   public void saveContentRelations(
      PSAaRelationship[] relationships) throws RemoteException,
      PSInvalidSessionFault, PSErrorsFault, PSContractViolationFault,
      PSNotAuthorizedFault
   {
      String serviceName = "saveContentRelations";
      authenticate();
      
      try
      {
         List<com.percussion.cms.objectstore.PSAaRelationship> rels = 
            (List<com.percussion.cms.objectstore.PSAaRelationship>) convert(
               List.class, relationships);

         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         service.saveContentRelations(rels);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorsException e)
      {
         handleErrorsException(e, serviceName);
      }      
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RemoteException(e.getLocalizedMessage());
      }
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see Content#saveFolders(data.PSFolder[])
    */
   @SuppressWarnings("unchecked")
   public SaveFoldersResponse saveFolders(PSFolder[] folders)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      String serviceName = "saveFolders";
      try
      {
         authenticate();

         // get data from request
         List<com.percussion.cms.objectstore.PSFolder> folderList =
            (List<com.percussion.cms.objectstore.PSFolder>) convert(
               List.class, 
               folders);

         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         List<IPSGuid> ids = service.saveFolders(folderList);
         
         SaveFoldersResponse response = new SaveFoldersResponse(
            PSWebserviceUtils.getLongsFromGuids(ids));
         return response;
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see Content#saveItems(SaveItemsRequest)
    */
   @SuppressWarnings("unchecked")
   public SaveItemsResponse saveItems(SaveItemsRequest saveItemsRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      String serviceName = "saveItems";
      
      List<PSPurgableTempFile> tempFiles = 
                                    new ArrayList<PSPurgableTempFile>();
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<PSCoreItem> items = (List<PSCoreItem>) convert(List.class, 
            saveItemsRequest.getPSItem());
         processAttachedFields(items, tempFiles);

         boolean enableRevisions = extractBooleanValue(
            saveItemsRequest.getEnableRevisions(), false);
         boolean checkin = extractBooleanValue(
            saveItemsRequest.getCheckin(), false);
         
         List<IPSGuid> ids = service.saveItems(items, enableRevisions, checkin);
         
         return new SaveItemsResponse(PSGuidUtils.toLongArray(ids));
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      finally
      {
         for(PSPurgableTempFile file:tempFiles)
         {
            file.release();
         }
      }
      
      return null;
   }

   /**
    * First retrieves all attachments from the current message. If there are
    * attachments then all parent and child binary fields are walked to match up
    * its attachment id with the attachment. If we find a match, the field value
    * is set with teh attachment input stream.
    * 
    * @param items all items to map the message attachments to, assumed not
    * <code>null</code>, may be empty.
    * @param tempFiles list of purgable files to store so that we can release
    * them later assumed not <code>null</code>, may be empty.
    * @throws RemoteException for any error mapping the attachments.
    */
   private void processAttachedFields(List<PSCoreItem> items, 
                                      List<PSPurgableTempFile> tempFiles) 
      throws RemoteException
   {
      try
      {
         AttachmentPart[] attachments = getAttachments();
         
         List<String> mappedAttachmentIds = new ArrayList<String>();
         for (AttachmentPart attachment : attachments)
         {
            boolean found = false;
            for (PSCoreItem item : items)
            {
               // walk the parent fields first
               found = mapAttachmentToField(attachment, 
                                            item.getAllFields(), tempFiles);
               
               if (!found)
               {
                  // walk the child fields if we have not found a match yet
                  Iterator<PSItemChild> children = item.getAllChildren();
                  while (!found && children.hasNext())
                  {
                     PSItemChild child = children.next();
                     Iterator<PSItemChildEntry> entries = child.getAllEntries();
                     while (!found && entries.hasNext())
                     {
                        PSItemChildEntry entry = entries.next();
                        found = mapAttachmentToField(attachment, 
                           entry.getAllFields(), tempFiles);
                        
                        if (found)
                           mappedAttachmentIds.add(attachment.getContentId());
                     }
                  }
               }
               else
                  mappedAttachmentIds.add(attachment.getContentId());
            }
         }
         
         if (mappedAttachmentIds.size() != attachments.length)
         {
            // this should never happen
            throw new IllegalArgumentException(
               "No fields found for some of the attachments. Found " + 
               attachments.length + " attachments but could only map " + 
               mappedAttachmentIds.size() + ".");
         }
      }
      catch (IOException e)
      {
         // this should never happen
         throw new RemoteException(e.getLocalizedMessage());
      }
   }
   
   /**
    * Walks the supplied field list and sets the supplied attachment as binary
    * value if a match is found.
    * 
    * @param attachment the attachment to set, assumed not <code>null</code>.
    * @param fields the fields to walk, assumed not <code>null</code>, may be
    *    empty.
    * @return <code>true</code> if we found a matching field for the
    *    supplied attachment, <code>false</code> otherwise.
    * @throws RemoteException for any error.
    */
   private boolean mapAttachmentToField(AttachmentPart attachment, 
      Iterator<PSItemField> fields, List<PSPurgableTempFile> tempFiles) 
   throws RemoteException
   {
      try
      {
         while (fields.hasNext())
         {
            PSItemField field = fields.next();
   
            // only process binary fields
            if (field.getItemFieldMeta().getBackendDataType() != 
               PSItemFieldMeta.DATATYPE_BINARY)
               continue;
            
            // only process binary fields with valid attachment id
            String attachmentId = field.getHrefLocation();
            if (StringUtils.isBlank(attachmentId))
               continue;
   
            // is the current attachment mapped to this field?
            if (!attachmentId.equals(attachment.getContentId()))
               continue;
            
            PSPurgableTempFile tempFile = new PSPurgableTempFile("psx", 
                                                                 ".bin", null);

            try(InputStream stream = attachment.getActivationDataHandler().getInputStream()){
               IOTools.copyStreamToFile(stream, tempFile);
               tempFiles.add(tempFile);
            }

            PSBinaryValue value = new PSPurgableFileValue(tempFile);
            field.addValue(value);
            return true;
         }
         
         return false;
      }
      catch (IOException e)
      {
         // this should never happen
         throw new RemoteException(e.getLocalizedMessage());
      }
   }
   
   /**
    * Creates attachments for all binary fields (parent and child) found in 
    * the supplied items and adds them to the current message.
    * 
    * @param items all items for which to attach the binary field contents
    *    to the current message, assumed not <code>null</code>, may be empty.
    */
   private void attachBinaryFields(List<PSCoreItem> items)
   {
      for (PSCoreItem item : items)
      {
         addAttachments(item.getAllFields());
         
         Iterator<PSItemChild> children = item.getAllChildren();
         while (children.hasNext())
         {
            PSItemChild child = children.next();
            Iterator<PSItemChildEntry> entries = child.getAllEntries();
            while (entries.hasNext())
            {
               PSItemChildEntry entry = entries.next();
               addAttachments(entry.getAllFields());
            }
         }
      }
   }
   
   /**
    * Creates attachments for all binary fields supplied and adds them to the
    * current message. The fields href location will be updated with the
    * ccontent id of the new attachment created.
    * 
    * @param fields the fields to walk, assumed not <code>null</code>, may be
    *    empty.
    */
   private void addAttachments(Iterator<PSItemField> fields)
   {
      MessageContext context = MessageContext.getCurrentContext();
      Message msg = context.getResponseMessage();

      while (fields.hasNext())
      {
         PSItemField field = fields.next();
         
         // only process binary fields
         if (field.getItemFieldMeta().getBackendDataType() != 
            PSItemFieldMeta.DATATYPE_BINARY)
            continue;
         
         PSBinaryValue value = (PSBinaryValue) field.getValue();
         if (value != null)
         {
            File file = value.getValueFile();
            if (file != null)
            {
               DataHandler handler = new DataHandler(
                  new FileDataSource(file));
               AttachmentPart part = new AttachmentPart(handler);

               msg.addAttachmentPart(part);
               field.setHrefLocation(part.getContentId());
            }
         }
      }
   }
   
   /* (non-Javadoc)
    * @see Content#viewItems(ViewItemsRequest)
    */
   public PSItem[] viewItems(ViewItemsRequest viewItemsRequest)
      throws RemoteException, PSErrorResultsFault, PSInvalidSessionFault,
      PSContractViolationFault
   {
      String serviceName = "viewItems";
      try
      {
         authenticate();
         
         IPSContentWs service = PSContentWsLocator.getContentWebservice();
         
         List<IPSGuid> ids = PSGuidUtils.toLegacyGuidList(
            viewItemsRequest.getId());
         
         List<String> fieldNames = null;
         if (viewItemsRequest.getFieldName() != null && 
                  viewItemsRequest.getFieldName().length > 0)
            fieldNames = Arrays.asList(viewItemsRequest.getFieldName());
         
         boolean includeBinary = extractBooleanValue(
            viewItemsRequest.getIncludeBinary(), false);
         boolean attachBinaries = extractBooleanValue(
            viewItemsRequest.getAttachBinaries(), false);
         
         boolean includeChildren = extractBooleanValue(
            viewItemsRequest.getIncludeChildren(), false);
         List<String> childNames = null;
         if (includeChildren && viewItemsRequest.getChildName() != null)
            childNames = Arrays.asList(viewItemsRequest.getChildName());
         else if (!includeChildren)
            childNames = new ArrayList<String>();
         
         boolean includeRelated = extractBooleanValue(
            viewItemsRequest.getIncludeRelated(), false);
         List<String> slotNames = null;
         if (includeRelated && viewItemsRequest.getSlotName() != null)
            slotNames = Arrays.asList(viewItemsRequest.getSlotName());
         else if (!includeRelated)
            slotNames = new ArrayList<String>();
         
         boolean includeFolderPath = extractBooleanValue(
            viewItemsRequest.getIncludeFolderPath(), false);
         
         List<PSCoreItem> items = service.viewItems(ids, includeBinary, 
            includeChildren, includeRelated, includeFolderPath);
         
         if (includeBinary && attachBinaries)
            attachBinaryFields(items);
         
         return convertItems(items, fieldNames, childNames, slotNames);
      }
      catch (IllegalArgumentException e)
      {
         handleInvalidContract(e, serviceName);
      }
      catch (PSErrorResultsException e)
      {
         handleErrorResultsException(e, serviceName);
      }
      
      return new PSItem[0];
   }
}
