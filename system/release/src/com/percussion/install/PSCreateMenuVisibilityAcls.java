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
package com.percussion.install;

import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;

import javax.naming.NamingException;
import java.io.PrintStream;
import java.security.acl.NotOwnerException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This plugin scans all menu actions (RXMENUACTION). For each menu action it
 * checks to see if there is a known ACL by asking the acl service. If there is
 * no acl it creates a simple ACL that adds runtime visibility access to the
 * "AnyCommunity" community.
 * <p>
 * After this runs you can look at the PSX_COMMUNITY_PERMISSION_VIEW for
 * OBJECTTYPE 107 (menu actions). For example:<br>
 * <b>SELECT * FROM PSX_COMMUNITY_PERMISSION_VIEW WHERE OBJECTTYPE = 107</b>
 * <br>
 * Which should yield entries for all communities for most menu actions. There
 * are some actions that will have an existing acl.
 * <p>
 * In the second part, it checks if there are any community entries in
 * RXMENUVISIBILITY table, for each such entry it does the following:
 * <ol>
 * <li>Sees if corresponding entry exists in the ACL for the action</li>
 * <li>If exists does not do anything to the ACL</li>
 * <li>If not, creates an ACL entry with no RunTime Visibiliy permission, means
 * to hide the action for that community</li>
 * </ol>
 * This part essentially ports actions' community visibility to ACLs.
 *  
 * @author dougrand
 */
public class PSCreateMenuVisibilityAcls extends PSSpringUpgradePluginBase
{
   /**
    * ACL service
    */
   private static IPSAclService ms_acl = PSAclServiceLocator.getAclService();

   private IPSUpgradeModule m_config;
   
   /**
    * Map of actionid and communities that the action is hidden for. Initialized
    * in {@link #process(IPSUpgradeModule, Element)}. Never <code>null</code>, may be empty.
    */
   private Map m_actionCommVis = new HashMap();
   
   /**
    * Communties' id name map, initialized in the
    * {@link #process(IPSUpgradeModule, Element)}. Never <code>null</code> or
    * empty (unless a system has no communities at all!).
    */
   private Map m_communityNames = new HashMap();

   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      StringBuilder problems = new StringBuilder();
      PrintStream logger = getLogger();
      
      Connection c = null;
      try
      {
         PSConnectionDetail detail = PSConnectionHelper.getConnectionDetail();
         c = PSConnectionHelper.getDbConnection();
         // initialize the action community visibility contexts
         String tablename = PSSqlHelper.qualifyTableName("RXMENUVISIBILITY", detail
            .getDatabase(), detail.getOrigin(), detail.getDriver());
         PreparedStatement st = c.prepareStatement("SELECT ACTIONID, VALUE FROM "
            + tablename + " WHERE VISIBILITYCONTEXT='2'");
         ResultSet rs = st.executeQuery();
         while (rs.next())
         {
            Integer actionid = new Integer(rs.getInt(1));
            Integer commid = new Integer(rs.getInt(2));
            List comids = (List) m_actionCommVis.get(actionid);
            if(comids == null)
            {
               comids = new ArrayList();
               m_actionCommVis.put(actionid, comids);
            }
            comids.add(commid);
         }
         //
         //Initialize community id-name map.
         List comms = PSRoleMgrLocator.getBackEndRoleManager()
            .findCommunitiesByName(null);
         for (int i=0; i<comms.size(); i++)
         {
            PSCommunity community = (PSCommunity)comms.get(i);
            m_communityNames.put(new Integer(community.getGUID().getUUID()),
                  community.getName());
         }
         //
         
         tablename = PSSqlHelper.qualifyTableName("RXMENUACTION", detail
               .getDatabase(), detail.getOrigin(), detail.getDriver());
         st = c.prepareStatement("SELECT ACTIONID FROM "
               + tablename);
         rs = st.executeQuery();
         while (rs.next())
         {
            int actionid = rs.getInt(1);
            try
            {
               logger.println("Processing actionid " + actionid);
               processAction(actionid);
               logger.println("Processing for actionid " + actionid
                     + " complete");
            }
            catch (Exception e)
            {
               if (problems.length() > 0)
               {
                  problems.append(",");
               }
               problems.append("Acl issue for action " + actionid + ":"
                     + e.getLocalizedMessage());
               e.printStackTrace(logger);
            }
         }
      }
      catch (NamingException e)
      {
         problems.append(e.getLocalizedMessage());
         e.printStackTrace(logger);
      }
      catch (SQLException e)
      {
         problems.append(e.getLocalizedMessage());
         e.printStackTrace(logger);
      }
      finally
      {
         if (c != null)
         {
            try
            {
               c.close();
            }
            catch (SQLException e)
            {
               problems.append(e.getLocalizedMessage());
               e.printStackTrace(logger);
            }
         }
      }

      if (problems.length() == 0)
         return new PSPluginResponse(PSPluginResponse.SUCCESS, "");

      logger.println(problems.toString());
      return new PSPluginResponse(PSPluginResponse.WARNING, problems.toString());
   }

   /**
    * Check if the action has an acl. If not then create a minimal acl.
    * 
    * @param actionid the action id
    * @throws PSSecurityException
    * @throws NotOwnerException
    */
   private void processAction(int actionid) throws PSSecurityException,
         NotOwnerException
   {
      IPSGuid action = new PSGuid(PSTypeEnum.ACTION, actionid);
      IPSAcl acl = ms_acl.loadAclForObjectModifiable(action);
      PrintStream logger = getLogger();
      if (acl == null)
      {
         logger.println("Creating ACL for actionid " + actionid);
         PSTypedPrincipal owner = new PSTypedPrincipal("Admin",
               PrincipalTypes.ROLE);
         PSTypedPrincipal community = new PSTypedPrincipal(
               PSTypedPrincipal.ANY_COMMUNITY_ENTRY, PrincipalTypes.COMMUNITY);
         acl = ms_acl.createAcl(action, owner);
         // Community entry
         IPSAclEntry entry = acl.createEntry(community);
         entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
         acl.addEntry(owner, entry);
         // Owner entry
         entry = acl.findEntry(owner);
         entry.addPermissions(new PSPermissions[]
         {PSPermissions.DELETE, PSPermissions.UPDATE, PSPermissions.READ});
         // Default entry
         entry = acl.createDefaultEntry(false, new PSPermissions[]
         {PSPermissions.READ});
         acl.addEntry(owner, entry);
         // Save
         List<IPSAcl> acls = new ArrayList<>();
         acls.add(acl);
         ms_acl.saveAcls(acls);
         logger.println("ACL created for actionid " + actionid);
      }
      // Translate community permissions from RXMENUVISIBILITY table to ACLs
      if(!m_actionCommVis.isEmpty())
      {
         //only if RXMENUVISIBILITY has entries for community context.
         portCommunityAccess(action, acl);
      }
   }
   
   /**
    * Looks at the community community contexts from the RXMENUVISIBILITY table
    * for the supplied action. If there are any entries in the table but the
    * that community entry is not in the ACL (supplied) then it adds the
    * community entry to the ACL without any permission. Remember that the
    * community entries in the table RXMENUVISIBILITY are menat to hide the
    * action.
    * 
    * @param action the GUID of theaction for which the community visibility
    * needs to be ported, assumed not <code>null</code>.
    * @param acl ACL of the action with supplied action GUID, assumed not
    * <code>null</code>. This ACL may be persisted, so it must be loaded as
    * modifiable.
    * @throws PSSecurityException
    */
   private void portCommunityAccess(IPSGuid action, IPSAcl acl)
      throws PSSecurityException
   {
      getLogger().println(
         "Porting community permisions from RXMENUVISIBILTY "
            + "table for action with actionid=" + action.getUUID());
      List comids = (List) m_actionCommVis.get(new Integer(action.getUUID()));
      if (comids == null || comids.isEmpty())
         return;
      for (int i=0; i<comids.size(); i++)
      {
         Integer comid = (Integer) comids.get(i);
         String comName = (String) m_communityNames.get(comid);
         if (StringUtils.isEmpty(comName))
         {
            getLogger().println(
               "Community does exist with communityid = " + comid);
            continue;
         }
         PSTypedPrincipal community = new PSTypedPrincipal(comName,
            PrincipalTypes.COMMUNITY);
         if (acl.findEntry(community) == null)
         {
            IPSAclEntry entry = acl.createEntry(community);
            try
            {
               acl.addEntry(acl.getFirstOwner(), entry);
            }
            catch (NotOwnerException e)
            {
               getLogger().println(
                  "Unexpected error adding community to ACL "
                     + e.getLocalizedMessage());
            }
         }
      }
      // Save
      List acls = new ArrayList();
      acls.add(acl);
      ms_acl.saveAcls(acls);
      getLogger().println("Porting community done");
   }

   /**
    * Gets the logstream to the current log file.  If configuration is
    * <code>null</code>, System.out is returned.
    */
   private PrintStream getLogger()
   {
      if (m_config != null)
         return m_config.getLogStream();
      else
         return System.out;
   }
}
