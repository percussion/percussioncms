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

import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Date;


/**
 * The PSLogInformation abstract class is used as the super-class of all
 * classes used for defining a message to be logged. The sub-classes provide
 * a cleaner interface for their specific function. They in turn call the
 * constructor in this class to store their information in the appropriate
 * fashion. All the methods in this class are used by the log manager to
 * access the data for writing to the log.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public abstract class PSLogInformation {
   /**
    * Construct a log message from the specified information. The
    * message will be stamped with the current date and time.
    *
    * @param   type     the message type code
    * @param   applId   the ID of the application logging this message
    */
   protected PSLogInformation(int type, int applId)
   {
      super();
      m_type   = type;
      m_applId = applId;
      m_time   = new Date();
   }
   
   /**
    * Get the log message type.
    *
    * @return  the message type code
    */
   public int getMessageType()
   {
      return m_type;
   }
   
   /**
    * Get the date and time the message was logged.
    *
    * @return  the date and time the message was logged
    */
   public java.util.Date getMessageTime()
   {
      return m_time;
   }
   
   /**
    * Get the application id of the associated application.
    *
    * @return  the associated application id or 0 if an application did
    *          not generate the message
    */
   public int getApplicationId()
   {
      return m_applId;
   }
   
   /**
    * Get the sub-messages (type and text). Most log messages contain only
    * one sub-message, though many are permitted.
    *
    * @return  an array of sub-messages (PSLogSubMessage)
    */
   public abstract PSLogSubMessage[] getSubMessages();
   
   /**
    *      Converts the object and its sub-messages into an XML string.
    *
    *      @return A String XML representation of the form:
    *
    *      &lt;PSXLogMessage type="foo"&gt;<BR>
    *            &lt;time&gt;19990204T130923000&lt/time&gt;<BR>
    *            &lt;applicationId&gt;foobar&lt;/applicationId&gt;<BR>
    *            &lt;data type="foobarbaz"&gt;<BR>
    *                  sub message #1 text<BR>
    *            &lt;/data&gt;<BR>
    *            &lt;data type="foobarbazcar"&gt;<BR>
    *                  sub message #2 text<BR>
    *            &lt;/data&gt;<BR>
    *      &lt;/PSXLogMessage&gt;
    */
   public String toXMLString()
   {
      StringBuilder buf = new StringBuilder(100);
      toXMLString(buf);
      return buf.toString();
   }

   /**
    *      Converts the object and its sub-messages into an XML string.
    *
    *      @return A String XML representation of the form:
    *
    *      &lt;PSXLogMessage type="foo"&gt;<BR>
    *            &lt;time&gt;19990204T130923000&lt/time&gt;<BR>
    *            &lt;applicationId&gt;foobar&lt;/applicationId&gt;<BR>
    *            &lt;data type="foobarbaz"&gt;<BR>
    *                  sub message #1 text<BR>
    *            &lt;/data&gt;<BR>
    *            &lt;data type="foobarbazcar"&gt;<BR>
    *                  sub message #2 text<BR>
    *            &lt;/data&gt;<BR>
    *      &lt;/PSXLogMessage&gt;
    */
   public void toXMLString(StringBuilder buf)
   {
      com.percussion.util.PSDateFormatISO8601 dateFmt =
         new com.percussion.util.PSDateFormatISO8601();
      
      buf.append("<PSXLogMessage type=\"");
      buf.append(String.valueOf(m_type));
      buf.append("\">\n");

      buf.append("\t<time>");
      buf.append(PSXmlTreeWalker.convertToXmlEntities(dateFmt.format(m_time)));
      buf.append("</time>\n");

      buf.append("\t<applicationId>");
      buf.append(String.valueOf(m_applId));
      buf.append("</applicationId>\n");

      PSLogSubMessage[] subs = getSubMessages();
      for (int i = 0; i < subs.length; i++)
      {
         buf.append("\t<data type=\"");
         buf.append(String.valueOf(subs[i].getType()));
         buf.append("\">");
         buf.append(PSXmlTreeWalker.convertToXmlEntities(subs[i].getText()));
         buf.append("</data>\n");
      }
      buf.append("</PSXLogMessage>\n");
   }

   public String toString()
   {
      StringBuilder buf = new StringBuilder(100);
      buf.append("Message Type: " + m_type);
      buf.append("\nTime: " + m_time);
      buf.append("\nAppID: " + m_applId);
      PSLogSubMessage[] subs = getSubMessages();
      if (subs != null)
      {
         for (int i = 0; i < subs.length; i++)
         {
            if (subs[i] == null)
            {
               buf.append("\n\tSubmessage " + i + ", null");
               continue;
            }
            buf.append("\n\tSubmessage " + i + ", Type: " + subs[i].getType());
            buf.append("\n\tText: " + subs[i].getText());
         }
      }
      return buf.toString();
   }

   /**
    * Writes the sub-messages to a String.
    *
    * @return The submessages formatted as a String, never <code>null</code>, 
    * may be empty.
    */
   public String getSubMessageText()
   {
      StringBuilder buf = new StringBuilder();
      PSLogSubMessage[] msgs = getSubMessages();
      if (msgs != null)
      {
         for (int i = 0; i < msgs.length; i++)
         {
            buf.append(msgs[i].getText());
            buf.append(" ");
         }
      }

      return buf.toString();
   }

   /**
    * Converts the object and its sub-messages into an XML Element.
    *
    * @param   doc      the document to treat as the parent of this node
    *
    * @return An XML tree of the form:
    *
    *      &lt;PSXLogMessage type="foo"&gt;<BR>
    *            &lt;time&gt;19990204T130923000&lt/time&gt;<BR>
    *            &lt;applicationId&gt;foobar&lt;/applicationId&gt;<BR>
    *            &lt;data type="foobarbaz"&gt;<BR>
    *                  sub message #1 text<BR>
    *            &lt;/data&gt;<BR>
    *            &lt;data type="foobarbazcar"&gt;<BR>
    *                  sub message #2 text<BR>
    *            &lt;/data&gt;<BR>
    *      &lt;/PSXLogMessage&gt;
    */
   public org.w3c.dom.Element toXml(org.w3c.dom.Document doc)
   {
      com.percussion.util.PSDateFormatISO8601 dateFmt =
         new com.percussion.util.PSDateFormatISO8601();
      
      org.w3c.dom.Element root = doc.createElement("PSXLogMessage");
      root.setAttribute("type", String.valueOf(m_type));
      PSXmlDocumentBuilder.addElement(
         doc, root, "time", dateFmt.format(m_time));
      PSXmlDocumentBuilder.addElement(
         doc, root, "applicationId", String.valueOf(m_applId));

      PSLogSubMessage[] subs = getSubMessages();
      for (int i = 0; i < subs.length; i++)
      {
         org.w3c.dom.Element data = PSXmlDocumentBuilder.addElement(
            doc, root, "data", subs[i].getText());
         data.setAttribute("type", String.valueOf(subs[i].getType()));
      }

      return root;
   }



   /**
    * Used when an application is not associated with the message.
    */
   public static final int      NULL_APPLID            = 0;

   /**
    * Used when a session is not associated with the user that generated
    * the message.
    */
   public static final String   NULL_SESSID            = null;
   
   
   private  int               m_type;
   private  int               m_applId;
   
   /**
   *   The time of creation of this log message
   */
   protected java.util.Date    m_time;
   
}

