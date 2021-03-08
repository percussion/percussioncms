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
package com.percussion.fastforward.managednav;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSAaRelationshipList;
import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeSet;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.macro.PSMacroUtils;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSSQLStatement;
import com.percussion.util.PSSqlHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * NavSlotContents is a cache of the relationships for all items in the Navon
 * and Navtree content types. This class is rooted as a single instance in the
 * NavConfig. It is built at initialization time (or whenever a NavReset occurs.
 * <p>
 * There is a separate list of each AuthType, although the current implements
 * treats all AuthTypes > 1 the same as AuthType = 1.
 * <p>
 * This implementation also limits Navons to the content valid states 'I' and
 * 'Y'. This effectively means that Navons and NavImages cannot be in "Staging"
 * states, although the landing pages can.
 * <p>
 *
 * @author DavidBenua
 *
 */
public class PSNavSlotContents
{
   /**
    * Construct a slot contents for a specific authType.
    *
    * @param req the parent request context.
    * @param authType the authtype. Must not be <code>null</code>
    * @throws PSNavException
    */
   public PSNavSlotContents(IPSRequestContext req, Integer authType)
         throws PSNavException
   {
      m_config = PSNavConfig.getInstance(req);
      m_authType = authType;
      log.debug("building content valid flag string for authtype: {}", m_authType);
      m_authTypeValidFlags = getContentValidFlagsForAuthtype(m_authType, false);
      m_authTypeValidFlagsNonPublic = getContentValidFlagsForAuthtype(
            m_authType, true);
      addStandardSlots();
      log.debug("loading relationships");
      loadMaps(req);
      log.debug("relationships loaded");
   }

   /**
    * Get the slot relationships for a specified owner id and slot.
    *
    * @param parent the item that owns these relationships.
    * @param slotId the slot id.
    * @return a list of relationship objects. May be <code>
    * empty</code> but
    *         never <code>null</code>.
    */
   public PSAaRelationshipList getSlotContents(PSLocator parent, int slotId)
   {
      Integer item = new Integer(parent.getId());
      Map<Integer, PSAaRelationshipList> slotMap = m_itemMap.get(item);
      if (slotMap == null)
      { //no relationships for this parent
         log.debug("no relationships for this parent {}", parent.getPart(PSLocator.KEY_ID));
         return new PSAaRelationshipList();
      }
      Integer slot = new Integer(slotId);
      PSAaRelationshipList relList = slotMap.get(slot);
      if (relList == null)
      { //no relationships for this slot
         log.debug("No relationships for this slot {}", slot);
         return new PSAaRelationshipList();
      }
      return relList;
   }

   /**
    * Loads the standard slots into the slot map. These slots are specified in
    * Navigation.properties. This method reads them from the current NavConfig.
    */
   private void addStandardSlots()
   {
      PSSlotType submenu = m_config.getMenuSlot();
      this.addSlot(submenu);
      PSSlotType imageslot = m_config.getImageSlot();
      this.addSlot(imageslot);
      String lpSlotName = m_config
            .getPropertyString(PSNavConfig.NAVON_LANDING_SLOT);
      PSSlotType lpSlot = m_config.getAllSlots().getSlotTypeByName(lpSlotName);
      this.addSlot(lpSlot);
   }

   /**
    * Add a slot to the slot map. The map is indexed by slotid.
    *
    * @param slot the slot to add.
    */
   public void addSlot(PSSlotType slot)
   {
      log.debug("adding slot {}", slot.getSlotName());
      Integer slotId = new Integer(slot.getSlotId());
      m_slotMap.put(slotId, slot);

   }

   /**
    * Builds the SQL IN clause for selecting all relationships in the required
    * slots.
    *
    * @return the SQL IN clause.
    */
   private String buildSlotInList()
   {
      StringBuffer sb = new StringBuffer();
      sb.append("(");
      Iterator it = m_slotMap.keySet().iterator();
      boolean first = true;
      while (it.hasNext())
      {
         Integer slotId = (Integer) it.next();
         if (!first)
         {
            sb.append(",");
         }
         sb.append(slotId);
         first = false;
      }
      sb.append(")");
      log.debug("Slot IN list {}", sb.toString());
      return sb.toString();
   }

   /**
    * Loads the maps from the database. Each map consists of a map of
    * PSAaRelationshipList objects representing the contents of each slot on
    * each content item.
    * <p>
    * All relationship objects built by this class use the standard Active
    * Assembly relationship, although this relationship is never used. The rest
    * of the managed nav subsystem relies on the content ids only, not the
    * relationship or its properties.
    * <p>
    *
    * @param req the parent request context.
    * @throws PSNavException
    */
   private synchronized void loadMaps(IPSRequestContext req)
         throws PSNavException
   {
      m_config = PSNavConfig.getInstance(req);
      PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
      PSContentTypeVariantSet allVariants = m_config.getAllVariants();
      PSSlotTypeSet allSlots = m_config.getAllSlots();

      PSRelationshipConfig relConfig;

      /*
       * Build a dummy relationship config. We need this to construct a
       * PSRelationship. We will never need to actually use this config for
       * anything.
       */
      try
      {
         relConfig = relProxy
               .getConfig(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
      }
      catch (PSCmsException e)
      {
         log.error("Invalid Relationship Config");
         throw new PSNavException(e);
      }
      List<Integer[]> resultSet = getResultSet();
      //If it is not preview get the adjusted list
      if (m_authType.intValue() != 0)
      {
         resultSet = adjustResultSetForLastPublicRevision(resultSet);
      }
      Iterator it1 = resultSet.iterator();
      Integer lastContentId = null;
      Map<Integer, PSAaRelationshipList> currentSlotMap = null;
      Integer lastSlotId = null;
      PSAaRelationshipList aaList = null;
      PSSlotType slot = null;
      while (it1.hasNext())
      {
         Integer[] currentRow = (Integer[]) it1.next();
         Integer currentContentId = currentRow[SQL_CONTENTID];
         log.debug(" Content id {}", currentContentId);
         //FB: RC_REF_COMPARISON NC 1-17-16
         if (lastContentId == null || !lastContentId.equals(currentContentId))
         {
            lastContentId = currentRow[SQL_CONTENTID];
            currentSlotMap = buildItemMap(lastContentId);
            lastSlotId = null;
         }
         Integer currentSlotId = currentRow[SQL_SLOTID];
         log.debug(" Slot Id {}", currentSlotId);
         //B: RC_REF_COMPARISON NC 1-17-16 
         if (lastSlotId == null || !lastSlotId.equals(currentSlotId))
         {
            lastSlotId = currentSlotId;
            aaList = buildSlotRelations(currentSlotMap, currentSlotId);
            slot = allSlots.getSlotTypeById(lastSlotId.intValue());
            if (slot == null)
            {
               throw new PSNavException("Unknown Slot " + lastSlotId);
            }
         }
         Integer sysId = currentRow[SQL_SYSID];
         log.debug(" System id {}", sysId);
         Integer revision = currentRow[SQL_REVISIONID];
         log.debug(" Revision {}", revision);
         Integer dependentId = currentRow[SQL_ITEMCONTENTID];
         log.debug(" Dependent Id {}", dependentId);
         PSLocator owner = new PSLocator(currentContentId.intValue(), revision
               .intValue());
         PSLocator dependent = new PSLocator(dependentId.intValue());
         PSRelationship relation = new PSRelationship(sysId.intValue(), owner,
               dependent, relConfig);
         Integer vart = currentRow[SQL_VARIANTID];
         log.debug("adding variant id {}", vart);
         PSContentTypeVariant variant = allVariants.getContentVariantById(vart
               .intValue());
         if (variant == null)
         {
            String msg = "Unknown variant " + vart;
            log.error(msg);
            throw new PSNavException(msg);
         }
         PSAaRelationship aaRel = new PSAaRelationship(relation, slot,
               variant);
         Integer folderidId = currentRow[SQL_FOLDERID];
         if (folderidId != null && folderidId.toString().trim().length() > 0
               && folderidId.intValue() != 0)
            aaRel.setProperty(IPSHtmlParameters.SYS_FOLDERID, folderidId
                  .toString().trim());
         aaList.add(aaRel);
      }
   }

   /**
    * Iterates through the bulk result set and checks whether the item exists in
    * the publishable but non public state. If exists in that list then replaces
    * its related content with the last public revision related content.
    * 
    * @param resultSet List of all publishable related content.
    * @return Adjusted list of related content for the last public revision. May
    *         be empty but never <code>null</code>.
    * @throws NumberFormatException
    * @throws PSNavException
    */
   private List<Integer[]> adjustResultSetForLastPublicRevision(
            List<Integer[]> resultSet) throws NumberFormatException,
            PSNavException
   {
      List<Integer[]> newList = new ArrayList<>();
      Iterator it1 = resultSet.iterator();
      List<Integer> qeIds = getQuickEditNavons();
      List<Integer> processedIds = new ArrayList<>();
      while (it1.hasNext())
      {
         Integer[] currentRow = (Integer[]) it1.next();
         Integer currentContentId = currentRow[SQL_CONTENTID];
         if (processedIds.contains(currentContentId))
         {
            continue;
         }
         if (!qeIds.contains(currentContentId))
         {
            newList.add(currentRow);
         }
         else
         {
            String lprevision = PSMacroUtils.getLastPublicRevision(
                  currentContentId.toString());
            List<Integer[]> newResults = getRelatedContentData(currentContentId
                  .intValue(), Integer.parseInt(lprevision));
            newList.addAll(newResults);
            processedIds.add(currentContentId);
         }
      }
      return newList;
   }
   
   /**
    * Adds an empty map for the given content id if one does not exist. The map
    * is keyed by content id (as an Integer object).
    *
    * @param contentId the content id to add.
    * 
    * @return the value of the specified content-id. It may be an empty map
    *    if the given content id does not exist.
    */
   private Map<Integer, PSAaRelationshipList> buildItemMap(Integer contentId)
   {
      Map<Integer, PSAaRelationshipList> currentItem = m_itemMap.get(contentId);
      if (currentItem == null)
      {
         currentItem = new HashMap<>();
         m_itemMap.put(contentId, currentItem);
      }
      return currentItem;
   }

   /**
    * Find the slot relationship list for a given slot id. If one does not
    * exist, build a new one.
    *
    * @param slotMap the slot map for this parent item.
    * @param slotId the slot id to find
    * @return the relationship list for a given slot. If one does not exist, a
    *         new empty slot relationship list will be returned.
    */
   private PSAaRelationshipList buildSlotRelations(
      Map<Integer, PSAaRelationshipList> slotMap, Integer slotId)
   {
      PSAaRelationshipList aaList = slotMap.get(slotId);
      if (aaList == null)
      {
         aaList = new PSAaRelationshipList();
         slotMap.put(slotId, aaList);
      }
      return aaList;
   }

   /**
    * Gets the set of all relationships where the parent is a Navon or NavTree
    * and the slot id is a recognized Nav slot. If the <code>authType</code>
    * is not 0, only items which are in public or ignore states will be
    * returned.
    *
    * @return a list of <code>Integer[]</code>. Never <code>null</code> but
    *         may be <code>empty</code>
    * @throws PSNavException
    */
   private List<Integer[]> getResultSet() throws PSNavException
   {
      Connection conn = PSNavSQLUtils.connect();
      List<Integer[]> results = new ArrayList<>();
      Statement stmt = null;
      ResultSet rs = null;

      try
      {
         String[] sqlParams = new String[2];
         sqlParams[0] = buildSlotInList();
         sqlParams[1] = m_authTypeValidFlags;

         String pattern = getSqlPattern(m_authType.intValue());
         String SqlStatement = MessageFormat.format(pattern,
                  (Object[]) sqlParams);

         log.debug("SQL Statement: {}", SqlStatement);

         stmt = PSSQLStatement.getStatement(conn);
         rs = stmt.executeQuery(SqlStatement);
         log.debug("loading rows...");
         boolean valid = rs.next();
         while (valid)
         {
            Integer[] resArray = new Integer[SQL_MAX_COL];
            for (int i = 1; i <= SQL_MAX_COL; i++)
            {
               resArray[i - 1] = new Integer(rs.getInt(i));
               //log.debug(" Column " + i + " value " + resArray[i-1]);
            }
            results.add(resArray);
            valid = rs.next();
            //log.debug("next row");
         }
         log.debug("finished loading rows");
      }
      catch (SQLException e)
      {
         log.error("SQL Error {}", e.getMessage());
         log.debug(e.getMessage(),e);
         throw new PSNavException(e);
      }
      finally
      {
         PSNavSQLUtils.closeout(conn, stmt, rs);
      }
      return results;
   }

   /**
    * Gets a list Integers of contentids of managed nav content types, that are
    * publishable but not in public state.
    * 
    * @return a list of <code>Integer[]</code>. Never <code>null</code> but
    *         may be <code>empty</code>
    * @throws PSNavException
    */
   private List<Integer> getQuickEditNavons() throws PSNavException
   {
      Connection conn = PSNavSQLUtils.connect();
      List<Integer> results = new ArrayList<>();
      Statement stmt = null;
      ResultSet rs = null;
      try
      {
         stmt = PSSQLStatement.getStatement(conn);
         String[] sqlParams = new String[2];
         sqlParams[0] = m_authTypeValidFlagsNonPublic;
         sqlParams[1] = "(" + m_config.getNavonType() + ","
               + m_config.getNavTreeType() + ")";

         String SqlStatement = SQL_NONPUBLIC_PUBLISHABLE_NAVONS_START
               + getQualifiedTables(SQL_NONPUBLIC_PUBLISHABLE_NAVONS_TABLES)
               + MessageFormat.format(SQL_NONPUBLIC_PUBLISHABLE_NAVONS_END,
                     (Object[])sqlParams);

         rs = stmt.executeQuery(SqlStatement);
         boolean valid = rs.next();
         while (valid)
         {
            results.add(new Integer(rs.getInt(1)));
            valid = rs.next();
         }
      }
      catch (SQLException e)
      {
         log.error("SQL Error {}", e.getMessage());
         log.debug(e.getMessage(),e);
         throw new PSNavException(e);
      }
      finally
      {
         PSNavSQLUtils.closeout(conn, stmt, rs);
      }
      return results;
   }

   /**
    * Gets the related content data for the given content id and revision.
    * 
    * @param contentID Id of the content item.
    * @param revision of the content item.
    * @return a list of <code>Integer[]</code>. Never <code>null</code> but
    *         may be <code>empty</code>
    * @throws PSNavException
    */
   private List<Integer[]> getRelatedContentData(int contentID, int revision)
         throws PSNavException
   {
      List<Integer[]> results = new ArrayList<>();
      Connection conn = PSNavSQLUtils.connect();
      String[] sqlParams = new String[2];
      PreparedStatement stmt = null;
      ResultSet rs = null;
      try
      {
         sqlParams[0] = buildSlotInList();
         sqlParams[1] = m_authTypeValidFlags;

         String SqlStatement = SQL_RELATIONSHIPS_START
               + getQualifiedTables(SQL_RELATIONSHIPS_TABLES)
               + MessageFormat.format(SQL_RELATIONSHIPS_END, (Object[])sqlParams);
         stmt = PSPreparedStatement.getPreparedStatement(conn, SqlStatement);
         stmt.setInt(1, contentID);
         stmt.setInt(2, revision);
         rs = stmt.executeQuery();
         log.debug("loading rows...");
         boolean valid = rs.next();
         while (valid)
         {
            Integer[] resArray = new Integer[SQL_MAX_COL];
            for (int i = 1; i <= SQL_MAX_COL; i++)
            {
               resArray[i - 1] = new Integer(rs.getInt(i));
               //log.debug(" Column " + i + " value " + resArray[i-1]);
            }
            results.add(resArray);
            valid = rs.next();
            //log.debug("next row");
         }
         log.debug("finished loading rows");
         
      }
      catch (SQLException e)
      {
         log.error("SQL Error {}", e.getMessage());
         throw new PSNavException(e);
      }
      finally
      {
         PSNavSQLUtils.closeout(conn, stmt, rs);
      }
      return results;
   }   

   /**
    * Get the content valid string in the format ('i','I','y','Y') suitable to
    * be a SQL parameter. Reads the property named authtype.XXX.validFlags from
    * the nav config file. If found, builds the string in the above syntax else
    * returns the default one {@link #DEFAULT_CONTENT_VALID_FLAGS}.
    * 
    * @param authType the authtype value, assumed not <code>null</code>.
    * @param nonPublic skips flag 'y' if it is <code>true</code>.
    * @return content valid string as described above, never <code>null</code>
    * or empty.
    */
   private String getContentValidFlagsForAuthtype(Integer authType,
         boolean nonPublic)
   {
      String validFlagsPropName = "authtype." + authType.toString()
         + ".validFlags";
      String validFlags = m_config.getPropertyString(validFlagsPropName);
      if (validFlags != null && validFlags.length() > 0)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Content valid flag string for authtype {} is specified as: {}", authType, validFlags);
         }
         String temp = validFlags;
         String[] flags = temp.split(",");
         validFlags = "";
         for (int i = 0; i < flags.length; i++)
         {
            temp = flags[i].trim();
            if (temp.length() == 1)
            {
               //Skip flag y if it is for non public auth type flags.
               if(nonPublic && temp.equalsIgnoreCase("y"))
                  continue;
               //add one flag in lower and one in upper case
               validFlags = validFlags + "'" + temp.toLowerCase() + "','"
                  + temp.toUpperCase() + "',";
            }
         }
         //Strip off the trailing ,
         if (validFlags.endsWith(","))
            validFlags = validFlags.substring(0, validFlags.length() - 1);
         if (validFlags.length() < 1)
         {
            log.warn("Content valid flags specified appears "
               + "to be invalid. Using the default");
            validFlags = DEFAULT_CONTENT_VALID_FLAGS;
         }
         else
            validFlags = "(" + validFlags + ")";
      }
      else
      {
         if (log.isDebugEnabled())
         {
            log.debug("Content valid flag string for authtype {} is not specified and is using the default", authType);
         }
         if(nonPublic)
            validFlags = DEFAULT_CONTENT_VALID_FLAGS_NONPUBLIC;
         else
            validFlags = DEFAULT_CONTENT_VALID_FLAGS;
      }
      if (log.isDebugEnabled())
      {
         log.debug("Content valid flag string for authtype {} is resolved to ", authType, validFlags);
      }
      return validFlags;
   }

   /**
    * Gets the pattern to use to generate the correct sql statement for the
    * given authtype.
    *
    * @param authType The authtype to use, must be either 0 or 1.
    *
    * @return The sql pattern, never <code>null</code> or empty.
    *
    * @todo Update for custom authtypes.
    */
   private String getSqlPattern(int authType)
   {
      String pattern = SQL_START;
      switch (authType)
      {
         case 0 :
            pattern += (getQualifiedTables(SQL_TABLES_0) + SQL_END_0);
            break;
         default:
            pattern += (getQualifiedTables(SQL_TABLES_1) + SQL_END_1);
      }

      return pattern;
   }

   /**
    * Creates a string of qualified table names suitable for use as the "FROM"
    * clause of a SQL statement.
    *
    * @param tables Two dimensional array of table names and aliases. Each entry
    *           is a String[2] of tablename and alias. Assume to contain at
    *           least one entry and to have non- <code>null</code>, non-empty
    *           values in all array positions.
    *
    * @return The tables string in the form "table alias[, table alias]", never
    *         <code>null</code> or empty.
    */
   private String getQualifiedTables(String[][] tables)
   {
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < tables.length; i++)
      {
         if (i > 0)
            buf.append(", ");
         buf.append(PSNavSQLUtils.qualifyTableName(tables[i][0]));
         buf.append(" ");
         buf.append(tables[i][1]);
      }

      return buf.toString();
   }

   /**
    * Confiuration instance for local access to config information.
    */
   private PSNavConfig m_config = PSNavConfig.getInstance();

   /**
    * Logger for this class.
    */
   private static final Logger log = LogManager.getLogger(PSNavSlotContents.class);

   /**
    * Authtype for this instance. Defaults to 0.
    */
   private Integer m_authType;

   /**
    * Map of all Navon and NavTree items by content id.
    */
   private Map<Integer, Map<Integer, PSAaRelationshipList>> m_itemMap = 
      new HashMap<>();

   /**
    * Map of all navon slots by slotid.
    */
   private Map<Integer, PSSlotType> m_slotMap = new HashMap<>();
   
   /**
    * Authtype valid string for the authtype of this object. Initialized in the
    * ctor. Never <code>null</code> after that.
    */
   private String m_authTypeValidFlags = null;

   /**
    * Authtype valid string for the authtype of this object that are publishable
    * but non public. Initialized in the ctor. Never <code>null</code> after
    * that.
    */
   private String m_authTypeValidFlagsNonPublic = null;

   /**
    * The starting portion of the select statement to use up to and including
    * the "from" keyword followed by a space
    */
   private static final String SQL_START = "SELECT rxc.RID, "
         + "rxc.OWNER_ID, rxc.OWNER_REVISION, rxc.DEPENDENT_ID, rxc.SLOT_ID, "
         + "rxc.VARIANT_ID, rxc.SORT_RANK, rxc.FOLDER_ID FROM ";

   private static final String[][] SQL_TABLES_0 =
   {
   {IPSConstants.PSX_RELATIONSHIPS, "rxc"},
   {"CONTENTSTATUS", "cs"}};

   private static final String[][] SQL_TABLES_1 =
   {
   {IPSConstants.PSX_RELATIONSHIPS, "rxc"},
   {"CONTENTSTATUS", "cs"},
   {"CONTENTSTATUS", "cs2"},
   {"STATES", "sts"},
   {"STATES", "sts2"}};

   private static final String SQL_END_0 =
           " WHERE cs.CONTENTID = rxc.OWNER_ID AND "
         + "cs.CURRENTREVISION = rxc.OWNER_REVISION AND "
         + "rxc.SLOT_ID IN {0} "
         + "ORDER BY rxc.OWNER_ID, rxc.SLOT_ID, rxc.SORT_RANK";

   private static final String SQL_END_1 =
           " WHERE cs.CONTENTID = rxc.DEPENDENT_ID AND "
         + "cs.CONTENTSTATEID = sts.STATEID AND " 
         + "cs.WORKFLOWAPPID = sts.WORKFLOWAPPID AND "
         + "cs2.CONTENTSTATEID = sts2.STATEID AND "
         + "cs2.WORKFLOWAPPID = sts2.WORKFLOWAPPID AND "
         + "cs2.CONTENTID = rxc.OWNER_ID AND "
         + "cs2.CURRENTREVISION = rxc.OWNER_REVISION AND "
         + "sts.CONTENTVALID IN {1} AND "
         + "sts2.CONTENTVALID IN {1} AND "
         + "rxc.SLOT_ID IN {0} "
         + "ORDER BY rxc.OWNER_ID, rxc.SLOT_ID, rxc.SORT_RANK";

   /**
    * SQL statement for getting the related content by content id and revision
    * id.
    */
   private static final String SQL_RELATIONSHIPS_START = "SELECT rxc.RID, "
      + "rxc.OWNER_ID, rxc.OWNER_REVISION, rxc.DEPENDENT_ID, rxc.SLOT_ID, "
      + "rxc.VARIANT_ID, rxc.SORT_RANK, rxc.FOLDER_ID FROM ";

   private static final String[][] SQL_RELATIONSHIPS_TABLES =
   {
      {IPSConstants.PSX_RELATIONSHIPS, "rxc"},
      {"CONTENTSTATUS", "cs"},
      {"STATES", "sts"}};

   private static final String SQL_RELATIONSHIPS_END = 
        " WHERE cs.CONTENTID = rxc.DEPENDENT_ID AND "
      + "cs.CONTENTSTATEID = sts.STATEID AND "
      + "cs.WORKFLOWAPPID = sts.WORKFLOWAPPID AND "
      + "rxc.OWNER_ID = ? AND "
      + "rxc.OWNER_REVISION = ? AND "
      + "sts.CONTENTVALID IN {1} AND "
      + "rxc.SLOT_ID IN {0} "
      + "ORDER BY rxc.OWNER_ID, rxc.SLOT_ID, rxc.SORT_RANK";


   /**
    * SQL Statement for getting the nav content items that are in publishable
    * state but not in public state.
    */
   private static final String SQL_NONPUBLIC_PUBLISHABLE_NAVONS_START = 
      "SELECT cs.CONTENTID FROM ";

   private static final String[][] SQL_NONPUBLIC_PUBLISHABLE_NAVONS_TABLES =
   {
      {"CONTENTSTATUS", "cs"},
      {"STATES", "st"}
   };
   private static final String SQL_NONPUBLIC_PUBLISHABLE_NAVONS_END = 
      " WHERE cs.CONTENTSTATEID=st.STATEID AND "
      + "cs.WORKFLOWAPPID=st.WORKFLOWAPPID AND st.CONTENTVALID IN {0} "
      + "AND CONTENTTYPEID IN {1}";

   /**
    * Maximum number of columns in the SQL Query. Columns in the Array are
    * numbed from 0, not 1.
    */
   private static final int SQL_MAX_COL = 8;

   /**
    * Column number for the System ID column.
    */
   private static final int SQL_SYSID = 0;

   /**
    * Column number for the Content ID column.
    */
   private static final int SQL_CONTENTID = 1;

   /**
    * Column number for the Revision ID column.
    */
   private static final int SQL_REVISIONID = 2;

   /**
    * Column number for the child Content Item ID column.
    */
   private static final int SQL_ITEMCONTENTID = 3;

   /**
    * Column number for the Slot ID column.
    */
   private static final int SQL_SLOTID = 4;

   /**
    * Column number for the Variant ID column.
    */
   private static final int SQL_VARIANTID = 5;

   /**
    * Column number for the Sort Rank column.
    */
   private static final int SQL_SORTRANK = 6;

   /**
    * Column number for the Folder id column.
    */
   private static final int SQL_FOLDERID = 7;
   
   /**
    * Default content valid flags.
    */
   private static String DEFAULT_CONTENT_VALID_FLAGS = "('i','I','y','Y')";

   /**
    * Default content valid flags for publishable items that are not in public
    * state.
    */
   private static String DEFAULT_CONTENT_VALID_FLAGS_NONPUBLIC = "('i','I')";
}
