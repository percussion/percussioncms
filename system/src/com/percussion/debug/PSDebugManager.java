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

package com.percussion.debug;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSTraceInfo;
import com.percussion.server.PSServer;
import com.percussion.util.PSCharSets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.ResourceBundle;

/**
 * Singleton used to manage all DebugLogHandlers and the output stream used by
 * tracing and debug logging
 */
public class PSDebugManager
{

   /**
    * Used to obtain the singlton instance of the DebugManager.  If an instance
    * has not yet been created, it will be done so in a lazy fashion.
    * @roseuid 39F492DC033C
    */
   public static PSDebugManager getDebugManager()
   {
      if (ms_debugManager == null)
         ms_debugManager = new PSDebugManager();

      return ms_debugManager;
   }

   /**
    * Puts the DebugLogHandler passed in in the hash table of log handlers
    * used by each application.
    *
    * @param handler The debug log handler for the application.  May not be
    * <code>null</code>.
    * @param appId The id of the application.  May not be <code>null</code>.
    * @roseuid 39F49332033C
    */
   public void registerLogHandler(PSDebugLogHandler handler, String appName)
   {
      m_logHandlers.put(appName, handler);
   }

   /**
    * Returns the DebugLogHandler for the specified application.
    * @param appName The name of the application for which the log handler
    * should be returned.  May not be <code>null</code>.
    * @return The DebugLogHandler of the application whose name was supplied.
    * @throws PSNotFoundException if the log handler has not been registered for
    * the specified app.
    * @roseuid 39F49473032C
    */
   public PSDebugLogHandler getLogHandler(String appName)
   throws PSNotFoundException
   {
      PSDebugLogHandler dh = (PSDebugLogHandler)m_logHandlers.get(appName);
      if (dh == null)
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_NOT_FOUND,
            appName);

      return dh;
   }

   /**
    * Removes the specified logHandler from the list of current log handlers for
    * each application.
    * @param appId Specifies the app whose log handler should be unregistered.
    * May not be <code>null</code>.
    * @roseuid 39F4953D02BF
    */
   public void unregisterLogHandler(String appName)
   {
      m_logHandlers.remove(appName);
   }

   /**
    * Used to obtain the writer used for tracing.  May point
    * to a file on disk, or ultimately a remote debugging console.  If the
    * object this writer is writing to (i.e. a file) is new, then a header
    * specifying the character encoding being used is written out to the stream.
    *
    * @param app application object for which the trace output stream is
    * being obtained.
    * @return The trace writer for the specified application.
    * @throws IOException if it cannot create the writer.
    */
   public PSTraceWriter getTraceWriter(PSApplication app)
      throws IOException
   {
      File traceFile = null;
      FileOutputStream out = null;

      // get the apps request root
      String root = app.getRequestRoot();

      // make sure it exits
      File rootDir = new File(PSServer.getRxDir(), root);
      if (!rootDir.exists())
         rootDir.mkdirs();

      // If the trace file exists, use it, else create a new one
      traceFile = new File(rootDir, getTraceFileName(app.getName()));
      boolean isNew = !traceFile.exists();
      out = new FileOutputStream(traceFile.getPath(), true);

      PSTraceWriter writer = new PSTraceWriter(out,
         getJavaOutputStreamEncoding());

      if (isNew)
      {
         // need to write out the encoding we will use
         String charHeader = getBundle().getString("traceEncodingMsg");
         Object[] args = {getStdOutputStreamEncoding()};
         writer.write(MessageFormat.format(charHeader, args));
         writer.flush();
      }

      return writer;
   }
   
   /**
    * Get the name of the trace file used based on the application name.
    * 
    * @param appName The name of the application, may not be <code>null</code> 
    * or empty.
    * 
    * @return The trace file name, never <code>null</code> or empty.
    */
   public static String getTraceFileName(String appName)
   {
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");
      
      return "~" + appName + ".trace";
   }

   /**
    * Returns the composite flag which indicates all options that have tracing
    * enabled.
    *
    * @param appName The Name of the application whose flags are to be returned.
    * @return the composite flag
    * @throws PSNotFoundException if no handler has been registered with
    * the specified appName.
    * @roseuid 39F5D5950213
    */
   public PSTraceFlag getTraceOptionsFlag(String appName)
      throws PSNotFoundException
   {
      PSDebugLogHandler dh = getLogHandler(appName);
      if (dh == null)
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_NOT_FOUND,
            appName);

       return dh.getTraceInfo().getTraceOptionsFlag();
   }

   /**
    * Set's the composite trace flag for the specified application.  All options
    * specified by the flag will be enabled.  All that are not enabled by the
    * flag will be disabled.
    *
    * @param appId The id of the application whose trace options are being set.
    * @param traceFlags an object containing the composite flags for each group
    * of trace options
    * @throws PSNotFoundException if no handler has been registered with
    * the specified appName.
    * @roseuid 39F5D6850177
    */
   public void setTraceOptionsFlag(String appName, PSTraceFlag traceFlags)
      throws PSNotFoundException
   {
      PSDebugLogHandler dh = getLogHandler(appName);
      if (dh == null)
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_NOT_FOUND,
            appName);

      PSTraceInfo info = dh.getTraceInfo();
      info.setTraceOptionsFlag(traceFlags);
   }

   /**
    * Restores the initial tracing options for the specified app.
    * These are the options that were enabled when it was first created or
    * loaded from xml.
    *
    * @param appName The Name of the application whose flags are to be returned.
    * @throws PSNotFoundException if no handler has been registered with
    * the specified appName.
    */
   public void restoreInitialTraceOptions(String appName)
      throws PSNotFoundException
   {
      PSDebugLogHandler dh = getLogHandler(appName);
      if (dh == null)
         throw new PSNotFoundException(IPSObjectStoreErrors.APP_NOT_FOUND,
            appName);

      dh.getTraceInfo().restoreInitialOptions();
   }

   /**
    * Returns the standard name of the encoding to use when writing
    * character data to the output stream.
    *
    * @return The standard name of the character encoding, never <code>null
    * </code> or empty.
    */
   public String getStdOutputStreamEncoding()
   {
      return PSCharSets.rxStdEnc();
   }

   /**
    * Returns the Java name of the encoding to use when writing
    * character data to the output stream.
    *
    * @return The Java name of the character encoding, never <code>null
    * </code> or empty.
    */
   public String getJavaOutputStreamEncoding()
   {
      return PSCharSets.getJavaName(getStdOutputStreamEncoding());
   }

   /**
    * Private constructor for this class.  Instance of this class may only be
    * obtained by calling getDebugManager
    * @roseuid 39F9CE1B037A
    */
   private PSDebugManager()
   {
      m_logHandlers = new Hashtable();
   }


   /**
    * Get the server's resource bundle to use for trace message formats.
    *
    * @param The bundle, never <code>null</code>.
    */
   private ResourceBundle getBundle()
   {
      if (m_resourceBundle == null)
         m_resourceBundle =
         ResourceBundle.getBundle("com.percussion.server.PSStringResources");

      return m_resourceBundle;
   }


   /**
    * Contains the single instance of this class.
    */
   private static PSDebugManager ms_debugManager = null;

   /**
    * The list of currently registered DebugLogHandlers.
    */
   private Hashtable m_logHandlers = null;

   /**
    * The resource bundle used for logging messages.  <code>null</code> until
    * the first call to {@link #getBundle()}, never modified after that.
    */
   private ResourceBundle m_resourceBundle = null;


}
