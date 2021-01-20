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

package com.percussion.webservices;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 
/**
 * Log soap request and responses to log4j see server-config.wsdd and client-config.wsdd
 * for configuration.  Change log level of this class in log4j to see messages
 */
public class PSSoapLogHandler extends BasicHandler {
 
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private static final Log log =  LogFactory.getLog(PSSoapLogHandler.class);
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