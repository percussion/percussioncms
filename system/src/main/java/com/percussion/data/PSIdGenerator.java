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

package com.percussion.data;

import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;

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
    * That uses the database defined in the server.properties, and a default
    * nextnumber table.
    */
   public static synchronized int[] getNextIdBlock(String keyId, int blockSize)
       throws SQLException
   {
      return mgr.createIdBlock(keyId, blockSize);
   }
   
   private static final IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
}
