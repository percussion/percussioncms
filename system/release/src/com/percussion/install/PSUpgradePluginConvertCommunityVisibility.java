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

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.datasource.PSDatasourceMgrLocator;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.IPSBackEndRoleMgr;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSRoleMgrLocator;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.IPSConnectionInfo;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionInfo;
import com.percussion.security.IPSTypedPrincipal;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Convert community visibility info from tables to acls.
 */
public class PSUpgradePluginConvertCommunityVisibility
      extends
         PSSpringUpgradePluginBase
{

   /**
    * Db specific information
    */
   private IPSDatasourceManager m_dbm;

   /**
    * Db info for connections
    */
   private IPSConnectionInfo m_info;

   /**
    * The known community ids. Used for cases where we are returning the shown
    * communities from the hidden
    */
   private List m_communityIds = new ArrayList();

   /**
    * A map of community ids to names.
    */
   private Map m_communityIdToName = new HashMap();

   /**
    * The connection details, used to qualify the table name
    */
   private PSConnectionDetail m_details;

   /**
    * Used for logging, initialized in
    * {@link #process(IPSUpgradeModule, Element)}
    */
   private IPSUpgradeModule m_config;
   
   /**
    * How many acls to save after accumulating them across different sources.
    */
   private static final int ACL_BATCH_SIZE = 50;

   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      log("Fixing Community Names");
      fixCommunityNames();

      // Record all community ids
      final List communities = getRoleMgr().findCommunitiesByName(null);
      Iterator citer = communities.iterator();
      while (citer.hasNext())
      {
         PSCommunity community = (PSCommunity) citer.next();
         m_communityIds.add(new Long(community.getId()));
         m_communityIdToName.put(new Long(community.getId()), community
               .getName());
      }

      log("Performing Community Visibility Conversion");

      m_dbm = PSDatasourceMgrLocator.getDatasourceMgr();
      String defaultds = m_dbm.getRepositoryDatasource();
      m_info = new PSConnectionInfo(defaultds);

      try
      {
         m_details = m_dbm.getConnectionDetail(m_info);
      }
      catch (Exception e)
      {
         if (m_config != null)
            e.printStackTrace(m_config.getLogStream());
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, e
               .getLocalizedMessage());
      }

      NodeList elements = elemData.getElementsByTagName("update");
      int count = elements.getLength();
      StringBuffer problems = new StringBuffer();

      IPSAclService asvc = PSAclServiceLocator.getAclService();
      List acls = new ArrayList();

      try
      {
         for (int i = 0; i < count; i++)
         {
            String result = processEntry(elements.item(i), acls);
            if (result != null)
            {
               problems.append(result);
            }
            saveAcls(asvc, acls, ACL_BATCH_SIZE);
         }
         saveAcls(asvc, acls, 0);
      }
      catch(Exception e)
      {
         return new PSPluginResponse(PSPluginResponse.EXCEPTION, 
               e.getLocalizedMessage());
      }
      
      if (problems.length() == 0)
         return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
      return new PSPluginResponse(PSPluginResponse.WARNING, problems.toString());
   }

   /**
    * Save acls if the count is greater than the threshold.
    * @param asvc the acl service, assumed never <code>null</code>.
    * @param acls the list of acls to save, assumed never <code>null</code>.
    * @param limit if the list has at least the number of elements in the 
    * limit, then save.
    * @throws Exception if there's an error saving the acls
    */
   private void saveAcls(IPSAclService asvc, List acls, int limit)
   throws Exception
   {
      if (acls.size() >= limit)
      {
         asvc.saveAcls(acls);
         acls.clear();
      }
   }

   /**
    * Replaces spaces with underscores in community names.
    */
   private void fixCommunityNames()
   {
      final List communities = getRoleMgr().findCommunitiesByName(null);
      final List communitiesToFixNames = findCommunitiesToFixNames(communities);
      final Set communityNames = getCommunityNames(communities);

      for (Iterator i = communitiesToFixNames.iterator(); i.hasNext();)
      {
         final PSCommunity community = (PSCommunity) i.next();
         community.setName(PSNameSpacesUtil.removeWhitespacesFromName(community
               .getName(), communityNames));
         communityNames.add(community.getName());
         getRoleMgr().saveCommunity(community);
      }
   }

   /**
    * Selects communities from the list which need their names to be fixed.
    */
   private List findCommunitiesToFixNames(final List communities)
   {
      final List communitiesToFixNames = new ArrayList();
      for (Iterator i = communities.iterator(); i.hasNext();)
      {
         final PSCommunity community = (PSCommunity) i.next();
         final String name = community.getName();
         if (!name.equals(StringUtils.deleteWhitespace(name)))
         {
            communitiesToFixNames.add(community);
         }
      }
      return communitiesToFixNames;
   }

   /**
    * Set of community names extracted from the provided communities.
    */
   private Set getCommunityNames(final List communities)
   {
      final Set names = new HashSet();
      for (Iterator i = communities.iterator(); i.hasNext();)
      {
         final PSCommunity community = (PSCommunity) i.next();
         names.add(community.getName());
      }
      return names;
   }

   /**
    * Process a single entry from the input document.
    * 
    * @param el the single entry, never <code>null</code>
    * @param acls the list to add the acls to, will be saved by the caller,
    *           assumed never <code>null</code>.
    * @return <code>null</code> if there are no errors, otherwise a
    *         description of the problem(s) found
    * 
    */
   private String processEntry(Node el, List acls)
   {
      if (el == null)
      {
         throw new IllegalArgumentException("el may not be null");
      }
      Element element = (Element) el;
      String type = element.getAttribute("type");
      short ordinal = Short.parseShort(type);

      try
      {    
         Map data = getData(element);

         IPSAclService asvc = PSAclServiceLocator.getAclService();
         Iterator iter = data.keySet().iterator();
         Map guidToAcl = new HashMap();

         log("Beginning community visibility conversion for data");

         while (iter.hasNext())
         {
            Long oid = (Long) iter.next();
            List communities = (List) data.get(oid);
            Iterator citer = communities.iterator();
            while (citer.hasNext())
            {
               Long c = (Long) citer.next();

               String community_name;

               if (c.longValue() > 0)
               {
                  community_name = (String) m_communityIdToName.get(c);
                  if (community_name == null)
                  {
                     log("Warning - community not present for id: " + c);
                     continue;
                  }
               }
               else
               {
                  community_name = PSTypedPrincipal.ANY_COMMUNITY_ENTRY;
               }

               PSGuid guid = new PSGuid();
               guid.setType(ordinal);
               guid.setUUID(oid.intValue());

               IPSAcl object_acl = (IPSAcl) guidToAcl.get(guid);
               if (object_acl == null)
               {
                  object_acl = asvc.loadAclForObject(guid);
                  if (object_acl != null)
                     continue;
                  object_acl = new PSAclImpl("x" + oid, new PSAclImpl()
                        .createDefaultEntry(false));
                  ((PSAclImpl) object_acl).setGUID(PSGuidHelper
                        .generateNext(PSTypeEnum.ACL));
                  guidToAcl.put(guid, object_acl);
                  acls.add(object_acl);
                  object_acl.setObjectId(oid.longValue());
                  ((PSAclImpl) object_acl).setObjectType(ordinal);
               }
               IPSTypedPrincipal owner = object_acl.getFirstOwner();
               // Add community with runtime visible permission
               IPSAclEntry entry = object_acl.createEntry(new PSTypedPrincipal(
                     community_name, PrincipalTypes.COMMUNITY),
                     new PSPermissions[]
                     {PSPermissions.RUNTIME_VISIBLE});
               object_acl.addEntry(owner, entry);
               // Give system entry all permissions
               IPSAclEntry ownerEntry = object_acl.findEntry(owner);
               ownerEntry.addPermission(PSPermissions.READ);
               ownerEntry.addPermission(PSPermissions.UPDATE);
               ownerEntry.addPermission(PSPermissions.DELETE);
            }
         }
         log("Community visiblity conversion complete for data");
         deleteData(element);
      }
      catch (Exception e)
      {
         log(e.getMessage());
         if (m_config != null)
            e.printStackTrace(m_config.getLogStream());
         return e.getLocalizedMessage();
      }

      return null;
   }

   /**
    * Convenience method to access role manager.
    */
   private IPSBackEndRoleMgr getRoleMgr()
   {
      return PSRoleMgrLocator.getBackEndRoleManager();
   }

   /**
    * Get the data from the element
    * 
    * @param element a dom element with the correct information to extract the
    *           data
    * @return the map contains Long ids to Long lists. The key is the object id
    *         and the value is a list of visible community ids
    * @throws SQLException
    * @throws NamingException
    */
   private Map getData(Element element) throws SQLException, NamingException
   {
      String style = element.getAttribute("style");

      if (style != null && style.equals("join"))
      {
         return getJoinData(element);
      }
      return getPropertyData(element);
   }

   /**
    * Deletes the rows from the table that correspond to those described by the
    * supplied element. If the table's style is not <code>property</code>,
    * returns immediately.
    * 
    * @param element a dom element with the correct information to extract the
    *           data, assumed not <code>null</code>.
    * @throws SQLException If any problems working w/ the db.
    * @throws NamingException If we can't lookup the name of the connection.
    */
   private void deleteData(Element element) throws SQLException,
         NamingException
   {
      String style = element.getAttribute("style");

      if (style == null || !style.equals("property"))
      {
         return;
      }

      // Get parameters
      String propertyColumn = element.getAttribute("property-column")
            .toUpperCase();

      // Leave property name value as is, do not change case
      String propertyName = element.getAttribute("property-name");
      String tableName = element.getAttribute("name").toUpperCase();

      String sqlTemplate = "DELETE FROM {0} WHERE {1} = ''{2}''";
      String sql = MessageFormat.format(sqlTemplate, new Object[]
      {
            PSSqlHelper.qualifyTableName(tableName, m_details.getDatabase(),
                  m_details.getOrigin(), m_details.getDriver()),
            propertyColumn, propertyName});

      Connection c = null;

      try
      {
         c = m_dbm.getDbConnection(m_info);
         Statement st = c.createStatement();
         log("Executing delete: " + sql);
         st.execute(sql);
         log("   Deleted " + st.getUpdateCount() + " rows.");
      }
      finally
      {
         if (c != null)
            c.close();
      }
   }

   /**
    * Get the information from a property table
    * <p>
    * Example: &lt;el type="SEARCH" style="property" name="psx_searchproperties"
    * objectid="propertyid" property-column="propertyname"
    * value-column="propertyvalue" property-name="sys_community" /&gt;
    * 
    * @param element the element
    * 
    * @return a list, see {@link #getData(Element)} for details on the return
    *         value
    * @throws SQLException
    * @throws NamingException
    */
   private Map getPropertyData(Element element) throws SQLException,
         NamingException
   {
      // Get parameters
      String objectIdColumn = element.getAttribute("objectid").toUpperCase();
      String propertyColumn = element.getAttribute("property-column")
            .toUpperCase();
      String valueColumn = element.getAttribute("value-column").toUpperCase();

      // Leave property name value as is, do not change case
      String propertyName = element.getAttribute("property-name");
      String tableName = element.getAttribute("name").toUpperCase();
      String hide = element.getAttribute("hide");

      // Create query string
      StringBuffer b = new StringBuffer();
      b.append("SELECT ");
      b.append(objectIdColumn);
      b.append(", ");
      b.append(valueColumn);
      b.append(" FROM ");
      b.append(PSSqlHelper.qualifyTableName(tableName, m_details.getDatabase(),
            m_details.getOrigin(), m_details.getDriver()));
      b.append(" WHERE ");
      b.append(propertyColumn);
      b.append(" = '");
      b.append(propertyName);
      b.append("'");

      return getQueryResults(b, "true".equalsIgnoreCase(hide));
   }

   /**
    * Get the information from a join table
    * <p>
    * Example: &lt;el type="TEMPLATE" style="join" name="rxvariantcommunity"
    * objectid="variantid" community="communityid" /&gt;
    * 
    * @param element the element
    * 
    * @return a map, see {@link #getData(Element)} for details on the return
    *         value
    * @throws SQLException
    * @throws NamingException
    */
   private Map getJoinData(Element element) throws SQLException,
         NamingException
   {
      // Get parameters
      String objectIdColumn = element.getAttribute("objectid").toUpperCase();
      String community = element.getAttribute("community").toUpperCase();
      String tableName = element.getAttribute("name").toUpperCase();

      // Create query string
      StringBuffer b = new StringBuffer();
      b.append("SELECT ");
      b.append(objectIdColumn);
      b.append(",");
      b.append(community);
      b.append(" FROM ");
      b.append(PSSqlHelper.qualifyTableName(tableName, m_details.getDatabase(),
            m_details.getOrigin(), m_details.getDriver()));

      return getQueryResults(b, false);
   }

   /**
    * Do query on the session
    * 
    * @param query
    * @param hide if this is <code>true</code> then we're returning the
    *           communities that are not in the query, i.e. the query returns
    *           the communities that don't have access
    * 
    * @return a list of results, see the hibernate doc for details
    * @throws SQLException
    * @throws NamingException
    */
   private Map getQueryResults(StringBuffer query, boolean hide)
         throws SQLException, NamingException
   {
      Connection c = null;
      Map rval = new HashMap();

      try
      {
         c = m_dbm.getDbConnection(m_info);

         log("Executing query: " + query.toString());

         PreparedStatement st = c.prepareStatement(query.toString());
         ResultSet rs = st.executeQuery();

         while (rs.next())
         {
            Long objectid, communityid;
            objectid = convertToLong(rs.getObject(1));
            communityid = convertToLong(rs.getObject(2));
            List communities = (List) rval.get(objectid);
            if (communities == null)
            {
               communities = new ArrayList();
               rval.put(objectid, communities);
            }
            communities.add(communityid);
         }

         if (hide)
         {
            Iterator eiter = rval.entrySet().iterator();
            while (eiter.hasNext())
            {
               Map.Entry entry = (Entry) eiter.next();
               List communities = (List) entry.getValue();
               // Invert the collections to include those communities that
               // are *not* listed
               List newclist = new ArrayList();
               Iterator iter = m_communityIds.iterator();
               while (iter.hasNext())
               {
                  Long cid = (Long) iter.next();
                  if (!communities.contains(cid))
                  {
                     newclist.add(cid);
                  }
               }
               entry.setValue(newclist);
            }
         }

         log("Query returned " + rval.size() + " different objects");

         return rval;
      }
      finally
      {
         if (c != null)
            c.close();
      }
   }

   /**
    * Convert object from string or other type to long value
    * 
    * @param object
    * @return converted number, never <code>null</code>.
    */
   private Long convertToLong(Object object)
   {
      if (object instanceof String)
      {
         return new Long((String) object);
      }
      else if (object instanceof Number)
      {
         return new Long(((Number) object).longValue());
      }
      else
      {
         throw new RuntimeException("Unknown type found " + object.getClass());
      }
   }

   /**
    * Prints message to the log printstream if it exists or just sends it to
    * System.out
    * 
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private void log(String msg)
   {
      if (msg == null)
      {
         return;
      }

      if (m_config != null)
      {
         m_config.getLogStream().println(msg);
      }
      else
      {
         System.out.println(msg);
      }
   }

}
