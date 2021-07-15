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

import com.percussion.design.objectstore.PSBackEndCredential;
import com.percussion.design.objectstore.PSTableLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import org.w3c.dom.Element;

/**
 * Class to convert v5.x {@link PSLegacyTableLocator} to use datasources. 
 */
public class PSTableLocatorConverter extends PSBaseComponentConverter
{

   /**
    * Construct the converter.  
    * 
    * @param configCtx Supplies the configurations required for conversion, may
    * not be <code>null</code>.
    * @param updateConfig <code>true</code> to create required configurations
    * if they are not found in the configurations supplied by the
    * <code>configCtx</code>, <code>false</code> to throw an exception if
    * the required configurations are not found.
    */
   public PSTableLocatorConverter(PSConfigurationCtx configCtx,
      boolean updateConfig)
   {
      super(configCtx, null, updateConfig);
   }

   // see IPSComponentConverter interface
   public Object convertComponent(Element source) 
      throws PSUnknownNodeTypeException
   {
      PSTableLocator newTableLoc;
      
      // try to load the source object
      PSUnknownNodeTypeException ex = null;
      PSLegacyTableLocator oldTableLoc = null;
      try
      {
         oldTableLoc = new PSLegacyTableLocator(source, null, null);
      }
      catch (PSUnknownNodeTypeException e)
      {
         ex = e;
      }      
      
      // handle forced conversion
      if (isForcedConversion())
      {
         boolean didConvert = false;
         if (ex != null)
         {
            /*
             * Should mean we've been passed current XML, if not this will
             * throw. Need to bypass conversion in this case by using a
             * different ctor and call fromXML.
             */ 
            newTableLoc = new PSTableLocator("temp");
            newTableLoc.fromXml(source, null, null);
            if (newTableLoc.getCredentials() != null)
            {
               newTableLoc.getCredentials().setDataSource(null);
               didConvert = true;
            }
         }
         else
         {
            // we've got an old one, just convert to empty datsource
            if (oldTableLoc.getCredentials() == null)
               newTableLoc = createTableLoc(oldTableLoc);
            else
            {
               newTableLoc = createTableLoc(null, oldTableLoc);
               didConvert = true;
            }
         }
         
         if (didConvert)
         {
            getLogger().info(
               "Converted table locator with credentials alias \""
                  + newTableLoc.getCredentials().getAlias()
                  + "\" " + getContextLogMessage() + 
                  " to use the repository datasource");
         }
      }
      else if (ex != null)
      {
         // should have been able to restore, re-throw
         throw (PSUnknownNodeTypeException) ex.fillInStackTrace();
      }
      else
      {
         // perform the conversion
         if (oldTableLoc.getCredentials() == null)
         {
            newTableLoc = createTableLoc(oldTableLoc);
         }
         else
         {
            String dsName = resolveToDatasource(oldTableLoc);
            newTableLoc = createTableLoc(dsName, oldTableLoc);

            if (dsName == null)
            {
               getLogger().info(
                  "Converted table locator with credentials alias \"" 
                  + newTableLoc.getCredentials().getAlias() + 
                  "\" " + getContextLogMessage() + 
                  " to use the repository datasource");
            }
            else
            {
               getLogger().info(
                  "Converted table locator with credentials alias \"" 
                  + newTableLoc.getCredentials().getAlias() + 
                  "\" " + getContextLogMessage() + "to use the \"" + dsName + 
                  "\" datasource");
            }
         }
      }
      
      return newTableLoc;
   }

   /**
    * Creates a table locator from the supplied legacy table locator using the
    * specified datasource name.  The supplied locator should have been 
    * constructed with backend credentials
    * 
    * @param dsName The datasource name to use, may be <code>null</code> or 
    * empty.
    * @param oldTableLoc The table locator on which the new locator is based,
    * assumed not <code>null</code> and to have been constructed with backend
    * credentials.
    * 
    * @return The new locator, never <code>null</code>.
    */
   private PSTableLocator createTableLoc(String dsName,
      PSLegacyTableLocator oldTableLoc)
   {
      PSLegacyBackEndCredential oldCred = oldTableLoc.getCredentials();
      PSBackEndCredential newCred = new PSBackEndCredential(oldCred.getAlias());
      newCred.setDataSource(dsName);
      PSTableLocator newTableLoc = new PSTableLocator(newCred);
      copyFrom(newTableLoc, oldTableLoc);
      
      return newTableLoc;
   }
   
   /**
    * Creates a new table loactor based on an old locator that was constructed
    * with an alias ref as opposed to backend credentials.
    * 
    * @param oldTableLoc The old table locator, assumed not <code>null</code> 
    * and to have been constructed with an alias ref.
    * 
    * @return The new locator, never <code>null</code>.
    */
   private PSTableLocator createTableLoc(PSLegacyTableLocator oldTableLoc)
   {
      PSTableLocator newTableLoc = new PSTableLocator(
         oldTableLoc.getAliasRef());
      copyFrom(newTableLoc, oldTableLoc);
      
      return newTableLoc;
   }   
   
   /**
    * Returns <code>true</code> for type of {@link PSLegacyTableLocator}.  See
    * {@link IPSComponentConverter} for more info.
    */
   public boolean canConvertComponent(Class type)
   {
      return type.getName().equals(PSTableLocator.class.getName());
   }

   /**
    * Resolves the connection information in the supplied table locator to a
    * datasource. If no configuration is found and {@link #shouldUpdateConfig()}
    * is <code>true</code>, will create the required configurations.
    * 
    * @param oldTableLoc The table locator to convert, assumed not 
    * <code>null</code>.
    * 
    * @return The datasource name, <code>null</code> if it is the respository
    * datasource, never empty.
    * 
    * @throws PSUnknownNodeTypeException If the datasource name cannot be
    * resolved.
    */
   private String resolveToDatasource(PSLegacyTableLocator oldTableLoc) 
      throws PSUnknownNodeTypeException
   {
      PSLegacyBackEndCredential oldCreds = oldTableLoc.getCredentials();
      
      getLogger().info(
         "Attempting to locate matching datasource for table "
            + "locator with credentials alias \"" + oldCreds.getAlias() + "\" " 
            + getContextLogMessage());
      
      return resolveToDatasource(oldCreds.getDriver(), oldCreds.getServer(),
         oldTableLoc.getDatabase(), oldTableLoc.getOrigin());
   }
   
   /**
    * Copies non-datsource info from source to target table locator.
    * 
    * @param convertedObj The target locator, assumed not <code>null</code>.
    * @param oldTableLoc The source locator, assumed not <code>null</code>.
    */
   private void copyFrom(PSTableLocator convertedObj,
      PSLegacyTableLocator oldTableLoc)
   {
      convertedObj.setAlias(oldTableLoc.getAlias());
      convertedObj.setId(oldTableLoc.getId());
      if (oldTableLoc.getCredentials() != null)
      {
         convertedObj.getCredentials().setAlias(
            oldTableLoc.getCredentials().getAlias());
         convertedObj.getCredentials().setComment(
            oldTableLoc.getCredentials().getComment());
         convertedObj.getCredentials().setConditionals(
            oldTableLoc.getCredentials().getConditionals());
         convertedObj.getCredentials().setId(
            oldTableLoc.getCredentials().getId());         
      }
   }   
}
