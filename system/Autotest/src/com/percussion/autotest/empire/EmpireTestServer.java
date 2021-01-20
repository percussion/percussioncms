/******************************************************************************
 *
 * [ EmpireTestServer.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.autotest.empire;

import com.percussion.autotest.framework.IQAWriter;
import com.percussion.autotest.framework.QAClient;
import com.percussion.autotest.framework.QAClientPoolEvent;
import com.percussion.autotest.framework.QAClientPoolListener;
import com.percussion.autotest.framework.QAFileWriter;
import com.percussion.autotest.framework.QAObjectDescription;
import com.percussion.autotest.framework.QAObjectRegistry;
import com.percussion.autotest.framework.QAObjectScope;
import com.percussion.autotest.framework.QARequestContext;
import com.percussion.autotest.framework.QAScriptDocument;
import com.percussion.autotest.framework.QAServer;
import com.percussion.autotest.framework.QAServerObject;
import com.percussion.autotest.framework.QATestResults;
import com.percussion.mail.PSMailMessage;
import com.percussion.mail.PSMailProvider;
import com.percussion.mail.PSSmtpMailProvider;
import com.percussion.test.io.IOTools;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.xml.sax.SAXException;

public class EmpireTestServer extends UnicastRemoteObject
   implements QAServer, ShutDownable, QAClientPoolListener
{
   class ScriptExecutionRequest
   {

      public void decrementCopies()
      {
         if(m_numCopies > 0)
            m_numCopies--;
      }

      public QAScriptDocument getDocument()
         throws IOException, SAXException
      {
         loadDocument();
         return m_doc;
      }

      public int getNumCopies()
      {
         return m_numCopies;
      }

      public URL getURL()
      {
         return m_scriptURL;
      }

      public void incrementCopies()
      {
         if(m_numCopies > 0)
            m_numCopies++;
      }

      private void loadDocument()
         throws IOException, SAXException
      {
         if(m_doc != null)
            return;
         synchronized(this)
         {
            if(m_doc != null)
            {
               return;
            }
            BufferedInputStream in = null;
            try
            {
               URLConnection scriptConn = m_scriptURL.openConnection();
               Object content = scriptConn.getContent();
               in = new BufferedInputStream((InputStream)content);
               m_doc = new QAScriptDocument(PSXmlDocumentBuilder.createXmlDocument(in, false), m_scriptURL.toString());
            }
            finally
            {
               try
               {
                  if(in != null)
                     in.close();
               }
               catch(IOException _ex)
               { }
            }
         }
      }

      private QAScriptDocument m_doc;
      private URL m_scriptURL;
      private int m_numCopies;

      public ScriptExecutionRequest(URL scriptURL, int numCopies)
         throws IOException, SAXException
      {
         if(numCopies < 0)
            numCopies = -1;
         m_numCopies = numCopies;
         m_doc = null;
         m_scriptURL = scriptURL;
         loadDocument();
      }
   }


   public EmpireTestServer(String serverPropertiesFilename)
      throws RemoteException, IOException, InstantiationException,
         ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
         InvocationTargetException
   {
      status("Server using config file: " + serverPropertiesFilename);
      m_serverPropertiesFilename = serverPropertiesFilename;
      (new File(m_serverPropertiesFilename)).createNewFile();
      m_serverProperties = new Properties(SERVER_DEFAULTS);
      m_serverProperties.load(new FileInputStream(m_serverPropertiesFilename));
      String writerClassName =
            m_serverProperties.getProperty("resultWriterClassName");
      if (null != writerClassName && writerClassName.trim().length() > 0)
      {
         status("Loading results writer: " + writerClassName);
         Class writerClass = Class.forName( writerClassName.trim());
         Constructor ctor = writerClass.getConstructor(
               new Class [] { Properties.class });
         m_writer = (IQAWriter) ctor.newInstance(
               new Object [] { m_serverProperties });
      }

      m_name = "rmi://" + m_serverProperties.getProperty("serverHost") + ":"
            + m_serverProperties.getProperty("serverPort") + "/"
            + m_serverProperties.getProperty("serverName");
      m_clientURLs = new Vector();
      m_clientPool = new EmpireTestServerClientPool(this);
      m_clientPool.addListener(this);
      m_queuedScriptDocuments = new Vector();
      m_clientPoolThread = new Thread(m_clientPool);
      m_clientPoolThread.start();
      File lockDir = new File(".qalocks");
      lockDir.mkdir();
      m_lockMgr = new EmpireTestServerLockManager(lockDir);
   }

   private File createDateFormattedFile(String dir, String prefix, String suffix)
      throws IOException
   {
      Date now = new Date();
      if(dir.startsWith("%"))
      {
         if(dir.length() > 1)
            dir = dir.substring(1);
         else
            dir = "";
         dir = dir.replace('\\', File.separatorChar);
         dir = dir.replace('/', File.separatorChar);
         dir = (new SimpleDateFormat(dir)).format(now);
      }
      File theDir = new File(dir);
      if(!theDir.exists())
         theDir.mkdirs();
      if(prefix.startsWith("%"))
      {
         if(prefix.length() > 1)
            prefix = prefix.substring(1);
         else
            prefix = "";
         prefix = prefix.replace('\\', File.separatorChar);
         prefix = prefix.replace('/', File.separatorChar);
         prefix = (new SimpleDateFormat(prefix)).format(now);
      }
      if(suffix.startsWith("%"))
      {
         if(suffix.length() > 1)
            suffix = suffix.substring(1);
         else
            suffix = "";
         suffix = suffix.replace('\\', File.separatorChar);
         suffix = suffix.replace('/', File.separatorChar);
         suffix = (new SimpleDateFormat(suffix)).format(now);
      }
      return File.createTempFile(prefix, suffix, theDir);
   }

   protected File createNewUniqueLogFile()
      throws IOException
   {
      return createDateFormattedFile(m_serverProperties.getProperty("logFileDir"), m_serverProperties.getProperty("logFilePrefix"), m_serverProperties.getProperty("logFileSuffix"));
   }

   protected File createNewUniqueResultsFile()
      throws IOException
   {
      return createDateFormattedFile(m_serverProperties.getProperty("resultsDir"), m_serverProperties.getProperty("resultsPrefix"), m_serverProperties.getProperty("resultsSuffix"));
   }

   private String createSubjectLine(QATestResults results)
   {
      String subject = m_serverProperties.getProperty("notifySubject");
      StringTokenizer st = new StringTokenizer(subject, "%");
      String newSubject = "";
      while(st.hasMoreTokens())
      {
         String curToken = st.nextToken();
         if(curToken.startsWith("s"))
         {
            newSubject = newSubject + results.getScriptName();
            if(curToken.length() > 1)
               newSubject = newSubject + curToken.substring(1);
         }
         else
            if(curToken.startsWith("p"))
         {
            newSubject = newSubject + results.getPassesCount();
            if(curToken.length() > 1)
               newSubject = newSubject + curToken.substring(1);
         }
         else
            if(curToken.startsWith("e"))
         {
            newSubject = newSubject + results.getErrorsCount();
            if(curToken.length() > 1)
               newSubject = newSubject + curToken.substring(1);
         }
         else
            if(curToken.startsWith("f"))
         {
            newSubject = newSubject + results.getFailuresCount();
            if(curToken.length() > 1)
               newSubject = newSubject + curToken.substring(1);
         }
         else
            if(curToken.startsWith("t"))
         {
            newSubject = newSubject + results.getTestsCount();
            if(curToken.length() > 1)
               newSubject = newSubject + curToken.substring(1);
         }
         else
         {
            newSubject = newSubject + curToken;
         }
      }

      return newSubject;
   }

   /**
    * Adds the execution request to the queued scripts and notifies the dispatch
    * thread that there is a new script in the queue.
    *
    * @param req The request to execute a script, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if req is <code>null</code>.
    * @throws IllegalStateException if the server is in the process of shutting
    * down.
    * @throws RemoteException if there is an error communicating with a remote
    * object.
    *
    * @see #startDispatchThread()
    * @see #shutDown(boolean)
    */
   public void dispatchScript(ScriptExecutionRequest req)
      throws RemoteException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      if (m_shutdown)
      {
         throw new IllegalStateException("Shutdown in process, script: " +
            req.getURL() + " will not be queued.");
      }
      else
      {
         synchronized(m_queuedScriptDocuments)
         {
            m_queuedScriptDocuments.add(req);
            m_queuedScriptDocuments.notify();
         }
      }
   }

   /**
    * Convenience version of {@link #dispatchScript(
    * EmpireTestServer.ScriptExecutionRequest)} that first constructs a
    * ScriptExecutionRequest from the supplied parameters.
    *
    * @param scriptURL Defines the location of the file containing the script.
    * May not be <code>null</code>.
    * @param numCopies Specifies the number of copies of the script that should
    * be dispatched.
    *
    * @throws IllegalArgumentException if scriptURL is <code>null</code>.
    * @throws RemoteException if an error occurs communicating with a remote
    * object.  This must be specified to support RMI, but will never actually be
    * thrown from this method.
    */
   public void dispatchScript(URL scriptURL, int numCopies)
      throws RemoteException
   {
      if (scriptURL == null)
         throw new IllegalArgumentException("scriptURL may not be null");

      try
      {
         dispatchScript(new ScriptExecutionRequest(scriptURL, numCopies));
         status("Queued script: " + scriptURL);
      }
      catch(Throwable t)
      {
         statusError("Error dispatching script: " + scriptURL, t);
      }
   }

   protected void displayStatus()
   {
      String statusString = "***** Status *****\n";
      statusString = statusString + "*** Connected clients:\n" + m_clientPool.getClientStatusString("\t");
      statusString = statusString + "*** Queued scripts:\n";
      synchronized(m_queuedScriptDocuments)
      {
         for(int i = 0; i < m_queuedScriptDocuments.size(); i++)
         {
            ScriptExecutionRequest req = (ScriptExecutionRequest)m_queuedScriptDocuments.get(i);
            statusString = statusString + "\t" + req.getURL().toString() + "," + req.getNumCopies() + "\n";
         }

      }
      statusString = statusString + "******************";
      status(statusString);
   }

   /**
    * Removes the specified script from the queue.
    *
    * @param scriptURL The URL of the script to drop.  May not be <code>null
    * </code>.  Will drop the script whose source URL matches the supplied URL.
    * If the specified script has been queued, it will be removed from
    * the list.
    *
    * @throws IllegalArgumentException if scriptURL is <code>null</code>.
    * @throws RemoteException if an error occurs communicating with a remote
    * object.
    */
   public void dropScript(URL scriptURL)
      throws RemoteException
   {
      if (scriptURL == null)
         throw new IllegalArgumentException("scriptURL may not be null");

      synchronized(m_queuedScriptDocuments)
      {
         for(int i = 0; i < m_queuedScriptDocuments.size(); i++)
         {
            ScriptExecutionRequest req =
               (ScriptExecutionRequest)m_queuedScriptDocuments.get(i);
            if(req.getURL().equals(scriptURL))
               m_queuedScriptDocuments.remove(i);
         }

      }
      status("Dropped " + scriptURL.toString());

      if (m_shutdown)
         shutDown(true);
   }

   private boolean exceedsErrorFailureThreshold(QATestResults results)
   {
      int errorThreshold = Integer.parseInt(m_serverProperties.getProperty("errorNotifyThreshold"));
      int failureThreshold = Integer.parseInt(m_serverProperties.getProperty("failureNotifyThreshold"));
      return errorThreshold <= results.getErrorsCount() || failureThreshold <= results.getFailuresCount();
   }

   public boolean lockObject(QARequestContext context, String name, long expiresInMs, long waitMs)
      throws RemoteException
   {
      status("Attempting to lock object " + name);
      QAServerObject ret = null;
      String fullName = null;
      synchronized(m_globalRegistry)
      {
         // try in the most specific context first, then pop up to look in more general contexts
         fullName = context.getClientURL() + "/" + context.getScriptName() + "/" + name;
         ret = m_globalRegistry.getObject(fullName);
         if (ret == null)
         {
            // System.out.println("Could not find " + fullName);

            fullName = context.getScriptName() + "/" + name;
            ret = m_globalRegistry.getObject(fullName);
         }
         if (ret == null)
         {
            // System.out.println("Could not find " + fullName);

            fullName = name;
            ret = m_globalRegistry.getObject(fullName);
         }
      }

      if (ret != null)
      {
         status("Found object " + name + " as " + fullName);
         try
         {
            EmpireLockerId lockId = getLockerId(context);
            Object lockKey = m_lockMgr.getLockKey(fullName, EmpireTestServerLockManager.EXCLUSIVE);
            return m_lockMgr.acquireLock(lockId, lockKey, expiresInMs, waitMs);
         }
         catch (Throwable t)
         {
            t.printStackTrace();
            status(t.toString());
         }
      }
      else
      {
         status("Could not find object named " + fullName);
      }

      return false;
   }

   private EmpireLockerId getLockerId(QARequestContext context)
   {
      return new EmpireLockerId(context);
   }

   public void initialize() throws RemoteException, AlreadyBoundException,
      MalformedURLException
   {
      initializeObjectRegistry();
      status("Binding server to " + m_name + " ...");
      LocateRegistry.createRegistry(
         Integer.parseInt(m_serverProperties.getProperty("serverPort")));
      Naming.bind(m_name, this);
      startDispatchThread();
      initializeClientPool();
      status("Finished initializing");
   }

   private void initializeClientPool()
   {
      status("Initializing Client Pool...");
      String clientList = m_serverProperties.getProperty("clientList");
      if(clientList == null)
         return;
      StringTokenizer st = new StringTokenizer(clientList, ";");
      String newList = "";
      while(st.hasMoreTokens())
      {
         String possibleClient = st.nextToken();
         try
         {
            registerClient(possibleClient);
         }
         catch(MalformedURLException _ex)
         {
            statusWarning("Malformed client URL in last known client list: " + 
               possibleClient + " , removing it from list.");
            continue;
         }
         catch(Exception _ex)
         { }
         newList = ";" + possibleClient;
      }

      m_serverProperties.setProperty("clientList", newList);
   }

   private void initializeObjectRegistry()
   {
      status("Initializing Object Registry...");
      m_globalRegistry = new QAObjectRegistry();
   }

   /**
    * Runs the test server as a standalone program. Server will run in console
    * mode and accept commands at the console.  The following commands are
    * supported:
    *
    * <ul>
    * <li>!quit - immediately shuts down the server, breaking communication
    * with any currently connected clients.</li>
    * <li>!shutdown - gracefully shuts down the server.  Will stop accepting
    * new requests to queue scripts for execution, wait until the queue has been
    * emptied, wait for each client to complete their last script, shutdown each
    * client, and finally shuts down the server.</li>
    * <li>!drop &lt;scriptUrl&gt; - removes the specified script from the queued
    * script list where "scriptUrl" points to the location of a file containing
    * the script.
    * Will drop the script whose source URL matches the supplied URL</li>
    * <li>!status - Will print out the current status of the server.</li>
    * <li>!load &lt;cmdList&gt; Will load the file specified by "cmdList",
    * which must be a text file with one valid command on each line.  Each
    * command will be processed as if they were entered at the console.  May not
    * contain a !load command.
    * </li>
    * <li>&lt;cmdList&gt - same as !load, but may only specify filename as long
    * as it ends with ".txt".</li>
    * <li>&lt;scirptURL[,&lt;numCopies&gt;]&gt; - specifies a valid URL for a
    * script to run.  Script will be queued and run.  Optionally can specify
    * the number of copies of the script to queue, to cause it to be executed
    * more than once.</li>
    * </ul>
    *
    * @param args An array of arguments passed to this program at the command
    * line:
    * <ol>
    * <li>properties - Optional.  The name of the server properties file.  If
    * not supplied, will attempt to load properties from a file named
    * "qaserver.properties" in the current directory.  All properties are
    * optional, and default properies are supplied if any or all are missing.
    * <li>cmdList - Optional.  May only be speciified if properties have also
    * been supplied. A URL or a filename to the list of commands to be executed.
    * The same as entering the "!Load" command from the console.</li>
    * </ol>
    */
   public static void main(String args[])
   {
      EmpireTestServer server = null;
      try
      {
         String serverPropertiesFilename = "qaserver.properties";
         if (args.length > 0)
            serverPropertiesFilename = args[0];

         server = new EmpireTestServer(serverPropertiesFilename);
         server.initialize();

         if (args.length > 1)
            processCommandList(server, getFileName(args[1]));

         BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));

         for (String line = in.readLine();
            line != null && m_dispatchThread.isAlive(); line = in.readLine())
         {
            line = line.trim();
            if (line.startsWith("!load"))
            {
               processCommandList(server, getFileName(
                  line.substring("!load".length())));
            }
            else if (line.indexOf(".txt") >= 0)
            {
               processCommandList(server, getFileName(line));
            }
            else
            {
               try
               {
                  processCommand(server, line);
               }
               catch (MalformedURLException e)
               {
                  System.out.println("Error: " + line + " has an invalid URL.");
                  help();
               }
            }
         }
      }
      catch (Throwable t)
      {
         t.printStackTrace();
         System.out.println("Error: " + t.getLocalizedMessage());
         usage();
      }
      finally
      {
         if (server != null)
         {
            try
            {
               server.shutDown(true);
            }
            catch (RemoteException e)
            {
               System.out.println("Error processing shutdown: " +
                  e.getLocalizedMessage());
               server.shutDown();
            }
         }
      }
   }

   /**
    * Parses the specified command and then executes it.  See {@link
    * #main(String[]) main()} for a list of valid commands.  Does not process
    * the <code>!load</code> command or its convenience version.
    *
    * @param server The server used to process the command.  Assumed not <code>
    * null</code>.
    * @param cmd The command to process.  Assumed not <code>null</code>.
    *
    * @throws MalformedURLException if a script URL is invalid
    * @throws RemoteException if there is an error communicating with a remote
    * object.
    */
   private static void processCommand(EmpireTestServer server, String cmd)
      throws MalformedURLException, RemoteException
   {
      if (cmd.startsWith("!"))
      {
         if (cmd.equals("!quit"))
            server.shutDown();
         if (cmd.equals("!shutdown"))
         {
            server.shutDown(true);
            server = null;
         }
         else if (cmd.startsWith("!drop"))
         {
            int spacePos = cmd.indexOf(32, 5);
            if (spacePos == -1)
               System.out.println("Invalid script URL");
            else
            {
               try
               {
                  URL dropURL = new URL(cmd.substring(spacePos));
                  server.dropScript(dropURL);
               }
               catch(MalformedURLException e)
               {
                  System.out.println("Invalid URL");
               }
            }
         }
         else if (cmd.startsWith("!status"))
            server.displayStatus();
         else
            help();
      }
      else
      {
         StringTokenizer tok = new StringTokenizer(cmd, ",");
         if (tok.hasMoreTokens())
         {
            String rawUrl = fixFileSeparators(tok.nextToken());
            URL scriptURL = new URL(rawUrl);
            int numCopies = 1;
            if (tok.hasMoreTokens())
            {
               try
               {
                  numCopies = Integer.parseInt(tok.nextToken());
               }
               catch (NumberFormatException e)
               {
                  System.out.println("Command (" + cmd +
                     ") has a malformed number of copies.");
               }
            }

            server.dispatchScript(scriptURL, numCopies);
         }
      }
   }
   
   /**
    * Fixes the url string for local file URL's (i.e. prefixed by file:)
    * by making sure the URL is using the correct file separator
    * for the current OS
    * @param url the URL string, assumed to be not <code>null</code>
    * @return the fixed URL string
    */
   private static String fixFileSeparators(String url)
   {
      if(!url.toLowerCase().startsWith("file:"))
         return url;
      char separator = File.separatorChar;
      char token = separator == '/' ? '\\' : '/';
      return url.replace(token, separator);
   }

   /**
    * Gets the file name from the provided text line. Assumes the provided line
    * is an URL. If there is an error converting the URL to a filename, it
    * assumes the provided line already contains the file name.
    *
    * @param line the line to get the file name from, might be <code>null</code>.
    * @return the file name, might be <code>null</code> if <code>null</code>
    *    was provided.
    */
   private static String getFileName(String line)
   {
      String fileName = null;
      if (line != null)
      {
         line = line.trim();
         try
         {
            URL url = new URL(line);
            fileName = url.getPath();
         }
         catch (MalformedURLException e)
         {
            // guess they provided the filename
            fileName = line;
         }
      }

      return fileName;
   }

   /**
    * Prints the usage information to the server console.
    */
   private static void usage()
   {
      System.out.println("Usage:");
      System.out.println("java com.percussion.autotest.empire.EmpireTestServer [<properties>, <scripts>]");
      System.out.println("Where <properties> must be the full server properties file name.");
      System.out.println("Where <scripts> is a URL or a filename to the list of scripts to be run.");
   }

   /**
    * Prints the help information to the server console.
    */
   private static void help()
   {
      System.out.println("Help:");
      System.out.println("Valid commands are: <url>, <file>, !quit, !drop " +
         "<url>, !status, !shutdown");
      System.out.println("Where <url> is a URL to the script or script list " +
         "to be run.");
      System.out.println("Where <file> is the full path of the script or " +
         "script list to bve run.");
   }

   /**
    * Execute all commands contained in the provided file. The provided
    * text file should have one line for each command.  See {@link #main(
    * String[]) main} for a list of commands.  <code>!load</code> or its
    * convenience version will be ignored and a message will print to the
    * console to that effect.  Lines starting with # are treated as comments.
    * For example:
    *    #
    *    # Test scripts...
    *    #
    *    file:Scripts\TestCookies.xml
    *    file:Scripts\TestCopies.xml, 5
    *    !shutdown
    *
    * @param server the server to dispatch the scripts to, assumed not
    *    <code>null</code>.
    * @param file the file string from where to get all commands to be
    *    executed, assumed not <code>null</code>.
    *
    * @throws RemoteException if there is an error communicating with a remote
    * object.
    * @throws FileNotFoundException If the command file does not exist.
    * @throws IOException If there is an error reading from the command file.
    */
   private static void processCommandList(EmpireTestServer server,
      String file) throws FileNotFoundException, IOException, RemoteException
   {
      String fixedFileString = file.replace('\\', '/');
      BufferedReader in = 
         new BufferedReader(new FileReader(fixedFileString));
      int lineNumber = 0;

      for (String line = in.readLine(); line != null; line = in.readLine())
      {
         lineNumber++;
         line = line.trim();
         if (line.startsWith("#"))
            continue;

         try
         {
            if (line.startsWith("!load") || (!line.startsWith("!") &&
               line.indexOf(".txt") >= 0))
            {
               System.out.println(file + "(" + lineNumber + "): " +
                  "Command file cannot contain !Load or a .txt file");
            }
            else
               processCommand(server, line);
         }
         catch (MalformedURLException _ex)
         {
            System.out.println(file + "(" + lineNumber + "): " + line +
               " has a malformed URL.");
         }
      }
   }

   /**
    * Records the results of a testing run.  When the server initializes, it
    * looks for a property in the server properties file named
    * resultWriterClassName, and instantiates that class to handle the writing
    * of the results. This class must have a ctor that accepts a single
    * parameter, Properties, which will be the server properties file. If the
    * property is not found, {@link QAFileWriter} is used. The class must
    * implement the {@link IQAWriter} interface.
    * <p>If the default writer is used, a message will also be sent to a
    * specified mail address with the results if the error threshold is
    * exceeded. If the writer is overridden, then no mail is sent.
    *
    * @param results The results object that contains all information to be
    *    recorded.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if results is <code>null</code>.
    *
    * @throws ClassCastException if the writer specified in the QA server
    *    properties does not implement the correct interface.
    */
   public void recordResults(QATestResults results)
      throws RemoteException
   {
      if ( null == results )
         throw new IllegalArgumentException( "results param can't be null" );

      // do not record results in stress test mode
      if (isStressTest())
         return;

      System.out.println("Receiving results from " +
         results.getClientName() + " for script " + results.getScriptName());
      try
      {
         if ( null != m_writer )
         {
            m_writer.write( results );
            return;
         }

         File resultsFile = createNewUniqueResultsFile();
         IQAWriter writer = new QAFileWriter(resultsFile);
         writer.write(results);

         // do we have to mail anything?
         if (exceedsErrorFailureThreshold(results))
         {
            String notifyEmails =
               m_serverProperties.getProperty("notifyEmails");
            if (notifyEmails.length() > 0)
            {
               System.out.println(
                  "Error/failure threshold exceeded. Sending email notifications.");
               String mailServer = m_serverProperties.getProperty("mailServer");
               if (mailServer == null || mailServer.length() == 0)
                  mailServer = System.getProperty("mail.host");

               if (mailServer == null)
               {
                  System.out.println(
                     "Error: Could not send results mail notification because the server was not set.");
                  return;
               }
               try
               {
                  Properties props = new Properties();
                  props.put(PSSmtpMailProvider.PROPERTY_HOST, mailServer);
                  PSMailProvider mailProv = new PSSmtpMailProvider(props);
                  PSMailMessage msg = new PSMailMessage();
                  msg.setCharEncoding("US-ASCII");
                  msg.setFrom("E2Server@percussion.com");
                  {
                     StringTokenizer tok =
                        new StringTokenizer(notifyEmails, ";");
                     while (tok.hasMoreTokens())
                     {
                        msg.addSendTo(tok.nextToken());
                     }
                  }

                  msg.setSubject(createSubjectLine(results));
                  msg.appendBodyText("Results file: " +
                     resultsFile.getCanonicalPath() + "\n" + results.toString());
                  status("Sending results to " + mailServer + "...");
                  mailProv.send(msg);
               }
               catch (Throwable e)
               {
                  e.printStackTrace();
                  statusWarning("Could not send results mail notification: " +
                     e.toString());
               }
            }
         }
      }
      catch (Throwable e)
      {
         statusError("Could not write results: " + e.getMessage());
      }
      finally
      {
         status( "Finished recording results" );
      }
   }

   public void registerClient(String URL)
      throws RemoteException, MalformedURLException, NotBoundException
   {
      QAClient client = (QAClient) Naming.lookup(URL);
      if(!m_clientPool.addClient(client))
      {
         m_clientURLs.add(URL);
         status("Registered client: " + URL);
      }
   }

   public void registerObject(QAObjectDescription obj, QARequestContext context)
      throws RemoteException
   {
      synchronized(m_globalRegistry)
      {
         int scope = obj.getScope();
         String qualifiedName = null;
         if(scope == QAObjectScope.INSTANCE)
            qualifiedName = context.getClientURL() + "/" + context.getScriptName() + "/" + obj.getName();
         else  if(scope == QAObjectScope.SCRIPT)
            qualifiedName = context.getScriptName() + "/" + obj.getName();
         else  if(scope == QAObjectScope.GLOBAL)
            qualifiedName = obj.getName();

         if (m_globalRegistry.getObject(qualifiedName) == null)
         {
            status("Registering object " + qualifiedName);
            QAServerObject newObj = new QAServerObject();
            m_globalRegistry.putObject(qualifiedName, newObj);
         }
      }
   }

   /**
    * Shuts down the server.  Once this method is called, no new scirpts will
    * be dispatched.  If {@link #getTestType()} is equal to  {@link
    * QAServer#TEST_STRESS}, then calling this method with <code>wait</code>
    * equal to <code>true</code> will not shutdown the server, and a message
    * will be logged to the console to that effect.  See {@link
    * QAServer#shutDown(boolean) shutDown} method in base class for more info.
    */
   public void shutDown(boolean wait) throws RemoteException
   {
      if (!wait)
         shutDown();
      else if (getTestType() == TEST_STRESS)
      {
         status("Cannot perform delayed shutdown in stress mode.");
         return;
      }
      else
      {
         // block queueing of any new scripts
         if (!m_shutdown)
         {
            m_shutdown = true;
            status("Shutdown initiated, no new scripts will be queued for " +
               "dispatch");
         }

         // wait for queue to clear
         boolean allClear = false;
         synchronized(m_queuedScriptDocuments)
         {
            if (m_queuedScriptDocuments.isEmpty())
               allClear = true;
         }

         /* If we have the go ahead, shutdown client pool, then the server.  If
          * not, then this method will be called again each time a script is
          * removed from the queue, and we'll keep checking.  Since no scripts
          * should be added to the queue, eventually the queue will be empty.
          */
         if (allClear)
         {
            m_clientPool.shutdown();  // this will wait
            shutDown();
         }
      }
   }

   /**
    * Immediately shuts down the server.  Any queued scripts will be discarded.
    * Clients are not notified of the shutdown.
    */
   public void shutDown()
   {
      status("Shutting down...");
      synchronized(m_queuedScriptDocuments)
      {
         m_queuedScriptDocuments.clear();
      }
      try
      {
         m_clientPoolThread.interrupt();
      }
      catch(Throwable t)
      {
         statusError("Client pool thread did not shut down cleanly: " + t.toString());
      }
      try
      {
         m_dispatchThread.interrupt();
      }
      catch(Throwable t)
      {
         statusError("Dispatch thread did not shut down cleanly: " + t.toString());
      }
      String newList = "";
      String clientList = m_serverProperties.getProperty("clientList");
      if(clientList != null)
      {
         for(StringTokenizer st = new StringTokenizer(clientList, ";"); st.hasMoreTokens();)
         {
            String client = st.nextToken();
            if(!m_clientURLs.contains(client))
            {
               m_clientURLs.add(client);
               newList = newList + ";" + client;
            }
         }

      }
      for(Iterator i = m_clientURLs.iterator(); i.hasNext();)
         newList = newList + ";" + i.next().toString();

      m_serverProperties.setProperty("clientList", newList);
      try
      {
         m_serverProperties.store(new FileOutputStream(m_serverPropertiesFilename), null);
      }
      catch(Throwable t)
      {
         statusError("Error saving server properties: " + t.toString());
      }
      finally
      {
         try
         {
            status("Unbinding from " + m_name);
            Naming.unbind(m_name);
         }
         catch(Throwable t)
         {
            statusError("Server may not have unbound cleanly from RMI: " + t.toString());
         }
         System.exit(0);
      }
   }

   private void startDispatchThread()
   {
      m_dispatchThread = new Thread(new Runnable()
      {
         public void run()
         {
            try
            {
               int startAt = 0;
               if (isRegressionTest())
                  status("Running in regression mode");
               else if (isPerformanceTest())
                  status("Running in performance mode");
               else
                  status("Running in stress mode");

               boolean stop = false;
               while (!stop)
               {
                  // see if we are shutting down
                  if (m_shutdown)
                     shutDown(true);

                  synchronized(m_queuedScriptDocuments)
                  {
                     try
                     {
                        if (m_queuedScriptDocuments.size() == 0)
                           m_queuedScriptDocuments.wait();
                     }
                     catch (InterruptedException e)
                     {
                        break;
                     }
                  }

                  if (isPerformanceTest())
                  {
                     ArrayList clientConfigurations = new ArrayList();
                     int requiredClients = loadClientConfigurations(
                        m_serverProperties, clientConfigurations);
                     if (requiredClients != -1 &&
                        m_clientPool.getAvailableClients() < requiredClients)
                     {
                        statusError(
                           "Not enough clients available! This test needs at least " +
                           requiredClients + " clients to run. Please start more " +
                           "clients and try again!");

                        m_queuedScriptDocuments.clear();
                     }
                     else
                     {
                        /*
                         * In PERFORMANCE mode if client configurations were
                         * specified we loop over each configuration and perform
                         * the test(s) specified. If no client configuration was
                         * specified we perform the test(s) on all available
                         * clients.
                         */
                        ScriptExecutionRequest req =
                           (ScriptExecutionRequest) m_queuedScriptDocuments.remove(0);
                        try
                        {
                           if (requiredClients == -1)
                           {
                              m_clientPool.dispatchScriptToAllClients(
                                 req.getDocument(), 300000L); // timeout in 5 minutes
                           }
                           else
                           {
                              String resultDb =
                                 m_serverProperties.getProperty("resultDatabase");
                              int pos = resultDb.indexOf('.');
                              if (pos == -1)
                              {
                                 statusError(
                                    "No file extension for the result database was provided.");

                                 stop = true;
                                 break;
                              }
                              String emptyResultDb = resultDb.substring(0, pos) +
                                 "Empty" + resultDb.substring(pos);

                              // first create a copy of the empty result database
                              copyFile(resultDb, emptyResultDb);

                              while (!clientConfigurations.isEmpty())
                              {
                                 m_allClientsDone = false;

                                 Integer clients =
                                    (Integer) clientConfigurations.remove(0);
                                 m_clientPool.dispatchScriptToRequiredClients(
                                    req.getDocument(), clients.intValue(),
                                    300000L); // timeout in 5 minutes

                                 while (!m_allClientsDone)
                                    Thread.sleep(1000);

                                 backupResults(resultDb, clients);
                                 copyFile(emptyResultDb, resultDb);
                              }
                           }
                        }
                        catch (FileNotFoundException e)
                        {
                           statusError("File not found: ", e);
                        }
                        catch (IOException e)
                        {
                           statusError("Could not dispatch script: ", e);
                        }
                        catch (SAXException e)
                        {
                           statusError("SAX parsing error: ", e);
                        }
                        catch (InterruptedException e)
                        {
                           statusError("Process interrupted, tests not started:", e);
                        }
                     }
                  }
                  else if (isRegressionTest())
                  {
                     /*
                      * In REGRESSION mode we wait for new clients. As soon as
                      * a new client becomes available, we dispatch the next
                      * preloaded script to that client. This is done for each
                      * queued script until no more scripts are available.
                      */
                     try
                     {
                        m_clientPool.waitForNewClient(1000L);
                     }
                     catch (InterruptedException e)
                     {
                        break;
                     }

                     // reset startAt (the round-robin script index) if necessary
                     if (startAt >= m_queuedScriptDocuments.size())
                        startAt = 0;

                     for (int i=startAt; i<m_queuedScriptDocuments.size(); i++)
                     {
                        ScriptExecutionRequest req =
                           (ScriptExecutionRequest) m_queuedScriptDocuments.get(i);
                        try
                        {
                           if (!m_clientPool.dispatchScriptToFreeClient(
                              req.getDocument()))
                              break; // we ran out of clients to dispatch to

                           req.decrementCopies();

                           if (req.getNumCopies() == 0)
                              m_queuedScriptDocuments.remove(i);

                           startAt++; // round robin increment script index
                        }
                        catch (FileNotFoundException e)
                        {
                           m_queuedScriptDocuments.remove(req);
                        }
                        catch (IOException e)
                        {
                           statusError("Could not dispatch script: ", e);
                        }
                        catch (SAXException e)
                        {
                           m_queuedScriptDocuments.remove(req);
                        }
                     }
                  }
                  else
                  {
                     /*
                      * In STRESS mode we wait for new clients and dispatch
                      * the preloaded scripts to every client as soon as they
                      * become available. Once a client has finished and
                      * recorded the results, it will be started over again.
                      */
                     try
                     {
                        m_clientPool.waitForNewClient(1000L);
                     }
                     catch (InterruptedException e)
                     {
                        break;
                     }

                     if (startAt >= m_queuedScriptDocuments.size())
                        startAt = 0;

                     for (int i=startAt; i<m_queuedScriptDocuments.size(); i++)
                     {
                        ScriptExecutionRequest req =
                           (ScriptExecutionRequest) m_queuedScriptDocuments.get(i);
                        try
                        {
                           m_clientPool.dispatchScriptToFreeClient(
                              req.getDocument());
                        }
                        catch (FileNotFoundException e)
                        {
                           m_queuedScriptDocuments.remove(req);
                        }
                        catch (IOException e)
                        {
                           statusError("Could not dispatch script: ", e);
                        }
                        catch (SAXException e)
                        {
                           m_queuedScriptDocuments.remove(req);
                        }
                     }
                  }
               }
            }
            catch (RemoteException e)
            {
               e.printStackTrace();
            }
         }
      });
      m_dispatchThread.start();
   }

   /**
    * Creates a backup of the result database.
    *
    * @param the file name including path of the result database to backup,
    *    assumed not <code>null</code>, a file extension is assumed.
    * @param clients the client configuration, assumed not <code>null</code>.
    * @throws FileNotFoundException if a file could not bee found.
    * @throws IOException for any failed IO operation.
    */
   private void backupResults(String resultDb, Integer clients)
      throws FileNotFoundException, IOException
   {
      String extension = "";
      int pos = resultDb.indexOf('.');
      if (pos != -1)
         extension = resultDb.substring(pos);
      String location = m_serverProperties.getProperty("resultBackupLocation");
      String resultDbBackup = location + "/results_" + clients.intValue();

      int i = 0;
      File test = new File(resultDbBackup);
      while (test.exists())
         i++;

      resultDbBackup += "_" + i + extension;

      copyFile(resultDb, resultDbBackup);
   }

   /**
    * Copies the input file to the output file.
    *
    * @param in the input file name, including path information, assumed not
    *    <code>null</code>.
    * @param out the output file name, including path information, assumed not
    *    <code>null</code>.
    * @throws FileNotFoundException if a file could not bee found.
    * @throws IOException for any failed IO operation.
    */
   private void copyFile(String in, String out) throws FileNotFoundException,
      IOException
   {
      FileInputStream fIn = new FileInputStream(new File(in));
      FileOutputStream fOut = new FileOutputStream(new File(out));

      IOTools.copyStream(fIn, fOut);

      fIn.close();
      fOut.close();
   }

   /**
    * Load all client configurations to be run for this test. The list is
    * separated by coma, single entries and range entries will be supported.
    * e.g. 1-5, 10, 100. If no client configuration is specified in the provided
    * properties, this will return -1.
    *
    * @param properties the properties from where to get the client
    *    configuration from, assumed not <code>null</code>.
    * @param clientConfigurations a list to which all client configurations will
    *    be stored, assumed not <code>null</code>.
    * @return the maximal needed number of clients to perform the test for all
    *    specified configurations, -1 if no client configuration was specified.
    */
   private int loadClientConfigurations(Properties properties,
      ArrayList clientConfigurations)
   {
      String clientConfiguration =
         properties.getProperty("clientConfigurations");
      if (clientConfiguration == null)
         return -1;

      StringTokenizer tok = new StringTokenizer(clientConfiguration, ",");
      while (tok.hasMoreElements())
      {
         String elem = tok.nextToken();
         int pos = elem.indexOf('-');
         if (pos > 0)
         {
            int startRange = Integer.parseInt(elem.substring(0, pos));
            int endRange = Integer.parseInt(elem.substring(pos+1));
            for (int i=startRange; i<=endRange; i++)
               clientConfigurations.add(new Integer(i));
         }
         else
         {
            int single = Integer.parseInt(elem);
            clientConfigurations.add(new Integer(single));
         }
      }

      int requiredClients = 0;
      for (int i=0; i<clientConfigurations.size(); i++)
      {
         int test = ((Integer) clientConfigurations.get(i)).intValue();
         if (test > requiredClients)
            requiredClients = test;
      }

      return requiredClients;
   }

   protected void status(String s)
   {
      System.out.println(s);
   }

   protected void statusError(String s)
   {
      System.err.println("*** ERROR *** " + s);
   }

   protected void statusError(String s, Throwable t)
   {
      StringWriter w = new StringWriter();
      PrintWriter p = new PrintWriter(w);
      t.printStackTrace(p);
      statusError(s + ": " + w.toString());
   }

   protected void statusWarning(String s)
   {
      System.out.println("WARNING: " + s);
   }

   protected static final Properties SERVER_DEFAULTS;
   protected static final String PROP_serverPort = "serverPort";
   protected static final String PROP_serverName = "serverName";
   protected static final String PROP_serverHost = "serverHost";
   protected static final String PROP_clientList = "clientList";
   protected static final String PROP_resultsDir = "resultsDir";
   protected static final String PROP_resultsPrefix = "resultsPrefix";
   protected static final String PROP_resultsSuffix = "resultsSuffix";
   protected static final String PROP_failureNotifyThreshold = "failureNotifyThreshold";
   protected static final String PROP_errorNotifyThreshold = "errorNotifyThreshold";
   protected static final String PROP_notifyEmails = "notifyEmails";
   protected static final String PROP_notifySubject = "notifySubject";
   protected static final String PROP_mailServer = "mailServer";
   protected static final String PROP_logFileDir = "logFileDir";
   protected static final String PROP_logFilePrefix = "logFilePrefix";
   protected static final String PROP_logFileSuffix = "logFileSuffix";
   private Properties m_serverProperties;
   private String m_name;
   private String m_serverPropertiesFilename;
   private QAObjectRegistry m_globalRegistry;
   private Vector m_clientURLs;
   private Vector m_queuedScriptDocuments;
   private EmpireTestServerClientPool m_clientPool;
   private Thread m_clientPoolThread;
   private static Thread m_dispatchThread;
   private EmpireTestServerLockManager m_lockMgr;

   /**
    * This is an optional test results writer. When the server initializes, it
    * looks for a property in the server properties file named
    * resultWriterClassName. If found, the class is instantiated as an
    * IQAWriter object using a single param ctor that accepts a Property
    * object. This property object will contain all of the properties defined
    * in the server properties file. If initialized, never <code>null</code>
    * after that.
    */
   private IQAWriter m_writer = null;

   static
   {
      SERVER_DEFAULTS = new Properties();
      SERVER_DEFAULTS.setProperty("serverPort", "1099");
      SERVER_DEFAULTS.setProperty("serverName", "EmpireQAServer");
      SERVER_DEFAULTS.setProperty("resultsPrefix", "%'res_'kkmmss");
      SERVER_DEFAULTS.setProperty("resultsSuffix", ".txt");
      SERVER_DEFAULTS.setProperty("resultsDir", "%.\\'TestResults'\\yyyy\\MM\\dd");
      SERVER_DEFAULTS.setProperty("failureNotifyThreshold", "1");
      SERVER_DEFAULTS.setProperty("errorNotifyThreshold", "1");
      SERVER_DEFAULTS.setProperty("notifyEmails", "");
      SERVER_DEFAULTS.setProperty("notifySubject", "%s T(%t)P(%p)F(%f)E(%e)");
      SERVER_DEFAULTS.setProperty("logFileDir", "'%.\\Logs'\\yyyy\\MM\\dd");
      SERVER_DEFAULTS.setProperty("logFilePrefix", "%'log_'kkmmss");
      SERVER_DEFAULTS.setProperty("logFileSuffix", ".log");
      SERVER_DEFAULTS.setProperty("testType", "regression");
      try
      {
         SERVER_DEFAULTS.setProperty("serverHost", InetAddress.getLocalHost().getHostName());
      }
      catch (java.net.UnknownHostException e)
      {
         e.printStackTrace();
      }
   }

   // implementation for QAServer
   public boolean isRegressionTest() throws RemoteException
   {
      return getTestType() == TEST_REGRESSION;
   }

   // implementation for QAServer
   public boolean isPerformanceTest() throws RemoteException
   {
      return getTestType() == TEST_PERFORMANCE;
   }

   // implementation for QAServer
      public boolean isStressTest() throws RemoteException
   {
      return getTestType() == TEST_STRESS;
   }

   // implementation for QAServer
   public int getTestType() throws RemoteException
   {
      if (m_testType >= 0)
         return m_testType;

      String testType = m_serverProperties.getProperty("testType", "regression");
      if (testType.trim().equalsIgnoreCase("performance"))
         m_testType = TEST_PERFORMANCE;
      else if (testType.trim().equalsIgnoreCase("stress"))
         m_testType = TEST_STRESS;
      else
         m_testType = TEST_REGRESSION;

      return m_testType;
   }

   /**
    * A flag indication if all currently working clients have reported the
    * results. This is set to <code>false</code> for each set of clients a
    * perfomance test is done. Its reset to <code>true</code> once all clients
    * for the current set are done.
    */
   private boolean m_allClientsDone = true;

   /**
    * Indicate that all clients have recorded the results collected in the
    * current run.
    */
   public void allClientsDone(QAClientPoolEvent event)
   {
      m_allClientsDone = true;
   }

   /**
    * Get a unique client ID for this autotest server.
    *
    * @return a new, unique client id. Alway greater than 0.
    */
   public int getClientId()
   {
      return ms_nextClientId++;
   }

   /**
    * Each client registering with this server will get a unique client ID.
    * This member reflects the next available client ID and is incremented each
    * time a new client requested one.
    */
   private static int ms_nextClientId = 1;

   /**
    * The test type to be performed, initialized on the first call to
    * getTestType(), never changed after that.
    */
   private int m_testType = -1;


   /**
    * Flag to indicate that {@link #shutDown(boolean) shutDown(true)} was called
    * while there were scripts queued, and the server should shutdown once the
    * queue is empty.  No new scripts should be added to the queue if this flag
    * is <code>true</code>.
    */
   private boolean m_shutdown = false;
}
