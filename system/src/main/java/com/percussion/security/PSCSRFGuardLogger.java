/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.security;

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.csrfguard.log.LogLevel;

/**
 * Wrapper log class for owasp csrf gaurd logging.  Is referenced in owaspcsrfguard.properties.
 */
public class PSCSRFGuardLogger implements org.owasp.csrfguard.log.ILogger{

  private static final Logger log = LogManager.getLogger(PSCSRFGuardLogger.class);

    /**
     * Log a message
     *
     * @param msg
     */
    @Override
    public void log(String msg) {
        log.info(msg);
    }

    /**
     * TODO document
     *
     * @param level
     * @param msg
     */
    @Override
    public void log(LogLevel level, String msg) {

        switch (level){
            case Info:
                log.info(msg);
                break;
            case Debug:
                log.debug(msg);
                break;
            case Error:
                log.error(msg);
                break;
            case Fatal:
                log.fatal(msg);
                break;
            case Trace:
                log.trace(msg);
                break;
            default:
                    log.warn(msg);
        }

    }

    /**
     * TODO document
     *
     * @param exception
     */
    @Override
    public void log(Exception exception) {
        log.error(PSExceptionUtils.getMessageForLog(exception));
    }

    /**
     * TODO document
     *
     * @param level
     * @param exception
     */
    @Override
    public void log(LogLevel level, Exception exception) {
        switch (level){
            case Info:
                log.info(PSExceptionUtils.getMessageForLog(exception));
                break;
            case Debug:
                log.debug(PSExceptionUtils.getMessageForLog(exception));
                break;
            case Error:
                log.error(PSExceptionUtils.getMessageForLog(exception));
                break;
            case Fatal:
                log.fatal(PSExceptionUtils.getMessageForLog(exception));
                break;
            case Trace:
                log.trace(PSExceptionUtils.getMessageForLog(exception));
                break;
            default:
                log.warn(PSExceptionUtils.getMessageForLog(exception));
        }
    }
}
