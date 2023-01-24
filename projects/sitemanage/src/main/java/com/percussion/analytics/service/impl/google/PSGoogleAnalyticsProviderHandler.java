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
package com.percussion.analytics.service.impl.google;

import com.google.api.services.analytics.Analytics;
import com.google.api.services.analytics.model.*;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.error.PSAnalyticsProviderException.CAUSETYPE;
import com.percussion.analytics.service.IPSAnalyticsProviderService;
import com.percussion.analytics.service.impl.IPSAnalyticsProviderHandler;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provider handler for the Google Analytics service.
 * @author erikserating
 *
 */
public class PSGoogleAnalyticsProviderHandler
         implements
            IPSAnalyticsProviderHandler
{
    private static final Logger log = LogManager.getLogger(PSGoogleAnalyticsProviderHandler.class);
  /* *//* (non-Javadoc)
    * @see com.percussion.analytics.service.impl.IPSAnalyticsProviderHandler#getProfiles(java.lang.String, java.lang.String)
    */
    public Map<String, String> getProfiles(String uid, String password) throws PSAnalyticsProviderException, PSValidationException
    {
        Map<String, String> profiles = new LinkedHashMap<>();
        Map<String, String[]> temp = new TreeMap<>();
        try {
            Analytics analytics = PSGoogleAnalyticsProviderHelper.getInstance()
                    .getAnalyticsService(uid, password);

            Accounts accounts =  analytics.management().accounts().list().execute();
            if (accounts.getItems().isEmpty()) {
                log.error("No accounts found");
            } else {
                List<Account> accountList = accounts.getItems();
                for (Account localAccountObj : accountList) {
                    Webproperties webproperties = analytics.management().webproperties().list(localAccountObj.getId()).execute();
                    if (!webproperties.getItems().isEmpty()) {
                        List<Webproperty> webPropertyList = webproperties.getItems();
                        for (Webproperty localWebProperty : webPropertyList) {
                            // Query profiles collection.
                            Profiles profilesObjects = analytics.management().profiles().list(localWebProperty.getAccountId(), localWebProperty.getId()).execute();
                            if (!profilesObjects.getItems().isEmpty()) {
                                List<Profile> profileList = profilesObjects.getItems();
                                for (Profile localProfileObj : profileList) {
                                    log.debug("Account ID: " + localProfileObj.getAccountId());
                                    log.debug("Web Property ID: " + localProfileObj.getWebPropertyId());
                                    log.debug("Web Property Internal ID: " + localProfileObj.getInternalWebPropertyId());
                                    log.debug("Profile ID: " + localProfileObj.getId());
                                    log.debug("Profile Name: " + localProfileObj.getName());


                                    String pId = localProfileObj.getId();
                                    String title = localProfileObj.getName();
                                    String wpId = localProfileObj.getWebPropertyId();
                                    String displayVal = title + " (" + wpId + ")";

                                    String[] val = {pId + "|" + wpId, displayVal};
                                    // First we put in treemap with a special key to get the
                                    // entries to sort by webpropertyId then by display value.
                                    temp.put(wpId + "_" + displayVal.toLowerCase(), val);


                                }
                            }
                        }
                    }
                }
            }
            // Now we add to the linked hash map to get the key/value we want
            // and we maintain
            // the desired sorting from the tree map.
            for (String key : temp.keySet())
            {
                String[] v = temp.get(key);
                profiles.put(v[0], v[1]);
            }
        } catch (Exception e)
        {
            if(e instanceof PSValidationException){
                throw (PSValidationException)e;
            }
            throw new PSAnalyticsProviderException("Error occurred while attempting to retrieve profiles: "
                    + e.getLocalizedMessage(), e);
        }
        return profiles;
    }

   /* (non-Javadoc)
    * @see com.percussion.analytics.service.impl.IPSAnalyticsProviderHandler#testConnection(java.lang.String, java.lang.String)
    */
   public void testConnection(String uid, String password) throws PSValidationException, PSAnalyticsProviderException {
      try
      {
         PSGoogleAnalyticsProviderHelper.getInstance().
                 getAnalyticsService(uid, password);
         getProfiles(uid, password);
      }     
      catch (PSAnalyticsProviderException  e)
      {
         if(e.getCauseType() == CAUSETYPE.NO_ANALYTICS_ACCOUNT)
         {
            String msg = "No Analytics account found for the specified user.";
             PSValidationErrorsBuilder builder = new PSValidationErrorsBuilder(this.getClass().getCanonicalName());
             builder.reject(PSAnalyticsProviderException.CAUSETYPE.INVALID_CREDS.toString(), msg).throwIfInvalid();

         }
         throw e;
      }
   }   
   
   @SuppressWarnings("unused")
   private IPSAnalyticsProviderService providerService;

   
   
}
