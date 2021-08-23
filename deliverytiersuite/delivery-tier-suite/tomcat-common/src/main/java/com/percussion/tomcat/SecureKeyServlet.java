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

package com.percussion.tomcat;

import com.percussion.security.PSEncryptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class SecureKeyServlet extends HttpServlet
{
   private static final Logger logger = LogManager.getLogger(SecureKeyServlet.class);
    public void init() throws ServletException
    {
        boolean secureKeyPresent = PSEncryptor.checkSecureKeyPresent();
        if(!secureKeyPresent){
           logger.error("*******SECURE KEY FILE IS MISSING!!! NEED TO COPY FROM CMS FIRST******");
           System.out.println("*******SECURE KEY FILE IS MISSING!!! NEED TO COPY FROM CMS FIRST******");
        }
    }
}