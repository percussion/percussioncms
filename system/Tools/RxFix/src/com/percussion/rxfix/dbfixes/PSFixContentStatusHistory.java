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
package com.percussion.rxfix.dbfixes;

import com.percussion.rxfix.IPSFix;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.util.PSStringTemplate.PSStringTemplateException;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * Detect and attempt to fix missing entries in CONTENTSTATUSHISTORY for items
 * that are in either a Public or QuickEdit state. For items in a Public state,
 * simply insert the correct value from the CONTENTSTATUS table. For items in a
 * QuickEdit state, make a reasonable guess (currentrevision - 1), insert and
 * print a warning message.
 * 
 */
public class PSFixContentStatusHistory extends PSFixDBBase implements IPSFix
{
   /**
    * Ctor
    * 
    * @throws SQLException
    * @throws NamingException
    */
   public PSFixContentStatusHistory() throws NamingException, SQLException {
      super();
   }

   /**
    * This query searches for content items that are in a public state and are
    * not in the contentstatushistory table for that public state and the
    * specific current revision id.
    */
   private static final PSStringTemplate ms_findMissingCSH1 = new PSStringTemplate(
         "SELECT CS.CONTENTID, S.STATENAME, S.STATEID, "
               + "W.WORKFLOWAPPID, S.CONTENTVALID, CS.CURRENTREVISION,"
               + "CS.CONTENTLASTMODIFIER,"
               + "CS.CONTENTLASTMODIFIEDDATE, CS.TITLE "
               + "FROM {schema}.CONTENTSTATUS CS, {schema}.STATES S, "
               + "     {schema}.WORKFLOWAPPS W "
               + "WHERE CS.WORKFLOWAPPID = W.WORKFLOWAPPID AND "
               + "CS.CONTENTSTATEID = S.STATEID AND CS.OBJECTTYPE = 1 AND "
               + "S.WORKFLOWAPPID = W.WORKFLOWAPPID AND "
               + "S.CONTENTVALID = 'y' AND CS.CONTENTID NOT IN "
               + "(SELECT CSH.CONTENTID FROM {schema}.CONTENTSTATUSHISTORY CSH"
               + "    WHERE CSH.VALID = 'Y' AND"
               + "          CSH.REVISIONID = CS.CURRENTREVISION )");

   /**
    * This query searches for content items that either have the revision lock
    * on, or are in a revision > 1 and have no public record in the content
    * status history table. New records will be created that make revision 1
    * public for this case.
    */
   private static final PSStringTemplate ms_findMissingCSH2 = new PSStringTemplate(
         "SELECT CS.CONTENTID, S.STATENAME, S.STATEID, "
               + "W.WORKFLOWAPPID, S.CONTENTVALID, 1 REVISION,"
               + "CS.CONTENTLASTMODIFIER, CS.CONTENTLASTMODIFIEDDATE, CS.TITLE "
               + "FROM {schema}.CONTENTSTATUS CS, {schema}.STATES S, "
               + "     {schema}.WORKFLOWAPPS W "
               + "WHERE CS.WORKFLOWAPPID = W.WORKFLOWAPPID AND "
               + "CS.CONTENTSTATEID = S.STATEID AND CS.OBJECTTYPE = 1 AND "
               + "S.WORKFLOWAPPID = W.WORKFLOWAPPID AND "
               + "(CS.REVISIONLOCK = 'Y' OR CS.CURRENTREVISION > 1) AND "
               + "CS.CONTENTID NOT IN " + " (SELECT CSH.CONTENTID "
               + "  FROM {schema}.CONTENTSTATUSHISTORY CSH "
               + "  WHERE CSH.VALID = 'Y')");

   /*
    * Define resultset indeces. Update if above query is modified.
    */
   private final int Q_CONTENTID = 1;

   private final int Q_STATENAME = 2;

   private final int Q_STATEID = 3;

   private final int Q_WORKFLOWAPPID = 4;

   // Not in use right now
   // private final int Q_CONTENTVALID = 5;

   private final int Q_CURRENTREV = 6;

   private final int Q_LASTMODIFIER = 7;

   private final int Q_LASTMODIFIEDDATE = 8;

   private final int Q_TITLE = 9;

   /**
    * Calculate the maximum content revision in use in the database. This is
    * used to aid in searching for missing history records.
    */
   private static final PSStringTemplate ms_maxRevision = new PSStringTemplate(
         "SELECT MAX(TIPREVISION) FROM {schema}.CONTENTSTATUS");

   /**
    * Find missing history records by looking for content items that don't have
    * a corresponding history record for a given revision. For check out cases,
    * we're searching for the previous revision. The second "not in" query looks
    * for those cases.
    */
   private static final PSStringTemplate ms_findMissingCSH3 = new PSStringTemplate(
         "SELECT CS.CONTENTID, CS.TITLE, CS.WORKFLOWAPPID "
               + "FROM {schema}.CONTENTSTATUS CS "
               + "WHERE CS.TIPREVISION >= ? " + "AND CS.TIPREVISION > 1 "
               + "AND CS.OBJECTTYPE = 1 AND "
               + "CS.CONTENTID NOT IN (SELECT CSH.CONTENTID "
               + "  FROM {schema}.CONTENTSTATUSHISTORY CSH "
               + "  WHERE CSH.REVISIONID = ?) AND "
               + "CS.CONTENTID NOT IN (SELECT CSH.CONTENTID "
               + "  FROM {schema}.CONTENTSTATUSHISTORY CSH "
               + "  WHERE CSH.CHECKOUTUSERNAME IS NOT NULL AND "
               + "        CSH.REVISIONID = ?)");

   /**
    * This insert statement puts a record into CSH. Some of these will get real
    * data, some will simply get assumed data or made up data
    */
   private final PSStringTemplate ms_insertIntoCSH = new PSStringTemplate(
         "INSERT INTO {schema}.CONTENTSTATUSHISTORY "
               + "(CONTENTSTATUSHISTORYID, CONTENTID, REVISIONID, SESSIONID, ACTOR,"
               + "VALID, STATEID, TRANSITIONID, ROLENAME, STATENAME, TRANSITIONLABEL,"
               + "CHECKOUTUSERNAME, LASTMODIFIERNAME, LASTMODIFIEDDATE, EVENTTIME,"
               + "WORKFLOWAPPID, TITLE, TRANSITIONCOMMENT) " + "VALUES "
               + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

   /*
    * Insert indeces, update if the insert changes
    */
   private final int I_CSHID = 1;

   private final int I_CONTENTID = 2;

   private final int I_REVISION = 3;

   private final int I_SESSION = 4;

   private final int I_ACTOR = 5;

   private final int I_VALID = 6;

   private final int I_STATEID = 7;

   private final int I_TRANSITIONID = 8;

   private final int I_ROLENAME = 9;

   private final int I_STATENAME = 10;

   private final int I_TRANSITIONLABEL = 11;

   private final int I_CHECKOUTUSERNAME = 12;

   private final int I_LASTMODIFIER = 13;

   private final int I_LASTMODIFIEDDATE = 14;

   private final int I_EVENTTIME = 15;

   private final int I_WORKFLOWAPP = 16;

   private final int I_TITLE = 17;

   private final int I_TRANSITIONCOMMENT = 18;

   @Override
   public void fix(boolean preview) throws Exception
   {
      super.fix(preview);

      Connection c = PSConnectionHelper.getDbConnection();

      try
      {
         int insertCount = 0;

         // Find candidate records and fix them for items that should be
         // in public state
         PreparedStatement st = PSPreparedStatement.getPreparedStatement(c,
               ms_findMissingCSH1.expand(m_defDict));
         PreparedStatement insert = PSPreparedStatement.getPreparedStatement(c,
               ms_insertIntoCSH.expand(m_defDict));
         ResultSet rs = st.executeQuery();
         List<Integer> items = null;

         try
         {
            items = recreateHistoryRecords(preview, true, insert, rs,
                  "RxFix added public state record");
         }
         catch (SQLException e)
         {
            logFailure(null, "Problem recreating public state records");
         }
         finally
         {
            rs.close();
            st.close();
         }

         if (items != null)
         {
            insertCount += items.size();

            if (items.isEmpty() == false)
            {
               logSuccess(idsToString(items),
                     "The following items had new public state records "
                           + "created for their currentrevision: ");
            }
         }

         PreparedStatement st2 = PSPreparedStatement.getPreparedStatement(c,
               ms_findMissingCSH2.expand(m_defDict));
         rs = st2.executeQuery();

         items = null;
         try
         {
            items = recreateHistoryRecords(preview, true, insert, rs,
                  "RxFix added public state record");
         }
         catch (SQLException e)
         {
            logFailure(null,
                  "Problem recreating public state records for revision 1 "
                        + e.getLocalizedMessage());
         }
         finally
         {
            rs.close();
            st2.close();
         }

         if (items != null)
         {
            insertCount += items.size();

            if (items.isEmpty() == false)
            {
               logInfo(idsToString(items),
                     "The following items had new public state records "
                           + "created for revision 1");
            }
         }

         PreparedStatement st3 = PSPreparedStatement.getPreparedStatement(c,
               ms_maxRevision.expand(m_defDict));
         rs = st3.executeQuery();
         rs.next();
         int maxrev = rs.getInt(1);
         rs.close();
         st3.close();

         PreparedStatement st4 = PSPreparedStatement.getPreparedStatement(c,
               ms_findMissingCSH3.expand(m_defDict));
         Timestamp now = new Timestamp(System.currentTimeMillis());
         Set<Integer> revContentIds = new HashSet<Integer>();
         for (int i = 1; i <= maxrev; i++)
         {
            st4.setInt(1, i); // Content item revision
            st4.setInt(2, i); // CSH revision
            st4.setInt(3, i - 1); // CSH checked out revision
            rs = st4.executeQuery();
            items = replaceRevisionRecords(preview, insert, rs, now, i);
            revContentIds.addAll(items);
            insertCount += items.size();
            rs.close();
         }
         st4.close();

         if (revContentIds.isEmpty() == false)
         {
            logInfo(idsToString(items),
                  "The following items had new revision records created");
         }

         if (preview)
         {
            logPreview(null, "The following actions would occur:");
         }
         MessageFormat msg = new MessageFormat("Inserted {0} records"
               + " into CONTENTSTATUSHISTORY");
         Object args = new Object[]
         {new Integer(insertCount)};
         if (insertCount == 0)
         {
            logPreview(null, "No problems found");
         }
         else
         {
            logPreview(null, msg.format(args));
         }
      }
      finally
      {
         if (c != null)
            c.close();
      }

   }

   /**
    * Loop through the results passed in and create the new history records for
    * the given content items.
    * 
    * @param preview Is this in preview mode. In preview mode, don't actually
    *           modify the database.
    * @param insert The insert statement to use, assumed not <code>null</code>.
    * @param rs The result set being used, assumed not <code>null</code>.
    * @param now The current time value, assumed not <code>null</code>.
    * @param revision The revision to insert into the history
    * 
    * @return a list of content ids, never <code>null</code>, but may be
    *         empty
    * @throws PSStringTemplateException
    * @throws SQLException
    * @throws NamingException
    */
   private List<Integer> replaceRevisionRecords(boolean preview,
         PreparedStatement insert, ResultSet rs, Timestamp now, int revision)
         throws PSStringTemplateException, SQLException, NamingException
   {
      List<Integer> rval = new ArrayList<Integer>();
      Logger l = Logger.getLogger(getClass());
      int nextid = getNextIdBlock(20, "CONTENTSTATUSHISTORY", preview);
      int end = nextid + 20 - 1;
      while (rs.next())
      {
         if (nextid >= end)
         {
            nextid = getNextIdBlock(20, "CONTENTSTATUSHISTORY", preview);
            end = nextid + 20 - 1;
         }
         int contentid = rs.getInt(1);
         if (!preview)
         {
            String title = rs.getString(2);
            int workflowappid = rs.getInt(3);
            l.debug("Inserting revision record for content id " + contentid
                  + " rev " + revision);
            insertContentStatusRecord(insert, now, "RxFix", null, "Draft",
                  title, "RxFix Inserted Revision History", false,
                  workflowappid, nextid++, contentid, revision);
         }
         else
         {
            l.debug("Would insert revision record for content id " + contentid
                  + " rev " + revision);
         }
         rval.add(new Integer(contentid));
      }
      return rval;
   }

   /**
    * Insert history records for a given item using information about that item.
    * 
    * @param preview Is this in preview mode. In preview mode, don't actually
    *           modify the database.
    * @param insert The insert statement to use, assumed not <code>null</code>.
    * @param rs The result set being used, assumed not <code>null</code>.
    * 
    * @return a list of content ids, never <code>null</code>, but may be
    *         empty
    * @throws PSStringTemplateException
    * @throws SQLException
    * @throws NamingException
    */
   private List<Integer> recreateHistoryRecords(boolean preview,
         boolean publicstate, PreparedStatement insert, ResultSet rs,
         String comment) throws PSStringTemplateException, SQLException,
         NamingException
   {
      List<Integer> rval = new ArrayList<Integer>();
      Logger l = Logger.getLogger(getClass());
      int nextid = getNextIdBlock(20, "CONTENTSTATUSHISTORY", preview);
      int end = nextid + 20 - 1;
      while (rs.next())
      {
         if (nextid >= end)
         {
            nextid = getNextIdBlock(20, "CONTENTSTATUSHISTORY", preview);
            end = nextid + 20 - 1;
         }
         int contentid = rs.getInt(Q_CONTENTID);
         rval.add(new Integer(contentid));
         int revision = rs.getInt(Q_CURRENTREV);
         if (!preview)
         {
            l.debug("Insert public record in CONTENTSTATUSHISTORY "
                  + "for content id " + contentid + " rev " + revision);
            insertContentStatusRecord(insert, rs
                  .getTimestamp(Q_LASTMODIFIEDDATE), rs
                  .getString(Q_LASTMODIFIER),
                  new Integer(rs.getInt(Q_STATEID)), rs.getString(Q_STATENAME),
                  rs.getString(Q_TITLE), comment, publicstate, rs
                        .getInt(Q_WORKFLOWAPPID), nextid++, contentid, revision);
         }
         else
         {
            l.debug("Would insert public record in CONTENTSTATUSHISTORY for "
                  + "content id " + contentid + " rev " + revision);
         }
      }
      return rval;
   }

   /**
    * Insert a record into the CONTENTSTATUSHISTORY table.
    * 
    * @param insert The insert statement, must correspond to the values of
    *           <code>I_*</code> values in use here.
    * @param lastModifiedDate the information for the last modified date,
    *           assumed not <code>null</code>
    * @param lastModifier the user name of the last modifier
    * @param stateid the statid identifier or <code>null</code> if unknown
    * @param stateName the name fo the state, assumed not <code>null</code>
    * @param title the title of the content item, assumed not <code>null</code>
    * @param comment the comment for this record, assumed not <code>null</code>
    * @param ispublic this history record represents a transition to a public
    *           state
    * @param workflowappid the workflowappid
    * @param historyid The id to use for the primary key in the record
    * @param contentid The content id of the item
    * @param revision The revision of the item
    * @throws SQLException if an exception occurs
    */
   private void insertContentStatusRecord(PreparedStatement insert,
         Timestamp lastModifiedDate, String lastModifier, Integer stateid,
         String stateName, String title, String comment, boolean ispublic,
         int workflowappid, int historyid, int contentid, int revision)
         throws SQLException
   {
      // Setup for insert
      Timestamp now = new Timestamp(System.currentTimeMillis());
      insert.setString(I_ACTOR, "RxFix");
      insert.setNull(I_CHECKOUTUSERNAME, Types.VARCHAR);
      insert.setInt(I_CONTENTID, contentid);
      insert.setInt(I_CSHID, historyid);
      insert.setTimestamp(I_EVENTTIME, now);
      insert.setTimestamp(I_LASTMODIFIEDDATE, lastModifiedDate);
      insert.setString(I_LASTMODIFIER, lastModifier);
      insert.setInt(I_REVISION, revision);
      insert.setNull(I_ROLENAME, Types.VARCHAR);
      insert.setNull(I_SESSION, Types.VARCHAR);
      if (stateid == null)
      {
         insert.setNull(I_STATEID, Types.INTEGER);
      }
      else
      {
         insert.setInt(I_STATEID, stateid.intValue());
      }
      insert.setString(I_STATENAME, stateName);
      insert.setString(I_TITLE, title);
      insert.setString(I_TRANSITIONCOMMENT, comment);
      insert.setNull(I_TRANSITIONID, Types.INTEGER);
      insert.setNull(I_TRANSITIONLABEL, Types.VARCHAR);
      insert.setString(I_VALID, ispublic ? "Y" : "N");
      insert.setInt(I_WORKFLOWAPP, workflowappid);
      insert.execute();
   }

   @Override
   public String getOperation()
   {
      return "Fix content status history";
   }
}
