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

package com.percussion.util;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.server.PSRemoteConsole;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * RemoteConsole is a java program that executes a remote console command using
 * the PSRemoteConsole, the default command being to shut down the server.  The
 * reason is that when we daemonize the server, there is no console
 * window (it is part of the process), so we need to shutdown the
 * server in a normal way, and this class allows us to do so.
 */
public class RemoteConsole
{

   private static final Logger log = LogManager.getLogger(RemoteConsole.class);

   /**
   * Executes a command against the specified server.  This may be run in one of
   * 2 modes -- non-interactive and interactive.  If the first 4 parameters are
   * provided, the program will run in non-interactive mode.  If not, the
   * program will provide default values and interactively allow the user to
   * modify them.  If there will not be a console window, or no interactive
   * user present when the program is run, be sure to supply all required
   * arguments so the program runs non-interactively. 
   *
   * The parameters are:
   *
   * <table border="1">
   * <tr>
   * <th>Name</th><th>Description</th><th>Required?</th>
   * </tr>
   * <tr>
   * <td>server</td>
   * <td>The hostname or ip address of the Rhthmyx server.</td>
   * <td>yes</td>
   * </tr>
   * <tr>
   * <td>port</td>
   * <td>The port the Rhythmyx server is using - if not supplied attempts to
   * read the port from the server config file and offers that value as a
   * default.
   * </td>
   * <td>For non-interactive only</td>
   * </tr>
   * <tr>
   * <td>user id</td>
   * <td>The user id to use to connect to the Rhythmyx server - if not supplied
   * the value of <code>System.getProperty("user.name")</code> is offered as
   * a default value.</td>
   * <td>For non-interactive only</td>
   * </tr>
   * <tr>
   * <td>password</td>
   * <td>The password to use to connect to the Rhythmyx server.  No default
   * value is provided.</td>
   * <td>For non-interactive only</td>
   * </tr>
   * <tr>
   * <td>command</td>
   * <td>The console command to execute against the Rhythmyx server.  If not
   * provided, the default command "stop server" is executed.  If the command
   * contains spaces, command must be surrounded by double quotes.</td>
   * <td>no</td>
   * </tr>
   * <tr>
   * <td>log output?</td>
   * <td>Should the server's response be logged to the console?  "yes" if it
   * should, "no" if it should not.  If in interactive mode, output will
   * automatically be logged.</td>
   * <td>no</td>
   * </tr>
   * </table>
   *
   * For example, to shutdown the server in interactive mode:<br>
   * java com.percussion.util.RemoteConsole localhost
   * <p>
   * To run in non-interactive mode, no logging:<br>
   * java com.percussion.util.RemoteConsole localhost 9992 admin1 demo
   * <p>
   * To run in non-interactive mode, with logging, specifying a command:<br>
   * java com.percussion.util.RemoteConsole localhost 9992 admin1 demo
   *     "show status server" y
   * <p>
   */
   public static void main(String[] args)
   {
      RemoteConsole remoteCon = new RemoteConsole();
      Properties connProps = new Properties();
      String cmd = "stop server";  //default command to execute
      boolean logOutput = false;
      int result = 1;

      if(args.length == 0 || args[0].trim().length() == 0 )
      {
         System.out.println("No Host Name or IP Address found.\n" +
                              "Execution Aborted\n");
         showUsage();
         System.exit(result);
      }
      String hostName = args[0] ;

      if (args.length < 4)
      {
         // interactive mode, so we'll automatically log output
         logOutput = true;

         // get port
         String portID = null;
         if (args.length >= 2)
            portID = args[1];
         else
         {
            portID = remoteCon.getPortID("rxconfig" + File.separator +
               "Server" + File.separator + "server.properties", "bindPort");
         }
               
         if(portID == null)
         {
            // go with the default
            System.out.println("Cannot determine port, using 9992.");
            portID = "9992";
         }

         // Leave this line in for future. If we come across any
         // Unix security manager. We'll see this if Rhythmyx is
         // installed on a large Unix box with Something like CICS
         // or Tuxedo (Some kind of TP control). At this point it
         // the return value should always be null.
//       SecurityManager s = System.getSecurityManager();

         // get userid
         String userID = null;
         if (args.length >= 3)
            userID = args[2];
         else
            userID = System.getProperty("user.name");

         String response = new String();
         connProps.put(SERVER_PROP, hostName);
         connProps.put(PORT_PROP, portID);
         connProps.put(USER_PROP, userID);

         do
         {
            System.out.println("\nExecuting console command with the " +
                                 "following parameters");
            System.out.println("   Server Name : " + hostName);
            System.out.println("   Port No     : " + portID);
            System.out.println("   User ID     : " + userID);
            System.out.println("   Command     : " + cmd);
            System.out.println("Is this Correct (Y/N) :");
            response = remoteCon.readKb().trim();
            if(response.toUpperCase().startsWith("Y") == true)
            {
               System.out.println("Please Enter password :");
               connProps.put(PW_PROP, remoteCon.readKb().trim());
               break;
            }
            else if(response.toUpperCase().startsWith("N") == true)
            {
               /* add the command to the properties so it can be offered to the
                * user as the current value
                */
               connProps.setProperty(CMD_PROP, cmd);

               // get new prop values from the user
               remoteCon.getNewParams(connProps);

               /* retrieve the command and remove it so it's not in the
                * properties when we use them to create the designer connection.
                */
               cmd = connProps.getProperty(CMD_PROP);
               connProps.remove(CMD_PROP);
               break;
            }
            else
               response="";
         } while(response.length()== 0);
      }
      //if all 4 parameters are provided
      else if(args.length >= 4)
      {
         connProps.put("hostName", hostName);
         connProps.put("port", args[1]);
         connProps.put("loginId", args[2]);
         connProps.put("loginPw", args[3]);

         // see if command is provided
         int logArg = 5;
         if (args.length >= 5)
         {
            if (args[4].startsWith("\""))
            {
               StringBuffer cmdBuf = new StringBuffer();
               logArg = parseCommand(args, 4, cmdBuf) + 1;
               cmd = cmdBuf.toString();
            }
            else
            {
               cmd = args[4];
            }
         }

         if (args.length > logArg)
         {
            logOutput = args[logArg].trim().toUpperCase().startsWith("Y") ?
               true : false;
         }
      }

      try
      {
         PSDesignerConnection   conn      = new PSDesignerConnection(connProps);
         PSRemoteConsole      console   = new PSRemoteConsole(conn);
         Document               xmlDoc   = console.execute(cmd);

         // log output if we should
         if (logOutput)
         {
            PSXmlDocumentBuilder.write(xmlDoc, System.out);
         }

         // success!   
         result = 0;
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      System.exit(result);
   }

   /**
   * Retreives a value assigned to a property from a specified
   * file (generally a .properties file). The format of the
   * property has to be <b><I>propertyname=propertyvalue</I></b>
   * Note the delimiter is the <b>=</b>
   *
   * @param fileName This can be execution directory or a full path.
   *
   * @param propName property name such as bindPort or databaseName etc.
   *
   * @return the String value of the property name or null if not
   *         found or exception encountered.
   *
   */
   public String getPortID(String fileName, String propName)
   {
      String propValue = null ;

      try
      {
         PSProperties ps = new PSProperties(fileName);
         propValue = Integer.toString(ps.getInt(propName));
      }
      catch(Exception e)
      {
      }
      return propValue;
   }

   /**
   * Reads a String from the keyboard up to the point of carriage
   * return.
   *
   * @return the String value of user input. Empty String if only
   * CR is pressed.
   *
   */
   public String readKb()
   {
      String response=new String("");
      BufferedReader stream  = null ;
      try
      {
         stream = new BufferedReader(new InputStreamReader(System.in));
         response = stream.readLine();
      }
      catch(IOException e)
      {
         System.err.println("error reading the User Input\n");
      }
      return(response);
   }

   /**
   * Resets the values in connProps via a set of Q & A.
   *
   * @param connProps assumed not <code>null</code.  User's responses will be
   * used to modify this object.
   */
   private void getNewParams(Properties connProps)
   {
      System.out.println("Please Enter the Following [enter for default] :");
      String hostName = connProps.getProperty(SERVER_PROP);
      System.out.println("HostName or IP Address [" + hostName + "]:");
      String newHostName = readKb().trim();
      if (newHostName.length() > 0)
         connProps.put(SERVER_PROP, newHostName);

      String portID = connProps.getProperty(PORT_PROP);
      System.out.println("Port Number [" + portID + "]:");
      String newPortID = readKb().trim();
      if (newPortID.length() > 0)
         connProps.put(PORT_PROP, newPortID);

      String userID = connProps.getProperty(USER_PROP);
         System.out.println("User ID [" + userID + "]:");
      String newUserID = readKb().trim();
      if (newUserID.length() > 0)
         connProps.put(USER_PROP, newUserID);

      System.out.println("Password :");
      String userPasswd = readKb().trim();
      connProps.put(PW_PROP, userPasswd.trim());

      String cmd = connProps.getProperty(CMD_PROP);
      System.out.println("Command [" + cmd + "]:");
      String newCmd = readKb().trim();
      if (newCmd.length() > 0)
         connProps.put(CMD_PROP, newCmd);
   }


   /**
    * Parses command that spans multiple commandline arguments as it is enclosed
    * in double quotes and contains spaces i.e. "show status server"
    *
    * @param args The command line arguments passed to the program
    * @param start The index of arg that is the first word of the command,
    * assumed to begin with a double quote.  Assumed to be less than the length
    * of the args array.
    * @param cmd A buffer in which the resulting command is returned.
    *
    * @return The index of the last argument that is part of the command.  If
    * array is empty, <code>-1</code> is returned.
    */
   private static int parseCommand(String[] args, int start, StringBuffer cmd)
   {
      int end = args.length - 1;
      for (int i = start; i < args.length; i++)
      {
         String arg = args[i];

         // Strip off leading quote if first arg
         if (i == start)
         {
            // only add it if its more than just the double quote
            if (arg.startsWith("\"") && arg.length() > 1)
               cmd.append(arg.substring(1));
         }
         else if (arg.endsWith("\""))
         {
            // strip off trailing quote and add it
            if (arg.length() > 1)
            {
               cmd.append(" ");
               cmd.append(arg.substring(0, arg.length() - 1));
            }
            
            // since this is the end, set the end index and break
            end = i;
            break;
         }
         else
         {
            cmd.append(" ");
            cmd.append(arg);
         }
      }

      return end;
   }

   /**
    * Logs usage to the console.
    */
   private static void showUsage()
   {
      System.out.println("Usage:");
      System.out.println("java com.percussion.util.RemoteConsole <server> " +
         "[<port> <userid> <password> <command> <log?>] ");
      System.out.println(
         "For example, to shutdown the server in interactive mode:");
      System.out.println("java com.percussion.util.RemoteConsole localhost");
      System.out.println();
      System.out.println("To run in non-interactive mode, no logging:");
      System.out.println(
         "java com.percussion.util.RemoteConsole localhost 9992 admin1 demo");
      System.out.println();
      System.out.println(
         "To run in non-interactive mode, with logging, specifying a command:");
      System.out.println("java com.percussion.util.RemoteConsole localhost " +
         "9992 admin1 demo \"show status server\" yes");
   }

   private static final String SERVER_PROP = "hostName";
   private static final String PORT_PROP = "port";
   private static final String USER_PROP = "loginId";
   private static final String PW_PROP = "loginPw";
   private static final String CMD_PROP = "cmd";
}



