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
