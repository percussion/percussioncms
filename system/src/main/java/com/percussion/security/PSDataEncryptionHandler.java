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

package com.percussion.security;

import com.percussion.design.objectstore.PSDataEncryptor;
import com.percussion.utils.server.IPSCgiVariables;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;

import javax.servlet.ServletRequest;


/**
 * This class verifies that the appropriate data encryption settings are
 * in use and returns an error if not.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDataEncryptionHandler
{
   /**
    * Verify the data encryption settings and redirect the request or
    * report an error if they are not met.
    *
    * @param   request         the request to check
    *
    * @param   encryptor      the encryptor settings to enforce
    *
    * @return                  <code>true</code> if the conditions are met
    */
   public static boolean checkEncryption(
      PSRequest request, PSDataEncryptor encryptor)
   {
      if ((encryptor != null) && encryptor.isSSLRequired()) {
         // if SSL is required, first make sure this came in over SSL
         return checkSecureChannel(
            request, encryptor.getKeyStrength(), null);
      }

      return true;
   }

   /**
    * Verify a secure channel is being used and redirect the request or
    * report an error if not.
    *
    * @param   request         the request to check
    *
    * @param   keyStrength      the required key strength
    *
    * @param   urlFile         the file portion of the URL to use to
    *                           redirect the request if a secure channel
    *                           is required (protocol will be https and
    *                           host will be taken from the request)
    *
    * @return                  <code>true</code> if a secure channel is in use
    */
   public static boolean checkSecureChannel(
      PSRequest request, int keyStrength, String urlFile)
   {
      boolean isValid = true;

      // if SSL is required, first make sure this came in over SSL
      ServletRequest req = request.getServletRequest();
      boolean httpsOn = req.isSecure();
      if (!httpsOn) {
         isValid = false;

         // if null, we use the current request url
         if (urlFile == null)
            urlFile = request.getCgiVariable(IPSCgiVariables.CGI_SCRIPT_NAME);

         // try to redirect them to the HTTPS version of this page
         String redirectURL = "https://" + 
            req.getServerName() + ":" + req.getServerPort() + 
            urlFile;

         try{
            request.getResponse().sendRedirect(redirectURL, request);
         } catch (java.io.IOException e) {
            /* log this */
            Object[] args = { request.getUserSessionId(),
                  com.percussion.error.PSException.getStackTraceAsString(e) };
            com.percussion.log.PSLogManager.write(
               new com.percussion.log.PSLogServerWarning(
               com.percussion.server.IPSServerErrors.RESPONSE_SEND_ERROR, args,
               true, "DataEncryptionHandler"));
         }
      }
      else {
         // then verify the appropriate SSL key strength is in use
         int userKeyStrength;
         try {
            userKeyStrength = Integer.parseInt(
               request.getCgiVariable(IPSCgiVariables.CGI_HTTPS_KEYSIZE, "0"));
         } catch (NumberFormatException e) {
            // must not be set, so we'll assume 0 key strength
            userKeyStrength = 0;
         }

         if (userKeyStrength < keyStrength) {
            isValid = false;
            Object[] args = { request.getUserSessionId(), "" + keyStrength, "" + userKeyStrength };
            com.percussion.log.PSLogManager.write(
               new com.percussion.log.PSLogServerWarning(
               IPSSecurityErrors.SSL_KEY_STRENGTH_TOO_WEAK, args,
               true, "DataEncryptionHandler"));

            // send a response to through the request
            sendErrorResponse(request.getResponse(),
               IPSSecurityErrors.SSL_KEY_STRENGTH_TOO_WEAK, args);
         }
      }

      return isValid;
   }

   private static void sendErrorResponse(PSResponse resp, int errorCode, Object[] args)
   {
      PSDataEncryptionError err = new PSDataEncryptionError(errorCode, args);
      PSServer.getErrorHandler().reportError(resp, err);
   }
}
