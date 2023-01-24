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

import com.percussion.design.objectstore.PSLocator;
import com.percussion.fastforward.managednav.IPSNavigationErrors;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.rxfix.IPSFix;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.services.purge.IPSSqlPurgeHelper;
import com.percussion.services.purge.PSSqlPurgeHelperLocator;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSqlHelper;
import com.percussion.util.PSStringTemplate;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author dougrand
 * 
 * Removes relationships that reference a non-existant content item as the
 * owner or dependent
 */
@Deprecated
public class PSFixNavigation extends PSFixDBBase implements IPSFix
{
   /**
    * This query finds relationship records where the owner or dependent doesn't
    * exist.
    */
   private PSStringTemplate ms_findBrokenNavigation = new PSStringTemplate("INSERT INTO {tablePrefix}.BROKEN_NAV_IDS_TEMP SELECT CONTENTSTATUS.CONTENTID FROM CONTENTSTATUS WHERE CONTENTTYPEID={navon_type_id} AND CONTENTID NOT IN  ( " +
         "SELECT C.CONTENTID " +
         "FROM {tablePrefix}.CONTENTSTATUS C " +
         "INNER JOIN {tablePrefix}.PSX_OBJECTRELATIONSHIP  REL_NAV_SLOT ON C.CONTENTID = REL_NAV_SLOT.DEPENDENT_ID AND REL_NAV_SLOT.SLOT_ID = {nav_slot_id} " +
         "INNER JOIN {tablePrefix}.CONTENTSTATUS OWNER_NAVON_NAVTREE ON REL_NAV_SLOT.OWNER_ID = OWNER_NAVON_NAVTREE.CONTENTID AND (REL_NAV_SLOT.OWNER_REVISION = OWNER_NAVON_NAVTREE.CURRENTREVISION OR REL_NAV_SLOT.OWNER_REVISION=OWNER_NAVON_NAVTREE.TIPREVISION) AND (OWNER_NAVON_NAVTREE.CONTENTTYPEID={navon_type_id} OR OWNER_NAVON_NAVTREE.CONTENTTYPEID={navtree_type_id}) " +
         "INNER JOIN {tablePrefix}.PSX_OBJECTRELATIONSHIP REL_NAV_FOLDER ON C.CONTENTID = REL_NAV_FOLDER.DEPENDENT_ID AND REL_NAV_FOLDER.CONFIG_ID = 3 OR REL_NAV_FOLDER.CONFIG_ID = 8 " +
         "INNER JOIN {tablePrefix}.PSX_OBJECTRELATIONSHIP REL_PARENT_FOLDER ON REL_NAV_FOLDER.OWNER_ID = REL_PARENT_FOLDER.DEPENDENT_ID AND REL_PARENT_FOLDER.CONFIG_ID = 3 OR REL_PARENT_FOLDER.CONFIG_ID = 8 " +
         "INNER JOIN {tablePrefix}.PSX_OBJECTRELATIONSHIP REL_PARENTNAV_FOLDER ON REL_NAV_SLOT.OWNER_ID = REL_PARENTNAV_FOLDER.DEPENDENT_ID AND REL_PARENTNAV_FOLDER.CONFIG_ID = 3 OR REL_PARENTNAV_FOLDER.CONFIG_ID = 8 " +
         "WHERE C.CONTENTTYPEID={navon_type_id} AND REL_PARENTNAV_FOLDER.OWNER_ID = REL_PARENT_FOLDER.OWNER_ID AND REL_NAV_SLOT.OWNER_ID NOT IN (SELECT NAVID FROM {tablePrefix}.BROKEN_NAV_IDS_TEMP) " +
         ") " +
         "AND CONTENTID NOT IN (SELECT NAVID FROM {tablePrefix}.BROKEN_NAV_IDS_TEMP)");
         
         
    private static final Logger ms_log = LogManager.getLogger(PSFixNavigation.class);

   private final String BROKEN_NAV_IDS_TEMP = "BROKEN_NAV_IDS_TEMP";
   private PSStringTemplate ms_createTempTable = new PSStringTemplate("CREATE TABLE {tablePrefix}."+BROKEN_NAV_IDS_TEMP+" (navid int)");
   private PSStringTemplate ms_dropTempTable = new PSStringTemplate("DROP TABLE {tablePrefix}."+BROKEN_NAV_IDS_TEMP);
   private PSStringTemplate ms_getBrokenItems = new PSStringTemplate("SELECT NAVID from {tablePrefix}."+BROKEN_NAV_IDS_TEMP);
   private PSStringTemplate ms_getNavonCount = new PSStringTemplate("SELECT COUNT(*) from {tablePrefix}.CONTENTSTATUS where CONTENTTYPEID={navon_type_id}" );
   
   /**
    * Ctor
    * @throws SQLException 
    * @throws NamingException 
    */
   public PSFixNavigation() throws NamingException, SQLException {
      super();
   }

   @Override
   public void fix(boolean preview)
         throws Exception
   {
      throw new UnsupportedOperationException("Task is currently not supported");
      /*
      super.fix(preview);

      Connection c = PSConnectionHelper.getDbConnection();

      // Identify candidate broken relationship records
      try
      {
         /* fully qualify the table names - we do this by using a fake name,
          * then stripping the fake name off the returned fully qualified name
          * and using that prefix to fixup all table names in the query
          */
        /* String tablePrefix = PSSqlHelper.qualifyTableName("rx");
         tablePrefix = tablePrefix.substring(0, tablePrefix.lastIndexOf('.'));
         m_defDict.put("tablePrefix", tablePrefix);
         
         if (tempTableExists())
         {
            PreparedStatement dropSt = PSPreparedStatement.getPreparedStatement(
                  c,
                  ms_dropTempTable.expand(m_defDict));
            
            dropSt.executeUpdate();
         }
         // create a temporary table
         
         PreparedStatement st =
               PSPreparedStatement.getPreparedStatement(
                  c,
                  ms_createTempTable.expand(m_defDict));
         
         st.executeUpdate();
         
         PSNavConfig navConfig = PSNavConfig.getInstance();
         if(navConfig == null){
            logFailure("-1", "Unable to initialize navigation");
            return;
         }

         List<IPSGuid> navonIds = navConfig.getNavonTypes();
         List<IPSGuid> navtreeIds = navConfig.getNavTreeTypes();

         List<String> submenuRel = navConfig.getSubmenuRelationships();
                  IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
         List<IPSTemplateSlot> slots = asm.findSlotsByNames(submenuRel);
         
         if (slots.isEmpty())
         {
            logFailure("-1", "Cannot find slot "+submenuRel);
            return;
         }
         List<Integer> navonids = new ArrayList<>();
         int navonCount = -1;

         for(IPSTemplateSlot ts : slots) {
            int slot = ts.getGUID().getUUID();

           // m_defDict.put("navon_type_id", navonId);
           // m_defDict.put("navtree_type_id", navtreeId);
            m_defDict.put("nav_slot_id", slot);
            int updated = 1;

            PreparedStatement st1 =
                    PSPreparedStatement.getPreparedStatement(
                            c,
                            ms_getNavonCount.expand(m_defDict));
            try (ResultSet resultSet = st1.executeQuery()) {
               if (resultSet.next())
                  navonCount = resultSet.getInt(1);
            }

            if (navonCount <= 0) {
               PSNavException ne = new PSNavException(
                       IPSNavigationErrors.NAVIGATION_SERVICE_CANNOT_FIND_ANY_NAVONS
               );
               logWarn(null, ne.getLocalizedMessage());
               return;
            }
            PreparedStatement st2 =
                    PSPreparedStatement.getPreparedStatement(
                            c,
                            ms_findBrokenNavigation.expand(m_defDict));

            while (updated > 0) {
               updated = st2.executeUpdate();
               ms_log.info("PSFixNavigation Found " + updated + " navigation items to fix");
            }

            PreparedStatement st3 =
                    PSPreparedStatement.getPreparedStatement(
                            c,
                            ms_getBrokenItems.expand(m_defDict));

            try (ResultSet rs = st3.executeQuery()) {


               while (rs.next()) {
                  int rid = rs.getInt(1);
                  navonids.add(rid);
               }
            }
         }
            if (navonids.isEmpty()) {
               logInfo(null, "There are no broken Navigation items");
            } else if (preview) {

               ms_log.info("PSFixNavigation removing " + navonids.size() + " invalid Navons");
               logPreview(StringUtils.collectionToCommaDelimitedString(navonids), "Would remove " + navonids.size() + " Navons of " + navonCount);

            } else {
               Iterator<Integer> iter = navonids.iterator();
               if (navonids.size() > (navonCount / 2)) {
                  ms_log.error("PSFixNavigation removing " + navonids.size() + " invalid Navons");
                  logInfo(StringUtils.collectionToCommaDelimitedString(navonids), "Failsafe not removing " + navonids + " broken Navons, it is more than half of " + navonCount + "total Navons");
               } else if (!navonids.isEmpty()) {
                  IPSSqlPurgeHelper purgeHelper = PSSqlPurgeHelperLocator.getPurgeHelper();
                  while (iter.hasNext()) {
                     Integer navId = (Integer) iter.next();
                     purgeHelper.purge(new PSLocator(navId));

                  }
                  ms_log.info("PSFixNavigation removing " + navonids.size() + " invalid Navons");
                  logInfo(StringUtils.collectionToCommaDelimitedString(navonids), "Removed " + navonids.size() + " broken Navons of " + navonCount);
               } else {
                  ms_log.info("PSFixNavigation Found no Navons requiring fix");
                  logInfo(null, "No Navons need fixing");
               }
            }

      }catch(Exception e){
         logFailure("-1", "An unexpected exception occurred while attempting to correct Navons. " + e.getLocalizedMessage());
      }
      finally
      {
         if (c!=null && !c.isClosed())
         {
            if (tempTableExists())
            {
               PreparedStatement dropSt = PSPreparedStatement.getPreparedStatement(
                     c,
                     ms_dropTempTable.expand(m_defDict));
               dropSt.executeUpdate();
            }
              
         }
         c.close();
      }

         */
   }

   private boolean tempTableExists() throws SQLException, NamingException
   {
      DatabaseMetaData metadata = PSConnectionHelper.getDbConnection().getMetaData();
      PSConnectionDetail detail = PSConnectionHelper.getConnectionDetail();
      boolean tempTableExists = false;
      try (ResultSet tables = metadata.getTables(detail.getDatabase(), detail.getOrigin(), BROKEN_NAV_IDS_TEMP, null)) {
         if (tables != null)
            tempTableExists = tables.next();
      }
      return tempTableExists;
   }
   
   @Override
   public String getOperation()
   {
      return "Remove broken Navigation Items";
   }
}
