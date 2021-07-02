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
