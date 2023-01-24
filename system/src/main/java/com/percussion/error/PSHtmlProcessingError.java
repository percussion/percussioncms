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

import com.percussion.data.IPSDataErrors;
import com.percussion.log.PSLogError;
import com.percussion.log.PSLogSubMessage;

import java.util.Locale;


/**
 * The PSHtmlProcessingError class is used to report an error
 * encountered during HTML generation. This will most often occur if
 * the XML document does not have the appropriate structure for the
 * style sheet or the style sheet is not syntactically correct.
 * <p>
 * The following information is logged:
 * <ul>
 * <li>the text of the error</li>
 * <li>the session id of the user (can be mapped back to the request if
 *     detailed user activity is being logged)</li>
 * <li>the name of the style sheet being used</li>
 * </ul>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSHtmlProcessingError extends PSLogError {
   
   /**
    * Report an error encountered during HTML generation.
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
    * @param      styleSheet     the name of the style sheet being used to 
    *                            generate the HTML
    */
   public PSHtmlProcessingError( int applId,
                                 java.lang.String sessionId,
                                 int errorCode,
                                 Object[] errorParams,
                                 java.lang.String styleSheet)
   {
      super(applId);
      
      if (sessionId == null)
         m_sessId = "";
      else
         m_sessId = sessionId;

      m_errorCode = errorCode;
      m_errorArgs = errorParams;

      if (styleSheet == null)
         m_styleSheet = "";
      else
         m_styleSheet = styleSheet;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected PSLogSubMessage[] buildSubMessages(Locale loc)
   {
      PSLogSubMessage[] msgs = new PSLogSubMessage[2];

      /* use IPSDataErrors.HTML_GENERATION_ERROR along with:
       *    [0] = m_sessId
       *    [1] = m_styleSheet
       * to format the first submessage
       */
      msgs[0]   = new PSLogSubMessage(
                              IPSDataErrors.HTML_GENERATION_ERROR,
                              PSErrorManager.createMessage(
                                    IPSDataErrors.HTML_GENERATION_ERROR,
                                    new Object[] { m_sessId, m_styleSheet },
                                    loc));

      /* use m_errorCode/m_errorArgs to format the second submessage */
      msgs[1]   = new PSLogSubMessage(
                              m_errorCode,
                              PSErrorManager.createMessage(   m_errorCode,
                                                            m_errorArgs,
                                                            loc));

      return msgs;
   }


   private final String      m_sessId;
   private final int         m_errorCode;
   private final Object[]   m_errorArgs;
   private final String      m_styleSheet;
}

