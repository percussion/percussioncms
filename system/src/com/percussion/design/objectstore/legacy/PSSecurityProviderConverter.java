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

import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.legacy.PSLegacyServerConfig;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSSecurityProvider;
import com.percussion.util.PSCollection;

import java.util.Iterator;
import java.util.Properties;

/**
 * Class to convert v5.x {@link PSLegacySecurityProviderInstance} to use
 * datasources.  Also performs decoupling for role, directory, connection 
 * security, and back-end table security providers, and e-mail/role attribute
 * name updates to directories. 
 */
public class PSSecurityProviderConverter extends PSBaseDSConverter
{
   /**
    * Construct the converter.  
    * 
    * @param configCtx Supplies the configurations required for conversion, may
    * not be <code>null</code>.
    * @param repositoryInfo The repository info, used to determine if creating
    * a datasource configuration that points to the repository, may not be 
    * <code>null</code>.
    * @param updateConfig <code>true</code> to create required configurations
    * if they are not found in the configurations supplied by the
    * <code>configCtx</code>, <code>false</code> to throw an exception if
    * the required configurations are not found.
    */
   public PSSecurityProviderConverter(PSConfigurationCtx configCtx, 
      IPSRepositoryInfo repositoryInfo, boolean updateConfig)
   {
      super(configCtx, repositoryInfo, updateConfig);
      
      if (repositoryInfo == null)
         throw new IllegalArgumentException("repositoryInfo may not be null");
   }

   /**
    * This method is responsible for performing the necessary conversion of all
    * legacy security providers to the new configuration.  This includes datasource
    * conversion for back-end table security providers, decoupling for role,
    * directory connection security, and back-end table security providers, and
    * email and/or role attribute updates to directory configurations.
    *
    * @throws PSUnknownNodeTypeException If a source document is invalid
    * @throws PSIllegalArgumentException If an invalid argument was passed
    */
   public void convert() 
      throws PSUnknownNodeTypeException, PSIllegalArgumentException
   {
      // get the legacy server config and security providers
      PSLegacyServerConfig legacyServerConfig = m_configCtx.getServerConfig();
      PSCollection legacySecProvs = legacyServerConfig.getSecurityProviderInstances();
           
      // convert all legacy security providers
      for (int i = 0; i < legacySecProvs.size(); i++)
      {
         PSLegacySecurityProviderInstance legacySP = 
            (PSLegacySecurityProviderInstance) legacySecProvs.get(i);
         int legacySPType = legacySP.getType();
         
         String dsName = "";
         
         // perform the datasource conversion for back-end table sp
         if (legacySPType == PSSecurityProvider.SP_TYPE_BETABLE)
         {
            // convert to use datasource
            dsName = resolveToDatasource(legacySP);
            
            Properties legacyProps = legacySP.getProperties();
            
            //remove the non-datasource properties
            legacyProps.remove("serverName");
            legacyProps.remove("driverName");
            legacyProps.remove("loginPw");
            legacyProps.remove("schemaName");
            legacyProps.remove("loginId");
            legacyProps.remove("databaseName");
            
            if (dsName == null)
            {
               // the default repository value is the empty string
               dsName = "";
               getLogger().info("Converted security provider \"" + legacySP.getName() + 
                  "\" to use the repository datasource");
            }
            else
            {
               getLogger().info("Converted security provider \"" + legacySP.getName() + 
                  "\" to use the \"" + dsName + "\" datasource");
            }
            
            // set the datasource name property
            legacyProps.setProperty("datasourceName", dsName);
            legacySP.setProperties(legacyProps);
         }
         
         // now update directories with group provider information, if necessary
         Iterator iterGP = legacySP.getGroupProviderNames();
         
         if (iterGP.hasNext())
         {
            // group provider names exist, so must make modifications to the
            // appropriate directories
            
            // get the directory set name
            String dirSetName = legacySP.getDirectoryProvider().getReference().getName();
            
            // get the directory set 
            PSDirectorySet dirSet = 
               legacyServerConfig.getDirectorySet(dirSetName);
         
            // get the directory names
            String dirNames[] = getDirNames(dirSet);
            
            // add group provider information to each directory
            for (int j = 0; j < dirNames.length; j++)
            {
               iterGP = legacySP.getGroupProviderNames();
               PSDirectory dir = legacyServerConfig.getDirectory(dirNames[j]);
               dir.setGroupProviderNames(iterGP);
            }
         }
      }
      
      // finally update e-mail and/or role attribute names in all directories 
      Iterator iterDS = legacyServerConfig.getDirectorySets();
      
      // iterate over all directory sets, modifying referenced directories
      // if necessary
      while (iterDS.hasNext())
      {
         PSDirectorySet drSet = (PSDirectorySet) iterDS.next();
         String emailAttrName = drSet.getRequiredAttributeName(
               PSDirectorySet.EMAIL_ATTRIBUTE_KEY);
         String roleAttrName = drSet.getRequiredAttributeName(
               PSDirectorySet.ROLE_ATTRIBUTE_KEY);
         
         String drNames[] = getDirNames(drSet);
         
         // for each directory, add e-mail and/or role attribute name from
         // parent directory set if not already there
         for (int k = 0; k < drNames.length; k++)
         {
            PSDirectory dr = legacyServerConfig.getDirectory(drNames[k]);
            PSCollection drAttrs = dr.getAttributes();
            
            if (drAttrs == null)
               drAttrs = new PSCollection(String.class);
            
            if (emailAttrName != null && emailAttrName.trim().length() > 0 &&
                  !drAttrs.contains(emailAttrName))
               drAttrs.add(emailAttrName);
            if (roleAttrName != null && roleAttrName.trim().length() > 0 &&
                  !drAttrs.contains(roleAttrName))
               drAttrs.add(roleAttrName);
            
            // only set with new attributes if they exist
            if (drAttrs.size() > 0)
               dr.setAttributes(drAttrs);
         }
      }
   }

   /**
    * Helper method which returns the directory names referenced by a given
    * directory set
    * 
    * @param dirSet the directory set, assumed not <code>null</code>
    * 
    * @return an array containing the directories referenced by this directory
    * set, never <code>null</code>, may be empty.
    */
   private String[] getDirNames(PSDirectorySet dirSet)
   {
      // get the directory names
      int dirSetSize = dirSet.size();
      String dirNames[] = new String[dirSetSize];
            
      for (int i = 0; i < dirSetSize; i++)
      {
         PSReference dirRef = (PSReference) dirSet.elementAt(i);
         dirNames[i] = dirRef.getName();
      }
      
      return dirNames;
   }
     
   /**
    * Resolves the connection information in the supplied security provider
    * to a datasource.  If no configuration is found and {@link #shouldUpdateConfig()} is 
    * <code>true</code>, will create the required configurations.
    * 
    * @param oldProvider The security provider to convert, assumed not <code>null</code>.
    *  
    * @return The datasource name, <code>null</code> if it is the respository
    * datasource, never empty.
    * 
    * @throws PSUnknownNodeTypeException If the datasource name cannot be
    * resolved. 
    */
   private String resolveToDatasource(PSLegacySecurityProviderInstance oldProvider) 
      throws PSUnknownNodeTypeException
   {
      getLogger().info("Attempting to locate matching datasource for \"" + 
         oldProvider.getName() + "\" security provider");
      
      Properties provProps = oldProvider.getProperties();
      
      return resolveToDatasource(
            provProps.getProperty("driverName"),
            provProps.getProperty("serverName"),
            provProps.getProperty("databaseName"),
            provProps.getProperty("schemaName"));
   }
}
