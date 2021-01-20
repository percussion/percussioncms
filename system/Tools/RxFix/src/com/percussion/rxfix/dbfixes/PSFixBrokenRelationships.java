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

import com.percussion.cms.IPSConstants;
import com.percussion.rxfix.IPSFix;
import com.percussion.util.PSPreparedStatement;
import com.percussion.util.PSStringTemplate;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.naming.NamingException;

/**
 * @author dougrand
 * 
 * Removes relationships that reference a non-existant content item as the
 * owner or dependent
 */
public class PSFixBrokenRelationships extends PSFixDBBase implements IPSFix
{
   /**
    * This query finds relationship records where the owner or dependent doesn't
    * exist.
    */
   PSStringTemplate ms_findBrokenRelationshipQuery = new PSStringTemplate(
         "SELECT RID FROM {schema}." + IPSConstants.PSX_RELATIONSHIPS
               + " WHERE OWNER_ID NOT IN (SELECT CONTENTID "
               + "FROM {schema}.CONTENTSTATUS) OR "
               + "DEPENDENT_ID NOT IN (SELECT CONTENTID "
               + "FROM {schema}.CONTENTSTATUS)");

   /**
    * Delete a specified relationship row
    */
   PSStringTemplate ms_deleteRelationship = new PSStringTemplate(
         "DELETE FROM {schema}." + IPSConstants.PSX_RELATIONSHIPS + " WHERE RID = ?");

   /**
    * Delete specified relationship from the {@link IPSConstants#PSX_RELATIONSHIPPROPERTIES} table
    */
   public static final PSStringTemplate ms_deleteFromRelationshipProps =
      new PSStringTemplate("DELETE FROM {schema}." + IPSConstants.PSX_RELATIONSHIPPROPERTIES
            + " WHERE RID = ?");   

   /**
    * Ctor
    * @throws SQLException 
    * @throws NamingException 
    */
   public PSFixBrokenRelationships() throws NamingException, SQLException {
      super();
   }

   @Override
   public void fix(boolean preview)
         throws Exception
   {
      super.fix(preview);
      Connection c = PSConnectionHelper.getDbConnection();
      // Identify candidate broken relationship records
      try
      {
         PreparedStatement st =
            PSPreparedStatement.getPreparedStatement(
               c,
               ms_findBrokenRelationshipQuery.expand(m_defDict));

         Collection<Integer> rids = new ArrayList<Integer>();
         ResultSet rs = st.executeQuery();
         while (rs.next())
         {
            int rid = rs.getInt(1);
            rids.add(new Integer(rid));
         }

         if (rids.size() == 0)
         {
            logInfo(null, "There are no broken relationships");
         }
         else if (preview)
         {
            Iterator iter = rids.iterator();
            while (iter.hasNext())
            {
               Integer rid = (Integer) iter.next();
               logPreview(rid.toString(), "Would remove relationship");
            }
            logPreview(null, "Would remove above broken relationships");
         }
         else
         {
            Iterator iter = rids.iterator();
            PreparedStatement deleteFromRelationships =
               PSPreparedStatement.getPreparedStatement(
                  c,
                  ms_deleteRelationship.expand(m_defDict));
            PreparedStatement deleteFromRelProps =
               PSPreparedStatement.getPreparedStatement(
                  c,
                  ms_deleteFromRelationshipProps.expand(m_defDict));
            while (iter.hasNext())
            {
               Integer rid = (Integer) iter.next();
               deleteFromRelationships.setInt(1, rid.intValue()); 
               deleteFromRelProps.setInt(1, rid.intValue());
               deleteFromRelProps.execute();
               deleteFromRelationships.execute();
            }
            logInfo(idsToString(rids), "Removed these broken relationships");
         }
      }
      finally
      {
         c.close();
      }
   }
   
   @Override
   public String getOperation()
   {
      return "Fix broken relationships";
   }
}
