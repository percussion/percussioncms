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

import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;

/**
 * Class to convert v5.x {@link PSLegacyBackEndTable} to use datasources. 
 */
public class PSBackendTableConverter extends PSBaseComponentConverter
{
   /**
    * Construct the converter.  
    * 
    * @param configCtx Supplies the configurations required for conversion, may
    * not be <code>null</code>.
    * @param repositoryInfo The repository info, used to determine if creating
    * a datasource configuration that points to the repository. May be 
    * <code>null</code> to convert everything to the repository, in which case
    * {@link PSBaseComponentConverter#isForcedConversion() isForcedConversion()}
    * must return <code>true</code>.
    * @param updateConfig <code>true</code> to create required configurations
    * if they are not found in the configurations supplied by the
    * <code>configCtx</code>, <code>false</code> to throw an exception if
    * the required configurations are not found.
    */
   public PSBackendTableConverter(PSConfigurationCtx configCtx, 
      IPSRepositoryInfo repositoryInfo, boolean updateConfig)
   {
      super(configCtx, repositoryInfo, updateConfig);
      
      m_infoNull = (repositoryInfo == null);
   }

   // see IPSComponentConverter interface
   public Object convertComponent(Element source) 
      throws PSUnknownNodeTypeException
   {
      PSBackEndTable newBeTable;
      
      // try to load the source object
      PSUnknownNodeTypeException ex = null;
      PSLegacyBackEndTable oldBeTable = null;
      try
      {
         oldBeTable = new PSLegacyBackEndTable(source, null, null);
      }
      catch (PSUnknownNodeTypeException e)
      {
         ex = e;
      }
      
      // handle forced conversion
      if (isForcedConversion())
      {
         if (ex != null)
         {
            /*
             * Should mean we've been passed current XML, if not this will
             * throw. Need to bypass conversion in this case by using a
             * different ctor and call fromXML.
             */
            newBeTable = new PSBackEndTable("temp");
            newBeTable.fromXml(source, null, null);
            newBeTable.setDataSource(null);
            newBeTable.setConnectionDetail(null);
         }
         else
         {
            // we've got an old one, just convert to empty datsource
            newBeTable = new PSBackEndTable(oldBeTable.getAlias());
            newBeTable.setDataSource(null);
            copyFrom(newBeTable, oldBeTable);
         }
         getLogger().info("Converted table \"" + newBeTable.getTable() + 
            "\" " + getContextLogMessage() + 
            "to use the repository datasource");
      }
      else if (ex != null)
      {
         // should have been able to restore, re-throw
         throw (PSUnknownNodeTypeException) ex.fillInStackTrace();
      }
      else
      {
         if (m_infoNull)
            throw new IllegalStateException("Cannot perform non-forced " +
               "conversion if repositoryInfo not supplied during construction");
         
         // perform the conversion
         newBeTable = new PSBackEndTable(oldBeTable.getAlias());
         String dsName = resolveToDatasource(oldBeTable);
         newBeTable.setDataSource(dsName);
         copyFrom(newBeTable, oldBeTable);
         if (dsName == null)
         {
            getLogger().info("Converted table \"" + newBeTable.getTable() + 
               "\"  " + getContextLogMessage() + 
               "to use the repository datasource");
         }
         else
         {
            getLogger().info("Converted table \"" + newBeTable.getTable() + 
            "\" " + getContextLogMessage() + 
            " to use the \"" + dsName + "\" datasource");
         }
      }
      
      return newBeTable;
   }

   /**
    * Resolves the connection information in the supplied table to a datasource.
    * If no configuration is found and {@link #shouldUpdateConfig()} is 
    * <code>true</code>, will create the required configurations.
    * 
    * @param oldBeTable The table to convert, assumed not <code>null</code>.
    *  
    * @return The datasource name, <code>null</code> if it is the respository
    * datasource, never empty.
    * 
    * @throws PSUnknownNodeTypeException If the datasource name cannot be
    * resolved. 
    */
   private String resolveToDatasource(PSLegacyBackEndTable oldBeTable) 
      throws PSUnknownNodeTypeException
   {
      getLogger().info("Attempting to locate matching datasource for table \"" + 
         oldBeTable.getTable() + "\" " + getContextLogMessage());
      
      String ds =  resolveToDatasource(oldBeTable.getDriver(), oldBeTable.getServer(),
         oldBeTable.getDatabase(), oldBeTable.getOrigin());
     return ds;
   }
   
   /**
    * Copies non-datsource info from source to target table.
    * 
    * @param convertedObj The target table, assumed not <code>null</code>.
    * @param oldBeTable The source table, assumed not <code>null</code>.
    */
   private void copyFrom(
      PSBackEndTable convertedObj, PSLegacyBackEndTable oldBeTable)
   {
      convertedObj.setAlias(oldBeTable.getAlias());
      convertedObj.setTable(oldBeTable.getTable());
      convertedObj.setId(oldBeTable.getId());
   }

   /**
    * Returns <code>true</code> for type of {@link PSLegacyBackEndTable}.  See
    * {@link IPSComponentConverter} for more info.
    */
   public boolean canConvertComponent(Class type)
   {
      return type.getName().equals(PSBackEndTable.class.getName());
   }
   
   /**
    * Flag to determine if the repository info was supplied during construction.
    */
   private boolean m_infoNull = false;
}
