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
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.security.IPSTypedPrincipal;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;

import java.security.acl.NotOwnerException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

/**
 * This exit was written for the specific purpose of adding an ACL for a newly
 * created workflow so the workflow would be visible in the user's community.
 * It was not made generic for multi-purpose usage. 
 *
 * @author paulhoward
 */
public class PSExitCreateDefaultCommunityAcl extends PSDefaultExtension
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
    * Creates an ACL for a newly created workflow. It is assumed that the 
    * workflow identified by the 'workflowid' parameter does not have an ACL.
    * The acl is configured to give the special entry Default all access and
    * ownership and the user's community read access.
    * <p>
    * If there is no community set in the session, returns w/o creating the ACL.
    * 
    * @param params Unused.
    * @param request Used to obtain the workflow and user's community.
    * @param resultDoc Returned on success.
    * 
    * @return The supplied doc.
    * 
    * @throws PSExtensionProcessingException If the workflowid or communityid
    * can't be parsed into numbers or the community cannot be loaded.
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
      String commId = "";
      String errorMsg = null;
      try
      {
         IPSAclService aclService = PSAclServiceLocator.getAclService();
         objectId = request.getParameter(objectIdParam);
         if (StringUtils.isBlank(objectId))
         {
            request.printTraceMessage(
                  "No object id found. Skipping ACL creation.");
            return resultDoc;
         }
         
         //fixme this needs to be modified when the ACL stuff is reworked
         IPSGuidManager guidMgr = PSGuidManagerLocator.getGuidMgr();
         objectGuid = guidMgr.makeGuid(Long.parseLong(objectId),
               objectTypeEnum);
         IPSTypedPrincipal owner = new PSTypedPrincipal(
            PSTypedPrincipal.DEFAULT_USER_ENTRY, PrincipalTypes.USER);
         IPSAcl acl = aclService.createAcl(objectGuid, owner);
         Enumeration entries = acl.entries();
         int entryCount = 0;
         IPSAclEntry ownerEntry = acl.findEntry(owner);
         assert(ownerEntry != null);
         ownerEntry.addPermissions(new PSPermissions[]
         {
            PSPermissions.READ, PSPermissions.UPDATE, PSPermissions.DELETE
         });
         
         commId = request.getSessionPrivateObject("sys_community").toString();
         
         IPSBackEndRoleMgr roleMgr = PSRoleMgrLocator.getBackEndRoleManager();
         PSCommunity comm = roleMgr.loadCommunity(guidMgr.makeGuid(Long
               .parseLong(commId), PSTypeEnum.COMMUNITY_DEF));
         
         IPSAclEntry communityEntry = acl.createEntry(new PSTypedPrincipal(comm
            .getName(), PrincipalTypes.COMMUNITY), new PSPermissions[]
         {
            PSPermissions.RUNTIME_VISIBLE
         });
         
         acl.addEntry(owner, communityEntry);
         aclService.saveAcls(Collections.singletonList(acl));
      }
      //because these errors should never happen, I haven't i18n the strings
      catch (NumberFormatException e)
      {
         //should never happen
         if (objectId.length() == 0)
         {
            errorMsg = "The object id ''{0}'' could not be parsed. ";
            MessageFormat.format(errorMsg, objectId);
         }
         else
         {
            errorMsg = "The community id ''{0}'' could not be parsed.";
            MessageFormat.format(errorMsg, commId);
         }
      }
      catch (PSSecurityException e)
      {
         //couldn't load community
         errorMsg = "Failed to find community {0} when creating workflow {1}";
         MessageFormat.format(errorMsg, commId);
      }
      catch (NotOwnerException e)
      {
         //should never happen
         errorMsg = e.getLocalizedMessage();
      }
      
      if (errorMsg != null)
      {
         request.printTraceMessage(MessageFormat.format("{0} {1}", errorMsg,
               "The created object was not added to a community."));
         throw new PSExtensionProcessingException(IPSServerErrors.RAW_DUMP,
               errorMsg);
      }
      return resultDoc;
   }
}
