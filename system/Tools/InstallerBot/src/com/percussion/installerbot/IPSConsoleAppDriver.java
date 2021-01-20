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


package com.percussion.installerbot;

/**
 * Adapter to a library which launches and drives console application. 
 * 
 * @author Andriy Palamarchuk
 */
public interface IPSConsoleAppDriver
{
   /**
    * Starts the application.
    * @param command application to run. Can't be blank.
    * @param defaultTimeoutInSec timeout in seconds for {@link #expect(String)}
    * command.
    * Can't be 0. A timeout of -1 will make the expect method wait indefinitely
    * untill the supplied pattern matches with the Standard Out.
    * @throws PSConsoleAppDriverException if an error occurred
    */
   void launchApplication(String command, long defaultTimeoutInSec)
         throws PSConsoleAppDriverException;
   
   /**
    * Returns when the specified string is matched or default timeout is reached.
    * @param pattern string to match. Can't be blank.
    * @throws PSConsoleAppDriverException if an error occurred.
    */
   void expect(String pattern) throws PSConsoleAppDriverException;
   
   /**
    * Returns when the specified string is matched or the specified timeout is
    * reached.
    * @param pattern string to match. Can't be blank.
    * @param timeoutInSec number of seconds to wait for pattern match.
    * @throws PSConsoleAppDriverException if an error occurred.
    */
   void expect(String pattern, long timeoutInSec)
         throws PSConsoleAppDriverException;
   
   /**
    * Writes the string to the standard input of the spawned process.
    * @param line to write to output. Can't be blank.
    * @throws PSConsoleAppDriverException if an error occurred.
    */
   void send(String line) throws PSConsoleAppDriverException;
   
   /**
    * Kills the underlying process.
    */ 
   void stop();
   
   /**
    * Returns <code>true</code> if last <code>expect()</code> call returned
    * because of a time out rather then a match against the output
    * of the process. 
    */
   boolean isLastExpectTimeOut();

   /**
    * Returns <code>true</code> if the process has already exited. 
    */
   boolean isClosed();

   /**
    * File ExpectJ stores log of the session to.
    */
   public void setLogFile(final String logFile);
   public String getLogFile();
   
   /**
    * This method returns the available contents of standard out.
    */
   public String getCurrentStandardOutContents();
}
