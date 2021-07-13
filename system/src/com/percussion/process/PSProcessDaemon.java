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
package com.percussion.process;

import com.percussion.util.IOTools;
import com.percussion.util.PSOsTool;
import com.percussion.util.PSServerShutdownHelper;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * This class is used to execute a small set of commands on a remote server.
 * The following commands are currently supported:
 * <ol>
 *    <li>get</li>
 *    <li>put</li>
 *    <li>mkdir</li>
 *    <li>rmdir</li>
 *    <li>execprocess</li>
 *    <li>waitforprocess</li>
 * </ol>
 * <pre>The format of the protocol is very simple. Command requests are sent
 * as follows:
 *     
 *    &lt;cmd&gt;&lt;p1-len&gt;&lt;p1-data&gt;&lt;p2-len&gt;&lt;p2-data&gt;&lt;pN-len&gt;&lt;pN-data&gt;&lt;term&gt;
 * 
 * where 
 *    cmd - the name of the command to execute. Fixed length of 20 bytes, 
 * space padded at the end of the command string. String is UTF-8 encoded. 
 * Case-insensitive.
 *    p1, p2 ... pN - are the parameters needed for the command, these are
 * defined for each command
 *    pN-len - the length, in bytes, of the param data, 4 byte, signed integer
 *    pN-data - the data for the Nth parameter, the command deteremines how it
 * is interpreted.
 *    term - 4 byte terminator, 0xffffffff. Since lengths are signed, this 
 * value can never occur as a param len.
 * 
 * Command results are sent as follows:
 * 
 *    &lt;code&gt;&lt;len&gt;&lt;result-data&gt;
 * 
 * where
 *    code - indicates error conditions encountered. Negative codes are used
 * by the framework, while positive codes are used by the commands. A code of
 * 0 indicates overall success. A 4 byte signed integer.
 *    len - the length, in bytes, of the result-data, 4 byte integer. The len
 * may be 0.
 *    result-data - a command dependent result
 * 
 * Integers should and will be written using Java's {@link 
 * DataOutputStream#writeInt(int)} method.
 * </pre>
 * If using the {@link #sendCommand(String, int, String, List, StringBuffer)
 * sendCommand} method, the parameters for all commands should be 
 * <code>String</code>, except <code>CMD_SAVE_BINARY_FILE</code>, which should
 * be an <code>InputStream</code> for the data. 
 * <table>
 *    <tr>
 *       <th>Command name</th>
 *       <th>Description</th>
 *       <th>Params</th>
 *       <th>Result</th>
 *    </tr>
 * 
 *    <tr>
 *       <td>{@link #CMD_GET_FILE}</td>
 *       <td>Retrieve a file from the remote file system.</td>
 *       <td>A single parameter, the path and name of the file. See note below
 * about path limitations.</td>
 *       <td>The content of the file. It is assumed that the file is a text 
 * file and that the content is encoded using the default encoding of the
 * system. The returned stream will be encoded as UTF-8.</td>
 *    </tr>
 *  
 *    <tr>
 *       <td>{@link #CMD_SAVE_FILE}</td>
 *       <td>Place a file on the remote file system. If it exists, it is 
 * overwritten w/o warning.</td>
 *       <td>2 parameters are required, the path and name of the file and the 
 * file content.  See note below about path limitations. The data is assumed to 
 * be text and encoded as UTF-8. It will be written to the file system using the 
 * default encoding of the OS.</td>
 *       <td>If successful, an empty result is returned. Otherwise, the text of 
 * the exception is returned.</td>
 *    </tr>
 * 
 *    <tr>
 *       <td>{@link #CMD_SAVE_BINARY_FILE}</td>
 *       <td>Place a binary file on the remote file system. If it exists, it is 
 * overwritten w/o warning.</td>
 *       <td>2 parameters are required, the path and name of the file and the 
 * file content.  See note below about path limitations.</td>
 *       <td>If successful, an empty result is returned. Otherwise, the text of 
 * the exception is returned.</td>
 *    </tr>
 * 
 *    <tr>
 *       <td>{@link #CMD_FS_OBJ_EXISTS}</td>
 *       <td>Determine if a file or directory exists on the file system.</td>
 *       <td>A single parameter is expected, the path of the object to
 * check. See note below about path limitations.</td>
 *       <td>If successful, a single digit is returned. Otherwise, the text of 
 * the exception is returned. The digit will have a 0 value to indicate
 * <code>false</code> and 1 value to indicate <code>true</code>.</td>
 *    </tr>
 * 
 *    <tr>
 *       <td>{@link #CMD_MAKE_DIRS}</td>
 *       <td>Create a directory structure on the remote file system.</td>
 *       <td>A single parameter is expected, the path of the directory to
 * create. If any part of the path is not present, it will be created. If 
 * the directories already exist, no action is taken. See note below
 * about path limitations.</td>
 *       <td>If successful, an empty result is returned. Otherwise, the text of 
 * the exception is returned.</td>
 *    </tr>
 * 
 *    <tr>
 *       <td>{@link #CMD_REMOVE_FS_OBJ}</td>
 *       <td>Remove a file or directory structure on the remote file system.
 * If a directory is specified, it is removed recursively. Even if an exception
 * occurs, some files and directories may have been removed.</td>
 *       <td>A single parameter is expected, the path to the object to
 * remove. If the specified object does not exist, silently returns. 
 * See note below about path limitations.</td>
 *       <td>If successful, an empty result is returned. Otherwise, the text of 
 * the exception is returned.</td>
 *    </tr>
 * 
 *    <tr>
 *       <td>{@link #CMD_EXEC_PROCESS}</td>
 *       <td>Execute a program defined as a {@link PSProcessDef} in an
 * xml file on a remote machine. The process definition file must be
 * available on the remote machine, its location defined in the daemon's 
 * config file.</td>
 *       <td>A single parameter, an xml document whose structure is defined by 
 * <code>PSProcessRequest</code>. The text must be in UTF-8.</td>
 *       <td>Will return the results in an xml structure defined by 
 * <code>PSProcessRequestResult</code>. The text will be in UTF-8.</td>
 *    </tr>
 * 
 *    <tr>
 *       <td>{@link #CMD_WAIT_FOR_PROCESS}</td>
 *       <td>Wait on a program previously spawned with the 
 * <code>CMD_EXEC_PROCESS</code>. </td>
 *       <td>2 parameters are required. The first is an integer handle returned
 * by a previous call to execute a process. The 2nd is an integer indicating
 * the maximum time to wait. Supply 0 to wait forever.</td>
 *       <td>Will return the results in an xml structure defined by 
 * <code>PSProcessRequestResult</code>. The text will be in UTF-8.</td>
 *    </tr>
 * 
 * </table>
 * 
 * <p>If an unrecognized command is supplied, an error code of -1 is returned
 * with an appropriate message.
 * 
 * <p><em>Path Limitations:</em> All paths are interpreted as virtual paths 
 * relative to the path root specified in the daemon's config file. ../ is 
 * not allowed. Forward or back slashes are allowed as path seperators 
 * regardless of OS. The encoding of the path must be UTF-8.
 * 
 * <p>The daemon is designed to be run as a standalone application. It requires 
 * a number of parameters that can be supplied in a properties file.
 * <p>
 * @author paulhoward
 */
public class PSProcessDaemon extends Thread
{
   private static org.apache.logging.log4j.Logger log = LogManager.getLogger(PSProcessDaemon.class);
   /**
    * One of the commands supported by this daemon. See class description for
    * details. 
    */
   public static final String CMD_EXEC_PROCESS = "execprocess";

   /**
    * One of the commands supported by this daemon. See class description for
    * details. 
    */
   public static final String CMD_WAIT_FOR_PROCESS = "waitforprocess";

   /**
    * One of the commands supported by this daemon. See class description for
    * details. 
    */
   public static final String CMD_REMOVE_FS_OBJ = "rm";

   /**
    * One of the commands supported by this daemon. See class description for
    * details. 
    */
   public static final String CMD_MAKE_DIRS = "mkdir";

   /**
    * One of the commands supported by this daemon. See class description for
    * details. 
    */
   public static final String CMD_SAVE_FILE = "put";

   /**
    * One of the commands supported by this daemon. See class description for
    * details. 
    */
   public static final String CMD_SAVE_BINARY_FILE = "putbinary";

   /**
    * One of the commands supported by this daemon. See class description for
    * details. 
    */
   public static final String CMD_GET_FILE = "get"; 

   /**
    * One of the commands supported by this daemon. See class description for
    * details. 
    */
   public static final String CMD_FS_OBJ_EXISTS = "exists"; 
   
   /**
    * The 'namespace' for variables in the properties file used to process
    * the process definitions.
    */
   public static final String PROC_ENV_PREFIX = "procenv.";
   
   /**
    * Loads the properties file then starts the daemon. 
    *  
    * @param args Expects 1 optional arg, which is the filename of the props
    * file. If not supplied, "procdaemon.properties" is used.
    * 
    * @throws Throwable Anything that causes the daemon to end prematurely is
    * passed on. The reason this is here is because it is caught and re-thrown
    * so the reason for the termination can be logged.
    */
   public static void main(String[] args)
      throws Throwable
   {
      //used for logging
      Throwable t = null;
      
      try
      {
         File location;
         if (args.length == 1)
            location = new File(args[0]);
         else
            location = new File("rxconfig/Server/procdaemon.properties");
         
         File f = new File(location.getAbsolutePath());
         if (!f.exists())
         {
            log.info("Configuration file missing. Cannot continue.");
            log.info("Looked for file at: " + f.getAbsolutePath());
            return;
         }
         FileInputStream is = new FileInputStream(f);
         
         Properties props = new Properties();
         props.load(is);
         
         PropertyConfigurator.configure(props);

         log.info("Loaded properties from: {}" , location.getAbsolutePath());
         
         PSProcessDaemon daemon = new PSProcessDaemon(props);
         if ( PSOsTool.isWindowsPlatform() )
         {
            PSServerShutdownHelper.cleanItsDescriptor();
            PSDaemonShutdown sp = daemon.new PSDaemonShutdown();
            sp.start();
         }
         

         log.info("Process Daemon is listening on port {} ...",props.getProperty("port"));
         
         //wait for it to finish
         daemon.join();
      }
      catch (Throwable th)
      {
         t = th;
         throw th;
      }
      finally
      {
         final String TERM_MSG = "Program terminating.";
         if (null != ms_logger)
         {
            if (null != t)
               ms_logger.fatal("Exception terminated main loop", t);
            ms_logger.debug(TERM_MSG);
         }
         else
         {
            if (null != t)
               log.error(t.getMessage());
            log.debug(t.getMessage(), t);
            log.info(TERM_MSG);
         }
      }
   }
   
   /**
    * Validates the properties and starts the thread running. The
    * following props are allowed/required (names are case-sensitive):
    * <table>
    *    <tr>
    *       <th>Name</th><th>Required?</th><th>Desc</th>
    *    </tr>
    *    <tr>
    *       <td>port</td><td>Yes</td><td>Listen for requests on this port</td>
    *    </tr>
    *    <tr>
    *       <td>pathRoot</td><td>yes</td><td>All commands that use a path must
    *          specify the path relative to this location.</td>
    *    </tr>
    *    <tr>
    *       <td>procDefFilename</td><td>No</td><td>The location of the process 
    *          definitions. If not supplied, rw_processes.xml is assumed.</td>
    *    </tr>
    *    <tr>
    *       <td>remoteIPFilter</td><td>No</td><td>A semi-colon delimited list of
    * IP addresses. Only requests from these addresses are allowed. A request
    * received from any other address gets an error message returned. If not
    * provided, requests from any address are processed. An error code of -2
    * is returned in the result sent to a filtered remote client.</td>
    *    </tr>
    *    <tr>
    *       <td>[log4j]</td><td>No</td><td>All properties in this file are
    *          passed to the log4j logger. By default, INFO priority
    *          messages are displayed on the console.</td>
    *    </tr>
    * 
    * @param props The properties needed by the daemon, log4j and process 
    * environment. Never <code>null</code>.
    * 
    * @throws Exception If any of the props are not valid. Also throws several
    * other more specific exceptions if the process file specified in the 
    * props can't be read, is not valid xml, or doesn't conform to the proper
    * dtd.
    */
   public PSProcessDaemon(Properties props)
      throws Exception
   {
      if (null == props)
      {
         throw new IllegalArgumentException("properties cannot be null");
      }
      String port = props.getProperty("port");
      if (null == port || port.trim().length() == 0)
         throw new Exception("Missing property 'port'.");
      try
      {
         m_port = Integer.parseInt(port);
      }
      catch (NumberFormatException e)
      {
         throw new Exception(
            "Invalid property 'port'. Expected a number but found '" + port 
            + "'.");
      }
      
      //validate the root path
      String path = props.getProperty("pathRoot");
      if (null == path || path.trim().length() == 0)
         throw new Exception("Missing property 'pathRoot'");
      File filePath = new File(path);
      if (!filePath.exists() || !filePath.isDirectory())
         throw new Exception("'pathRoot' must specify an existing directory.");
      m_pathRoot = filePath;
      
      //pull out all process environment props and store in separate map
      Enumeration iter = props.propertyNames();
      while (iter.hasMoreElements())
      {
         String prop = (String) iter.nextElement();
         if (prop.startsWith(PROC_ENV_PREFIX))
            m_procEnv.put(prop.substring(PROC_ENV_PREFIX.length()), 
            props.get(prop));
      }
      
      String procDefFilename = props.getProperty("procDefFilename");
      if (null == procDefFilename || procDefFilename.trim().length() == 0)
         procDefFilename = "rw_processes.xml";
      FileInputStream is = new FileInputStream(procDefFilename);
      m_procMgr = new PSProcessManager(is);
      
      String REMOTE_IP_FILTER = "remoteIPFilter";
      String remoteIpFilter = props.getProperty(REMOTE_IP_FILTER);
      if (null != remoteIpFilter && remoteIpFilter.trim().length()>0)
      {
         StringTokenizer toker = new StringTokenizer(remoteIpFilter, ";");
         while (toker.hasMoreTokens())
         {
            String address = toker.nextToken();
            if (!validateInetAddressForm(address))
            {
               throw new Exception("Invalid address found in " 
                     + REMOTE_IP_FILTER + ": " + address 
                     + ". Expect either n.n.n.n or n:n:n:n:n:n:n:n.");
            }
            m_remoteIpFilters.add(address);
         }
      }
      
      start();
   }
   
   /**
    * This method is for use by users of this class. It encapsulates the 
    * communication protocol.
    * <p>Opens a socket on <code>server</code> using <code>port</code>. 
    * It formats and sends a command to the process daemon, using the proper 
    * protocol and returns the results thru a returned error code and a 
    * returned string.
    * 
    * @param server The name of the server to which the command will be sent.
    * Never <code>null</code> or empty.
    * 
    * @param port The port over which the command will be sent.
    * 
    * @param cmdName Never <code>null</code> or empty. One of the CMD_xxx
    * commands supported by the daemon. If not, an error code will be 
    * returned.
    * 
    * @param params Never <code>null</code>. If an entry is of type 
    * <code>InputStream</code>, the data is read from it, otherwise, a 
    * <code>toString</code> is performed on the entry. 0 or more entries as 
    * appropriate for the specified command.
    * 
    * @param result The text returned from the daemon is set on this object. 
    * It is not cleared before appending. The interpretation of the data is
    * dependentant upon the command. Never <code>null</code>.
    * 
    * @return A value of 0 indicates success, a value &lt; 0 indicates a bad
    * command or other framework problem. A value &gt; 0 indicates the 
    * associated command handler failed. If a non-zero value is returned,
    * <code>result</code> will contain the error text.
    * 
    * @throws Exception If any problems during communication.
    */
   public static int sendCommand(String server, int port, String cmdName, 
         List params, StringBuffer result)
      throws IOException
   {
      if (null == server || server.trim().length() == 0)
         throw new IllegalArgumentException("server can't be null or empty");
      if (null == cmdName || cmdName.trim().length() == 0)
         throw new IllegalArgumentException("cmdName can't be null or empty");
      if (null == params)
         throw new IllegalArgumentException("params can't be null");
      if (null == result)
         throw new IllegalArgumentException("result can't be null");
         
      OutputStream out = null;
      InputStream in = null;
      Socket sock = null;
      try
      {
         sock = new Socket(server, port);
         out = sock.getOutputStream();
         
         for (int spaces = 20 - cmdName.length(); spaces > 0; spaces--)
         {
            cmdName += " ";
         }
         
         DataOutputStream dos = new DataOutputStream(out);
         //assume there are no funky chars in the cmd name
         dos.write(cmdName.getBytes());
         
         for (Iterator iter = params.iterator(); iter.hasNext();)
         {
            Object o = iter.next();
            InputStream is;
            byte[] buf;
            int count;
            if (o instanceof InputStream)
            {
               if (o instanceof ByteArrayInputStream)
               {
                  is = ((InputStream) o);
                  count = is.available();
               }
               else
               {
                  ByteArrayOutputStream bos = new ByteArrayOutputStream();
                  IOTools.copyStream((InputStream)o, bos);
                  buf = bos.toByteArray();
                  count = buf.length;
                  is = new ByteArrayInputStream(buf);
               }
            }
            else
            {
               String param = o.toString();
               buf = param.getBytes("UTF8");
               count = buf.length;
               is = new ByteArrayInputStream(buf);
            }
            dos.writeInt(count);
            IOTools.copyStream(is, dos);
         }
         // terminating value
         dos.writeInt(-1);
         dos.flush();
         
         in = sock.getInputStream();
         DataInputStream dis = new DataInputStream(in);
         int errorCode = dis.readInt();
         int bytes = dis.readInt();
         byte[] buf = new byte[bytes];
         dis.readFully(buf);
         result.append(new String(buf, "UTF8"));
         return errorCode;
      } 
      finally
      {
         PSLocalCommandHandler.close(out);
         PSLocalCommandHandler.close(in);
         PSLocalCommandHandler.close(sock);
      }
   }

   /**
    * Verifies that the supplied string conforms to a valid IPv4 or IPv6 form.
    * IPv4 form is n.n.n.n, where each n is a number from 0 to 255 (e.g.
    * 255.128.64.3). IPv6 has 
    * the form n:n:n:n:n:n:n:n, where each n is a 4 digit hex number (e.g.
    * 1080:0:0:0:8:800:200C:417A).
    * 
    * @param address The IP address to validate. Any value is allowed. 
    * 
    * @return If <code>address</code> is <code>null</code> or empty, 
    * <code>false</code> is returned. <code>true</code> is returned if the 
    * supplied address matches a form described above.
    */
   private boolean validateInetAddressForm(String address)
   {
      //7 is the absolute shortest address
      if (null == address || address.trim().length() < 7)
         return false;
      
      try
      {
         if (address.indexOf('.') > 0)
         {
            //IPv4
            StringTokenizer toker = new StringTokenizer(address, ".");
            int i = 0;
            while (toker.hasMoreTokens())
            {
               i++;
               if (i > 4)
                  return false;
               String token = toker.nextToken();
               int subnet = Integer.parseInt(token);
               if (subnet < 0 || subnet > 255)
                  return false;
            }
         }
         else if (address.indexOf(':') > 0)
         {
            //IPv6
            StringTokenizer toker = new StringTokenizer(address, ":");
            int i = 0;
            while (toker.hasMoreTokens())
            {
               i++;
               if (i > 8)
                  return false;
               String token = toker.nextToken();
               int subnet = Integer.parseInt(token, 16);
               if (subnet < 0 || subnet > 0xffff)
                  return false;
            }
         }
         return true;
      }
      catch (NumberFormatException e)
      {
         //ignore, false returned below
      }
      return false;
   }

   //see base class
   public void run()
   {
      ServerSocket serverSock = null;
      try
      {
         ms_logger.info(
            "Started Rhythmyx processor daemon, listening on port {}" , m_port);
         serverSock = new ServerSocket(m_port);
         serverSock.setSoTimeout(5000);
         
         do
         {
            try
            {
               //block for SoTimeout seconds
               Socket sock = serverSock.accept();
               
               RequestHandler rh = new RequestHandler(sock);
               rh.start();
            }
            catch(InterruptedIOException intEx)
            {
               //SoTimeout elapsed, check shutdown flag
               if (ms_shutdownFlag)
               {
                  ms_logger.info("Daemon received shut down command.");
                  break;
               }
            }
         }
         while(!ms_shutdownFlag);
         
         ms_logger.info("Daemon was interrupted - shutting down.");
      }
      catch (Exception e)
      {
         throw new RuntimeException("Exception: " + e.getLocalizedMessage());
      }
      finally
      {
         try
         {
            if (null != serverSock)
               serverSock.close();
         }
         catch (IOException e1)
         {
            //ignore, nothing we can do at this point
         }
      }
   }
   
   /**
    * Does the work of looking up the process def, launching it and returning
    * the results in its own thread.
    *
    * @author paulhoward
    */
   private class RequestHandler extends Thread
   {
      /**
       * Saves the supplied socket until the thread starts, at which time
       * it reads the data from the sock, processes the request and writes
       * the results back to the socket before closing it.
       * 
       * @param sock The source of the request. Never <code>null</code>. It is
       * closed after this thread has finished with it.
       */
      public RequestHandler(Socket sock)
      {
         if (null == sock)
         {
            throw new IllegalArgumentException("socket cannot be null");
         }     
         m_sock = sock;
      }

      //see base class      
      public void run()
      {
         if (null == m_sock)
         {
            throw new IllegalStateException(
                  "run can only be called once per instance");
         }
                  
         ms_logger.info("Received request from " 
               + m_sock.getInetAddress().getHostAddress()); 
   
         DataInputStream dis = null;
         DataOutputStream dos = null;
         try
         {
            //is this from an allowed client
            if (!m_remoteIpFilters.isEmpty())
            {
               InetAddress remoteAddr = m_sock.getInetAddress();
               String dotAddr = remoteAddr.getHostAddress();
               if (!m_remoteIpFilters.contains(dotAddr))
               {
                  ms_logger.info("Rejecting request from disallowed address.");
                  String msg = "Requests from your IP address (" 
                        + dotAddr + ") are not supported.";
                  send(m_sock.getOutputStream(), -2, msg);
                  return;
               }
            }

            InputStream in = m_sock.getInputStream();
            dis = new DataInputStream(in);
            byte[] cmdBuf = new byte[20]; 
            dis.readFully(cmdBuf);
            String cmd = new String(cmdBuf, "UTF8").trim().toLowerCase();
            ms_logger.info("Processing request for command " + cmd);
            
            // get all params
            boolean done = false;
            List params = new ArrayList();
            do
            {
               int bytes = dis.readInt();
               if (bytes == -1)
               {
                  done = true;
               }
               else
               {
                  byte[] buf = new byte[bytes];
                  dis.readFully(buf);
                  params.add(buf);
               }
            }
            while(!done);

            //dispatch the request
            String result;
            int resultCode = 0;
            try
            {
               if (cmd.equals(CMD_GET_FILE))
                  result = handleGet(params);
               else if (cmd.equals(CMD_SAVE_FILE))
                  result = handlePut(params);
               else if (cmd.equals(CMD_SAVE_BINARY_FILE))
                  result = handlePutBinary(params);
               else if (cmd.equals(CMD_MAKE_DIRS))
                  result = handleMkdir(params);
               else if (cmd.equals(CMD_REMOVE_FS_OBJ))
                  result = handleRm(params);
               else if (cmd.equals(CMD_EXEC_PROCESS))
                  result = handleExecProcess(params);
               else if (cmd.equals(CMD_WAIT_FOR_PROCESS))
                  result = handleWaitForProcess(params);
               else if (cmd.equals(CMD_FS_OBJ_EXISTS))
                  result = handleCheckFSObject(params);
               else
               {
                  result =
                     "Unrecognized command (cmd) supplied. No action taken.";
                  resultCode = -1;
               }
            }
            catch (Exception e1)
            {
               result = e1.getClass().getName() + ": " 
                     + e1.getLocalizedMessage();
               resultCode = 1;
            }
            
            send(m_sock.getOutputStream(), resultCode, result);
         }
         catch (IOException e)
         {
            //nothing else we can do here
            ms_logger.error(e.getLocalizedMessage(), e);
         }
         finally
         {
            PSLocalCommandHandler.close(dis);
            PSLocalCommandHandler.close(m_sock);
            m_sock = null;
            ms_logger.debug("Request processing completed");
         }
      }

      /**
       * Formats and writes the supplied data onto the supplied stream. 
       * 
       * @param out Assumed not <code>null</code>. Takes ownership and closes
       * when finished.
       * 
       * @param resultCode Written to the stream as the error code.
       * 
       * @param text Assumed not <code>null</code>. Written to the stream as 
       * the result text.
       * 
       * @throws IOException If any errors while writing to the supplied 
       * stream.
       */
      private void send(OutputStream out, int resultCode, String text)
         throws IOException
      {
         DataOutputStream dos = null;
         
         try
         {
            dos = new DataOutputStream(out);
            dos.writeInt(resultCode);
            byte[] buf = text.getBytes("UTF8");
            dos.writeInt(buf.length);
            IOTools.copyStream(new ByteArrayInputStream(buf), dos);
            dos.close();
         }
         finally
         {
            //out is closed when the enclosing stream is closed
            PSLocalCommandHandler.close(dos);
         }
      }

      /**
       * Processes the 'execprocess' command. See class description for details.
       *  
       * @param params See daemon class description for details. If the 
       * expected params are not found, an error condition results. Assumed
       * not <code>null</code> and that all entries are <code>byte[]</code>.
       * 
       * @return A serialized xml document conforming to the format as 
       * defined in the dtd found in PSXProcessRequestResult.dtd.
       * 
       * @throws SAXException If the byte[] supplied as the first param 
       * cannot be parsed as a UTF8 encoded xml document.
       */
      private String handleExecProcess(List params)
         throws SAXException
      {
         if (params.size() == 0)
         {
            //let the processing below handle the error
            params.add(0, new byte[0]);
         }
          
         String result = "";
         try
         {
            byte[] buf = (byte[]) params.get(0);   
            String request = new String(buf, "UTF8");
            Document inputDoc = PSXmlDocumentBuilder.createXmlDocument(
                  new StringReader(request), false);
            if (ms_logger.isInfoEnabled())
               ms_logger.info(PSXmlDocumentBuilder.toString(inputDoc));
            
            Document outputDoc = processRequest(inputDoc);  
            if (ms_logger.isInfoEnabled())
               ms_logger.info(PSXmlDocumentBuilder.toString(outputDoc));
            result = PSXmlDocumentBuilder.toString(outputDoc);
         }
         catch (UnsupportedEncodingException e)
         {
            //should never happen as UTF8 is always supported
            ms_logger.error(e.getLocalizedMessage(), e);
            throw new RuntimeException("Java doesn't support UTF8.");
         }
         catch (IOException e)
         {
            //should never happen as the stream is backed by a byte[]
            ms_logger.error(e.getLocalizedMessage(), e);
            throw new RuntimeException("IOException on byte[] backed stream.");
         }
         return result;
      }


      /**
       * Processes the 'waitforprocess' command. See class description for 
       * details.
       *  
       * @param params See daemon class description for details. If the 
       * expected params are not found, an error condition results. Assumed
       * not <code>null</code> and that all entries are <code>byte[]</code>.
       * 
       * @return A serialized xml document conforming to the format as 
       * defined in the dtd found in PSXProcessRequestResult.dtd.
       * 
       * @throws Exception If the byte[] supplied as the params 
       * cannot be parsed as integers.
       */
      private String handleWaitForProcess(List params)
         throws Exception
      {
         if (params.size() != 2)
         {
            throw new RuntimeException("Command requires 2 parameters, "
                  + params.size() + " supplied.");
         }
          
         String result = "";
         try
         {
            int handle = Integer.parseInt(
                  new String((byte[]) params.get(0), "UTF8"));
            int wait = Integer.parseInt(
                  new String((byte[]) params.get(1), "UTF8"));
            
            PSProcessRequestResult processResult = 
                  PSLocalCommandHandler.doWaitOnProcess(handle, wait);
            
            Document outputDoc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.replaceRoot(outputDoc, 
                  processResult.toXml(outputDoc));
            if (ms_logger.isInfoEnabled())
               ms_logger.info(PSXmlDocumentBuilder.toString(outputDoc));
            result = PSXmlDocumentBuilder.toString(outputDoc);
         }
         catch (UnsupportedEncodingException e)
         {
            //should never happen as UTF8 is always supported
            ms_logger.error(e.getLocalizedMessage(), e);
            throw new RuntimeException("Java doesn't support UTF8.");
         }
         return result;
      }

      /**
       * Processes the 'rm' command. See class description for details.
       * 
       * @param params See daemon class description for details. If the 
       * expected params are not found, an error condition results. Assumed
       * not <code>null</code> and that all entries are <code>byte[]</code>.
       *  
       * @return always an empty string
       * 
       * @throws Exception if the supplied path is not valid or all specified
       * directories cannot be created.
       */
      private String handleRm(List params)
         throws Exception
      {
         StringBuffer validateResult = new StringBuffer(1000);
         File path = validatePath((byte[])params.get(0));
         PSLocalCommandHandler.doRemoveFileSystemObject(path);
         return "";
      }

      /**
       * Processes the 'exists' command. See class description for details.
       * 
       * @param params See daemon class description for details. If the 
       * expected params are not found, an error condition results. Assumed
       * not <code>null</code> and that all entries are <code>byte[]</code>.
       *  
       * @return a single digit that has either a value of "1" to 
       * indicate the object does exist in the file system or "0" otherwise.
       * 
       * @throws Exception if the supplied path is not valid. 
       */
      private String handleCheckFSObject(List params)
         throws Exception
      {
         StringBuffer validateResult = new StringBuffer(1000);
         File path = validatePath((byte[])params.get(0));
         return path.exists() ? "1" : "0";
      }

      /**
       * Processes the 'mkdir' command. See class description for details.
       * 
       * @param params See daemon class description for details. If the 
       * expected params are not found, an error condition results. Assumed
       * not <code>null</code> and that all entries are <code>byte[]</code>.
       *  
       * @return always an empty string
       * 
       * @throws Exception if the supplied path is not valid or all specified
       * directories cannot be created.
       */
      private String handleMkdir(List params)
         throws Exception
      {
         StringBuffer validateResult = new StringBuffer(1000);
         File dirs = validatePath((byte[])params.get(0));
         PSLocalCommandHandler.doMakeDirectories(dirs);
         return "";
      }

      /**
       * Verifies the rules defined in the daemon class description are 
       * followed and creates a full path by concatenating the daemon's root
       * and the supplied path.
       * 
       * @param source The path to validate and normalize. Assumed in UTF-8
       * encoding.
       * 
       * @return A fully qualified <code>File</code>.  
       * 
       * @throws Exception if UTF8 not supported by Java, if any path rules are
       * violated or if the virtual root no longer exists.
       */
      private File validatePath(byte[] source)
         throws Exception
      {
         String sourcePath;
         try
         {
            sourcePath = new String(source, "UTF8");
         }
         catch (UnsupportedEncodingException e)
         { 
            /* should never happen as Java always supports utf8 */
            throw new Exception("Java doesn't support UTF-8 anymore.");
         }
         
         //FB: RV_RETURN_VALUE_IGNORED NC 1-17-16
         sourcePath = sourcePath.replace('\\', '/');
         if (sourcePath.indexOf("../") != -1
               || (sourcePath.length()==2 && sourcePath.equals("..")))
         {
            throw new Exception(
                  "Cannot use ../ or just .. in a supplied path.");
         }

         //make the path absolute
         if (!m_pathRoot.exists() || !m_pathRoot.isDirectory())
         {
            throw new Exception("Virtual root is no longer present.");
         }
         if (sourcePath.startsWith("/"))
            sourcePath = sourcePath.substring(1, sourcePath.length());
         File fullPath = new File(m_pathRoot, sourcePath);
         return fullPath;
      }

      /**
       * Processes the 'put' command. See daemon class description for details.
       * 
       * @param params See daemon class description for details. If the 
       * expected params are not found, an error condition results. Assumed
       * not <code>null</code> and that all entries are <code>byte[]</code>.
       * 
       * @return Always an empty string.
       * 
       * @throws Exception if the supplied path is not valid or the file 
       * cannot be written for any reason.
       */
      private String handlePut(List params)
         throws Exception
      {
         File path = validatePath((byte[])params.get(0));
         String content = new String((byte[])params.get(1), "UTF8");
         PSLocalCommandHandler.doSaveTextFile(path, content);
         return "";
      }

      /**
       * Processes the 'putbinary' command. See daemon class description for 
       * details.
       * 
       * @param params See daemon class description for details. If the 
       * expected params are not found, an error condition results. Assumed
       * not <code>null</code> and that all entries are <code>byte[]</code>.
       * 
       * @return Always an empty string.
       * 
       * @throws Exception if the supplied path is not valid or the file 
       * cannot be written for any reason.
       */
      private String handlePutBinary(List params)
         throws Exception
      {
         File path = validatePath((byte[])params.get(0));
         InputStream content = new ByteArrayInputStream((byte[])params.get(1));
         PSLocalCommandHandler.doSaveBinaryFile(path, content);
         return "";
      }

      /**
       * Processes the 'get' command. See daemon class description for details.
       * 
       * @param params See daemon class description for details. If the 
       * expected params are not found, an error condition results. Assumed
       * not <code>null</code> and that all entries are <code>byte[]</code>.
       * 
       * @return The content of the requested file. Never <code>null</code>,
       * may be empty.
       * 
       * @throws Exception if the supplied path is not valid or the file cannot
       * be read for any reason.
       */
      private String handleGet(List params)
         throws Exception
      {
         File path = validatePath((byte[])params.get(0));
         return PSLocalCommandHandler.doGetTextFile(path);
      }

      /**
       * Parses the input doc into an object, and executes the requested
       * process. The return code and console output are built into a 
       * result document and returned.
       * 
       * @param inputDoc Assumed not <code>null</code>. Must conform to the
       * format as defined in the dtd found in PSXProcessRequest.dtd.
       * 
       * @return Never <code>null</code>. Will conform to the format as 
       * defined in the dtd found in PSXProcessRequestResult.dtd.
       */
      private Document processRequest(Document inputDoc)
      {
         String resultText = "";
         String procName = "unspecified";
         PSProcessRequestResult resultObj = null;
         try
         {
            Element root = inputDoc.getDocumentElement();
            if (null == root)
            {
               String msg = "An invalid request document was supplied." 
                     + " Expected a root of " + PSProcessRequest.XML_NODE_NAME;
               //The exception will be caught below and processed into a doc
               throw new Exception(msg);
            }
            PSProcessRequest req = new PSProcessRequest(root);
            procName = req.getName();
            Map env = new HashMap(m_procEnv);
            env.putAll(req.getParams());
            resultObj = PSLocalCommandHandler.doExecuteProcess(m_procMgr, 
                  procName, env, req.getWait(), req.isTerminate(),
                  ms_logger);
         }
         catch (Exception e)
         {
            resultText = getErrorText(e);
         }
         
         if (null == resultObj)
         {
            // get here if an exception occurred
            resultObj = new PSProcessRequestResult( procName, -1, resultText, 
                  PSProcessRequestResult.STATUS_ERROR);
         }
         Document resultDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.replaceRoot(resultDoc, 
               resultObj.toXml(resultDoc));
                  
         if (ms_logger.isInfoEnabled())
         {
            ms_logger.info("'" + procName + "' completed with return code = " 
                  + resultObj.getResultCode());
         }
         if (ms_logger.isDebugEnabled())
         {
            ms_logger.debug(PSXmlDocumentBuilder.toString(resultDoc));
         }
            
         return resultDoc;
      }

      /**
       * Creates a text message from the supplied exception. If the exception
       * has no text, the class name is used.
       * 
       * @param e Assumed not <code>null</code>.
       * 
       * @return Never <code>null</code> or empty.
       */
      private String getErrorText(Throwable e)
      {
         String msg = e.getLocalizedMessage();
         if (msg == null)
         {
            msg = e.getClass().getName(); 
         }
         return msg;
      }

      /**
       * Set in <code>ctor</code>, set to <code>null</code> when the {@link
       * #run()} method has finished.
       */
      private Socket m_sock;
   }

   /**
    * Schedule a shut down of the daemon.
    *
    * @param   downWhen    the amount of time, in milliseconds, to wait
    * before shutting down the server. note: Ignored in this release.
    */
   public static void scheduleShutdown(long downWhen)
   {
      ms_shutdownFlag = true;
   }
   
   /**
    * The port to listen on. Set in ctor, then never changed.
    */
   private int m_port;

   /**
    * This map holds the parameters used when processing the process 
    * defininitions. Each entry has a key and value as <code>String</code>.
    * Never <code>null</code>, may be empty.
    */
   private Map m_procEnv = new HashMap();

   /**
    * Contains the process definitions. Set in ctor, then never
    * <code>null</code> or modified.
    */
   private PSProcessManager m_procMgr;
   
   /**
    * Contains the virtual root where command paths originate. Defined in the 
    * daemon's configuration file. Set in ctor, then never changed or modified 
    * after that. Guaranteed to exist when initialized.
    */
   private File m_pathRoot;
   
   /**
    * Is either empty, or contains the allowed remote IP addresses. Each entry
    * is a <code>String</code> in the dot notation of an IP address, e.g.
    * 144.122.111.1. If empty, all remote addresses are allowed. 
    */
   private Set m_remoteIpFilters = new HashSet();
   
   /**
    * The log4j logger for this class. Initialized in <code>main</code>, then
    * never <code>null</code>.
    */
   private static final Logger ms_logger = LogManager.getLogger(PSProcessDaemon.class);
   
   /**
    * This is used to signal a shutdown request has been scheduled.
    */ 
   private static boolean ms_shutdownFlag = false;

   /**
    * inner class to help shutdown of this daemon: shared by PSServer also
    * 
    */ 
   class PSDaemonShutdown extends PSServerShutdownHelper
         implements IPSShutdownListener
   {
      /**
       * See IPSShutdownListener interface for details.
       */
      public void psShutdown()
      {
         PSProcessDaemon.scheduleShutdown(1);
      }
   }
}
