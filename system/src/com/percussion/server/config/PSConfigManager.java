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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.server.config;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSConfig;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * A singleton class to manage server configuration files.
 */
public class PSConfigManager
{
   /**
    * Private ctor to enforce the singleton design pattern. Creates a 
    * configuration manager. Use {@link #getInstance()} to obtain the singleton 
    * instance of this class. {@link #getInstance()} calls
    * {@link #loadConfigurations()} before the instance is returned.
    */
   private PSConfigManager()
   {
   }
   
   /**
    * Gets the singleton instance of this class. The first caller must ensure 
    * that a reference to that instance is maintained to avoid garbage 
    * collection.
    *
    * @return the singleton instance of this class, never <code>null</code>.
    * @throws PSServerConfigException if anything fails loading the 
    *    configurations from the repository.
    */
   public static PSConfigManager getInstance() throws PSServerConfigException
   {
      if (ms_manager == null)
      {
         ms_manager = new PSConfigManager();
         ms_manager.loadConfigs();
      }
         
      return ms_manager;
   }
   
   /**
    * Gets the rx configuration either in edit mode or read mode based on the 
    * lock and override parameters specified in request document. Currently only
    * 'relationships' configuration is allowed to edit. Expected document format
    * is:
    * <pre><code>
    * &lt;ELEMENT PSXDesignRxConfigLoad (EMPTY)>
    * &lt;ATTLIST PSXDesignerRxConfigLoad 
    *    name CDATA #REQUIRED
    *    lock (yes|no) "no"
    *    overrideSameUser (yes|no) "no"
    *    overrideDifferentUser (yes|no) "no" >
    * </code></pre>
    * 
    * @param inDoc the XML document containing the get request, may not be 
    * <code>null</code>.     
    * @param reqUser the user requesting to save, may not be <code>null</code>
    * or empty.
    * 
    * @return the config document that confirms to the dtd specified in 
    * <code>PSConfig</code>
    * 
    * @throws PSServerConfigException if the config for the specified name in 
    * request doc does not exist or if the configuration is not allowed to edit
    * and user requested the config to edit.
    * @throws PSLockedException if it can not acquire the lock to edit.
    * @throws IllegalArgumentException if any param is invalid.
    */
   public Document getRxConfiguration(Document inDoc, String reqUser) 
      throws PSServerConfigException, PSLockedException
   {
      if(inDoc == null)
         throw new IllegalArgumentException("inDoc may not be null.");
         
      Element root = inDoc.getDocumentElement();
      
      if(root == null)
         throw new IllegalArgumentException(
            "inDoc must have the document element");
         
      if(reqUser == null || reqUser.trim().length() == 0)
         throw new IllegalArgumentException(
            "reqUser may not be null or empty.");
      
      String name = root.getAttribute("name");
      boolean lock = XML_BOOL_TRUE.equalsIgnoreCase(root.getAttribute("lock"));
      boolean overrideSame = XML_BOOL_TRUE.equalsIgnoreCase(
         root.getAttribute("overrideSameUser"));      
      boolean overrideDifferent = XML_BOOL_TRUE.equalsIgnoreCase(
         root.getAttribute("overrideDifferent"));               
      
      Document cfgDoc = getRxConfiguration(name, lock, overrideSame, 
         overrideDifferent, reqUser);

      return cfgDoc;
   }
   
   /**
    * Gets the rx configuration either in edit mode or read mode based on the 
    * lock and override parameters specified. Currently only the 'relationships' 
    * configuration is allowed to be edited. 
    * 
    * @param name The name of the configuration to edit, may not be 
    * <code>null</code> or empty.
    * @param locked <code>true</code> to get the config locked for editing, 
    * <code>false</code> to get it read-only.
    * @param overrideSameUser <code>true</code> to override the lock if the
    * user specified by <code>reqUser</code> currently holds the lock, 
    * <code>false</code> to not allow the override.
    * @param overrideDifferentUser <code>true</code> to override the lock if a
    * user other than the user specified by <code>reqUser</code> currently holds 
    * the lock, <code>false</code> to not allow the override.
    * @param reqUser the user requesting to save, may not be <code>null</code>
    * or empty.
    * 
    * @return the config document that confirms to the dtd specified in 
    * <code>PSConfig</code>
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSServerConfigException if the config for the specified name in 
    * request doc does not exist or if the configuration is not allowed to edit
    * and user requested the config to edit.
    * @throws PSLockedException if it can not acquire the lock to edit.
    */
   public Document getRxConfiguration(String name, boolean locked, 
      boolean overrideSameUser, boolean overrideDifferentUser, String reqUser) 
         throws PSServerConfigException, PSLockedException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      
      if (reqUser == null || reqUser.trim().length() == 0)
         throw new IllegalArgumentException("reqUser may not be null or empty");
      
      PSConfig cfg = (PSConfig) m_configs.get(name);
      if (cfg == null)
      {
         Object[] args =
         {
            name
         };
         throw new PSServerConfigException(
            IPSServerErrors.UNKNOWN_CONFIGURATION, args);
      }      

      if(locked) //need to lock
      {
         if(cfg.isLocked()) 
         {
            if(cfg.getLocker().equals(reqUser)) //same user holding the lock
            {
               if(!overrideSameUser) //if not allowed to override
               {
                  throw new PSLockedException(
                     IPSServerErrors.CONFIG_LOCKED_SAME, 
                     new String[] { name } );
               }
            }
            //different user holding the lock and not allowed to override            
            else if(!overrideDifferentUser) 
            {
               throw new PSLockedException(IPSServerErrors.CONFIG_LOCKED, 
                  new String[] { name, cfg.getLocker() } );               
            }
         }
         
         if( name.equals(PSConfigurationFactory.RELATIONSHIPS_CFG) )
         {
            cfg.lock(reqUser);
            saveConfig(name);
         }
         else
         {
            throw new PSServerConfigException(
               IPSServerErrors.CONFIG_NOT_ALLOWED_EDIT, 
               new String[] { name } ); 
         }               
      }      
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      doc.appendChild(cfg.toXml(doc));
      
      return doc;
   }

   /**
    * Updates the lock state on the rx configuration and/or the rx configuration
    * specified in the request document. Expected document format is:
    * <pre><code>
    * &lt;ELEMENT PSXDesignRxConfigSave (CONFIGURATION?)>
    * &lt;ATTLIST PSXDesignRxConfigSave
    *    name CDATA #REQUIRED
    *    releaseLock (yes|no) "no" >
    * &lt;ELEMENT CONFIGURATION (#PCDATA)>
    * </code></pre>
    * 
    * @param inDoc the XML document containing the save request, may not be 
    * <code>null</code>.     
    * @param reqUser the user requesting to save, may not be <code>null</code>
    * or empty.
    * 
    * @throws PSServerConfigException if the config for specified name is not 
    * found.    
    * @throws PSNotLockedException if the user requesting to save is not the
    * locked user of rx configuration.
    * @throws IOException if an io error occurs saving the config.
    * @throws SAXException if an error occurs parsing the configuration.
    * @throws IllegalArgumentException if any param is invalid.
    */
   public void saveRxConfiguration(Document inDoc, String reqUser)
      throws PSServerConfigException, PSNotLockedException, IOException, 
      SAXException
   {
      if(inDoc == null)
         throw new IllegalArgumentException("inDoc may not be null.");
         
      Element root = inDoc.getDocumentElement();
      
      if(root == null)
         throw new IllegalArgumentException(
            "inDoc must have the document element");
         
      if(reqUser == null || reqUser.trim().length() == 0)
         throw new IllegalArgumentException(
            "reqUser may not be null or empty.");
      
      String name = root.getAttribute("name");
      boolean releaseLock = 
         XML_BOOL_TRUE.equalsIgnoreCase(root.getAttribute("releaseLock"));
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(inDoc);
      String config = tree.getElementData("CONFIGURATION");
      saveRxConfiguration(name, config, releaseLock, reqUser);
   }

   /**
    * Updates the lock state on the specified rx configuration and/or saves the 
    * supplied rx configuration.
    * 
    * @param name The name of the config, may not be <code>null</code> or empty.
    * @param config The config data, may be <code>null</code> in order to 
    * release the lock only.
    * @param releaseLock <code>true</code> to release the lock, 
    * <code>false</code> to keep the lock.
    * @param reqUser the user requesting to save, may not be <code>null</code>
    * or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSServerConfigException if the config for specified name is not 
    * found.    
    * @throws PSNotLockedException if the user requesting to save is not the
    * locked user of rx configuration.
    * @throws IOException if an io error occurs saving the config.
    * @throws SAXException if an error occurs parsing the configuration.
    */
   public void saveRxConfiguration(String name, String config, 
      boolean releaseLock, String reqUser) 
         throws PSServerConfigException, PSNotLockedException, IOException, 
      SAXException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      if (reqUser == null || reqUser.trim().length() == 0)
         throw new IllegalArgumentException("reqUser may not be null or empty");
   
      PSConfig cfg = (PSConfig) m_configs.get(name);
      if (cfg == null)
      {
         Object[] args =
         {
            name
         };
         throw new PSServerConfigException(
            IPSServerErrors.UNKNOWN_CONFIGURATION, args);
      }      
      
      if(!(cfg.isLocked() && cfg.getLocker().equals(reqUser)))
      {
         PSNotLockedException ex = new PSNotLockedException(
            IPSObjectStoreErrors.LOCK_NOT_HELD, name);
         throw ex;
      }
      
      //update the lock status if the user specified to release the lock,
      //so that calling saveConfig saves to database.
      if(releaseLock)
         cfg.releaseLock();
         
      //set new configuration
      if(config != null)
         cfg.setConfig(config);
         
      saveConfig(name);
   }
   
   /**
    * Returns the requested XML configuration.
    * 
    * @param name the configuration name, not <code>null</code> or empty.
    * @return the requested XML configuration, never <code>null</code>.
    * @throws IllegalArgumentException if the supplied name in <code>null</code>
    *    or empty.
    * @throws PSServerConfigException if no configuration exists for the 
    *    supplied name.
    */
   public Document getXMLConfig(String name) throws PSServerConfigException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");

      PSConfig cfg = (PSConfig) m_configs.get(name);
      if (cfg == null)
      {
         Object[] args =
         {
            name
         };
         throw new PSServerConfigException(
            IPSServerErrors.UNKNOWN_CONFIGURATION, args);
      }
      
      if (!cfg.isXML())
         throw new RuntimeException(
            "the requested config is not an XML config");

      return (Document) cfg.getConfig();
   }

   /**
    * Returns the requested properties configuration.
    * 
    * @param name the configuration name, not <code>null</code> or empty.
    * @return the requested properties configuration, never <code>null</code>.
    * @throws IllegalArgumentException if the supplied name in <code>null</code>
    *    or empty.
    * @throws PSServerConfigException if no configuration exists for the 
    *    supplied name.
    */
   public Properties getPropertyConfig(String name) throws PSServerConfigException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");

      PSConfig cfg = (PSConfig) m_configs.get(name);
      if (cfg == null)
      {
         Object[] args =
         {
            name
         };
         throw new PSServerConfigException(
            IPSServerErrors.UNKNOWN_CONFIGURATION, args);
      }

      if (cfg.isXML())
         throw new RuntimeException(
            "the requested config is not a Property config");

      return (Properties) cfg.getConfig();
   }
   
   /**
    * Sets the configuration document for the supplied name.
    * 
    * @param name the configuration name, not <code>null</code> or empty.
    * @param config the configuration to be set, not <code>null</code> or empty.
    * @throws PSServerConfigException if no configuration was found for the
    *    supplied name or the configuration is not of type XML. 
    */
   public void setConfig(String name, Document config)
      throws PSServerConfigException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");
      
      if (config == null)
         throw new IllegalArgumentException("config cannot be null");
         
      PSConfig cfg = (PSConfig) m_configs.get(name);
      if (cfg == null)
      {
         Object[] args =
         {
            name
         };
         throw new PSServerConfigException(
            IPSServerErrors.UNKNOWN_CONFIGURATION, args);
      }
      
      if (!cfg.isXML())
      {
         Object[] args =
         {
            config.getClass().getName(),
            "org.w3c.dom.Document"
         };
         throw new PSServerConfigException(
            IPSServerErrors.INVALID_CONFIG_OBJECT, args);
      }
      
      cfg.setConfig(config);
   }
   
   /**
    * Sets the configuration properties for the supplied name.
    * 
    * @param name the configuration name, not <code>null</code> or empty.
    * @param config the configuration to be set, not <code>null</code> or empty.
    * @throws PSServerConfigException if no configuration was found for the
    *    supplied name or the configuration is not of type property. 
    */
   public void setConfig(String name, Properties config)
      throws PSServerConfigException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name cannot be null or empty");
      
      if (config == null)
         throw new IllegalArgumentException("config cannot be null");
         
      PSConfig cfg = (PSConfig) m_configs.get(name);
      if (cfg == null)
      {
         Object[] args =
         {
            name
         };
         throw new PSServerConfigException(
            IPSServerErrors.UNKNOWN_CONFIGURATION, args);
      }
      
      if (!cfg.isProperty())
      {
         Object[] args =
         {
            config.getClass().getName(),
            "org.w3c.dom.Document"
         };
         throw new PSServerConfigException(
            IPSServerErrors.INVALID_CONFIG_OBJECT, args);
      }
      
      cfg.setConfig(config);
   }
   
   /**
    * Clears all server configurations and reloads them from the repository.
    * 
    * @throws PSServerConfigException if anything goes wrong reloading the 
    *    server configurations.
    */
   public void reloadConfigs() throws PSServerConfigException
   {
      m_configs.clear();
      
      loadConfigs();
   }
   
   /**
    * Loads all server configurations from the repository.
    * 
    * @throws PSServerConfigException if anything goes wrong loading the 
    *    server configurations.
    */
   private void loadConfigs() throws PSServerConfigException
   {
      try
      {
         IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
         Collection<PSConfig> configs = cmsMgr.findAllConfigs();
         for (PSConfig config : configs)
         {
            PSConsole.printMsg("Server", 
                  "Configuration loaded: " + config.getName());
            
            m_configs.put(config.getName(), config);
         }
      }
      catch (Exception e)
      {
         throw new PSServerConfigException(
            IPSServerErrors.ERROR_LOAD_CONFIGS, e.toString());
      }
   }
   
   /**
    * Save the supplied XML configuration to the repository.
    * 
    * @param name the configuration to be saved, not <code>null</code> or
    *    empty.
    * @param config the configuration to be saved, not <code>null</code>.
    * @throws IllegalArgumentException if the supplied name is <code>null</code>
    *    or empty.
    * @throws PSServerConfigExcception if no configuration exists for the
    *    provided name or the supplied configuration is not the expected type.
    */
   public void saveConfig(String name, Document config) 
      throws PSServerConfigException
   {
      setConfig(name, config);
      saveConfig(name);
   }
   
   /**
    * Save the supplied priperties configuration to the repository.
    * 
    * @param name the configuration to be saved, not <code>null</code> or
    *    empty.
    * @param config the configuration to be saved, not <code>null</code>.
    * @throws IllegalArgumentException if the supplied name is <code>null</code>
    *    or empty.
    * @throws PSServerConfigExcception if no configuration exists for the
    *    provided name or the supplied configuration is not the expected type.
    */
   public void saveConfig(String name, Properties config) 
      throws PSServerConfigException
   {
      setConfig(name, config);
      saveConfig(name);
   }
   
   /**
    * Save the addressed configuration to the repository.
    * 
    * @param name the configuration to be saved, not <code>null</code> or
    *    empty.
    * @throws IllegalArgumentException if the supplied name is <code>null</code>
    *    or empty.
    * @throws PSServerConfigExcception if no configuration exists for the
    *    provided name.
    */
   public void saveConfig(String name) throws PSServerConfigException
   {
      try
      {
         PSConfig cfg = m_configs.get(name);
         if (cfg == null)
         {
            Object[] args =
            {
               name
            };
            throw new PSServerConfigException(
               IPSServerErrors.UNKNOWN_CONFIGURATION, args);
         }

         IPSCmsObjectMgr cmsMgr = PSCmsObjectMgrLocator.getObjectManager();
         cmsMgr.saveConfig(cfg);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         String[] args = {name, e.getLocalizedMessage()};
         throw new PSServerConfigException(
               IPSServerErrors.ERROR_LOAD_CONFIGS, args);
      }
   }

   /**
    * A map of server configurations using the configuration name as key. The
    * values are of type PSConfig. Initialized during the first call to 
    * {@link #getInstance()}, never <code>null</code> after that, may be empty.
    */
   private Map<String, PSConfig> m_configs = new HashMap<>();
   
   /**
    * The singleton instance of this class. Initialized by the first call to
    * {@link #getInstance()}, never <code>null</code> after that.
    */
   private static PSConfigManager ms_manager = null;
      
   //xml attribute value constants
   private static final String XML_BOOL_TRUE = "yes";   
   private static final String XML_BOOL_FALSE = "no";
}
