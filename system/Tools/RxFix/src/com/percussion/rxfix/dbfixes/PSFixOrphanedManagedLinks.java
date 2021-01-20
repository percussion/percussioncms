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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;

import com.percussion.rxfix.IPSFix;
import com.percussion.util.PSPreparedStatement;
import com.percussion.utils.jdbc.PSConnectionHelper;

public class PSFixOrphanedManagedLinks extends PSFixDBBase implements IPSFix
{
   private static final String SQL_SELECT_CHILDID = "SELECT * FROM PSX_MANAGEDLINK WHERE CHILDID NOT IN (SELECT CONTENTID FROM CONTENTSTATUS)";
   private static final String SQL_SELECT_PARENTID = "SELECT * FROM PSX_MANAGEDLINK WHERE PARENTID NOT IN (SELECT CONTENTID FROM CONTENTSTATUS) AND PARENTID <> -1";
   private static final String SQL_DELETE = "DELETE FROM PSX_MANAGEDLINK WHERE LINKID = ?";
   
   private static final String WOULD_BE_REMOVED = "Would be removed from the PSX_MANAGEDLINK table.";
   private static final String HAS_BEEN_REMOVED = "Has been removed from the PSX_MANAGEDLINK table.";
   private static final String LINK_ID = "Link ID: ";
   
   public PSFixOrphanedManagedLinks() throws NamingException, SQLException
   {
      super();
   }
   
   @Override
   public void fix(boolean preview) throws Exception
   {
      super.fix(preview);

      Connection c = PSConnectionHelper.getDbConnection();

      try
      {
         Set<Integer> linkIds = new HashSet<Integer>();
         
         PreparedStatement selectChildId = PSPreparedStatement.getPreparedStatement(c, SQL_SELECT_CHILDID);
         
         ResultSet rs = selectChildId.executeQuery();
         
         while (rs.next()) {
            linkIds.add(rs.getInt(1));
         }
         
         rs.close();
         
         PreparedStatement selectParentId = PSPreparedStatement.getPreparedStatement(c, SQL_SELECT_PARENTID);
         
         rs = selectParentId.executeQuery();
         
         while (rs.next()) {
            linkIds.add(rs.getInt(1));
         }
         
         rs.close();
         
         PreparedStatement delStatement = PSPreparedStatement.getPreparedStatement(c, SQL_DELETE);
         
         for (int linkId : linkIds) {
            if (preview) {
               logPreview(LINK_ID + String.valueOf(linkId), WOULD_BE_REMOVED);
            }
            else {
               delStatement.setInt(1, linkId);
               delStatement.execute();
               logInfo(LINK_ID + String.valueOf(linkId), HAS_BEEN_REMOVED);
            }
         }
         
         delStatement.close();
         selectChildId.close();
         selectParentId.close();
      }
      finally
      {
         c.close();
      }
   }
   
   @Override
   public String getOperation()
   {
      return "Remove orphaned managed links.";
   }
}
