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
