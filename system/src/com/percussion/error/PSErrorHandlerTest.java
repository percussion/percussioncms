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
package com.percussion.error;

import com.percussion.design.objectstore.PSNotifier;
import com.percussion.design.objectstore.PSRecipient;
import com.percussion.log.PSLogError;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.PSCollection;
import com.percussion.util.PSMapClassToObject;

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
   public static void main(String[] args)
   {
      if (args.length != 3)
      {
         System.out.println("usage: java com.percussion.error.PSErrorHandlerTest send-to smtp-host from");
         System.out.println();
         System.out.println("  eg: java com.percussion.error.PSErrorHandlerTest myname@percussion.com pan rhythmyx@percussion.com");
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

         System.out.println("Test finished.");
      }
      catch (PSIllegalArgumentException e1)
      {
         System.err.println("Caught PSIllegalArgumentException.");
      }
   }
}
