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

import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.util.PSStringTemplate.PSStringTemplateException;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;

/**
 * A common base class for database fix classes.
 */
public abstract class PSFixDBBase extends PSFixBase
{
   /**
    * Get the slot ids that are inline links
    */
   public static PSStringTemplate ms_slotIdTypes = new PSStringTemplate(
         "SELECT SLOTID FROM {schema}.RXSLOTTYPE WHERE SLOTTYPE = 1");

   /**
    * Get the next number from the table
    */
   private final PSStringTemplate ms_nextCSH = new PSStringTemplate(
         "select NEXTNR from {schema}.NEXTNUMBER WHERE KEYNAME = ?");

   /**
    * Update the next number from the table
    */
   private final PSStringTemplate ms_updateCSH = new PSStringTemplate(
         "update {schema}.NEXTNUMBER set NEXTNR = ? WHERE KEYNAME = ?");

   /**
    * The dictionary that is used when creating sql statements. Never
    * <code>null</code> after construction.
    */
   protected Map<String, Object> m_defDict = null;

   /**
    * Ctor for the class
    * 
    * @throws SQLException
    * @throws NamingException
    */
   public PSFixDBBase() throws NamingException, SQLException {
      PSConnectionDetail detail = PSConnectionHelper.getConnectionDetail();

      m_defDict = new HashMap<String, Object>();
      m_defDict.put("schema", detail.getOrigin());
      m_defDict.put("db", detail.getDatabase());
      m_defDict.put("driver", detail.getDriver());
   }

   /**
    * Return the first id in a series of ids to use for inserts into the named
    * key for the next number table.
    * 
    * @param count the number of items to potentially insert
    * @param preview this is being called in preview mode, just return a fixed
    *           number
    * 
    * @return the first id to use, id through id + count - 1 will be valid. At
    *         id + count this method must be called again.
    * @throws PSStringTemplateException
    * @throws SQLException if there is an underlying database problem
    * @throws NamingException if there is a problem creating the connection
    */
   protected int getNextIdBlock(int count, String key, boolean preview)
         throws PSStringTemplateException, SQLException, NamingException
   {
      if (preview)
         return 0;

      Connection c = PSConnectionHelper.getDbConnection();

      boolean isAutoCommit = c.getAutoCommit();
      try
      {
         if (! isAutoCommit)
            c.setAutoCommit(true);
         
         PreparedStatement st = PSPreparedStatement.getPreparedStatement(c,
               ms_nextCSH.expand(m_defDict));
         st.setString(1, key);
         ResultSet rs = st.executeQuery();
         if (rs.next() == false)
         {
            throw new SQLException("Missing key " + key);
         }
         // value in table is actually the last number used
         int next = rs.getInt(1) + 1;
         PreparedStatement stupdate = PSPreparedStatement.getPreparedStatement(
               c, ms_updateCSH.expand(m_defDict));
         stupdate.setInt(1, next + count);
         stupdate.setString(2, key);
         stupdate.executeUpdate();
         st.close();
         rs.close();
         return next;
      }
      finally
      {
         // set auto commit to its original state.
         if (! isAutoCommit)
            c.setAutoCommit(false);
         
         c.close();
      }
   }

   /**
    * @param items
    */
   protected String idsToString(Collection<? extends Number> items)
   {
      NumberFormat fmt = new DecimalFormat(" 000000");
      for (Iterator<? extends Number> itemiter = items.iterator(); itemiter
            .hasNext();)
      {
         StringBuilder buf = new StringBuilder(80);
         for (int j = 0; j < 10 && itemiter.hasNext(); j++)
         {
            Number item = itemiter.next();
            buf.append(fmt.format(item.intValue()));
            if (itemiter.hasNext())
            {
               buf.append(", ");
            }
         }
         return buf.toString();
      }
      return "";
   }

}
