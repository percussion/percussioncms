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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSContentType;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.i18n.PSLocale;
import com.percussion.services.assembly.data.PSTemplateSlot;
import com.percussion.services.content.data.PSAutoTranslation;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.services.contentmgr.data.PSContentTemplateDesc;
import com.percussion.services.filter.data.PSItemFilter;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.services.system.data.PSAuditTrail;
import com.percussion.services.system.data.PSSharedProperty;
import com.percussion.services.ui.data.PSHierarchyNode;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSLockErrorException;
import com.percussion.webservices.assembly.data.PSAssemblyTemplateWs;
import com.percussion.webservices.content.PSChildEntry;
import com.percussion.webservices.faults.PSError;
import com.percussion.webservices.faults.PSErrorResultsFault;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCall;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCallError;
import com.percussion.webservices.faults.PSErrorResultsFaultServiceCallResult;
import com.percussion.webservices.faults.PSLockFault;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.Converter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Converts objects between the classes 
 * <code>com.percussion.webservices.PSErrorResultsException</code> and 
 * <code>com.percussion.webservices.faults.PSErrorResultsFault</code>.
 */
public class PSErrorResultsExceptionConverter extends PSConverter
{
   /* (non-Javadoc)
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSErrorResultsExceptionConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /* (non-Javadoc)
    * @see PSConverter#convert(Class, Object)
    */
   @Override
   public Object convert(@SuppressWarnings("unused") Class type, Object value)
   {
      if (value == null)
         return null;
      
      if (value instanceof PSErrorResultsFault)
      {
         PSErrorResultsFault source = (PSErrorResultsFault) value;

         PSErrorResultsException target = new PSErrorResultsException();
         PSErrorResultsFaultServiceCall[] calls = source.getServiceCall();
         for (int i=0; i<calls.length; i++)
         {
            PSErrorResultsFaultServiceCall call = calls[i];
            if (call.getError() != null)
            {
               PSErrorResultsFaultServiceCallError error = call.getError();
               
               Object errorValue = error.getPSError();
               Class errorType = PSErrorException.class;
               if (errorValue == null)
               {
                  errorValue = error.getPSLockFault();
                  errorType = PSLockErrorException.class;
               }
               
               if (errorValue == null)
               {
                  String warningMsg = 
                     "No error value found for PSErrorResultsFault converter.";
                  m_log.warn(warningMsg);
                  throw new ConversionException(warningMsg);
               }
                  
               Converter converter = getConverter(errorType);
               target.addError(new PSDesignGuid(error.getId()), 
                  converter.convert(errorType, errorValue));
            }
            else
            {
               PSErrorResultsFaultServiceCallResult result = call.getResult();
               
               Object resultValue = result.getPSAaRelationship();
               Class resultType = PSAaRelationship.class;
               if (resultValue == null)
               {
                  resultValue = result.getPSAclImpl();
                  resultType = PSAclImpl.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSAction();
                  resultType = PSAction.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSAssemblyTemplate();
                  resultType = PSAssemblyTemplateWs.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSAuditTrail();
                  resultType = PSAuditTrail.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSAutoTranslation();
                  resultType = PSAutoTranslation.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSChildEntry();
                  resultType = PSChildEntry.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSCommunity();
                  resultType = PSCommunity.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSContentType();
                  resultType = PSContentType.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSDisplayFormat();
                  resultType = PSDisplayFormat.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSFolder();
                  resultType = PSFolder.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSHierarchyNode();
                  resultType = PSHierarchyNode.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSItem();
                  resultType = PSCoreItem.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSItemStatus();
                  resultType = PSItemStatus.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSKeyword();
                  resultType = PSKeyword.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSLocale();
                  resultType = PSLocale.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSRelationshipConfig();
                  resultType = PSRelationshipConfig.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSSearchDef();
                  resultType = PSSearch.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSSharedProperty();
                  resultType = PSSharedProperty.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSTemplateSlot();
                  resultType = PSTemplateSlot.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSViewDef();
                  resultType = PSSearch.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSItemFilter();
                  resultType = PSItemFilter.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getPSContentTemplateDesc();
                  resultType = PSContentTemplateDesc.class;
               }
               if (resultValue == null)
               {
                  resultValue = result.getState();
                  resultType = String.class;
               }
               
               if (resultValue == null)
               {
                  String warningMsg = 
                     "No result value found for PSErrorResultsFault converter.";
                  m_log.warn(warningMsg);
                  throw new ConversionException(warningMsg);
               }
               
               Converter converter = getConverter(resultType);
               target.addResult(new PSDesignGuid(result.getId()), 
                  converter.convert(resultType, resultValue));
            }
         }

         return target;
      }
      else
      {
         PSErrorResultsException source = (PSErrorResultsException) value;

         PSErrorResultsFault target = new PSErrorResultsFault();

         Iterator ids = source.getIds().iterator();
         Map <IPSGuid, Object> errors = source.getErrors();
         Map <IPSGuid, Object> results = source.getResults();
         
         PSErrorResultsFaultServiceCall[] calls = 
            new PSErrorResultsFaultServiceCall[source.getIds().size()];
         target.setServiceCall(calls);
         
         int index = 0;
         while (ids.hasNext())
         {
            IPSGuid id = (IPSGuid) ids.next();
            
            PSErrorResultsFaultServiceCall call = null;
            Object callValue = errors.get(id);
            if (callValue == null)
            {
               callValue = results.get(id);

               Class converterClass = callValue.getClass();
               if (callValue instanceof PSServerItem)
                  converterClass = PSCoreItem.class;
               Converter converter = getConverter(converterClass);

               PSErrorResultsFaultServiceCallResult result = 
                  new PSErrorResultsFaultServiceCallResult();
               result.setId((new PSDesignGuid(id)).getValue());
               PSErrorResultsFaultServiceCallError error = null;
               
               if (callValue instanceof PSAaRelationship)
               {
                  Class resultType = 
                     com.percussion.webservices.content.PSAaRelationship.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSAaRelationship(
                     (com.percussion.webservices.content.PSAaRelationship) resultValue);
               }
               else if (callValue instanceof PSAclImpl)
               {
                  Class resultType = 
                     com.percussion.webservices.system.PSAclImpl.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSAclImpl(
                     (com.percussion.webservices.system.PSAclImpl) resultValue);
               }
               else if (callValue instanceof PSAction)
               {
                  Class resultType = 
                     com.percussion.webservices.ui.data.PSAction.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSAction(
                     (com.percussion.webservices.ui.data.PSAction) resultValue);
               }
               else if (callValue instanceof PSAssemblyTemplateWs)
               {
                  Class resultType = 
                     com.percussion.webservices.assembly.data.PSAssemblyTemplate.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSAssemblyTemplate(
                     (com.percussion.webservices.assembly.data.PSAssemblyTemplate) resultValue);
               }
               else if (callValue instanceof PSAuditTrail)
               {
                  Class resultType = 
                     com.percussion.webservices.system.PSAuditTrail.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSAuditTrail(
                     (com.percussion.webservices.system.PSAuditTrail) resultValue);
               }
               else if (callValue instanceof PSAutoTranslation)
               {
                  Class resultType = 
                     com.percussion.webservices.content.PSAutoTranslation.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSAutoTranslation(
                     (com.percussion.webservices.content.PSAutoTranslation) resultValue);
               }
               else if (callValue instanceof PSField[])
               {
                  Class resultType = 
                     com.percussion.webservices.content.PSChildEntry.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSChildEntry(
                     (com.percussion.webservices.content.PSChildEntry) resultValue);
               }
               else if (callValue instanceof PSCommunity)
               {
                  Class resultType = 
                     com.percussion.webservices.security.data.PSCommunity.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSCommunity(
                     (com.percussion.webservices.security.data.PSCommunity) resultValue);
               }
               else if (callValue instanceof PSContentType)
               {
                  Class resultType = 
                     com.percussion.webservices.content.PSContentType.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSContentType(
                     (com.percussion.webservices.content.PSContentType) resultValue);
               }
               else if (callValue instanceof PSDisplayFormat)
               {
                  Class resultType = 
                     com.percussion.webservices.ui.data.PSDisplayFormat.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSDisplayFormat(
                     (com.percussion.webservices.ui.data.PSDisplayFormat) resultValue);
               }
               else if (callValue instanceof PSFolder)
               {
                  Class resultType = 
                     com.percussion.webservices.content.PSFolder.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSFolder(
                     (com.percussion.webservices.content.PSFolder) resultValue);
               }
               else if (callValue instanceof PSHierarchyNode)
               {
                  Class resultType = 
                     com.percussion.webservices.ui.data.PSHierarchyNode.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSHierarchyNode(
                     (com.percussion.webservices.ui.data.PSHierarchyNode) resultValue);
               }
               else if (callValue instanceof PSCoreItem)
               {
                  Class resultType = 
                     com.percussion.webservices.content.PSItem.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSItem(
                     (com.percussion.webservices.content.PSItem) resultValue);
               }
               else if (callValue instanceof PSItemStatus)
               {
                  Class resultType = 
                     com.percussion.webservices.content.PSItemStatus.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSItemStatus(
                     (com.percussion.webservices.content.PSItemStatus) resultValue);
               }
               else if (callValue instanceof PSKeyword)
               {
                  Class resultType = 
                     com.percussion.webservices.content.PSKeyword.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSKeyword(
                     (com.percussion.webservices.content.PSKeyword) resultValue);
               }
               else if (callValue instanceof PSLocale)
               {
                  Class resultType = 
                     com.percussion.webservices.content.PSLocale.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSLocale(
                     (com.percussion.webservices.content.PSLocale) resultValue);
               }
               else if (callValue instanceof PSRelationshipConfig)
               {
                  Class resultType = 
                     com.percussion.webservices.system.PSRelationshipConfig.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSRelationshipConfig(
                     (com.percussion.webservices.system.PSRelationshipConfig) resultValue);
               }
               else if (callValue instanceof PSSearch)
               {
                  PSSearch search = (PSSearch) callValue;
                  if (search.isView())
                  {
                     Class resultType = 
                        com.percussion.webservices.ui.data.PSViewDef.class;
                     Object resultValue = converter.convert(resultType, callValue);
                     result.setPSViewDef(
                        (com.percussion.webservices.ui.data.PSViewDef) resultValue);
                  }
                  else
                  {
                     Class resultType = 
                        com.percussion.webservices.ui.data.PSSearchDef.class;
                     Object resultValue = converter.convert(resultType, callValue);
                     result.setPSSearchDef(
                        (com.percussion.webservices.ui.data.PSSearchDef) resultValue);
                  }
               }
               else if (callValue instanceof PSSharedProperty)
               {
                  Class resultType = 
                     com.percussion.webservices.system.PSSharedProperty.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSSharedProperty(
                     (com.percussion.webservices.system.PSSharedProperty) resultValue);
               }
               else if (callValue instanceof PSTemplateSlot)
               {
                  Class resultType = 
                     com.percussion.webservices.assembly.data.PSTemplateSlot.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSTemplateSlot(
                     (com.percussion.webservices.assembly.data.PSTemplateSlot) resultValue);
               }
               else if (callValue instanceof PSItemFilter)
               {
                  Class resultType = 
                     com.percussion.webservices.system.PSItemFilter.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSItemFilter(
                     (com.percussion.webservices.system.PSItemFilter) resultValue);
               }
               else if (callValue instanceof PSContentTemplateDesc)
               {
                  Class resultType = 
                     com.percussion.webservices.content.PSContentTemplateDesc.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  result.setPSContentTemplateDesc(
                     (com.percussion.webservices.content.PSContentTemplateDesc) resultValue);
               }
               else if (callValue instanceof String)
               {
                  result.setState((String) callValue);
               }
               else // unknown data type, should not be here
               {
                  result = null;
                  error = getUnknownDataTypeError(callValue.getClass());
               }

               call = new PSErrorResultsFaultServiceCall(result, error);
            }
            else
            {
               Converter converter = getConverter(callValue.getClass());

               PSErrorResultsFaultServiceCallError error = 
                  new PSErrorResultsFaultServiceCallError();
               error.setId((new PSDesignGuid(id)).getValue());
               if (callValue instanceof PSLockErrorException)
               {
                  Class resultType = PSLockFault.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  error.setPSLockFault((PSLockFault) resultValue);
               }
               else if (callValue instanceof PSErrorException)
               {
                  Class resultType = PSError.class;
                  Object resultValue = converter.convert(resultType, callValue);
                  error.setPSError((PSError) resultValue);
               }
               else
               {
                  error = getUnknownDataTypeError(callValue.getClass());
               }

               call = new PSErrorResultsFaultServiceCall(null, error);
            }
            calls[index++] = call;
         }
         
         return target;
      }
   }
   
   /**
    * Creates an error for the specified unknown data type.
    * 
    * @param clz the unknown data type; assumed not <code>null</code>.
    * 
    * @return the created error; never <code>null</code>. 
    */
   private PSErrorResultsFaultServiceCallError getUnknownDataTypeError(Class clz) 
   {
      PSErrorResultsFaultServiceCallError error = 
         new PSErrorResultsFaultServiceCallError();
      String msg = "Failed to convert an unknown data type: " + clz.toString();
      PSError e = new PSError(0, msg, null);
      error.setPSError(e);

      m_log.warn(msg);
      
      return error;
   }

   /**
    * the logger used by this class, never <code>null</code>.
    */
   private static final Logger m_log = LogManager.getLogger(
      PSErrorResultsExceptionConverter.class);
}

