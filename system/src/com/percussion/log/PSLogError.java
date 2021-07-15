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

package com.percussion.log;

import com.percussion.error.PSErrorHumanReadableNames;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Locale;

/**
 * The PSLogError class is used as the base class for all error logging
 * classes.
 * <p>
 * The following information is logged:
 * <ul>
 * <li>the session id of the user making this request. This can be used to
 *     map back to the request when logging detailed user activity is
 *     enabled.</li>
 * <li>the type and name of the object which reported the error. The format
 *     is "Type(Name)", such as "DataSet(MyDataSet)".</li>
 * <li>the exception defining the error. The message text is obtained by
 *     calling <code>toString</code> on the exception.</li>
 * <li>any other context data available for the error</li>
 * </ul>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSLogError extends PSLogInformation  {

   /**
     * Construct a log message to report an error condition.
     *
    * @param   applId            the id of the application that generated
    *                              the error
    */
   protected PSLogError(int applId)
   {
      super(LOG_TYPE, applId);
   }

   /**
    * Get the sub-messages (type and text). A sub-message is created
    * for each piece of information reported when this object was created.
    *
    * @return   an array of sub-messages (PSLogSubMessage)
    */
   public PSLogSubMessage[] getSubMessages()
   {
      return getSubMessages(ms_defaultLocale);
   }

   /**
    * Get the sub-messages (type and text). A sub-message is created
    * for each piece of information reported when this object was created.
    *
    * @param   loc   the locale to use
    *
    * @return         an array of sub-messages (PSLogSubMessage)
    */
   public PSLogSubMessage[] getSubMessages(Locale loc)
   {
      /* first time, build it and set the built version's locale */
      if (m_SubMessages == null) {
         m_SubMessages         = buildSubMessages(loc);
         m_SubMessagesLocale   = loc.toString();
      }
      else {
         /* we'll build it in the specified locale, but don't reset the
          * default version
          */
         return buildSubMessages(loc);
      }

      /* otherwise, return the default version */
      return m_SubMessages;
   }

   /**
    * Errors may have an XML document defined for them which allows a
    * style sheet to be used to dynamically generate the response with
    * request specific data.
    *
    * @return         the XML document containing the error data
    */
   public Document getXmlErrorData()
   {
      return getXmlErrorData(ms_defaultLocale);
   }

   /**
    * Errors may have an XML document defined for them which allows a
    * style sheet to be used to dynamically generate the response with
    * request specific data.
    *
    * @param   loc   the locale to use
    *
    * @return         the XML document containing the error data
    */
   public Document getXmlErrorData(Locale loc)
   {
      /* first time, build it and set the built version's locale */
      if (m_doc == null) {
         m_doc         = buildXmlDocument(loc);
         m_docLocale   = loc.toString();
      }
      else if (!m_docLocale.equals(loc.toString())) {
         /* we'll build it in the specified locale, but don't reset the
          * default version
          */
         return buildXmlDocument(loc);
      }

      /* otherwise, return the default version */
      return m_doc;
   }

   /**
    * Override toString to get the error text as a readable string.
    */
   public java.lang.String toString()
   {
      return toString(ms_defaultLocale);
   }

   /**
    * Override toString to get the error text as a readable string
    * in the specified locale.
    *
    * @param   loc      the locale to use
    */
   public java.lang.String toString(Locale loc)
   {
      String ret = "";

      PSLogSubMessage[] msgs = getSubMessages(loc);
      if (msgs != null) {
         for (int i = 0; i < msgs.length; i++) {
            if (msgs[i] != null) {
               if (i > 0)
                  ret += "  " + msgs[i].getText();
               else
                  ret += msgs[i].getText();
            }
         }
      }

      if (ret.length() == 0)
         return this.getClass().getName();

      return ret;
   }

   /**
    * sublcasses must override this to build the messages in the
    * specified locale
    */
   protected abstract PSLogSubMessage[] buildSubMessages(Locale loc);

   /**
    * The default error document returns a document conforming to the
    * sys_defaultError.dtd and will contain all error messages available.
    *
    * @param loc the locale to use to build the error document, may be
    *    <code>null</code> in which case the default locale is used.
    * @return a document conforming to the sys_defaultError.dtd with all
    *    error messages, never <code>null</code>.
    */
   protected Document buildXmlDocument(Locale loc)
   {
      PSLogSubMessage[] msgs = getSubMessages(loc);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();

       Element root = PSXmlDocumentBuilder.createRoot(doc, "PSXLogErrorSet");
      root.setAttribute("class",
         PSErrorHumanReadableNames.getHumanReadableName(getClass()));

      PSRequest request = (PSRequest) PSRequestInfo
         .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      if(request != null)
      {   
         String protocol = request.getUserSession().getOriginalProtocol();
         String host = request.getUserSession().getOriginalHost();
         String port = String.valueOf(request.getUserSession().getOriginalPort());
         String reqroot = PSServer.getRequestRoot();
         String rxroot = protocol + "://" + host;
         if(port != null && port.trim().length()>0)
             rxroot += ":" + port;
         rxroot += reqroot;
   
         root.setAttribute("protocol",protocol);
         root.setAttribute("host", host);
         root.setAttribute("port", port);
         root.setAttribute("root", reqroot);
         root.setAttribute("rxroot", rxroot);
      }
      else
      {
         root.setAttribute("host", PSServer.getHostName());
         root.setAttribute("port", Integer.toString(PSServer.getListenerPort()));
         root.setAttribute("root", PSServer.getRequestRoot());
      }
      
      for (int i=0; i<msgs.length; i++)
      {
         Element error = PSXmlDocumentBuilder.addElement(doc, root, "Error",
            msgs[i].getText());
         error.setAttribute("id", Integer.toString(i));
      }

      return doc;
   }


   /**
    * Error is set as type 1.
    */
   private static final int LOG_TYPE = 1;

   /**
    * Get the default locale we'll be using for writing messages.
    */
   private static Locale ms_defaultLocale = Locale.getDefault();



   /**
    * This is where sub-classes can store their messages.
    */
   private PSLogSubMessage[]   m_SubMessages = null;
   private String               m_SubMessagesLocale = "";

   /**
    * This is where sub-classes can store their XML data.
    */
   private Document            m_doc = null;
   private String               m_docLocale = "";
}
