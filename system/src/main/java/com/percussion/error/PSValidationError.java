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
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Element;

import java.util.Locale;


/**
 * The PSValidationError class is used to report a calidation
 * error. Validation errors usually occur when an a user submits data
 * which does not meet the validation requirements defined in the 
 * application's data set.
 * <p>
 * <p>
 * An error message containing the user's session id and the text of the
 * message is logged when this error is encountered. If
 * detailed user activity logging is also enabled, the request can be
 * tracked back to see all the data associated with the request.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSValidationError extends PSLogError {
   
   /**
    * Report an application validation error.
    * <p>
    * The application id is most commonly obtained by calling
    * {@link com.percussion.data.PSExecutionData#getId PSExecutionData.getId()} or
    * {@link com.percussion.server.PSApplicationHandler#getId PSApplicationHandler.getId()}.
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
    * @param      source         the XML representation of the object
    *                              in error
    */
   public PSValidationError(  int applId,
                              java.lang.String sessionId,
                              int errorCode,
                              Object[] errorParams,
                              Element source)
   {
      super(applId);

      if (sessionId == null)
         m_sessId = "";
      else
         m_sessId = sessionId;

      m_errorCode = errorCode;
      m_errorArgs = errorParams;

      if (source == null)
         m_source = "";
      else
         m_source = PSXmlDocumentBuilder.toString(source);
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      int msgCount = (m_source.length() > 0) ? 3 : 2;
      PSLogSubMessage[] msgs = new PSLogSubMessage[msgCount];

      /* the generic submessage first (contains session id) */
      msgs[0]   = new PSLogSubMessage(
                                 IPSServerErrors.VALIDATION_ERROR,
                                 PSErrorManager.createMessage(
                                       IPSServerErrors.VALIDATION_ERROR,
                                       new Object[] { m_sessId },
                                       loc));

      /* the submessage containing m_errorCode/m_errorArgs */
      msgs[1]   = new PSLogSubMessage(
                                 m_errorCode,
                                 PSErrorManager.createMessage(   m_errorCode,
                                                               m_errorArgs,
                                                               loc));

      if (msgCount == 3)   /* write the source data */
         msgs[2]   = new PSLogSubMessage(
                                 IPSServerErrors.RAW_DUMP,
                                 PSErrorManager.createMessage(
                                       IPSServerErrors.RAW_DUMP,
                                       new Object[] { m_source },
                                       loc));

      return msgs;
   }


   private String      m_sessId;
   private int         m_errorCode;
   private Object[]   m_errorArgs;
   private String      m_source;
}

