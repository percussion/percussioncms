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
package com.percussion.server.command;

import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.i18n.PSTmxResourceBundle;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRemoteConsoleHandler;
import com.percussion.server.PSRequest;

import java.util.Locale;

import org.w3c.dom.Document;


/**
 * The PSConsoleCommandDebugI18n class implements processing of the
 * "i18n debug on/off" console command.
 *
 * @see PSRemoteConsoleHandler
 */
public class PSConsoleCommandDebugI18n 
   extends PSConsoleCommand
{
   /**
    * The constructor for this class. The allowed values are 
    * true|false|on|off|yes|no. Any of the values from (true|on|yes) will set 
    * the debug mode on and does not affect if it is already on. Any other 
    * value will set the debug mode off and does not affect if it is already 
    * off.
    * @param cmdArgs must not be <code>null</code> ot <code>empty</code>.
    * @throws PSIllegalArgumentException if the argument is <code>null</code> 
    * or <code>empty</code>.
    */
   public PSConsoleCommandDebugI18n(String cmdArgs)
      throws PSIllegalArgumentException
   {
      super(cmdArgs);
      // need the debug mode ('true'/'false' or 'on/'off' or 'yes/'no') 
      // for this command
      if ((cmdArgs == null) || (cmdArgs.length() == 0)) {
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_DEBUGMODE_REQD, ms_cmdName);
      }
   }

   /**
    * Sets the debug tracing on/off as specified in the command arg.
    * 
    * @return A doc conforming to the recommended format.
    */
   public Document execute(PSRequest request)
      throws PSConsoleCommandException
   {
      try
      {
         // reload the TMX resources
         boolean debug = false;
         if(m_cmdArgs.equalsIgnoreCase("yes") ||
            m_cmdArgs.equalsIgnoreCase("on") ||
            m_cmdArgs.equalsIgnoreCase("true"))
         {
            debug = true;
         }
         PSTmxResourceBundle.getInstance().setDebugMode(debug);
         Object[] args =
         { 
            PSTmxResourceBundle.getInstance().getDebugMode() ? "on" : "off" 
         };
         
         Document doc = getResultsDocument(request, ms_cmdName, 
               IPSServerErrors.RCONSOLE_DEBUG_SETTING, args);
         return doc;
      } catch (Exception e) {
         Locale loc;
         if (request != null)
            loc = request.getPreferredLocale();
         else
            loc = Locale.getDefault();

         String msg;
         if (e instanceof PSException)
            msg = ((PSException)e).getLocalizedMessage(loc);
         else
            msg = e.getMessage();

         Object[] args = { (ms_cmdName + " " + m_cmdArgs), msg };
         throw new PSConsoleCommandException(
            IPSServerErrors.RCONSOLE_EXEC_EXCEPTION, args);
      }
   }


   /**
    * The command executed by this class.
    */
   static final String   ms_cmdName = "debug i18n";
}
