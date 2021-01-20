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


   private String      m_sessId;
   private int         m_errorCode;
   private Object[]   m_errorArgs;
   private String      m_styleSheet;
}

