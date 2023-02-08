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
package com.percussion.analytics.service;

import com.percussion.analytics.data.PSAnalyticsProviderConfig;
import com.percussion.analytics.error.IPSAnalyticsErrorMessageHandler;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.exception.PSValidationException;

import java.util.Map;

/**
 * Connects to an analytics provider pulling data from the provider into
 * the local database, based on the Analytics provider handler used. I.e Google Analytics. 
 * @author erikserating
 *
 */
public interface IPSAnalyticsProviderService
{
   
   /**
    * Sets the credentials used to access the analytics account. May include
    * additional data specific to the provider that selects a specific set of
    * data within the account. These are stored in persistent storage and re-used for
    * the site until a new set of credentials is assigned. 
    * 
    * @param config The analytics <code>PSAnalyticsProviderConfig</code> object. Cannot
    * be <code>null</code>.
    */
   public void saveConfig(PSAnalyticsProviderConfig config) throws IPSGenericDao.LoadException, IPSGenericDao.SaveException, PSValidationException;
   
   /**
    * Deletes the stored configuration if exists.
    */
   public void deleteConfig() throws IPSGenericDao.LoadException, IPSGenericDao.DeleteException;
   
   /**
    * Returns the stored analytics provider config if one exists. 
    * @param encrypted if <code>true</code> then the password will be encrypted
    * in the returned config object.
    * @return the config object or <code>null</code> if not found.
    */
   public PSAnalyticsProviderConfig loadConfig(boolean encrypted) throws IPSGenericDao.LoadException, PSValidationException;
   
   /**
    * Retrieves a list of "profiles" from the provider. Profiles are basically id's used to
    * get access to a particular data set from the provider.
    * @param uid the user id for access to the provider. May be <code>null</code> or empty. If
    * so it will attempt to use stored uid or error if it does not find one.
    * @param password the password for access to the provider. May be <code>null</code> or empty.
    * If so it will attempt to use stored password or error if it does not find one.
    * @return a map of strings, with the key being the profile|webpropertyId value and the value being the
    * profile display value. Never <code>null</code>, may be empty.
    * @throws PSAnalyticsProviderException, upon any error.
    */
   public Map<String, String> getProfiles(String uid, String password) throws PSAnalyticsProviderException, IPSGenericDao.LoadException, PSValidationException;
   
   /**
    * Tests a connection to the provider using the specified credentials.
    * @param uid the user id for access to the provider. May be <code>null</code> or empty. If
    * so it will attempt to use stored uid or error if it does not find one.
    * @param password the password for access to the provider. May be <code>null</code> or empty.
    * If so it will attempt to use stored password or error if it does not find one.
    * @throws PSAnalyticsProviderException if failed to connect with the specified parameters.
    */
   public void testConnection(String uid, String password) throws PSAnalyticsProviderException, IPSGenericDao.LoadException, IPSGenericDao.SaveException, PSValidationException;
   
   
   /**
    * Indicates if an analytics profile is configured for the specified site.
    * @param sitename the name of the site to check. Cannot be <code>null</code> or empty.
    * @return <code>true</code> if the profile is configured for the site
    */
   public boolean isProfileConfigured(String sitename) throws IPSGenericDao.LoadException, PSValidationException;
   
   /**
    * Returns the configured profile id|webpropertyId for the specified sitename.
    * @param sitename the name of the site to check. Cannot be <code>null</code> or empty.
    * @return the profile id string or <code>null</code> if not set.
    */
   public String getProfileId(String sitename) throws IPSGenericDao.LoadException;
   
   /**
    * Gets the Web Property ID for the specified site.
    * @param sitename the name of the site, not blank.
    * @return the web property ID. It may be <code>null</code> if it is not configured for the site.
    */
   public String getWebPropertyId(String sitename) throws IPSGenericDao.LoadException;
   
   
   /**
    * Gets the Google API key for the specified site.
    * @param sitename the name of the site, not blank.
    * @return the API key. It may be <code>null</code> if it is not configured for the site.
    */
   public String getGoogleApiKey(String sitename) throws IPSGenericDao.LoadException;
   
   /**
    * Get the proper error message handler for the analytics provider service in use.
    * @return the message handler, never <code>null</code>.
    */
   public IPSAnalyticsErrorMessageHandler getErrorMessageHandler();
   
   
   
}
