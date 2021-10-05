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

package com.percussion.error;

import com.percussion.log.PSLogError;
import com.percussion.log.PSLogSubMessage;
import com.percussion.server.IPSServerErrors;

import java.util.Locale;

/**
 * The PSUnknownProcessingError class is used to report unexpected errors.
 * This usually occurs when an exception is thrown which the code did not
 * anticipate.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSUnknownProcessingError extends PSLogError
{
   /**
    * Report an unknown processing error.
    * The error code and parameters should
    * clearly define where the error occurred for easy debugging.
    *
    * @param      applId      the id of the application in error
    *                           (0 for server)
    *
    * @param      sessId      the session id of the requestor
    *
    * @param      errorCode   the error code describing the type of error
    *
    * @param      errorParams   if the error string associated with the
    *                           error code specifies parameters, this is
    *                           an array of values to use to fill the string
    *                           appropriately. Be sure to include the
    *                           correct arguments in their correct
    *                           positions!
    */
   public PSUnknownProcessingError(
      int applId, String sessId, int errorCode, Object[] errorParams)
   {
      super(applId);
      m_sessId = sessId;
      m_errorCode = errorCode;
      m_errorArgs = errorParams;
   }

   /**
    * Report am unknown processing error.
    * The error code and parameters should
    * clearly define where the error occurred for easy debugging.
    *
    * @param      applId      the id of the application in error
    *                           (0 for server)
    *
    * @param      sessId      the session id of the requestor
    *
    * @param      errorCode   the error code describing the type of error
    *
    *
    * @param      singleArg   the argument to use as the sole argument in
    *                           the error message
    */
   public PSUnknownProcessingError(
      int applId, String sessId, int errorCode, Object singleArg)
   {
      this(applId, sessId, errorCode, new Object[] { singleArg });
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[2];

      /* the generic submessage first */
      msgs[0]   = new PSLogSubMessage(
         IPSServerErrors.UNKNOWN_PROCESSING_ERROR,
         PSErrorManager.createMessage(
            IPSServerErrors.UNKNOWN_PROCESSING_ERROR,
            new Object[] { m_sessId }, loc));

      /* use the errorCode/errorParams to format the second submessage */
      msgs[1]   = new PSLogSubMessage(
         m_errorCode,
         PSErrorManager.createMessage(m_errorCode, m_errorArgs, loc));

      return msgs;
   }


   private int         m_errorCode;
   private Object[]   m_errorArgs;
   private String      m_sessId;
}

