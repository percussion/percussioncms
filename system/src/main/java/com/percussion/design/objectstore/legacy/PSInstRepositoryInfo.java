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

package com.percussion.design.objectstore.legacy;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Class which provides the installer with the appropriate repository information.
 */
public class PSInstRepositoryInfo implements IPSRepositoryInfo
{
   /**
    * Construct the repository information
    * 
    * @param rxRoot the rhythmyx root installation directory, never <code>null</code>
    * @throws IOException, FileNotFoundException if an error occurs loading
    *  properties
    */
   public PSInstRepositoryInfo(String rxRoot)
    throws IOException, FileNotFoundException
   {
      if (rxRoot == null)
         throw new IllegalArgumentException("rxRoot may not be null");
      
      PSProperties props = new PSProperties(rxRoot + File.separator + m_repositoryProps);
            
      m_driver = props.getProperty(PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY);
      m_server = props.getProperty(PSJdbcDbmsDef.DB_SERVER_PROPERTY);
      m_database = props.getProperty(PSJdbcDbmsDef.DB_NAME_PROPERTY);
      m_origin = props.getProperty(PSJdbcDbmsDef.DB_SCHEMA_PROPERTY);
   }
   
   /**
    * @see IPSRepositoryInfo#getDriver
    */
   public String getDriver()
   {
      return m_driver;
   }

   /**
    * @see IPSRepositoryInfo#getServer
    */
   public String getServer()
   {
      return m_server;
   }
   
   /**
    * @see IPSRepositoryInfo#getDatabase
    */
   public String getDatabase()
   {
      return m_database;
   }
   
   /**
    * @see IPSRepositoryInfo#getOrigin
    */
   public String getOrigin()
   {
      return m_origin;
   }
         
   /**
    * The relative path to the repository properties file
    */
   private String m_repositoryProps = "rxconfig/Installer/rxrepository.properties";
   
   /**
    * The database driver 
    */
   private String m_driver;
   
   /**
    * The database server
    */
   private String m_server;
   
   /**
    * The database name
    */
   private String m_database;
   
   /**
    * The database origin
    */
   private String m_origin;
   
}
