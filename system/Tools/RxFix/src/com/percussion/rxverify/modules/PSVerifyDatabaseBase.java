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
