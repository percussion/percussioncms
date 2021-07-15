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

