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

import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.utils.jdbc.PSConnectionHelper;

import java.sql.SQLException;

import javax.naming.NamingException;

/**
 * The PSSqlUpdateBuilderFactory class is used to retrieve a 
 * PSSqlUpdateBuilder-based object for building SQL DELETE, UPDATE and
 * INSERT statements.
 *
 * @see         PSSqlUpdateBuilder
 * @see         PSQueryJoiner
 */
public class PSSqlUpdateBuilderFactory
{
   /**
    * Private constructor.  This class will never be instantiated.
    */
   private PSSqlUpdateBuilderFactory()
   {
   }

   /**
    * Get the most appropriate sql update builder (PSSqlUpdate builder
    * or class derived therefrom) for generating statements to execute
    * SQL update, insert or delete statements.
    *
    *
    * @param type   The type of builder based on plan type, either 
    *         <code>PSUpdateOptimizer.PLAN_TYPE_UPDATE</code>,
    *         <code>PSUpdateOptimizer.PLAN_TYPE_INSERT</code>, or
    *         <code>PSUpdateOptimizer.PLAN_TYPE_DELETE</code>.
    *
    * @param table  The back end table the builder will be creating a 
    *               statement for.  Never <code>null</code>.
    *
    * @param login  The back end login for the builder we are creating.
    *               Can be <code>null</code>.
    *
    * @param allowInserts Are inserts allowed?  <code>true</code> if so,
    *       <code>false</code> if not.
    *
    * @return The PSSqlUpdateBuilder (or derived class) to be used to 
    *         create the statements.
    *
    * @throws IllegalArgumentException if a builder throws 
    *         PSIllegalArgumentException when being constructed if an
    *         attempt to use two different tables is detected, or any
    *         parameter is invalid
    * @throws SQLException If there is an error obtaining connection details.
    * @throws NamingException If there is an error resolving the login to a 
    *         datasource
    */
   public static PSSqlUpdateBuilder getSqlUpdateBuilder(int type,
      PSBackEndTable table, PSBackEndLogin login, boolean allowInserts)
      throws IllegalArgumentException, NamingException, SQLException
   {
      PSSqlUpdateBuilder builder = null;
       
      if (table == null)
         throw new IllegalArgumentException("Table must be supplied");
      
      try {
         if (type == PSUpdateOptimizer.PLAN_TYPE_UPDATE
             || type == PSUpdateOptimizer.PLAN_TYPE_INSERT)
         {
            if (login != null)
            {
               String driver = PSMetaDataCache.getConnectionDetail(
                  login).getDriver();
               if (driver.toUpperCase().indexOf("ORACLE") > -1)
               {
                  // Return an Oracle-specific builder!
                  if (type == PSUpdateOptimizer.PLAN_TYPE_UPDATE)
                     if (allowInserts)
                        builder = new PSOracleUpdateInsertBuilder(table);
                     else
                        builder = new PSOracleUpdateBuilder(table);
                  else // type is insert
                     builder = new PSOracleInsertBuilder(table);
               }
            }
         }

         if (builder == null) {
            if (type == PSUpdateOptimizer.PLAN_TYPE_UPDATE)
               if (allowInserts)
                  return new PSSqlUpdateInsertBuilder(table);
               else
                  return new PSSqlUpdateBuilder(table);
            else if (type == PSUpdateOptimizer.PLAN_TYPE_INSERT)
               return new PSSqlInsertBuilder(table);
            else  
               /* remnant of original PSUpdateOptimizer algorithm...
                  it defaulted to a delete builder */
               builder =  new PSSqlDeleteBuilder(table);
         }
      } catch (PSIllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.toString());
      }

      // Extension stuff goes here (based on connection)
      return builder;
   }
}
