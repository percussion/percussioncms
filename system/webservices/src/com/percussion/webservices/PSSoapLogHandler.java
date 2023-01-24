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

package com.percussion.webservices;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
 
/**
 * Log soap request and responses to log4j see server-config.wsdd and client-config.wsdd
 * for configuration.  Change log level of this class in log4j to see messages
 */
public class PSSoapLogHandler extends BasicHandler {
 
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private static final Logger log =  LogManager.getLogger(PSSoapLogHandler.class);
    @Override
    public void invoke(MessageContext mc) throws AxisFault {
        if (mc.getResponseMessage() != null && mc.getResponseMessage().getSOAPPartAsString() != null) {
            String resMsg = mc.getResponseMessage().getSOAPPartAsString();
            log.debug("SOAP Response: " + resMsg);
        } else if (mc.getRequestMessage() != null && mc.getRequestMessage().getSOAPPartAsString() != null) {
            String reqMsg = mc.getRequestMessage().getSOAPPartAsString();
            log.debug("SOAP Request: " + reqMsg);
        }
    }
}
