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
package com.percussion.services.contentmgr.ui;

import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;

/**
 * Base class for consistency checker classes
 * 
 * @author dougrand
 */
public class PSConsistencyBase
{

   /**
    * The content manager, used here to enumerate the node defs available
    */
   protected static IPSContentMgr ms_cmgr = PSContentMgrLocator.getContentMgr();

   /**
    * The connection details, initialized in the first ctor called
    */
   protected static PSConnectionDetail ms_cdetails = null;

   /**
    * Represents a single problem found
    */
   public static class Problem
   {
      /**
       * The content id of the item
       */
      int mi_contentid;

      /**
       * The table involved
       */
      String mi_table;

      /**
       * The revisions that are not present in the table
       */
      Set<Integer> mi_missingrevisions = new TreeSet<>();

      /**
       * Ctor
       * 
       * @param contentid the content id of the problem item
       * @param table the table containing the issue, never <code>null</code>
       *           or empty
       */
      public Problem(int contentid, String table) {
         if (StringUtils.isBlank(table))
         {
            throw new IllegalArgumentException("table may not be null or empty");
         }
         mi_contentid = contentid;
         mi_table = table;
      }

      /**
       * @return the contentid
       */
      public int getContentid()
      {
         return mi_contentid;
      }

      /**
       * @return the table
       */
      public String getTable()
      {
         return mi_table;
      }

      /**
       * @return the missingrevisions
       */
      public Set<Integer> getMissingRevisions()
      {
         return mi_missingrevisions;
      }

      /**
       * Add a revision to the problem
       * 
       * @param rev the revision
       */
      void addMissingRevision(int rev)
      {
         mi_missingrevisions.add(rev);
      }
   }

   /**
    * Qualify a table name using the current connection details
    * 
    * @param name the table name to qualify, never <code>null</code> or empty
    * @return the qualified name, never <code>null</code> or empty
    */
   protected String getTableName(String name)
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      return PSSqlHelper.qualifyTableName(name, ms_cdetails.getDatabase(),
            ms_cdetails.getOrigin(), ms_cdetails.getDriver());
   }

   /**
    * Ctor
    * 
    * @throws SQLException see {@link PSConnectionHelper#getConnectionDetail()}
    *            for details
    * @throws NamingException see
    *            {@link PSConnectionHelper#getConnectionDetail()} for details
    */
   public PSConsistencyBase() throws NamingException, SQLException {
      synchronized (PSConsistencyProblemFinder.class)
      {
         ms_cdetails = PSConnectionHelper.getConnectionDetail();
      }
   }

}
