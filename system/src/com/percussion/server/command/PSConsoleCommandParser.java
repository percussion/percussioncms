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
package com.percussion.server.command;

import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSConsoleCommand;
import com.percussion.server.IPSServerErrors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;


/**
 * The PSConsoleCommandParser class is used to parse console commands into
 * executable objects.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSConsoleCommandParser
{
   /**
    * Parse the specified console command string into an executable object.
    * <P>
    * Currently supported commands are:
    *   <TABLE BORDER="1">
    *      <TR>
    *         <TH>Command</TH>
    *         <TH>Parameters</TH>
    *         <TH>Description</TH>
    *      </TR>
    *      <TR>
    *         <TD>application appName</TD>
    *         <TD>this will start the specified application if it is not
    *            currently running. If the application is set as disabled, it
    *            will be enabled.</TD>
    *      </TR>
    *      <TR>
    *         <TD rowspan="2">restart</TD>
    *         <TD>server</TD>
    *         <TD>this will shut down the E2 server and restart it</TD>
    *      </TR>
    *      <TR>
    *         <TD>restart application appName</TD>
    *         <TD>this will stop the specified application if it is
    *            currently running and restart it. If it is not running,
    *            it will be started</TD>
    *      </TR>
    *      <TR>
    *         <TD rowspan="2">stop</TD>
    *         <TD>server</TD>
    *         <TD>this will shut down the E2 server</TD>
    *      </TR>
    *      <TR>
    *         <TD>stop application appName</TD>
    *         <TD>this will stop the specified application if it is
    *            currently running.</TD>
    *      </TR>
    *      <TR>
    *         <TD rowspan="6">show</TD>
    *         <TD>status server</TD>
    *         <TD>general server statistics such as time running,
    *            performance metrics, etc.</TD>
    *      </TR>
    *      <TR>
    *         <TD>status application appName</TD>
    *         <TD>application statistics such as time running,
    *            performance metrics, etc.</TD>
    *      </TR>
    *      <TR>
    *         <TD>status objectstore</TD>
    *         <TD>object store statistics such as performance metrics, etc.</TD>
    *      </TR>
    *      <TR>
    *         <TD>applications active</TD>
    *         <TD>return the list of active applications</TD>
    *      </TR>
    *      <TR>
    *         <TD>applications all</TD>
    *         <TD>return the list of all applications defined on the server</TD>
    *      </TR>
    *      <TR>
    *         <TD>applications disabled</TD>
    *         <TD>return the list of disabled applications</TD>
    *      </TR>
    *      <TR>
    *         <TD rowspan="2">log</TD>
    *         <TD>flush</TD>
    *         <TD>flush any queued log entries to the log</TD>
    *      </TR>
    *      <TR>
    *         <TD>dump</TD>
    *         <TD>dump the log, locating entries by date, type and/or app</TD>
    *      </TR>
    * </TABLE>
    *
    * @param   command      the command string to parse
    *
    * @exception   PSIllegalArgumentException
    *                           if command is <code>null</code>, empty or
    *                           specifies an invalid command syntax
    */
   public static IPSConsoleCommand parse(String command)
      throws PSIllegalArgumentException
   {
      if (command == null)
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_CMD_EMPTY);

      // remove any whitespace so we know if we have some valid text
      command = command.trim();
      if (command.length() == 0)
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_CMD_EMPTY);

      IPSConsoleCommand retCmd = null;

      String   cmdBase = "";
      String   cmdName = "";
      String   cmdArgs = command;
      String   validCmds = null;
      int      wordPos;
      Object   cmdObject;

      while (true)
      {
         // get the next token, which is part of the command name
         if ((wordPos = cmdArgs.indexOf(' ')) == -1)
         {
            cmdName = cmdArgs.toLowerCase();
            cmdArgs = "";
         }
         else
         {
            cmdName = cmdArgs.substring(0, wordPos).toLowerCase();
            cmdArgs = cmdArgs.substring(wordPos+1).trim();
            if ( (cmdArgs.startsWith("\"") && cmdArgs.endsWith("\"")) ||
                  (cmdArgs.startsWith("'")  && cmdArgs.endsWith("'")) )
               cmdArgs = cmdArgs.substring(1, cmdArgs.length());
         }

         // get the object stored for this command from our hashtable.
         // it may be the IPSConsoleCommand to use or a String containing
         // the valid subcommands for this command
         // our hash table of cmd name/classes
         if (cmdBase.length() == 0)
            cmdObject = ms_cmdSet.get(cmdName);
         else
            cmdObject = ms_cmdSet.get(cmdBase + " " + cmdName);
         if (cmdObject == null)
         {
            // this is not a valid command string!
            if (validCmds == null)
            {
               Object[] args = { cmdName, (String) ms_cmdSet.get("") };
               throw new PSIllegalArgumentException(
                  IPSServerErrors.RCONSOLE_INVALID_CMD, args);
            }
            else
            {
               Object[] args = { cmdBase, cmdName, validCmds };
               throw new PSIllegalArgumentException(
                  IPSServerErrors.RCONSOLE_INVALID_SUBCMD, args);
            }
         }

         // put the full cmd name in cmdBase
         if (cmdBase.length() == 0)
            cmdBase = cmdName;
         else
            cmdBase += " " + cmdName;

         if (cmdObject instanceof java.lang.Class)
         {
            try
            {
               Constructor c = ((Class) cmdObject).getConstructor(
                  new Class[] { Class.forName("java.lang.String") } );
               return (IPSConsoleCommand)c.newInstance(new Object[] {cmdArgs});
            }
            catch (Throwable t)
            {
               // if the constructor throw an exception, set t to it
               if (t instanceof java.lang.reflect.InvocationTargetException)
                  t = ((InvocationTargetException)t).getTargetException();

               if (t instanceof com.percussion.error.PSIllegalArgumentException)
                  throw (PSIllegalArgumentException)t;
               else
               {

                  Object[] args = { cmdBase, t.getMessage() };
                  throw new PSIllegalArgumentException(
                     IPSServerErrors.RCONSOLE_GET_HANDLER_EXCEPTION, args);
               }
            }
         }
         else
         {
            // save the valid sub-cmds for the next run
            validCmds = (String) cmdObject;
         }

         if (cmdArgs.length() == 0)
         {
            // if there are no more tokens, we've got a problem
            Object[] args = { cmdBase, validCmds };
            throw new PSIllegalArgumentException(
               IPSServerErrors.RCONSOLE_SUBCMD_REQD, args);
         }
      }
   }

   /**
    * We store the command set by setting a key with a String containing the
    * valid sub-commands if it requires sub-commands or an
    * IPSConsoleCommand object's class if it is a command we can respond to.
    *
    * Search for the key "" to get the list of valid base commands.
    */
   private final static Hashtable ms_cmdSet;
   static
   {
      // and this is the hashtable containing the sub commands
      ms_cmdSet = new Hashtable();

      // in case they use an invalid base command, store the base list
      ms_cmdSet.put("", "start, restart, stop, show, log, trace, flush, "
            + "dump, reload, debug, clear, search, autotune");

      // build the sub-command list for start
      ms_cmdSet.put("start", "application, cache");

      ms_cmdSet.put("start application",
         com.percussion.server.command.PSConsoleCommandStartApplication.class);

      ms_cmdSet.put("start cache",
         com.percussion.server.command.PSConsoleCommandStartCache.class);

      // build the sub-command list for restart
      ms_cmdSet.put("restart", "application, cache");

      // build the sub-command list for reload
      ms_cmdSet.put("reload", "i18nresources");

      // build the sub-command list for debug
      ms_cmdSet.put("debug", "i18n, search");

      // build the actual command handlers for restart
      /* Commented out until implemented
      ms_cmdSet.put("restart server",
         com.percussion.server.PSConsoleCommandRestartServer.class);*/

      ms_cmdSet.put("reload i18nresources",
         PSConsoleCommandReloadI18nResources.class);

      ms_cmdSet.put("debug i18n",
         PSConsoleCommandDebugI18n.class);

      ms_cmdSet.put("debug search",
         PSConsoleCommandDebugSearch.class);

      ms_cmdSet.put("restart application",
         com.percussion.server.command.PSConsoleCommandRestartApplication.class);

      ms_cmdSet.put("restart cache",
         com.percussion.server.command.PSConsoleCommandRestartCache.class);

      // build the sub-command list for stop
      ms_cmdSet.put("stop", "server, application, cache");

      // build the actual command handlers for stop
      ms_cmdSet.put("stop server",
         com.percussion.server.command.PSConsoleCommandStopServer.class);

      ms_cmdSet.put("stop application",
         com.percussion.server.command.PSConsoleCommandStopApplication.class);

      ms_cmdSet.put("stop cache",
         com.percussion.server.command.PSConsoleCommandStopCache.class);

      // build the sub-command list for show
      ms_cmdSet.put("show", "status, applications, version");

      // build the sub-sub-command list for show
      ms_cmdSet.put("show status", "server, objectstore, application, search");

      // build the actual command handlers for show
      ms_cmdSet.put("show status server",
         com.percussion.server.command.PSConsoleCommandShowStatusServer.class);

      ms_cmdSet.put("show status objectstore",
         com.percussion.server.command.PSConsoleCommandShowStatusObjectStore.class);

      ms_cmdSet.put("show status application",
         com.percussion.server.command.PSConsoleCommandShowStatusApplication.class);

      ms_cmdSet.put("show status search",
         PSConsoleCommandShowStatusSearch.class);

      ms_cmdSet.put("show applications",
         com.percussion.server.command.PSConsoleCommandShowApplications.class);

      ms_cmdSet.put("show version",
         com.percussion.server.command.PSConsoleCommandShowVersion.class);

      // build the sub-command list for log
      ms_cmdSet.put("log", "flush, dump");

      ms_cmdSet.put("log flush",
         com.percussion.server.command.PSConsoleCommandLogFlush.class);

      ms_cmdSet.put("log dump",
         com.percussion.server.command.PSConsoleCommandLogDump.class);

      // build the sub-command list for trace
      ms_cmdSet.put("trace",
         com.percussion.server.command.PSConsoleCommandTrace.class);

      // build the sub-command list for flush
      ms_cmdSet.put("flush", "dbmd, cache, foldercache");

      // build the actual command handlers for flush
      ms_cmdSet.put("flush dbmd", PSConsoleCommandFlushDbmd.class);

      ms_cmdSet.put("flush cache", PSConsoleCommandFlushCache.class);

      ms_cmdSet.put("flush foldercache", PSConsoleCommandFlushFolderCache.class);

      // build the subcommandlist for dump
      ms_cmdSet.put("dump",
         "resources, usersessions, handlers, " +
         "cache, folderrelationshipcache, " + 
         "itemsummarycache, datasources");

      // build the actual command handlers for dump
      ms_cmdSet.put("dump resources",
         PSConsoleCommandDumpServerResources.class);
      ms_cmdSet.put("dump usersessions",
         PSConsoleCommandDumpUserSessions.class);
      ms_cmdSet.put("dump handlers",
         PSConsoleCommandDumpHandlers.class);
      ms_cmdSet.put("dump cache",
         PSConsoleCommandDumpCache.class);
      ms_cmdSet.put(PSConsoleCommandDumpFolderRelationshipCache.ms_cmdName,
            PSConsoleCommandDumpFolderRelationshipCache.class);
      ms_cmdSet.put(PSConsoleCommandDumpItemSummaryCache.ms_cmdName,
            PSConsoleCommandDumpItemSummaryCache.class);
      ms_cmdSet.put(PSConsoleCommandDumpDatasources.ms_cmdName,
         PSConsoleCommandDumpDatasources.class);
      
      // build the sub-command list for clear
      ms_cmdSet.put("clear", "server");

      // build the sub-sub-command list for clear
      ms_cmdSet.put("clear server", "lock");

      // build the actual command handlers for clear
      ms_cmdSet.put("clear server lock", PSConsoleCommandClearServerLock.class);
      
      // build the sub-command list for search
      ms_cmdSet.put("search", "index, queue");

      ms_cmdSet.put("search index", "item, type, recreate");

      ms_cmdSet.put("search index item", PSConsoleCommandSearchIndexItem.class);
      ms_cmdSet.put("search index type", PSConsoleCommandSearchIndexType.class);
      ms_cmdSet.put("search index recreate", 
         PSConsoleCommandSearchIndexRecreate.class);
      
      ms_cmdSet.put("search queue", "clear");
      ms_cmdSet.put("search queue clear",
            PSConsoleCommandSearchQueueClear.class);
      
      ms_cmdSet.put("search queue", "pause");
      ms_cmdSet.put("search queue pause",
            PSConsoleCommandSearchQueuePause.class);
      
      ms_cmdSet.put("search queue", "resume");
      ms_cmdSet.put("search queue resume",
            PSConsoleCommandSearchQueueResume.class);
      
      ms_cmdSet.put("autotune", "cache");
      ms_cmdSet.put("autotune cache",
            PSConsoleCommandAutotuneCache.class);
      
   }
}


