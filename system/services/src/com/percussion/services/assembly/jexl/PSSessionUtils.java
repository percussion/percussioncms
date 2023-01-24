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
