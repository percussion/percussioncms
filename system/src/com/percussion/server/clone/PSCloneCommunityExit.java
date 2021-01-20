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
package com.percussion.server.clone;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSRole;
import com.percussion.design.objectstore.PSRoleConfiguration;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSXmlObjectStoreLockerId;
import com.percussion.error.PSException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * This exit is used to clone communities. If the HTML parameter
 * <code>sourcecloneid</code> is supplied, this exit gets all Content Types,
 * Workflows, Components, Variants, Sites that are related to the source
 * community and adds them to the new community clone. If that parameter is not
 * found, this exit does nothing. This exit also needs the newly created
 * community id in the form of HTML parameter named "communityid" if it is
 * missing, the exit skips the copying community relations. If roleid HTML
 * parameter exists then it adds that role to the new community, if rolename
 * exists instead of roleid then it creates a new role with that name and newly
 * created roleid will be added to the community. If there are any errors while
 * creating the role it writes to the trace and user has to create the role in
 * Admin client and then add that role to the new community manually.
 */
public class PSCloneCommunityExit extends PSCloneBase
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#
    *       processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {

      int sourceCommunityId = getCloneSourceId(request);
      String targetCommunityId = request.getParameter("communityid","").trim();
      if(targetCommunityId.length()<0)
      {
         request
         .printTraceMessage("Error: Missing communityid html parameter, " + 
               "skipped copying the community relationships."
               + "Check the resource on which this exit is placed and make sure"
               + "it supplies communityid html parameter.");
         return resultDoc;
      }
      if (sourceCommunityId > 0)
      {
         
         String rolename = request.getParameter("rolename", "").trim();
         Map qrParams = new HashMap();
         qrParams.put(IPSHtmlParameters.SYS_COMMUNITYID, Integer
               .toString(sourceCommunityId));
         Map upParams = new HashMap();
         upParams.put("DBActionType", "INSERT");
         //Call the base class cloneChildObjects method to clone the states,
         //roles and others.
         cloneChildObjects(request, "CommunityId", targetCommunityId,
               ms_queryResources, ms_updateResources, qrParams, upParams);

         //If roleid is present, user wants to add an existsing role
         //to the community.
         String roleId = request.getParameter("roleId", "").trim();

         //When there is no roleid but rolename is present user wants to create
         //a new role and add that role to new community
         if (roleId.length() < 1 && rolename.length() > 0)
         {
            PSRole role = createRole(request, rolename);
            roleId = Integer.toString(role.getDatabaseComponentId());
         }
         
         if (roleId.length() > 0)
         {
            Map roleParams = new HashMap();
            roleParams.put("DBActionType", "INSERT");
            roleParams.put("roleid", roleId);
            roleParams.put("communityid", targetCommunityId);
            updateContent(request, UPDATE_ROLE_COMMUNITY_RES, roleParams);
         }
         else
         {
            request
                  .printTraceMessage("Skipping role community relation as "
                        + "roleid is empty");
         }
      }
      return resultDoc;
   }

   /**
    * Utility method to create a Role with the supplied name.
    * 
    * @param request IPSRequestContext object assumed not <code>null</code>.
    * @param roleName The name of the role to create, assumed not
    *           <code>null</code> or empty.
    * @return The role, never <code>null</code>.
    * 
    * @throws PSExtensionProcessingException if the role cannot be created.
    */
   private PSRole createRole(IPSRequestContext request, String roleName) 
      throws PSExtensionProcessingException
   {
      PSRole role = null;
      if (roleName.length() > 0)
      {
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         PSXmlObjectStoreLockerId lockId = null;
         try
         {
            PSSecurityToken tok = request.getSecurityToken();
            PSRoleConfiguration roleCfg = os.getRoleConfigurationObject(tok);
            String userName = request
                  .getUserContextInformation("User/Name", "").toString();
            lockId = new PSXmlObjectStoreLockerId(userName, true, tok
                  .getUserSessionId());
            os.getServerConfigLock(lockId, 30);

            roleCfg.getRoles().add(new PSRole(roleName));
            os.saveRoleConfiguration(roleCfg, lockId, tok);
            role = getRole(roleCfg, roleName);
         }
         catch (PSException e)
         {
            request.printTraceMessage("Error occurred while creating the role."
                  + e.getMessage());
            Object[] args = {roleName, e.getLocalizedMessage()};
            throw new PSExtensionProcessingException(
               IPSCloneErrors.ROLE_CREATION_ERROR, args);            
         }
         finally
         {
            if (lockId != null)
            {
               try
               {
                  os.releaseServerConfigLock(lockId);
               }
               catch (PSServerException e)
               {
                  // not fatal
               }
            }
         }
      }
      return role;

   }

   /**
    * Get the specified role from the supplied role cfg.
    * 
    * @param roleCfg The role cfg, assumed not <code>null</code>.
    * @param roleName The name of the role to get, assumed not <code>null</code>
    *           or empty.
    * 
    * @return The role, may be <code>null</code> if not found in the supplied
    *         cfg.
    */
   private PSRole getRole(PSRoleConfiguration roleCfg, String roleName)
   {
      PSRole role = null;
      Iterator roles = roleCfg.getRoles().iterator();
      while (roles.hasNext() && role == null)
      {
         PSRole test = (PSRole) roles.next();
         if (test.getName().equalsIgnoreCase(roleName))
            role = test;
      }

      return role;
   }

   /**
    * Array of query resource names of community relations.
    */
   private static final String[] ms_queryResources =
   {"sys_commCloning/QueryWorkflowCommunity",
         "sys_commCloning/QueryContentTypeCommunity",
         "sys_commCloning/QueryComponentCommunity",
         "sys_commCloning/QueryVariantCommunity",
         "sys_commCloning/QuerySiteCommunity"};

   /**
    * Array of update resource names of community relations.
    */
   private static final String[] ms_updateResources =
   {"sys_commCloning/UpdateWorkflowCommunity",
         "sys_commCloning/UpdateContentTypeCommunity",
         "sys_commCloning/UpdateComponentCommunity",
         "sys_commCloning/UpdateVariantCommunity",
         "sys_commCloning/UpdateSiteCommunity"};

   /**
    * Role community relationship update resource name.
    */
   private static final String UPDATE_ROLE_COMMUNITY_RES = "sys_commSupport/updaterolerelation";
}