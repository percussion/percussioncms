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
package com.percussion.error;

import com.percussion.design.objectstore.PSNotifier;
import com.percussion.design.objectstore.PSRecipient;
import com.percussion.log.PSLogError;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.PSCollection;
import com.percussion.util.PSMapClassToObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The PSErrorHandlerTest class tests running the major methods of
 * the PSErrorHandler objects.
 *
 * @author    Jian Huang
 * @version   1.0
 * @since     1.0
 */
public class PSErrorHandlerTest
{
   private static final Logger log = LogManager.getLogger(PSErrorHandlerTest.class);
   public static void main(String[] args)
   {
      if (args.length != 3)
      {
         log.info("usage: java com.percussion.error.PSErrorHandlerTest send-to smtp-host from");
         log.info("");
         log.info("  eg: java com.percussion.error.PSErrorHandlerTest myname@percussion.com pan rhythmyx@percussion.com");
         System.exit(1);
      }

      // PSErrorWebPages errPage = new PSErrorWebPages();
      PSMapClassToObject errPage = new PSMapClassToObject();
      PSNotifier      notify     = null;
      int errCode = 0;
      String ip = null;
      String login = null;
      String sessId = null;

      String emailAddress = args[0];
      String host = args[1];
      String from = args[2]; // only allows "user@domain" format

      try{
         PSCollection collect = new PSCollection(com.percussion.design.objectstore.PSRecipient.class);
         PSRecipient recipient = new PSRecipient(emailAddress);

         recipient.setSendEnabled(true);
         recipient.setErrorThresholdByCount(true);
         recipient.setErrorThresholdByInterval(false);
         recipient.setErrorThresholdInterval(1);   // set one min. to wait
         recipient.setErrorThresholdCount(3);      // send every 3 error counts

         recipient.setAppAuthorizationFailureEnabled(true);
         recipient.setAppAuthorizationFailureCount(2);   // send every 2 counts

         recipient.setBackEndDataConversionErrorEnabled(true);
         recipient.setAppValidationErrorEnabled(true);
         recipient.setAppRequestQueueLargeEnabled(true);
         recipient.setAppRequestQueueMax(15);
         recipient.setAppResponseTimeEnabled(true);
         recipient.setAppResponseTimeMax(1000);
         collect.add(recipient);

         notify = new PSNotifier(PSNotifier.MP_TYPE_SMTP, host);  // server=host
         notify.setRecipients(collect);
         notify.setFrom(from);

         PSErrorHandler errHandler = new PSErrorHandler(errPage, true, notify);

         errCode = IPSServerErrors.AUTHORIZATION_ERROR;
         ip = "38.131.120.53";
         login = "zzz";
         PSApplicationAuthorizationError appAuthErr = new
                  PSApplicationAuthorizationError(11, ip, login, errCode, null);
         errHandler.notifyAdmins((PSLogError)appAuthErr);

         ip = "38.161.134.51";
         login = "aaa";
         appAuthErr = new PSApplicationAuthorizationError(21, ip, login, errCode, null);
         errHandler.notifyAdmins((PSLogError)appAuthErr);
         appAuthErr = new PSApplicationAuthorizationError(22, ip, login, errCode, null);
         errHandler.notifyAdmins((PSLogError)appAuthErr);

         errCode = IPSServerErrors.DATA_CONV_ERROR;
         sessId = "session_dataConversion";
         PSDataConversionError conversionErr = new
               PSDataConversionError(31, sessId, errCode, null, null, null, null);
         errHandler.notifyAdmins((PSLogError)conversionErr);

         errCode = IPSServerErrors.VALIDATION_ERROR;
         sessId = "session_validation";
         PSValidationError validErr = new
               PSValidationError(41, sessId, errCode, null, null);
         errHandler.notifyAdmins((PSLogError)validErr);

         sessId = "session_queue";
         PSLargeRequestQueueError qErr = new PSLargeRequestQueueError(51, sessId, 20);
         errHandler.notifyAdmins((PSLogError)qErr);

         sessId = "session_responseTime";
         PSPoorResponseTimeError respErr = new PSPoorResponseTimeError(61, sessId, 1500);
         errHandler.notifyAdmins((PSLogError)respErr);

         errCode = IPSServerErrors.AUTHORIZATION_ERROR;
         ip = "38.131.120.53";
         login = "zzz";
         appAuthErr = new PSApplicationAuthorizationError(12, ip, login, errCode, null);
         errHandler.notifyAdmins((PSLogError)appAuthErr);


         errCode = IPSServerErrors.DATA_CONV_ERROR;
         sessId = "session_dataConversion";
         conversionErr = new
               PSDataConversionError(101, sessId, errCode, null, null, null, null);
         errHandler.notifyAdmins((PSLogError)conversionErr);
         errHandler.shutdown();

         log.info("Test finished.");
      }
      catch (PSIllegalArgumentException e1)
      {
         log.error("Caught PSIllegalArgumentException.");
         log.debug(e1.getMessage(), e1);
      }
   }
}
