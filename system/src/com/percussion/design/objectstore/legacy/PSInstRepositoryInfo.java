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

package com.percussion.design.objectstore.legacy;

import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSProperties;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

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
