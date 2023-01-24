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

import com.percussion.log.PSLogError;
import com.percussion.log.PSLogSubMessage;
import com.percussion.server.IPSServerErrors;

import java.util.Locale;


/**
 * The PSDataConversionError class is used to report an error
 * encountered during data conversion.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDataConversionError extends PSLogError {
   
   /**
    * Report a data conversion error.
    * <p>
    * The application id is most commonly obtained by calling
    * {@link com.percussion.data.PSExecutionData#getId PSExecutionData.getId()}
    * <p>
    * The session id can be obtained from the
    * {@link com.percussion.server.PSUserSession PSUserSession} object
    * contained in the
    * {@link com.percussion.server.PSRequest PSRequest} object.
    *
    * @param      applId         the id of the application that generated
    *                            the error
    *
    * @param      sessionId      the session id of the user making the
    *                            request
    *
    * @param      errorCode      the error code describing the type of error
    *
    * @param      errorParams    if the error string associated with the
    *                            error code specifies parameters, this is
    *                            an array of values to use to fill the string
    *                            appropriately. Be sure to include the
    *                            correct arguments in their correct
    *                            positions!
    *
    * @param      sourceType     the data type of the source data
    *
    * @param      sourceData     the raw source data
    *
    * @param      targetType     the desired target data type
    */
   public PSDataConversionError( int applId,
                                 java.lang.String sessionId,
                                 int errorCode,
                                 Object[] errorParams,
                                 java.lang.String sourceType,
                                 java.lang.String sourceData,
                                 java.lang.String targetType)
   {
      super(applId);

      if (sessionId == null)
         m_sessId = "";
      else
         m_sessId = sessionId;

      m_errorCode      = errorCode;
      m_errorArgs      = errorParams;
      m_sourceType   = sourceType;
      m_sourceData   = sourceData;
      m_targetType   = targetType;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[3];

      /* use IPSServerErrors.DATA_CONV_ERROR along with:
       *    [0] = m_sessId
       *    [1] = m_sourceType
       *    [2] = m_targetType
       * to format the first submessage
       */
      msgs[0] = new PSLogSubMessage(
                                 IPSServerErrors.DATA_CONV_ERROR,
                                 PSErrorManager.createMessage(
                                       IPSServerErrors.DATA_CONV_ERROR, 
                                       new Object[] { m_sessId, m_sourceType, m_targetType },
                                       loc));

      /* use IPSServerErrors.RAW_DUMP along with:
       *    [0] = m_sourceData
       * to format the second submessage
       */
      msgs[1] = new PSLogSubMessage(
                                 IPSServerErrors.RAW_DUMP,
                                 PSErrorManager.createMessage(
                                       IPSServerErrors.RAW_DUMP,
                                       new Object[] { m_sourceData },
                                       loc));

      /* use the errorCode/errorParams to format the third submessage */
      msgs[2] = new PSLogSubMessage(
                                 m_errorCode,
                                 PSErrorManager.createMessage(
                                       m_errorCode, m_errorArgs, loc));

      return msgs;
   }


   private String      m_sessId;
   private int         m_errorCode;
   private Object[]   m_errorArgs;
   private String      m_sourceType;
   private String      m_sourceData;
   private String      m_targetType;
}

