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
package com.percussion.log;

import com.percussion.design.objectstore.PSLogger;
import com.percussion.server.PSApplicationHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSRequestStatistics;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Document;

/**
 * The PSLogHandler class defines the logging rules used by a particular server
 * or application. The server and each application may follow different logging
 * rules. All loggings enabled for the server will also be enabled for each
 * application. An application may specify additional loggings. As such, each
 * has its own PSLogHandler object. This object is used to determine if the
 * specified type of action should be logged. It is also used to send the log
 * message to the back-end log. The server or application passes the
 * PSLogInformation sub-object to this object through the
 * {@link #write(PSLogInformation) write}method which in turn passes it on to
 * the PSLogManager for writing to the log.
 * <p>
 * If an attempt is made to log something while currently processing a log
 * request (within the same thread), those secondary requests will be ignored.
 * This is done to prevent infinite loops in the system. This is accomplished in
 * two ways. First, if any of the <code>isXXX</code> methods are called, they
 * will return <code>false</code>. Secondly, if the log request is called
 * anyway, it will return w/o performing the requested logging and without
 * indicating any errors.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSLogHandler
{
   /**
    * Construct a log handler with the specified logging rules. If rules is
    * <CODE>null</CODE>, all types of logging will be disabled.
    *
    * @param      rules    the logging rules this object should enforce
    */
   public PSLogHandler(PSLogger rules)
   {
      m_rules = rules;
      m_combinedRules = m_rules;
   }

   /**
    * Construct a log handler with all types of logging disabled.
    */
   public PSLogHandler()
   {
      this(null);
   }

   /**
    * Is the logging of errors enabled?
    *
    * @return     <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isErrorLoggingEnabled()
   {
      if (m_combinedRules == null || isInRequest())
         return false;

      return m_combinedRules.isErrorLoggingEnabled();
   }

   /**
    * Is the logging of server startup and shutdown events enabled?
    *
    * @return     <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isServerStartStopLoggingEnabled()
   {
      if (m_combinedRules == null || isInRequest())
         return false;

      return m_combinedRules.isServerStartStopLoggingEnabled();
   }

   /**
    * Is the logging of application startup and shutdown events enabled?
    *
    * @return     <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isAppStartStopLoggingEnabled()
   {
      if (m_combinedRules == null || isInRequest())
         return false;

      return m_combinedRules.isAppStartStopLoggingEnabled();
   }

   /**
    * Is the logging of application statistics when the application shuts down
    * enabled?
    *
    * @return     <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isAppStatisticsLoggingEnabled()
   {
      if (m_combinedRules == null || isInRequest())
         return false;

      return m_combinedRules.isAppStatisticsLoggingEnabled();
   }

   /**
    * Is the execution plan logged when an application is started?
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isExecutionPlanLoggingEnabled()
   {
      if (m_combinedRules == null || isInRequest())
         return false;

      return m_combinedRules.isExecutionPlanLoggingEnabled();
   }

   /**
    * Is the logging of basic user activity enabled?
    *
    * @return <code>true</code> if basic or detailed user logging is enabled,
    *         <code>false</code> if it is disabled
    */
   public boolean isBasicUserActivityLoggingEnabled()
   {
      if (m_combinedRules == null || isInRequest())
         return false;

      return m_combinedRules.isBasicUserActivityLoggingEnabled();
   }

   /**
    * Is the logging of detailed user activity enabled?
    *
    * @return     <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isDetailedUserActivityLoggingEnabled()
   {
      if (m_combinedRules == null || isInRequest())
         return false;

      return m_combinedRules.isDetailedUserActivityLoggingEnabled();
   }

   /**
    * Is the logging of full user activity enabled?
    *
    * @return      <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isFullUserActivityLoggingEnabled()
   {
      if (m_combinedRules == null || isInRequest())
         return false;

      return m_combinedRules.isFullUserActivityLoggingEnabled();
   }

   /**
    * Is the detection and logging of multiple handlers for a request enabled?
    * <p>
    * When processing requests, the first data set matching the request criteria
    * handles it. If other data sets exist which are alse interested in the
    * request, they will be ignored. This may cause confusion to end users and
    * application designers. By enabling logging, E2 will check subsequent
    * applications and log a message for each application also matching the
    * request criteria. This should only be used for debugging purposes as it
    * may impact performance.
    *
    * @return     <code>true</code> if this type of logging is enabled,
    *             <code>false</code> if it is disabled
    */
   public boolean isMultipleHandlerLoggingEnabled()
   {
      if (m_combinedRules == null || isInRequest())
         return false;

      return m_combinedRules.isMultipleHandlerLoggingEnabled();
   }

   /**
    * Write the log message.
    *
    * @param      msg                     the log message to be written
    *
    * @exception IllegalStateException if the log manager is not associated with
    *               a log mechanism (init was not called or close was already
    *               called)
    */
   public void write(PSLogInformation msg)
         throws IllegalStateException
   {
      write(msg, false);
   }

   /**
    * Write the log message.
    *
    * @param      msg                     the log message to be written
    *
    * @param forceLogging if <code>true</code>, the entry will be logged even
    *           if the log handler has this type of logging disabled
    *
    * @exception IllegalStateException if the log manager is not associated with
    *               a log mechanism (init was not called or close was already
    *               called)
    */
   public void write(PSLogInformation msg, boolean forceLogging)
         throws IllegalStateException
   {
      int msgType = msg.getMessageType();

      /*
       * In case this is for an application we OR the server rules to the
       * application rules. So all server logging enabled will be enabled for
       * each application as well.
      */
      PSLogger serverRules = PSServer.getLogHandler().getLogger();
      m_combinedRules = m_rules;
      if (m_combinedRules == null)
         m_combinedRules = serverRules;
      else
         m_combinedRules.or(serverRules);

      boolean write = true;
      if (!forceLogging)
      {
         if (isInRequest())
            return;
         
         switch (msgType)
         {
            case 1: // errors
               write = isErrorLoggingEnabled();
               break;
            case 2: // server start
               // fall through
            case 3: // server stop
               write = isServerStartStopLoggingEnabled();
               break;
            case 4: // application start
               // fall through
            case 5: // application stop
               write = isAppStartStopLoggingEnabled();
               break;
            case 6: // application statistics
               write = isAppStatisticsLoggingEnabled();
               break;
            case 7: // basic user activity
               write = isBasicUserActivityLoggingEnabled();
               break;
            case 8: // detailed user activity
               write = isDetailedUserActivityLoggingEnabled();
               break;
            case 9: // full user activity
               write = isFullUserActivityLoggingEnabled();
               break;
            case 10: // server warnings are always logged
               break;
            case 11: // multiple handlers
               write = isMultipleHandlerLoggingEnabled();
               break;
            case 12: // execution plan
               write = isExecutionPlanLoggingEnabled();
               break;
            default: // we log things we don't know about yet by default
               break;
         }
      }

      if (write)
         PSLogManager.write(msg);
   }

   /**
    * Helper method to log basic user activity info for the specified request.
    *
    * @param   request         the request to log
    */
   public void logBasicUserActivity(PSRequest request)
   {
      logBasicUserActivity(request, false);
   }

   /**
    * Helper method to log basic user activity info for the specified request.
    *
    * @param   request         the request to log
    *
    * @param forceLogging if <code>true</code>, the entry will be logged even
    *           if the log handler has this type of logging disabled
    */
   public void logBasicUserActivity(PSRequest request, boolean forceLogging)
   {
      if (isInRequest())
         return;
      
      if (!forceLogging && !isBasicUserActivityLoggingEnabled()
         && !isDetailedUserActivityLoggingEnabled()
         && !isFullUserActivityLoggingEnabled())
         return;

      try
      {
         setInRequest(true);
         Map<String, String> info = new HashMap<>();
         
         /* basic user info has sessionId, host, user, url */
         
         PSUserSession sess = request.getUserSession();
         String sessId = "";
         StringBuffer userNames = new StringBuffer();
         if (sess != null)
         {
            sessId = sess.getId();
            
            // get the authenticated user name(s)
            com.percussion.security.PSUserEntry[] entries = sess
            .getAuthenticatedUserEntries();
            if (entries != null)
            {
               for (int i = 0; i < entries.length; i++)
               {
                  if (userNames.length() != 0)
                     userNames.append(", ");
                  userNames.append(entries[i].getName());
               }
            }
         }
         
         info.put("sessionId", sessId);
         
         String sTemp = request.getServletRequest().getRemoteHost();
         if (sTemp == null)
            sTemp = "";
         info.put("host", sTemp);
         
         info.put("user", userNames.toString());
         
         info.put(
            "url",
            request.getServletRequest().getRequestURL().toString());
         
         PSApplicationHandler ah = request.getApplicationHandler();
         int applId = (ah == null) ? 0 : ah.getId();
         
         write(new PSLogBasicUserActivity(applId, info), true);
      }
      finally
      {
         setInRequest(false);
      }
   }

   /**
    * Helper method to log detailed user activity info for the specified
    * request.
    *
    * @param   request         the request to log
    */
   public void logDetailedUserActivity(PSRequest request)
   {
      logDetailedUserActivity(request, false);
   }

   /**
    * Helper method to log detailed user activity info for the specified
    * request.
    *
    * @param   request         the request to log
    *
    * @param forceLogging if <code>true</code>, the entry will be logged even
    *           if the log handler has this type of logging disabled
    */
   public void logDetailedUserActivity(PSRequest request, boolean forceLogging)
   {
      if (isInRequest())
         return;
      
      if (!forceLogging && !isDetailedUserActivityLoggingEnabled()
         && !isFullUserActivityLoggingEnabled())
         return;

      try
      {
         setInRequest(true);
         Map<String, String> info = new HashMap<>();
         PSRequestStatistics stats = request.getStatistics();
         
         /*
          * detailed user info has sessionId, postBody, xmlFile, rowsSelected,
          * rowsInserted, rowsUpdated, rowsDeleted, rowsSkipped, rowsFailed
          */
         
         PSUserSession sess = request.getUserSession();
         String sTemp = "";
         if (sess != null)
            sTemp = sess.getId();
         info.put("sessionId", sTemp);
         
         Map params = request.getParameters();
         if ((params != null) && (params.size() != 0))
         {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintWriter pw = new PrintWriter(buf);
            Iterator entries = params.entrySet().iterator();
            Map.Entry entry;
            Object curValue;
            
            for (int i = 0; entries.hasNext(); i++)
            {
               entry = (Map.Entry) entries.next();
               if (i != 0) /* write the param separator */
                  pw.write("&");
               
               /* then key=value for each param */
               curValue = entry.getValue();
               if (curValue instanceof ArrayList)
               {
                  ArrayList vals = (ArrayList) curValue;
                  for (int j = 0; j < vals.size(); j++)
                  {
                     pw.write((String) entry.getKey());
                     pw.write("=");
                     curValue = vals.get(j);
                     pw.write(((curValue == null) ? "" : curValue.toString()));
                  }
               }
               else
               {
                  pw.write((String) entry.getKey());
                  pw.write("=");
                  pw.write(curValue == null ? "null" : curValue.toString());
               }
            }
            pw.flush(); /* force the stream in case it's buffering */
            
            info.put("postBody", buf.toString());
         }
         
         Document inDoc = request.getInputDocument();
         if (inDoc != null)
         {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try
            {
               PSXmlDocumentBuilder.write(inDoc, buf);
               sTemp = buf.toString();
            }
            catch (IOException e)
            {
               /* this should never happen, but I guess we'll write this! */
               sTemp = "Exception: " + e.toString();
            }
            
            info.put("xmlFile", sTemp);
         }
         
         info.put("rowsSelected", String.valueOf(stats.getRowsSelected()));
         info.put("rowsInserted", String.valueOf(stats.getRowsInserted()));
         info.put("rowsUpdated", String.valueOf(stats.getRowsUpdated()));
         info.put("rowsDeleted", String.valueOf(stats.getRowsDeleted()));
         info.put("rowsSkipped", String.valueOf(stats.getRowsSkipped()));
         info.put("rowsFailed", String.valueOf(stats.getRowsFailed()));
         info.put("duration", String.valueOf(stats.getProcessingTime()));
         
         PSApplicationHandler ah = request.getApplicationHandler();
         int applId = (ah == null) ? 0 : ah.getId();
         
         write(new PSLogDetailedUserActivity(applId, info), true);
      }
      finally
      {
         setInRequest(false);
      }
   }

   /**
    * Helper method to log detailed user activity info for the specified
    * request.
    *
    * @param   request         the request to log
    *
    * @param   msgCode         the message code to use
    *
    * @param   msgArgs         the arguments associated with the message
    */
   public void logFullUserActivityAction(PSRequest request, int msgCode,
      Object[] msgArgs)
   {
      logFullUserActivityAction(request, msgCode, msgArgs, false);
   }

   /**
    * Helper method to log detailed user activity info for the specified
    * request.
    *
    * @param   request         the request to log
    *
    * @param   msgCode         the message code to use
    *
    * @param   msgArgs         the arguments associated with the message
    *
    * @param   forceLogging   if <code>true</code>, the entry will be logged
    *                           even if the log handler has this type of
    *                           logging disabled
    */
   public void logFullUserActivityAction(PSRequest request, int msgCode,
         Object[] msgArgs, boolean forceLogging)
   {
      if (isInRequest())
         return;
      
      if (!forceLogging && !isFullUserActivityLoggingEnabled())
         return;

      try
      {
         setInRequest(true);
         PSApplicationHandler ah = request.getApplicationHandler();
         int applId = (ah == null) ? 0 : ah.getId();
         
         PSLogFullUserActivity logMsg = new PSLogFullUserActivity(applId, 
            msgCode, msgArgs);
         
         write(logMsg, true);
      }
      finally
      {
         setInRequest(false);
      }
   }

   /** 
    * Accessor to this handlers logger. This may be <code>null</code>.
    *
    * @return the active rules for this log handler
    */
   public PSLogger getLogger()
   {
      return m_rules;
   }

   /**
    * Every logging method must call this method with <code>true</code> before
    * initiating the actual logging work and <code>false</code> upon leaving.
    * The 2nd call must be executed from a finally block to prevent leaving the
    * logger in state in which it would never log again.
    * 
    * @param active <code>true</code> if starting a log request,
    *           <code>false</code> when finishing the request.
    */
   private void setInRequest(boolean active)
   {
      m_inLogRequest.set(active ? new Object() : null);
   }

   /**
    * All <code>isXXX</code> methods must call this method and return
    * <code>false</code> if this method returns <code>true</code>.
    * 
    * <p>
    * If a logging method does not call one of the <code>isXXX</code> methods,
    * then it must call this method and if <code>true</code> is returned, they
    * must return immediately without performing the requested logging action.
    * No error should be indicated.
    * <p>
    * 
    * @return <code>true</code> if a logging request is currently being
    *         processed within this thread, <code>false</code> otherwise.
    * 
    * @see #setInRequest(boolean)
    */
   private boolean isInRequest()
   {
      return m_inLogRequest.get() != null;
   }
   
   /**
    *   The rules we use to decide whether to log the message or not
    */
   private PSLogger m_rules;

   /**
    * The combined (ORed) rules for this log handler and the server settings.
    * This will be set each time a log message is written.
    */
   private PSLogger m_combinedRules;
   
   /**
    * This property stores a flag that indicates whether a logging request is
    * currently being processed within the current thread. Initialized before
    * ctor, then never <code>null</code>.
    * <p>
    * Should only be accessed through the {@link #isInRequest()}and
    * {@link #setInRequest(boolean)} methods.
    */
   private ThreadLocal<Object> m_inLogRequest = new ThreadLocal<>();
}
