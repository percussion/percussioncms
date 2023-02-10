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
package com.percussion.rxfix.dbfixes;

import com.percussion.cms.objectstore.PSSearch;
import com.percussion.rxfix.IPSFix;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.ui.IPSUiWs;
import com.percussion.webservices.ui.PSUiWsLocator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

/**
 * This class is used to clean up the ACLs for View objects. Some of the View 
 * objects may have 2 types of ACLs after upgrading from 5.7 to 6.5.2, one type 
 * is {@link PSTypeEnum#SEARCH_DEF}, another is {@link PSTypeEnum#VIEW_DEF}.
 * <p>
 * This clean up process will remove the ACLs with {@link PSTypeEnum#SEARCH_DEF} 
 * type for all View objects.
 * .
 */
public class PSFixCommunityVisibilityForViews extends PSFixDBBase implements
      IPSFix
{
   /**
    * Ctor
    * @throws SQLException 
    * @throws NamingException 
    */
   public PSFixCommunityVisibilityForViews()
      throws NamingException, SQLException
   {
   }

   @Override
   public void fix(boolean preview)
         throws Exception
   {
      super.fix(preview);
    
      // don't run this if not inside server
      if ((!PSServer.isInitialized()))
      {
         return;
      }
      
      /**
       * Get all view's IDs and convert them to search IDs (same ID, but 
       * {@link PSTypeEnum#SEARCH_DEF} type.
       */
      IPSUiWs uiSrv = PSUiWsLocator.getUiWebservice();
      List<PSSearch> views = uiSrv.loadViews(null);
      List<IPSGuid> viewIds = new ArrayList<IPSGuid>();
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      
      for (PSSearch v : views)
      {
         IPSGuid id = mgr.makeGuid(v.getGUID().getUUID(),
               PSTypeEnum.SEARCH_DEF);
         viewIds.add(id);
      }
      
      // Get all ACLs for the IDs
      IPSAclService aclSrv = PSAclServiceLocator.getAclService();
      List<IPSAcl> tmpAcls = aclSrv.loadAclsForObjects(viewIds);
      List<IPSAcl> acls = new ArrayList<IPSAcl>();
      for (IPSAcl acl : tmpAcls)
      {
         if (acl != null)
            acls.add(acl);
      }

      // do the actual clean up.
      if (!preview)
      {
         for (IPSAcl acl : acls)
         {
            aclSrv.deleteAcl(acl.getGUID());
         }
      }

      if (acls.size() == 0)
      {
         logInfo(null, "No problems found");
      }
      else
      {
         logInfo(null, (preview ? "Would remove " : "Removed ")
               + acls.size() + " invalid ACLs for View objects");
      }
   }

   @Override
   public String getOperation()
   {
      return "Fix ACLs for Views";
   }
}
