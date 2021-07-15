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

package com.percussion.data;

import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.tablefactory.PSJdbcTableFactoryException;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * This class is used to generate unique numeric IDs.  It relies on a table with
 * the following schema to be present in a database that is specified when
 * requesting a new key (column names may be whatever you like):
 * <table>
 * <tr><td>Column</td><td>DataType</td><td>Use</td></tr>
 * <tr>
 * <td>Key</td>
 * <td>Varchar(50) or the equivalent</td>
 * <td>Identify type of id. Primary key.</td>
 * </tr>
 * <tr>
 * <td>Current Id</td>
 * <td>Integer or equivalent</td>
 * <td>The last id used</td></tr>
 * </table>
 * For each type of Id, the current value is incremented, updated and returned.
 * The user must provide the connection info, table name and column
 * names at run time.
 */
public class PSIdGenerator
{
   /**
    * Convenience version of {@link #getNextIdBlock(String, int)} that calls
    * <code>getNextIdBlock(keyId, 1).
    */
   public static synchronized int getNextId(String keyId) throws SQLException
   {
      return mgr.createId(keyId);
   }

   /**
    * Convenience version of {@link #getNextIdBlock(Connection, String, String,
    * String, String, String, String, String, String, String, String, int) getNextId} 
    * that uses the database defined in the server.properties, and a default 
    * nextnumber table.
    */
   public static synchronized int[] getNextIdBlock(String keyId, int blockSize)
       throws SQLException
   {
      return mgr.createIdBlock(keyId, blockSize);
   }
   
   private static IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
}
