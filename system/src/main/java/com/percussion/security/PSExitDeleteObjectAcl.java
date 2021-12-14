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
package com.percussion.security;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionParams;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import java.text.MessageFormat;

/**
 * This exit was written for the specific purpose of deleting an ACL for a newly
 * deleted objects like workfloe, sites to prevent orphan acls. 
 * It was not made generic for multi-purpose usage.
 * 
 * @author paulhoward
 */
public class PSExitDeleteObjectAcl extends PSDefaultExtension
   implements IPSResultDocumentProcessor
{
   /**
    * Not meant for stylesheets.
    * 
    * @return Always false.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Deletes an ACL for a newly deleted object. The object id is obtained
    * from the value supplied as objectid parameter. 
    * It is not an error for no acl to exist.
    * 
    * @param params object array of parameters.
    * The first param should designate the object ids parameter.
    * The second param should designate the type of object.
    * @param request IPSRequestContext object to get the object ids
    * @param resultDoc Returned on success.
    * 
    * @return The supplied doc.
    * 
    * @throws PSExtensionProcessingException If the object id can't be parsed
    * into numbers or a problem occurs attempting to delete the acl.
    */
   public Document processResultDocument(
         @SuppressWarnings("unused") Object[] params,
         IPSRequestContext request, Document resultDoc)
      throws PSExtensionProcessingException
   {
      PSExtensionParams extParams;
      String objectIdParam;
      String objectType = null;
      int objectTypeId = 0;
      try
      {
         extParams = new PSExtensionParams(params);
         objectIdParam = extParams.getStringParam(0,"",true);
         objectType = extParams.getStringParam(1,"",true);
         objectTypeId = Integer.parseInt(objectType);
      }
      catch (PSConversionException e)
      {
         throw new PSExtensionProcessingException(e);
      }
      catch(NumberFormatException e)
      {
         String msg = "The object type id ''{0}'' could not be parsed. ";
         MessageFormat.format(msg, objectType);
         throw new PSExtensionProcessingException(IPSServerErrors.RAW_DUMP,
               msg);
      }
      
      PSTypeEnum objectTypeEnum = PSTypeEnum.valueOf(objectTypeId);
      if(objectTypeEnum == null)
         throw new IllegalArgumentException("object type must be a valid type enum");

      String objectId = "";
      IPSGuid objectGuid = null;
      String errorMsg = null;
      try
      {
         IPSAclService aclService = PSAclServiceLocator.getAclService();
         objectId = request.getParameter(objectIdParam);
         if (StringUtils.isBlank(objectId))
         {
            request.printTraceMessage(
                  "No object id found. Skipping ACL deletion.");
            return resultDoc;
         }
         
         IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
         objectGuid = guidMgr.makeGuid(Long.parseLong(objectId),
               objectTypeEnum);
         //fixme don't use impl class when acl stuff cleaned up
         IPSAcl acl = aclService.loadAclForObject(objectGuid);
         if (acl != null)
            aclService.deleteAcl(((PSAclImpl) acl).getGUID());
      }
      //because these errors should never happen, I haven't i18n the strings
      catch (NumberFormatException e)
      {
         errorMsg = "The object id ''{0}'' could not be parsed. ";
         MessageFormat.format(errorMsg, objectId);
      }
      catch (PSSecurityException e)
      {
         //couldn't delete acl
         errorMsg = "Failed to delete acl for object {0}: {1}";
         MessageFormat.format(errorMsg, objectId, e.getLocalizedMessage());
      }
      
      if (errorMsg != null)
      {
         request.printTraceMessage(errorMsg);
         throw new PSExtensionProcessingException(IPSServerErrors.RAW_DUMP,
               errorMsg);
      }
      return resultDoc;
   }
}
