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
package com.percussion.server;

import com.percussion.server.command.PSConsoleCommandException;
import org.w3c.dom.Document;


/**
 * The IPSConsoleCommand interface defines the methods a console command
 * handler class must implement.
 *
 * @see         PSRemoteConsoleHandler
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSConsoleCommand
{
   /**
    * Execute the command specified by this object. The results are returned
    * as an XML document appropriate to the command. The following format (dtd)
    * is suggested as basis for this document to provide consistency and  
    * machine processing capability. 
    * <P>Additional nodes can be appended to the root node after resultText.
    * <PRE><CODE>
    *    &lt;ELEMENT PSXConsoleCommandResults   (command, resultCode,
    *    resultText)&gt;
    *
    *    &lt;--
    *       the command that was executed (includes the arguments)
    *    --&gt;
    *    &lt;ELEMENT command  (#PCDATA)&gt;
    *
    *    &lt;--
    *       the result code for the command execution
    *    --&gt;
    *    &lt;ELEMENT resultCode  (#PCDATA)&gt;
    *
    *    &lt;--
    *       the message text associated with the result code
    *    --&gt;
    *    &lt;ELEMENT resultText  (#PCDATA)&gt;
    * </CODE></PRE>
    *
    * @param request the requestor object, may be <code>null</code>
    *
    * @return the result document, never <code>null</code>
    *
    * @throws PSConsoleCommandException   if an error occurs during execution
    */
   public Document execute(PSRequest request)
      throws PSConsoleCommandException;
}

