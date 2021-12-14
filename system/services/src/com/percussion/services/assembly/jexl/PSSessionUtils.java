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
package com.percussion.services.assembly.jexl;

import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.server.PSRequest;
import com.percussion.utils.request.PSRequestInfo;

/**
 * This is a collection of session utilities required for accessing the server
 * through "front-door" requests. These are requests that need authentication to
 * pass the security filter.
 * 
 * @author dougrand
 */
public class PSSessionUtils extends PSJexlUtilBase
{
   @IPSJexlMethod(description = "Get the session id. The session id can be "
         + "passed back into Rhythmyx when calling a Rhythmyx application or "
         + "other URL via HTTP. Put the session id into the URL as a parameter "
         + "called PSSESSIONID.", params =
   {})
   public String getPSSessionID()
   {
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      return req.getUserSessionId();
   }

   @IPSJexlMethod(description = "Get the jsession id. The session id can be "
         + "passed back into Rhythmyx when calling a Rhythmyx application or "
         + "other URL via HTTP. The id must be either passed back in using "
         + "a cookie called JSESSIONID or via the special URL syntax required.", params =
   {})
   public String getJSessionID()
   {
      return (String) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_JSESSIONID);
   }
}
