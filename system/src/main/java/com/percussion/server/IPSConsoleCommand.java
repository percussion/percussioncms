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

