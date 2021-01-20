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
package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;

/**
 * UDF to get the rhythmyx request root. This can be used as base request url
 * to make XSL document calls to rhythmyx resources. 
 */
public class PSRhythmyxRoot extends PSSimpleJavaUdfExtension
{
   /**
    * This UDF constructs the rhythmyx root url out of the supplied request
    * and returns it as <code>String</code>.
    * 
    * @param params no parameters are used for this UDF, all values will be 
    *    ignored.
    * @param request the request from which the protocol, host and port will
    *    be inherited.
    * @return a rhythmyx request root as <code>String</code>, something like
    *    <code>http://localhost:9992/Rhythmyx</code>.
    * @throws PSConversionException is never thrown.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      String protocol = request.getOriginalProtocol();
      String host = request.getOriginalHost();
      int port = request.getOriginalPort();
      String root = PSServer.getRequestRoot();
      
      return protocol + "://" + host + ":" + port + root;
   }
}
