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
import com.percussion.xml.IPSXmlErrors;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Element;

import java.util.Locale;


/**
 * The PSXmlProcessingError class is used to report an error
 * encountered during XML construction or deconstruction. This will most
 * often occur due to an invalid document structure.
 * <p>
 * The following information is logged:
 * <ul>
 * <li>the text of the error</li>
 * <li>the XML element node(s) in error</li>
 * </ul>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSXmlProcessingError extends PSLogError {
   
   /**
    * Report an error encountered during XML construction/deconstruction.
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
    * @param      source         the XML sub-tree containing the element(s)
    *                            causing the error
    */
   public PSXmlProcessingError(  int applId,
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

      /* use IPSXmlErrors.XML_PROCESSING_ERROR along with:
       *    [0] = m_sessId
       * to format the first submessage
       */
      msgs[0]   = new PSLogSubMessage(
                                 IPSXmlErrors.XML_PROCESSING_ERROR,
                                 PSErrorManager.createMessage(
                                       IPSXmlErrors.XML_PROCESSING_ERROR,
                                       new Object[] { m_sessId },
                                       loc));

      /* the next submessage contains m_errorCode/m_errorArgs */
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

