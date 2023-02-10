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
