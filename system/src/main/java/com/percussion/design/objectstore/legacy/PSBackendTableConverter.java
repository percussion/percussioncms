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
