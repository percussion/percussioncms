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

        //Try localized message first and if there isn't one - just do default.
        String message = exception.getLocalizedMessage();

        //Get line number and class
        int line = exception.getStackTrace()[0].getLineNumber();
        String c = exception.getStackTrace()[0].getClassName();

        if(message==null || message.equals("")) {
            message = exception.getMessage();
        }

        message = message + " C:" + c + ":L:" + line;

        return message;

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
