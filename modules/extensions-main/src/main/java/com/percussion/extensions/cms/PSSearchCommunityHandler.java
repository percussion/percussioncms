/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.extensions.cms;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Walk the search properties in the request document. For sys_community, remove
 * the property and create an appropriate acl using the service.
 * <p>
 * There are three cases:
 * <ul>
 * <li>The workbench is saving a search. In this case the sys_community 
 * properties have already been removed and the acl has been processed in web
 * services. This exit should not modify the acls at all. This is detected by
 * noticing that the "new" acl hasn't been calculated since it is calculated
 * on the first property found.
 * <li>The CX (or workbench) is deleting a search. In this case an existing
 * acl can be deleted safely since the associated object is being removed. This
 * will replicate work from the web services, but shouldn't cause a problem
 * <li>The CX is saving a search. In this case there will be no sys_community
 * properties, and no new acl will be calculated. No changes should be made to
 * the acls.
 * </ul>
 * 
 * @author dougrand
 * 
 */
public class PSSearchCommunityHandler implements IPSRequestPreProcessor
{
   protected static Logger ms_log = LogManager.getLogger(PSSearchCommunityHandler.class);

   @SuppressWarnings("unused")
   public void preProcessRequest(Object[] params, IPSRequestContext request)
   {
      IPSBackEndRoleMgr rmgr = PSRoleMgrLocator.getBackEndRoleManager();
      Document doc = request.getInputDocument();
      if (doc == null) return;
      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
      String searchId = walker.getElementData("PSXKey/SEARCHID");
      IPSGuid searchGid = new PSGuid(PSTypeEnum.SEARCH_DEF, searchId);
      boolean deleting = false;

      IPSAclService asvc = PSAclServiceLocator.getAclService();

      NodeList actions = doc.getElementsByTagName("Action");
      if (actions.getLength() > 0)
      {
         Element action = (Element) actions.item(0);
         if ("DELETE".equals(action.getAttribute("dbAction")))
         {
            deleting = true;
         }
      }
      
      NodeList properties = doc.getElementsByTagName("PSXSProperty");
      int len = properties.getLength();

      PSAclImpl object_acl = null;

      List<Element> toremove = new ArrayList<>();

      for (int i = 0; i < len; i++)
      {
         Element property = (Element) properties.item(i);
         walker = new PSXmlTreeWalker(property);
         String name = walker.getElementData("PSXKey/PROPERTYNAME");
         String value = walker.getElementData("PSXKey/PROPERTYVALUE");
         if (name.equals("sys_community"))
         {
            String state = property.getAttribute("state");
            if (!state.equals("db_marked_for_deletion"))
            {
               try
               {
                  String community_name = null;
                  if (!value.equals("-1"))
                  {
                     PSCommunity comm = rmgr.loadCommunity(new PSGuid(
                           PSTypeEnum.COMMUNITY_DEF, value));
                     community_name = comm.getName();
                  }
                  else
                  {
                     community_name = "AnyCommunity";
                  }
                  if (object_acl == null)
                  {
                     object_acl = new PSAclImpl("x" + searchId, new PSAclImpl()
                           .createDefaultEntry(false));
                     object_acl.setGUID(PSGuidHelper
                           .generateNext(PSTypeEnum.ACL));
                     object_acl.setObjectId(Long.parseLong(searchId));
                     object_acl.setObjectType(PSTypeEnum.SEARCH_DEF
                           .getOrdinal());
                  }
                  IPSTypedPrincipal community = new PSTypedPrincipal(
                        community_name, PrincipalTypes.COMMUNITY);
                  IPSAclEntry e = object_acl.createEntry(community,
                        new PSPermissions[]
                        {PSPermissions.READ, PSPermissions.RUNTIME_VISIBLE});
                  object_acl.addEntry(object_acl.getFirstOwner(), e);
               }
               catch (PSSecurityException se)
               {
                  ms_log.debug(se);
               }
               catch (Exception e1)
               {
                  ms_log.error(e1);
               }
            }
            toremove.add(property);
         }
      }

      // Remove community property nodes
      for (Element e : toremove)
      {
         e.getParentNode().removeChild(e);
      }

      try
      {
         PSAclImpl existing_acl = (PSAclImpl) asvc.loadAclForObject(searchGid);

         if ((object_acl != null || deleting) && existing_acl != null)
         {
            asvc.deleteAcl(existing_acl.getGUID());
         }
         
         if (object_acl != null)
         {
            List<IPSAcl> acls = new ArrayList<>();
            acls.add(object_acl);
            asvc.saveAcls(acls);
         }
      }
      catch (PSSecurityException e1)
      {
         ms_log.error(e1);
      }

   }

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
   {
      // TODO Auto-generated method stub

   }

}
