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
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Locale;

import org.w3c.dom.Element;


/**
 * The PSApplicationDesignError class is used to report a design error
 * in the application. When E2 encounters a design error at run-time,
 * this may be caused by files having been deleted or renamed, or the
 * application may have been saved with validation disabled.
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
public class PSApplicationDesignError extends PSLogError {
   
   /**
    * Report an application design error.
    * <p>
    * The application id is most commonly obtained by calling
    * {@link com.percussion.data.PSExecutionData#getId PSExecutionData.getId()} or
    * {@link com.percussion.server.PSApplicationHandler#getId PSApplicationHandler.getId()}.
    *
    * @param      applId         the id of the application that generated
    *                            the error
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
   public PSApplicationDesignError( int applId,
                                    int errorCode,
                                    Object[] errorParams,
                                    Element source)
   {
      super(applId);

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
      int msgCount   = (m_source.length() == 0) ? 1 : 2;
      PSLogSubMessage[] msgs   = new PSLogSubMessage[msgCount];

      /* use the errorCode/errorString to format the first submessage */
      msgs[0]   = new PSLogSubMessage(
                                 m_errorCode,
                                 PSErrorManager.createMessage(   m_errorCode,
                                                               m_errorArgs,
                                                               loc));

      if (msgCount == 2) {
         msgs[1] = new PSLogSubMessage(
                                 IPSServerErrors.RAW_DUMP,
                                 PSErrorManager.createMessage(
                                       IPSServerErrors.RAW_DUMP,
                                       new Object[] { m_source },
                                       loc));
      }

      return msgs;
   }


   private int         m_errorCode;
   private Object[]   m_errorArgs;
   private String      m_source;
}
