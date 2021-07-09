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
package com.percussion.services.contentmgr.ui;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.contentmgr.impl.legacy.PSContentRepository;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.naming.NamingException;

/**
 * Find problems with a given content type's items. For a given content type,
 * this code will iterate. On each iteration it will obtain a set of component
 * summaries.
 * <p>
 * The type configuration(s) will be iterated. For each table, the ids and
 * revisions found in the component summaries are checked against the available
 * ids and revs in the tables.
 * 
 * <p>
 * The content items are:
 * <ul>
 * <li>good (in which case they are forgotten)
 * <li>need to be fixed because one or more records in one or more tables is
 * missing, and those tables and revisions are recordsed
 * <li>broken, which means that there are missing rows for rev 1.
 * </ul>
 * 
 * @author dougrand
 */
public class PSConsistencyProblemFinder extends PSConsistencyBase
{
   /**
    * How many content ids to check at a time
    */
   private static int BLOCK = 300;

   /**
    * Ctor
    * 
    * @throws SQLException see {@link PSConnectionHelper#getConnectionDetail()}
    *            for details
    * @throws NamingException see
    *            {@link PSConnectionHelper#getConnectionDetail()} for details
    */
   public PSConsistencyProblemFinder() throws NamingException, SQLException {
   }

   /**
    * Gets the relevant component summaries for the given type. These are then
    * processed in blocks to find problems.
    * 
    * @param contentTypeName the name of the content type being processed
    * @return a collection of zero or more problems found
    * @throws RepositoryException if there's a problem loading the node
    * def
    * @throws SQLException see {@link #check(List, PSTypeConfiguration)}
    * @throws NamingException see {@link #check(List, PSTypeConfiguration)}
    * @throws PSORMException if there's a problem loading component summaries
    */
   public Collection<Problem> check(String contentTypeName)
         throws RepositoryException, SQLException, NamingException,
         PSORMException
   {
      List<Problem> problems = new ArrayList<>();
      IPSNodeDefinition def = ms_cmgr.findNodeDefinitionByName(contentTypeName);
      long ctid = def.getGUID().longValue();
      PSTypeConfiguration config = PSContentRepository
            .getTypeConfiguration((int) ctid);

      Connection connection = PSConnectionHelper.getDbConnection();
      try
      {
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         List<PSComponentSummary> summaries = cms
               .findComponentSummariesByType(ctid);
         for (int i = 0; i < summaries.size(); i += BLOCK)
         {
            int e = Math.min(i + BLOCK, summaries.size());
            List<PSComponentSummary> sblock = summaries.subList(i, e);
            problems.addAll(check(sblock, config));
         }

      }
      finally
      {
         if (connection != null)
            connection.close();
      }

      return problems;
   }

   /**
    * Check the component summaries in the given block for problems. First the
    * summaries are scanned. From this we obtain a list of content ids. We then
    * check each table associated with the configuration in turn. There are two
    * checkers, one for children and one for normal tables.
    * <p>
    * If this is a parent, then it gets any child configuration and recurses.
    * 
    * @param sblock the sublist being checked, never <code>null</code> but
    *           might be empty, in which case this returns immediately with an
    *           empty problem list
    * @param config the configuration of the content type.
    * @return a collection of any problems found, may be empty, but not
    *         <code>null</code>
    * @throws SQLException see {@link #checkTable(List, String, List)}
    * @throws NamingException see {@link #checkTable(List, String, List)}
    */
   private Collection<Problem> check(List<PSComponentSummary> sblock,
         PSTypeConfiguration config) throws NamingException, SQLException
   {
      List<Problem> problems = new ArrayList<>();
      List<Integer> cids = new ArrayList<>();
      for (PSComponentSummary s : sblock)
      {
         cids.add(s.getContentId());
      }
      if (cids.size() > 0)
      {
         for (String table : config.getTableNames())
         {
            problems.addAll(checkTable(sblock, table, cids));
         }
      }
      return problems;
   }

   /**
    * Check a single table for problems. A table has a problem if it is missing
    * one or more revisions that should be there from the summary data. We
    * detect this by first getting all the rows for the given content ids. From
    * this we build a map from a given content id to the revisions seen in the
    * table, which covers both parent and child tables.
    * 
    * @param sblock the block of summaries being processed
    * @param table the table, assumed not <code>null</code>
    * @param cids a list of content ids to load
    * @return the problems found, may be empty, but not <code>null</code>
    * @throws SQLException if there are problems with the jdbc calls
    * @throws NamingException if there is a problem obtaining the database
    *            connection
    */
   private Collection<Problem> checkTable(List<PSComponentSummary> sblock,
         String table, List<Integer> cids) throws NamingException, SQLException
   {
      List<Problem> problems = new ArrayList<>();
      if (table.equalsIgnoreCase("CONTENTSTATUS"))
      {
         return problems;
      }

      Connection c = PSConnectionHelper.getDbConnection();
      String qualifiedname = getTableName(table);
      try
      {
         StringBuilder query = new StringBuilder();

         query.append("SELECT DISTINCT CONTENTID, REVISIONID FROM ");
         query.append(qualifiedname);
         query.append(" WHERE ");
         boolean first = true;
         for (Integer cid : cids)
         {
            if (first)
            {
               first = false;
            }
            else
            {
               query.append(" OR");
            }
            query.append(" CONTENTID = ");
            query.append(cid.toString());
         }
         PreparedStatement st = c.prepareStatement(query.toString());

         ResultSet rs = st.executeQuery();
         Map<Integer, Set<Integer>> present = new HashMap<>();
         while (rs.next())
         {
            int contentid = rs.getInt(1);
            int revision = rs.getInt(2);
            Set<Integer> revisions = present.get(contentid);
            if (revisions == null)
            {
               revisions = new HashSet<>();
               present.put(contentid, revisions);
            }
            revisions.add(revision);
         }
         // Now walk the component summaries and look for problems
         for (PSComponentSummary s : sblock)
         {
            
            int publicRevision = s.getPublicRevision();
            // PSPurgeRevisions.perform can only remove revisions lower than public revision if it exists
            // or current revision otherwise.
            // tip revision may be same as current revision if not checked out but may be higher otherwise.  
            // Every revision between the minimum and tip should exist.
            int minRevision = publicRevision > 0 ? publicRevision : s.getCurrentLocator().getRevision();
            int maxRevision = s.getTipLocator().getRevision();
            int contentid = s.getContentId();
            
            Set<Integer> revisions = present.get(contentid);
            Problem p = new Problem(contentid, table);
            for (int rev = minRevision; rev <= maxRevision; rev++)
            {
               if (revisions==null || !revisions.contains(rev))
                     p.addMissingRevision(rev);
                  }
            
            if (p.getMissingRevisions().size() > 0)
            {
               problems.add(p);
            }
         }
      }
      catch (SQLException e)
      {
         throw new RuntimeException("Problem while checking table " + table
               + ":" + e.getLocalizedMessage());
      }
      finally
      {
         if (c != null)
            c.close();
      }
      return problems;
   }
}
