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
package com.percussion.analytics.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.percussion.analytics.data.PSAnalyticsProviderConfig;
import com.percussion.analytics.error.IPSAnalyticsErrorMessageHandler;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.service.IPSAnalyticsProviderService;
import com.percussion.analytics.service.impl.google.PSGoogleAnalyticsErrorMessageHandler;
import com.percussion.analytics.service.impl.google.PSGoogleAnalyticsProviderHandler;
import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.util.PSSiteManageBean;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Implementation of the analytics provider service.
 * @author erikserating
 *
 */
@PSSiteManageBean("analyticsProviderService")
public class PSAnalyticsProviderService implements IPSAnalyticsProviderService
{
    private static final Log log = LogFactory.getLog(PSAnalyticsProviderService.class);
   @Autowired
   public PSAnalyticsProviderService(IPSMetadataService metadataService)
   {
      this.metadataService = metadataService;
   }
   
   /* (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderService#storeConfig(
    *  com.percussion.analytics.data.PSAnalyticsProviderConfig)
    */
   public void saveConfig(PSAnalyticsProviderConfig config)
   {
      /* Storing configuration to metadata service as a JSON string in the following format:
       *  {"uid": "userid",
       *   "password": "encryptedPassword",
       *   "params": {"name": "value"}}
       */

      String pwd = config.getPassword();
      String ePwd = null;
      
      PSAnalyticsProviderConfig current = loadConfig(true);
      
      if(pwd == null)
      {
         //Use stored password
        
         if(current != null) {
             ePwd = current.getPassword();
             config.setUserid(current.getUserid());
         }
      }
      else
      {
          try {
              ePwd = config.isEncrypted() ? pwd : PSEncryptor.getInstance("AES",
                      PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)
              ).encrypt(pwd);
          } catch (PSEncryptionException e) {
              ePwd = pwd;
              config.setEncrypted(false);
          }
      }

       config.setPassword(ePwd);

       com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
       String objectToJson = null;
       try {
           objectToJson = objectMapper.writeValueAsString(config);
       } catch (JsonProcessingException e) {
           log.error("Exception occurred while parsing - >" + e);
       }

      PSMetadata metadata = new PSMetadata(METADATA_KEY, objectToJson);
      metadataService.save(metadata);
   }   
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderService#deleteConfig()
    */
   public void deleteConfig()
   {
       metadataService.delete(METADATA_KEY);
   }
   

   /* (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderService#getStoredConfig(boolean)
    */
   public PSAnalyticsProviderConfig loadConfig(boolean encrypted)
   {

      PSMetadata metadata = metadataService.find(METADATA_KEY);
       PSAnalyticsProviderConfig config = null;
       String userID;
       if(metadata == null)
           return null;
       String json = metadata.getData();
       com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
       try {
           config = objectMapper.readValue(json, PSAnalyticsProviderConfig.class);
           String rawPwd = config.getPassword();
           userID=config.getUserid();
           if(userID==null){
               userID=config.getUid();
           }

           String pwd = null;
           try {
               pwd = encrypted ? rawPwd : PSEncryptor.getInstance("AES",
                           PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)).decrypt(rawPwd);
           } catch (PSEncryptionException | IllegalArgumentException  e) {
               pwd = PSLegacyEncrypter.getInstance(
               PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR)).decrypt(
               rawPwd, PSLegacyEncrypter.getInstance(
                  PathUtils.getRxDir().getAbsolutePath().concat(PSEncryptor.SECURE_DIR).CRYPT_KEY(),null);
           }

           config.setPassword(pwd);
           config.setUserid(userID);

       } catch (JsonProcessingException e) {
           log.error("Error parsing Analytics configuration: " + e.getMessage(),e);
       }

       return config;
   }

   /* (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderService#getProfiles(java.lang.String, java.lang.String)
    */
   public Map<String, String> getProfiles(String uid, String password) 
     throws PSAnalyticsProviderException
   {
      if(StringUtils.isBlank(uid) || StringUtils.isBlank(password))
      {
         //one of the creds is null, try to use stored cred
         PSAnalyticsProviderConfig config = loadConfig(false);
         if(config != null)
         {
            if(StringUtils.isBlank(uid))
               uid = config.getUserid();
            if(StringUtils.isBlank(password))
               password = config.getPassword();
         }
         else
         {
            throw new PSAnalyticsProviderException("User id and password are both required.");
         }
      }
      return handler.getProfiles(uid, password);
   }

   /* (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderService#testConnection(java.lang.String, java.lang.String)
    */
   public void testConnection(String uid, String password) throws PSAnalyticsProviderException
   {
      if(StringUtils.isBlank(uid) || StringUtils.isBlank(password))
      {
         //one of the creds is null, try to use stored cred
         PSAnalyticsProviderConfig config = loadConfig(false);
         if(config != null)
         {
            if(StringUtils.isBlank(uid))
               uid = config.getUserid();
            if(StringUtils.isBlank(password))
               password = config.getPassword();
         }
         else
         {
            throw new PSAnalyticsProviderException("User id and keyfile are both required.");
         }
      } 
      else
      {
          PSAnalyticsProviderConfig config = loadConfig(false);
          if(config == null || !StringUtils.equalsIgnoreCase(config.getUserid(), uid) || 
                  (StringUtils.isNotEmpty(password) ) || 
                  !StringUtils.equalsIgnoreCase(config.getUserid(), uid) )
          {
              config = new PSAnalyticsProviderConfig(uid,password,false, null);
              
              saveConfig(config);
          }
      }
     
      handler.testConnection(uid, password);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderService#isProfileConfigured(java.lang.String)
    */
   public boolean isProfileConfigured(String sitename)
   {
      return getSiteProfile(sitename) != null;     
   }   
   
       
   /* (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderService#getProfileId(java.lang.String)
    */
   public String getProfileId(String sitename)
   {
       return getProfileProperty(sitename, 0);
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderService#getTrackingCode(java.lang.String)
    */
   public String getWebPropertyId(String sitename)
   {
       return getProfileProperty(sitename, 1);
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderService#getApiKey(java.lang.String)
    */
   public String getGoogleApiKey(String sitename)
   {
       return getProfileProperty(sitename, 2);
   }
   
   /**
    * Gets the specified profile property for the specified site.
    * 
    * @param sitename the name of the site, assumed not blank.
    * @param propertyIndex the index of the property. Assume it is 0, 1 or 2, 
    * which is 'profile id', 'web property ID' or 'API key'.
    * 
    * @return the profile property. It is <code>null</code> if it is not configured for the site.
    */
   private String getProfileProperty(String sitename, int propertyIndex)
   {
       String profile = getSiteProfile(sitename);
       if (profile == null)
           return null;

       String[] properties = profile.split("\\|");
       if (properties.length <= propertyIndex)
           return null;
       
       return properties[propertyIndex];
   }
   
   /**
    * Gets the profile for the specified site. The profile contains <profile-id>|<web-property-id>|<api-key>.
    * The API key is optional. If the profile is configured correctly, it should contain profile-id and
    * tracking-code.
    * 
    * @param sitename the name of the site, assumed not blank.
    * 
    * @return the profile of the site. It may be <code>null</code> if it is not configured for the site.
    */
   private String getSiteProfile(String sitename)
   {
       if(StringUtils.isBlank(sitename))
           throw new IllegalArgumentException("sitename cannot be null or empty.");
        PSAnalyticsProviderConfig config = loadConfig(false);
        if(config == null)
           return null;
        Map<String, String> params = config.getExtraParamsMap();
        if(params != null && !params.isEmpty())
        {
           return params.get(sitename);
        }
        else
        {
           return null;
        }       
   }
   
   /* (non-Javadoc)
    * @see com.percussion.analytics.service.IPSAnalyticsProviderService#getErrorMessageHandler()
    */
   public IPSAnalyticsErrorMessageHandler getErrorMessageHandler()
   {
      return messageHandler;
   }
   
   /**
    * The provider handler. Right now this is hard coded to use the Google Analytics handler.
    * When a new provider is implemented we will need to have spring load the appropriate
    * handler.
    */
   private final IPSAnalyticsProviderHandler  handler = new PSGoogleAnalyticsProviderHandler();
   
   /**
    * The provider error message handler. Right now this is hard coded to use the Google Analytics message handler.
    * When a new provider is implemented we will need to have spring load the appropriate
    * handler.
    */
   private final IPSAnalyticsErrorMessageHandler messageHandler = new PSGoogleAnalyticsErrorMessageHandler();
   
   /**
    * The metadata service. Initialized via the constructor. Never <code>null</code>
    * after that.
    */
   private IPSMetadataService metadataService;
   
   /**
    * The metadata key to store the config.
    */
   public static final String METADATA_KEY = "perc.analytics.provider.config";
   


  

   

}
