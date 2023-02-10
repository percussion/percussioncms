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
package com.percussion.services.schedule.impl;

import com.percussion.utils.jdbc.IPSDatasourceManager;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.utils.ConnectionProvider;

/**
 * Provides Rhythmyx database connection to Quartz.
 * Uses statically defined datasource manager, which should be set with
 * {@link #setDatasourceManager(IPSDatasourceManager)} before calling
 * {@link #getConnection()} the first time.
 * 
 * @author Andriy Palamarchuk
 */
public class PSRhythmyxConnectionProvider implements ConnectionProvider
{
   private static final Logger ms_log = LogManager.getLogger(PSRhythmyxConnectionProvider.class);
   
   /**
    * Returns Rhythmyx connection.
    * {@inheritDoc}
    */
   public Connection getConnection() throws SQLException
   {
      if (ms_datasourceManager == null)
      {
         throw new IllegalStateException(
               "Datasource Manager was not specified!");
      }
      try
      {
         return ms_datasourceManager.getDbConnection(null);
      }
      catch (NamingException e)
      {
         // should not happen
         throw new IllegalStateException(e);
      }
   }

   /**
    * Does nothing.
    * {@inheritDoc}
    */
   public void shutdown()
   {
   }

   /**
    * The datasource manager to use by connection provider. Stored statically.
    * Must be specified before the first call to {@link #getConnection()}.
    * Must be set only once. 
    * @param datasourceManager the new value for the datasource manager.
    * Not <code>null</code>.
    */
   public static void setDatasourceManager(
         IPSDatasourceManager datasourceManager)
   {
      if (ms_datasourceManager != null)
      {
         ms_log.debug("datasourceManager already set");
         return;
      }
      if (datasourceManager == null)
      {
         throw new IllegalArgumentException(
               "Datasource Manager can't be null!");
      }
      ms_datasourceManager = datasourceManager;
   }

   /**
    * The class uses this datasource manager.
    */
   private static IPSDatasourceManager ms_datasourceManager;

   public void initialize() throws SQLException
   {
      // TODO Auto-generated method stub
      
   }
}
