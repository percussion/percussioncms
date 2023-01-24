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
package com.percussion.rx.design.impl;

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.error.PSException;
import com.percussion.rx.design.IPSAssociationSet;
import com.percussion.server.config.PSConfigManager;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.util.IOTools;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PSRelationshipConfigModel extends PSDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      return loadRelationshipConfig(guid, true);
   }
   
   @Override
   public Object loadModifiable(IPSGuid guid)
   {
      return loadRelationshipConfig(guid, false);
   }

   @Override
   public Object load(String name)
   {
      return loadRelationshipConfig(name, true);
      
   }
   
   @Override
   public Object loadModifiable(String name)
   {
      return loadRelationshipConfig(name, false);
   }
   
   /**
    * Loads the readonly or modifiable relationship config from the for the
    * supplied guid based on the readonly flag.
    * 
    * @param guid Must not be <code>null</code> and must be a relationship
    * config guid.
    * @param readonly Flag to indicate whether to load a readonly or modifiable
    * relationship config.
    * @return Object relationship config object never <code>null</code>,
    * throws {@link RuntimeException} in case of an error.
    */
   private Object loadRelationshipConfig(IPSGuid guid, boolean readonly)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      PSRelationshipConfigSet cfgSet = PSRelationshipCommandHandler
            .getConfigurationSet();
      PSRelationshipConfig config = null;
      // Right now for the configurations the read only and readwrite objects
      // are same
      if (readonly)
         config = cfgSet.getConfig(guid);
      else
         config = cfgSet.getConfig(guid);
      if (config == null)
      {
         String msg = "Failed to get the design object for guid {0}";
         Object[] args = { guid };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return config;

   }
   
   /**
    * Loads the readonly or modifiable relationship config from the for the
    * supplied name based on the readonly flag.
    * 
    * @param guid Must not be <code>null</code> and must be a relationship
    * config guid.
    * @param readonly Flag to indicate whether to load a readonly or modifiable
    * relationship config.
    * @return Object relationship config object never <code>null</code>,
    * throws {@link RuntimeException} in case of an error.
    */
   private Object loadRelationshipConfig(String name, boolean readonly)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      PSRelationshipConfigSet cfgSet = 
         PSRelationshipCommandHandler.getConfigurationSet();
      PSRelationshipConfig config = null;
      //Right now for the configurations the read only and readwrite objects are same
      if(readonly)
         config = cfgSet.getConfig(name);
      else
         config = cfgSet.getConfig(name);
         
      if (config == null)
      {
         String msg = "Failed to get the design object for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      return config;
   }
   
   @Override
   public Long getVersion(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
  
      Long version = null;
      
      try
      {
         PSRelationshipConfigSet cfgSet = 
            PSRelationshipCommandHandler.getConfigurationSet();
         PSRelationshipConfig config = cfgSet.getConfig(name);
         if (config != null)
         {
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            version = IOTools.getChecksum(PSXmlDocumentBuilder.toString(
                  config.toXml(doc)));
         }
      }
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
      
      if (version == null)
      {
         String msg = "Failed to get the design object version for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      
      return version;
   }
   
   @Override
   public void delete(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      try 
      {         
         PSRelationshipConfigSet cfgSet = 
            PSRelationshipCommandHandler.getConfigurationSet();
         cfgSet.deleteConfig(name);
         PSWebserviceUtils.saveRelationshipConfigSet(cfgSet, 
               IPSWebserviceErrors.DELETE_FAILED);
      }
      catch (Exception e) 
      {
         String msg = "Failed to delete the object with name ({0}).";
         Object[] margs = { name };
         throw new RuntimeException(MessageFormat.format(msg, margs), e);
      }
   }
   
   @Override
   public void save(Object obj)
   {
      save(obj, null);
   }

   @Override
   public void save(Object obj, List<IPSAssociationSet> associationSets)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj must not be null");

      if (!(obj instanceof PSRelationshipConfig))
      {
         throw new RuntimeException("Invalid Object passed for save.");
      }
      try
      {
         PSRelationshipConfig config = (PSRelationshipConfig) obj;
         PSRelationshipCommandHandler.loadConfigs();
         PSRelationshipConfigSet configSet = PSRelationshipCommandHandler
               .getConfigurationSet();
         PSRelationshipConfig tgtConfig = configSet
               .getConfig(config.getName());
         if (tgtConfig != null)
            tgtConfig.copyFrom(config);
         else
            configSet.add(config);
         saveRelationshipConfigSet(configSet);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error saving relationship config model",e);
      }
   }
   
   /**
    * Saves the specified relationship configurations into the repository
    *  
    * @param configSet the to be saved relationship config set, may not be 
    *    <code>null</code>.
    * @param errorCode the error code should an error occurs while saving
    *    the relationship configs.
    * 
    * @throws PSErrorException
    * @throws PSException 
    */
   public static void saveRelationshipConfigSet(
         PSRelationshipConfigSet configSet)
      throws PSErrorException, PSException
   {
      if (configSet == null)
         throw new IllegalArgumentException("configSet may not be null");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = configSet.toXml(doc);
      PSXmlDocumentBuilder.replaceRoot(doc, root);
      PSConfigManager.getInstance().saveConfig(
            PSConfigurationFactory.RELATIONSHIPS_CFG, doc);

      // reset the cached relationship configs
      PSRelationshipCommandHandler.reloadConfigs();
      IPSRelationshipService relsvc = PSRelationshipServiceLocator
            .getRelationshipService();
      relsvc.reloadConfigs();
   }
   
   @Override
   public IPSGuid nameToGuid(String name)
   {
      IPSGuid guid = null;
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be blank");
      PSRelationshipConfigSet cfgSet = 
         PSRelationshipCommandHandler.getConfigurationSet();
      PSRelationshipConfig config = cfgSet.getConfig(name);
      if(config != null)
         guid = config.getGUID();
      return guid;
   }
   
   @Override
   public String guidToName(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      String name = "";
      PSRelationshipConfigSet cfgSet = 
         PSRelationshipCommandHandler.getConfigurationSet();
      PSRelationshipConfig config = cfgSet.getConfig(guid);
      if(config != null)
         name = config.getName();
      return name;
   }
   
   @Override
   public void delete(IPSGuid guid)
   {
      if (guid == null || !isValidGuid(guid))
         throw new IllegalArgumentException("guid is not valid for this model");
      String name = guidToName(guid);
      if(StringUtils.isNotBlank(name))
      {
         delete(name);
      }
   }
}
