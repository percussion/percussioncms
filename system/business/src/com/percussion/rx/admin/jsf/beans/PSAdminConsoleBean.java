/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.rx.admin.jsf.beans;

import com.percussion.error.PSIllegalArgumentException;
import com.percussion.rx.jsf.PSLockableNode;
import com.percussion.server.IPSConsoleCommand;
import com.percussion.server.PSRequest;
import com.percussion.server.command.PSConsoleCommandException;
import com.percussion.server.command.PSConsoleCommandParser;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Backs Rhythmyx Command Console UI.
 * 
 * @author Andriy Palamarchuk
 */
public class PSAdminConsoleBean extends PSLockableNode
{
   /**
    * The outcome of the console page.
    */
   public static final String CONSOLE_VIEW = "admin-console-view";
   
   /**
    * The command to initiate garbage collection.
    */
   static final String GARBAGECOLLECT_CMD = "garbagecollect";

   /**
    * Another format of the {@link #GARBAGECOLLECT_CMD} command.
    */
   static final String GARBAGECOLLECT2_CMD = "gc";

   /**
    * The command to stop server.
    */
   static final String STOP_SERVER_CMD = "stop server";

   /**
    * The command to stop server.
    * Interpreted the same way as {@link #STOP_SERVER_CMD}.
    */
   static final String QUIT_CMD = "quit";

   /**
    * The command to stop server.
    * Interpreted the same way as {@link #STOP_SERVER_CMD}.
    */
   static final String EXIT_CMD = "exit";

   /**
    * Constructs an object with the given parameters.
    * 
    * @param title never <code>null</code> or empty.
    * @param outcome the outcome, may be <code>null</code>.
    * @param label the value returned by {@link #getLabel()}.
    *    Can be <code>null</code> or blank.
    */
   public PSAdminConsoleBean(String title, String label)
   {
      super(title, CONSOLE_VIEW, label);
   }
   
   /**
    * Executes the console command.
    * Named as a getter to make it work.
    * @return the results of the command, optionally concatenated with the
    * previous commands results. Never <code>null</code>, can be empty.
    */
   public String getResult()
   {
      if (StringUtils.isBlank(getCommand()))
      {
         return m_result.toString();
      }
      resetResults();
      m_result.append("Command: " + getCommand() + "\n");

      if (GARBAGECOLLECT_CMD.equalsIgnoreCase(getCommand())
         || GARBAGECOLLECT2_CMD.equalsIgnoreCase(getCommand()))
      {
         // call the garbage collector
         java.lang.System.gc();
         m_result.append("garbage collection initiated");
      }
      else
      {
         // go through the remote console handler
         try
         {
            m_result.append(executeInConsoleHandler());
         }
         catch (Throwable t)
         {
            m_result.append(t.getMessage());
         }
      }

      setCommand("");
      return m_result.toString();
   }
   
   /**
    * A dummy setter.
    * @see #getResult()
    */
   public void setResult(@SuppressWarnings("unused") String result) {
   }

   /**
    * Clears {@link #m_result} if {@link #isAppend()} is <code>false</code>.
    * Otherwise adds content to separate new results from the old results.
    */
   private void resetResults()
   {
      if (isAppend())
      {
         if (m_result.length() > 0)
         {
            m_result.append("\n\n");
         }
      }
      else
      {
         m_result.setLength(0);
      }
   }

   /**
    * Executes the command in the remote console handler.
    * @return the command result. Never <code>null</code>.
    */
   private String executeInConsoleHandler() throws PSIllegalArgumentException,
         PSConsoleCommandException, IOException
   {
      final StringWriter cout = new StringWriter();
      // parse the command string (which validates it,
      // etc.) and execute it
      final IPSConsoleCommand parsedCmd =
            PSConsoleCommandParser.parse(getCommand());
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      final Document respDoc = parsedCmd.execute(req);
      PSXmlDocumentBuilder.write(respDoc, cout);
      return cout.toString();
   }

   /**
    * The current command for the server to execute.
    * @return the command. Can be <code>null</code> or empty.
    */
   public String getCommand()
   {
      return m_command;
   }


   /**
    * @param command the command to run. Can be <code>null</code> or empty.
    * Is converted to lower case.
    * Values {@link #EXIT_CMD} and {@link #QUIT_CMD} are interpreted as if value
    * {@link #STOP_SERVER_CMD} was specified.
    * @see #getCommand()
    */
   public void setCommand(String command)
   {
      if (command == null)
      {
         m_command = null;
      }
      else
      {
         if (command.equalsIgnoreCase(QUIT_CMD) 
            || command.equalsIgnoreCase(EXIT_CMD))
         {
            m_command = STOP_SERVER_CMD;
         }
         else
         {
            m_command = command;
         }
      }
   }

   /**
    * Whether to append result to the previous result.
    * @return indication whether {@link #getResult()} returns
    * the current command concatenated with the result of the previous command.
    * If <code>false</code>, returns only result of the current command.
    * @see #getCommand()
    */
   public boolean isAppend()
   {
      return m_append;
   }

   /**
    * @param append the value returned by {@link #isAppend()}.
    * @see #isAppend()
    */
   public void setAppend(boolean append)
   {
      m_append = append;
   }
   
   @Override
   public String getHelpTopic()
   {
      return "console";
   }

   /**
    * @see #getCommand()
    */
   private String m_command;

   /**
    * @see #isAppend()
    */
   private boolean m_append;
   
   /**
    * Results of the commands.
    * @see #isAppend()
    */
   private final StringBuilder m_result = new StringBuilder();
}
