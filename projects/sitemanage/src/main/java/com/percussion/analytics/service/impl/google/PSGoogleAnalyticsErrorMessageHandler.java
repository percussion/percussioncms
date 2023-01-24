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

import com.percussion.analytics.error.IPSAnalyticsErrorMessageHandler;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.error.PSAnalyticsProviderException.CAUSETYPE;

import java.util.HashMap;
import java.util.Map;

/**
 * @author erikserating
 *
 */
public class PSGoogleAnalyticsErrorMessageHandler
         implements
            IPSAnalyticsErrorMessageHandler
{

   /* (non-Javadoc)
    * @see com.percussion.analytics.error.IPSAnalyticsErrorMessageHandler#getMessage(
    * com.percussion.analytics.error.PSAnalyticsProviderException)
    */
   public String getMessage(PSAnalyticsProviderException e)
   {
      String preMsg = "Unable to retrieve analytics data.  ";
      String errorMsg;
      CAUSETYPE causetype = e.getCauseType();
      if (causetype != null)
      {           
         errorMsg = messages.get(causetype); 
         if(errorMsg == null)
            errorMsg = e.getLocalizedMessage();
      }
      else
      {
          errorMsg = e.getLocalizedMessage();
      }
      
      return preMsg + errorMsg;
  }
   
   private static Map<CAUSETYPE, String> messages = new HashMap<>();
   static
   {
      messages.put(CAUSETYPE.ACCOUNT_DELETED, "The analytics account has been deleted.");
      messages.put(CAUSETYPE.ACCOUNT_DISABLED, "The analytics account has been disabled.");         
      messages.put(CAUSETYPE.ANALYTICS_NOT_CONFIG, "Please use the Google Setup gadget to connect to your analytics account.");         
      messages.put(CAUSETYPE.AUTHENTICATION_ERROR, "The analytics account could not be authenticated.");         
      messages.put(CAUSETYPE.NO_PROFILE, "Please use the Google Setup gadget to select a profile for the desired site(s).");         
      messages.put(CAUSETYPE.NO_ANALYTICS_ACCOUNT, "A valid analytics account is required.");         
      messages.put(CAUSETYPE.NOT_VERIFIED, "The analytics account could not be verified.");         
      messages.put(CAUSETYPE.INVALID_CREDS, "Invalid Google configuration. Please use the Google Setup gadget to connect to your "
             + "analytics account.");         
      messages.put(CAUSETYPE.INVALID_DATA, "Invalid data.");         
      messages.put(CAUSETYPE.SESSION_EXPIRED, "The current session has expired.");         
      messages.put(CAUSETYPE.SERVICE_UNAVAILABLE, "The service is currently unavailable.");         
      messages.put(CAUSETYPE.TERMS_NOT_AGREED, "Terms not agreed.");
         
   }
   
}

