/******************************************************************************
 *
 * [ EmpireTestClient.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.autotest.empire;

import com.percussion.autotest.empire.script.EmpireScriptInterpreter;
import com.percussion.autotest.empire.script.EmpireWgetScriptInterpreter;
import com.percussion.autotest.framework.QAClient;
import com.percussion.autotest.framework.QAClientEvent;
import com.percussion.autotest.framework.QAClientListener;
import com.percussion.autotest.framework.QAObjectDescription;
import com.percussion.autotest.framework.QARequestContext;
import com.percussion.autotest.framework.QAScriptDocument;
import com.percussion.autotest.framework.QAServer;
import com.percussion.autotest.framework.QATestResults;
import com.percussion.test.io.LogSink;
import com.percussion.util.PSBase64Encoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * The autotest client. It communicate with both autotest server and 
 * Rhythmyx. 
 * <p>
 * Usage example: -Dfile.encoding=UTF-8 -DDbmsSupportDefsScript=file:SupportDefs/SqlServer.xml
 * <p> 
 * where -Dfile.encoding=UTF-8 is VM arguments  
 */
public class EmpireTestClient
   extends UnicastRemoteObject
   implements QAClient, ShutDownable, LogSink
{

   public static void main(String args[])
   {
      EmpireTestClient client = null;
      
      try
      {
         int startArg = 0;
         String clientPropertiesFilename = null;
         if (args.length == 1)
         {
            if (!args[startArg].toLowerCase().startsWith("-d"))
               clientPropertiesFilename = args[startArg++];
         }

         if (clientPropertiesFilename == null)
            clientPropertiesFilename = "qaclient.properties";

         client = new EmpireTestClient(clientPropertiesFilename);

         for (int i=startArg; i<args.length; i++)
         {
            String arg = args[i];
            if (arg.startsWith("-d") || arg.startsWith("-D"))
            {
               String rest = arg.substring(2);
               int eqPos = rest.indexOf("=");
               if (eqPos == -1 || eqPos == (rest.length() - 1))
               {
                  System.err.println("WARNING: Ignoring invalid option " + arg);
               }
               else
               {
                  String macName = rest.substring(0, eqPos);
                  String macVal = rest.substring(eqPos+1);
                  client.defineGlobalMacro(macName, macVal);
               }
            }
            else
            {
               clientPropertiesFilename = arg;
            }
         }

         client.start();
      }
      catch(Throwable t)
      {
         t.printStackTrace();
         if(client != null)
            client.shutDown();
         else
            System.exit(1);
      }
   }


   /**
    * Construct a QA Client that will read its properties from the given
    * filename. It will immediately try to connect to the server.
    *
    * @author  chadloder
    * 
    * @version 1.2 1999/08/20
    * 
    * 
    * @param   clientPropertiesFilename
    * 
    *
    * @throws  IOException
    * @throws  MalformedURLException
    * @throws  RemoteException
    * 
    */
   public EmpireTestClient(String clientPropertiesFilename)
      throws IOException
   {
      m_statusLock = new Object();

      m_clientPropertiesFilename = clientPropertiesFilename;
      
      // if the file does not exist, creates an empty one by that name
      (new File(m_clientPropertiesFilename)).createNewFile();

      // load the config from the file
      m_clientProperties = new Properties(CLIENT_DEFAULTS);
      m_clientProperties.load(new FileInputStream(m_clientPropertiesFilename));

      // set up the auto login credential
      String cred = m_clientProperties.getProperty( "autoLoginUid" );
      if ( null != cred && cred.trim().length() > 0 )
      {
         String pw = m_clientProperties.getProperty( "autoLoginPassword" );
         if ( pw != null && pw.trim().length() > 0 )
            cred += ":" + pw;
         m_loginCredential = PSBase64Encoder.encode(cred);
      }

      // what is our RMI name?
      m_name = "rmi://" + m_clientProperties.getProperty("clientHost")
         + ":" + m_clientProperties.getProperty(PROP_clientPort)
         + "/" + m_clientProperties.getProperty(PROP_clientName);

      // how many times will we try to connect ?
      m_numStartTries = Integer.parseInt(
            m_clientProperties.getProperty("startRetries"));

      // at what interval will we space our retries ?
      m_startRetryInterval = Integer.parseInt(
            m_clientProperties.getProperty("startRetryInterval"));

      // will we log messages to the screen?
      if(!m_clientProperties.getProperty("logToScreen").equals("0"))
         m_logToScreen = true;
      else
         m_logToScreen = false;

      // load and define any predefined macros
      loadMacroFiles();
      
      m_isExecuting = true;      

      m_isExecuting = false;
   }


   /**
    * These are credentials specified in the client properties file. They are
    * returned ready for the Authorization header of an http request.
    *
    * @return <code>null</code> if no creds have been specified. Otherwise,
    *    the credentials are returned in the form uid:pw, base64 encoded.
    */
   public String getLoginCredential()
   {
      return m_loginCredential;
   }

   public void start() throws IOException,
      InterruptedException, RemoteException
   {
      String freeName = null;
      for (int i=1; i<=m_numStartTries; i++)
      {
         try
         {
            freeName = m_name + "_" + i;
            log("Binding client to " + freeName + " ...");
            Naming.bind(freeName, this);

            m_name = freeName;
            selfRegister(m_clientProperties.getProperty(PROP_preferredServer));
            
            // if we got here with no exceptions, then we are OK
            break;
         }
         catch(AlreadyBoundException abe)
         {
            /*
             * there might already be a client running with the current name,
             * try the next name
             */
         }
         catch(Throwable t)
         {
            log("*** WARNING *** " + "Could not start: " + t.toString()
                  + ". Retrying...");
            
            if (i >= m_numStartTries) 
            {
               throw new RemoteException("Tried " + i + " times to start", t);
            }
            
            // wait to retry
            Thread.sleep(m_startRetryInterval);
         }
      }
   }

   @SuppressWarnings("unused")
   public void addClientListener(QAClientListener listener)
      throws RemoteException
   {
      if(listener != null)
         synchronized(m_clientListeners)
      {
         m_clientListeners.add(listener);
      }
   }

   void defineGlobalMacro(String name, String value)
   {
      log("Defining global macro " + name + "=" + value);
      m_globalMacros.put(name, value);
   }

   public boolean equals(Object object) {
      if (this == object) return true;
      if (!(object instanceof EmpireTestClient)) return false;
      if (!super.equals(object)) return false;
      EmpireTestClient that = (EmpireTestClient) object;
      return m_logToScreen == that.m_logToScreen &&
              m_isExecuting == that.m_isExecuting &&
              m_numStartTries == that.m_numStartTries &&
              m_startRetryInterval == that.m_startRetryInterval &&
              m_shutdown == that.m_shutdown &&
              java.util.Objects.equals(m_clientProperties, that.m_clientProperties) &&
              java.util.Objects.equals(m_name, that.m_name) &&
              java.util.Objects.equals(m_clientPropertiesFilename, that.m_clientPropertiesFilename) &&
              java.util.Objects.equals(m_clientListeners, that.m_clientListeners) &&
              java.util.Objects.equals(m_statusLock, that.m_statusLock) &&
              java.util.Objects.equals(m_server, that.m_server) &&
              java.util.Objects.equals(m_scriptName, that.m_scriptName) &&
              java.util.Objects.equals(m_executionThread, that.m_executionThread) &&
              java.util.Objects.equals(m_globalMacros, that.m_globalMacros) &&
              java.util.Objects.equals(m_loginCredential, that.m_loginCredential);
   }

   public int hashCode() {
      return Objects.hash(super.hashCode(), m_clientProperties, m_name, m_clientPropertiesFilename, m_clientListeners, m_logToScreen, m_isExecuting, m_statusLock, m_server, m_scriptName, m_numStartTries, m_startRetryInterval, m_executionThread, m_globalMacros, m_shutdown, m_loginCredential);
   }

   // see interface desc
   public void prepareScript( QAScriptDocument script )
   {
      if ( null == script )
         throw new IllegalArgumentException( "script can't be null" );

      System.out.println(
            "Received script execution request for: " + script.getName());
      try
      {
         boolean wasRunning = false;
         synchronized(m_statusLock)
         {
            if(m_isExecuting)
               wasRunning = true;
            else
               m_isExecuting = true;
         }
         if(wasRunning)
         {
            System.out.println("We were running.");
            publishScriptCompleted(script.getName());
            return;
         }

         m_scriptName = script.getName();

         System.out.println("Executing script " + script.getName());

         String wgetDirectory =
               m_clientProperties.getProperty(PROP_wgetDirectory);
         EmpireScriptInterpreter interp = null;

         boolean logResults = !m_server.isStressTest();
         if (wgetDirectory != null && wgetDirectory.length() > 0)
         {
            interp = new EmpireWgetScriptInterpreter(script, this,
               new QATestResults(script.getName(), m_name, logResults),
               m_logToScreen && logResults, new File(wgetDirectory));
         }
         else
         {
            interp = new EmpireScriptInterpreter(script, this,
                  new QATestResults(script.getName(), m_name, logResults),
                  m_logToScreen && logResults);
         }
         m_executionThread = new Thread(interp);
      }
      catch(Throwable t)
      {
         log("*** ERROR *** " + "Could not execute script "
               + script.getName(), t);
      }
   }

   // see interface for desc
   public void executePreparedScript()
   {
      if ( null == m_executionThread )
      {
         throw new IllegalStateException(
               "Test must be prepared before execution." );
      }
      m_executionThread.start();
      m_executionThread = null;
   }

   // see interface description
   @SuppressWarnings("unused")
   public void executeScript(QAScriptDocument script)
      throws RemoteException
   {
      prepareScript( script );
      executePreparedScript();
   }

   public Map getGlobalMacros()
   {
      return Collections.unmodifiableMap(m_globalMacros);
   }

   @SuppressWarnings("unused")
   public String getName()
      throws RemoteException
   {
      return m_name;
   }

   @SuppressWarnings("unused")
   public boolean lockServerObject(String name, long expiresInMs,
         long waitForMs)
      throws RemoteException
   {
      log("Trying to lock...");
      try
      {
         return m_server.lockObject(
            new QARequestContext(getName(), m_scriptName),
            name,
            expiresInMs,
            waitForMs);
      }
      catch (Throwable t)
      {
         t.printStackTrace();
      }
      return false;
   }

   /**
    * Interrupts the client's processing.  Client will immediately suspend the
    * execution of any currently running scripts and notify any registered
    * client listeners.  If the client was not executing a script, listeners
    * are not notified.  If {@link #shutDown(boolean) shutDown(true)} had been
    * called while executing a script, the client will shutdown at this time.
    *
    * @param message A message that should provide information as to why the
    * client is beign interrupted.  May be <code>null</code> or empty.
    *
    * @throws RemoteException if an error occurs notifying a remote listener.
    *
    * @see QAClientListener
    */
   public void interrupt(String message)
      throws RemoteException
   {
      synchronized(m_statusLock)
      {
         if(m_isExecuting)
            publishScriptInterrupted(message);
         m_isExecuting = false;
      }

      // see if delayed shutdown must be processed
      if (m_shutdown)
         shutDown();
   }

   protected void loadMacroFiles()
   {
      String macroFiles = m_clientProperties.getProperty(PROP_macroFiles);
      File f;
      for(StringTokenizer tok = new StringTokenizer(macroFiles, ";");
            tok.hasMoreTokens(); readMacros(f))
      {
         String curFile = tok.nextToken();
         f = new File(curFile);
      }

   }

   /**
    * Causes the client to post its results to the server.  If the client is
    * just creating expected data, no results will be posted.  All client
    * listenters are notified that results were recorded.  If results cannot
    * be recorded by the server, they are written to a text file on the client
    * in the current directory and the name and location recorded to the
    * console.  If @link #shutdown(boolean) shutdown(true)} was called while the
    * script was executing, the client will shutdown now.
    *
    * @param results The results object that contains all information to be
    *    recorded.  May not be <code>null</code> unless generating expect data.
    *
    * @throws IllegalArgumentException if results is <code>null</code>.
    * @throws RemoteException if there is an error communicating with the server
    * or notifying a remote listener.
    */
   public void postResults(QATestResults results) throws RemoteException
   {
      // don't report results when we're just creating expect data
      String wgetDirectory = m_clientProperties.getProperty(PROP_wgetDirectory);
      if (wgetDirectory == null || wgetDirectory.length() == 0)
      {
         if ( null == results )
            throw new IllegalArgumentException( "results may not be null" );

         try
         {
            m_server.recordResults(results);
         }
         catch (Exception e)
         {
            log("*** ERROR *** "
                  + "Could not post test results to the test server.", e);
            try
            {
               File resultsFile = File.createTempFile("test_", ".txt",
                     new File("."));
               FileOutputStream out = new FileOutputStream(resultsFile);
               results.write(new OutputStreamWriter(out));
               System.out.println("Saving results to a file: "
                     + resultsFile.getAbsolutePath());
               out.close();
            }
            catch(Exception ee)
            {
               log("*** ERROR *** Encountered an error while saving results " +
                  "to a file", ee);
            }
         }
         log("Posted results");
         notifyResultsRecorded(m_scriptName);
      }

      // see if delayed shutdown must be processed
      if (m_shutdown)
         shutDown();
   }

   public void publishClientShutdown(String msg) throws RemoteException
   {
      log("Client shutting down: " + msg);
      synchronized(m_clientListeners)
      {
         for (int i=0; i<m_clientListeners.size(); i++)
         {
            m_clientListeners.get(i).notifyClientShutdown(
               new QAClientEvent(m_name, msg));
         }

      }
   }

   public void publishScriptCompleted(String msg) throws RemoteException
   {
      m_isExecuting = false;
      log("Script completed: " + msg);
      synchronized(m_clientListeners)
      {
         for (int i=0; i<m_clientListeners.size(); i++)
         {
            m_clientListeners.get(i).notifyScriptCompleted(
               new QAClientEvent(m_name, msg));
         }

      }
   }

   public void publishScriptInterrupted(String msg) throws RemoteException
   {
      m_isExecuting = false;
      log("*** WARNING *** " + "Script interrupted: " + msg);
      synchronized(m_clientListeners)
      {
         for (int i=0; i<m_clientListeners.size(); i++)
         {
            m_clientListeners.get(i).notifyScriptInterrupted(
               new QAClientEvent(m_name, msg));
         }

      }
   }

   public void publishScriptStarted(String msg) throws RemoteException
   {
      m_isExecuting = true;
      log("Script started.");
      synchronized(m_clientListeners)
      {
         for (int i=0; i<m_clientListeners.size(); i++)
         {
            m_clientListeners.get(i).notifyScriptStarted(
               new QAClientEvent(m_name, msg));
         }

      }
   }

   public void notifyResultsRecorded(String msg) throws RemoteException
   {
      m_isExecuting = false;
      log("Script results recorded.");
      synchronized(m_clientListeners)
      {
         for (int i=0; i<m_clientListeners.size(); i++)
         {
            m_clientListeners.get(i).notifyResultsRecorded(
               new QAClientEvent(m_name, msg));
         }

      }
   }

   private void readMacros(File f)
   {
      try
      {
         BufferedReader reader = new BufferedReader(new InputStreamReader(
               new FileInputStream(f)));
         for(String curLine = reader.readLine(); curLine != null;
               curLine = reader.readLine())
         {
            curLine = curLine.trim();
            log("curLine: " + curLine);
            if(curLine.length() != 0 && !curLine.startsWith("#"))
            {
               int eqPos = curLine.indexOf(61);
               if(eqPos < 1)
               {
                  log("*** WARNING *** " + "Malformed line: \"" + curLine
                        + "\" in macro file " + f.toString());
               }
               else
               {
                  String macroName = curLine.substring(0, eqPos).trim();
                  String macroValue = "";
                  if(eqPos < curLine.length() - 2)
                  {
                     macroValue = curLine.substring(eqPos + 1,
                           curLine.length());
                  }
                  defineGlobalMacro(macroName, macroValue);
                  log("Defining macro: " + macroName + "=" + macroValue);
               }
            }
         }

         reader.close();
      }
      catch(IOException e)
      {
         log("*** ERROR *** " + "Error while reading macro file "
               + f.toString() + ": " + e.getMessage(), e);
      }
   }

   public void registerServerObject(QAObjectDescription obj)
   {
      try
      {
         m_server.registerObject(obj, new QARequestContext(getName(),
               m_scriptName));
      }
      catch(RemoteException e)
      {
         log("*** ERROR *** " + "Could not register the server object "
               + obj.getName(), e);
      }
   }

   @SuppressWarnings("unused")
   public void removeClientListener(QAClientListener listener)
      throws RemoteException
   {
      if (listener != null)
         synchronized(m_clientListeners)
      {
         m_clientListeners.remove(listener);
      }
   }

   public void selfRegister(String serverURL)
      throws RemoteException, MalformedURLException, NotBoundException
   {
      log("Connecting to server " + serverURL + " ...");
      m_server = (QAServer) Naming.lookup(serverURL);
      log("Registering with server...");
      m_server.registerClient(m_name);
      log("Registered with server.");
   }

   // see QAClient interface for description
   @SuppressWarnings("unused")
   public void shutDown(boolean wait) throws RemoteException
   {
      if (!wait)
         shutDown();
      else
      {
         // need to wait if we are currently busy
         synchronized(m_statusLock)
         {
            if (m_isExecuting)
               m_shutdown = true;
         }
         
         if (!m_shutdown)
            shutDown();
      }
   }      


   /**
    * Immediately shuts down the client, interrupting any currently executing
    * script.
    */
   public void shutDown()
   {
      m_isExecuting = true;
      log("Shutting down");
      try
      {
         if(m_executionThread != null)
            m_executionThread.interrupt();
      }
      catch(Throwable t)
      {
         log("*** ERROR *** "
               + "Script execution thread did not shut down cleanly", t);
      }
      try
      {
         publishClientShutdown("Shut down");
      }
      catch(Throwable t)
      {
         log("*** ERROR *** " + "May not have told server we went down", t);
      }
      finally
      {
         try
         {
            log("Unbinding from " + m_name);
            Naming.unbind(m_name);
         }
         catch(Throwable t)
         {
            log("*** ERROR *** "
                  + "May not have unbound cleanly from RMI", t);
         }
         System.exit(0);
      }
   }

   public void log(String s)
   {
      System.out.println(s);
   }

   public void log(String s, Throwable t)
   {
      if (s != null)
         log(s);

      StringWriter w = new StringWriter();
      PrintWriter p = new PrintWriter(w);
      t.printStackTrace(p);
      log(w.toString());
   }

   public void log(Throwable t)
   {
      log(null, t);
   }

   protected static final Properties CLIENT_DEFAULTS;
   protected static final String PROP_clientHost = "clientHost";
   protected static final String PROP_clientName = "clientName";
   protected static final String PROP_clientPort = "clientPort";
   protected static final String PROP_logFileDir = "logFileDir";
   protected static final String PROP_logFileExtension = "logFileExtension";
   protected static final String PROP_logToScreen = "logToScreen";
   protected static final String PROP_macroFiles = "macroFiles";
   protected static final String PROP_preferredServer = "preferredServer";
   protected static final String PROP_startRetries = "startRetries";
   protected static final String
         PROP_startRetryInterval = "startRetryInterval";
   protected static final String PROP_wgetDirectory = "wgetDirectory";

   private Properties m_clientProperties;
   private String m_name;
   private String m_clientPropertiesFilename;
   private final List<QAClientListener> m_clientListeners =
      new ArrayList<QAClientListener>();
   private boolean m_logToScreen;
   private boolean m_isExecuting;
   private Object m_statusLock;
   private QAServer m_server;
   private String m_scriptName;
   private int m_numStartTries;
   private int m_startRetryInterval;
   private Thread m_executionThread;
   private final Map<String, String> m_globalMacros =
         new HashMap<String, String>();

   /**
    * Flag to indicate that {@link #shutDown(boolean) shutDown(true)} was called
    * while a script was executing, and this client should shutdown once the
    * script has completed.
    */
   private boolean m_shutdown = false;

   /**
    * The credentials supplied in the client properties file with the
    * 'autoLoginUid' and 'autoLoginPassword' properties. If none were
    * supplied, this is <code>null</code>. Otherwise, it should contain the
    * creds in the form uid:pw, base64 encoded. Either <code>null</code> or
    * non-empty.
    */
   private String m_loginCredential;

   static
   {
      CLIENT_DEFAULTS = new Properties();
      CLIENT_DEFAULTS.setProperty(PROP_wgetDirectory, "");
      CLIENT_DEFAULTS.setProperty("logFileDir", "logs\\%yyyy\\%MM\\%dd");
      CLIENT_DEFAULTS.setProperty("logFileExtension", ".log");
      CLIENT_DEFAULTS.setProperty("logToScreen", "1");
      CLIENT_DEFAULTS.setProperty(PROP_macroFiles, "");
      CLIENT_DEFAULTS.setProperty("startRetries", "30");
      CLIENT_DEFAULTS.setProperty("startRetryInterval", "10000");
      CLIENT_DEFAULTS.setProperty(PROP_clientName, "EmpireQAClient");
      CLIENT_DEFAULTS.setProperty(PROP_clientPort, "1099");
      CLIENT_DEFAULTS.setProperty(
            PROP_preferredServer, "rmi://:1099/EmpireQAServer");
      try
      {
         CLIENT_DEFAULTS.setProperty("clientHost",
               InetAddress.getLocalHost().getHostName());
      }
      catch (java.net.UnknownHostException e)
      {
         e.printStackTrace();
      }
   }
}
