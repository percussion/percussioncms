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

package com.percussion.error;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/***
 * A class for common exception utilities.
 */
public class PSExceptionUtils {

    private static final Logger log = LogManager.getLogger(PSExceptionUtils.class);
    public static String getMessageForLog(Throwable exception){

        //Try localized message first and if there isn't one - just do default.
        String message = exception.getLocalizedMessage();

        //Get line number and class
        int line = exception.getStackTrace()[0].getLineNumber();
        String c = exception.getStackTrace()[0].getClassName();

        if(message==null || message.equals("")) {
            message = exception.getMessage();
        }

        message = message + " C:" + c + ":L:" + line;

        //Add cause if there is one
        if (exception.getCause() != null) {
            String cause = exception.getCause().getMessage();
            line = exception.getCause().getStackTrace()[0].getLineNumber();
            c = exception.getCause().getStackTrace()[0].getClassName();
            message += " Cause:" + cause + " C:" + c + ":L:" + line;
        }
        return message;

    }

    /**
     * Use when outputting error messages or warnings to the log based on exceptions.
     *
     * The message will be written out localized if the localized message is available and the
     * error message will include C:[ClassName] L:[Line Number] to aid in problem diagnosis without
     * flooding the log with stack traces,  Stack traces should only ever be written to the debug log.
     * @param exception A valid exception, never null;
     * @return A string with the message, never null;
     */
    public static String getMessageForLog(Exception exception){
        return getMessageForLog((Throwable) exception);
    }

    /**
     * Use when outputting stack trace etc to debug log.
     * @param e A valid exception
     * @return A safe debug string to wrote to the log.
     */
    public static String getDebugMessageForLog(Exception e){
        try(StringWriter sw = new StringWriter()) {
            try(PrintWriter pw = new PrintWriter(sw)) {
               e.printStackTrace(new PrintWriter(sw));
               return sw.toString().trim();
            }
        } catch (IOException ioException) {
            return "Unable to extract stack trace for exception. Error: " + PSExceptionUtils.getMessageForLog(ioException);
        }
    }

}
