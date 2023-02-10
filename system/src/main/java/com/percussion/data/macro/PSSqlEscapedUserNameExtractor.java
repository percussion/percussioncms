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

package com.percussion.data.macro;

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.security.PSUserEntry;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.util.PSSqlHelper;

public class PSSqlEscapedUserNameExtractor implements IPSMacroExtractor
{

   @Override
   public Object extract(PSExecutionData data) throws PSDataExtractionException
   {
      try
      {
         String result = "";
         PSRequest request = data.getRequest();
         PSUserSession sess = (request == null) ? null : request.getUserSession();
         if (sess != null)
         {
            PSUserEntry[] users = sess.getAuthenticatedUserEntries();
            if (users.length > 0)
            {
               result = PSSqlHelper.escapeQueryParamValue(users[0].getName());
            }
         }
         
         return result;
      }
      catch (Exception e)
      {
         throw new PSDataExtractionException(IPSServerErrors.RAW_DUMP, e.getLocalizedMessage());
      }
   }

}
