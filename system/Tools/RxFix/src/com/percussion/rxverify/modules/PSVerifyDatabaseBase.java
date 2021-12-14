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
package com.percussion.rxverify.modules;

import com.percussion.tablefactory.PSJdbcDbmsDef;

import java.io.File;
import java.util.Properties;

/**
 * @author dougrand
 *
 * Support code for database verifications
 */
public class PSVerifyDatabaseBase
{
   /**
    * Get the database definition from the given rhythmyx installation 
    * directory
    * @param rxdir the rhythmyx directory, must never be <code>null</code> 
    * @return the def, never returns <code>null</code>
    * @throws Exception on various errors
    */
   public PSJdbcDbmsDef getDbmsDef(File rxdir) throws Exception
   {
      // Load the repository info
      Properties dbProps = PSJdbcDbmsDef.loadRxRepositoryProperties(
            rxdir.getAbsolutePath());
      
      // Get the db info from the properties
      String user, encpw, server, driver, schema, dbname, driverClass;
      user = dbProps.getProperty(PSJdbcDbmsDef.UID_PROPERTY);
      encpw = dbProps.getProperty(PSJdbcDbmsDef.PWD_PROPERTY);
      server = dbProps.getProperty(PSJdbcDbmsDef.DB_SERVER_PROPERTY);
      driver = dbProps.getProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY);
      schema = dbProps.getProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY);
      dbname = dbProps.getProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY);
      driverClass = dbProps.getProperty(PSJdbcDbmsDef.DB_DRIVER_CLASS_NAME_PROPERTY);

      // If anything is missing, fatal out
      if (user == null || encpw == null || server == null || driver == null
            || schema == null || dbname == null || driverClass == null)
      {
         throw new Exception("Invalid repository information: " +
               dbProps.toString());
      }

      return new PSJdbcDbmsDef(dbProps);
   }
}
