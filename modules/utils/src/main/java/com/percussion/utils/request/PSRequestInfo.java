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
package com.percussion.utils.request;


import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * This class stores a variety of per request information. This is initialized
 * in a servlet at the start of a request, and cleared at the end of the
 * request. The per-request information is stored locally to the thread and is
 * therefore not accessible to other requests.
 * <P>
 * Information is stored under keys. The available keys are string values that
 * are defined on this class. The values are specified as part of the per-key
 * documentation. Note that not every key is necessarily available in every
 * environment this class is used in.
 * 
 * @author dougrand
 */
public class PSRequestInfo extends PSRequestInfoBase
{
   /**
    * Initialize request information from a servlet. This method calls
    * {@link #initRequestInfo(Map)}with the derived information.
    *
    * @param req the servlet request, never <code>null</code>
    */
   public static void initRequestInfo(HttpServletRequest req)
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req may not be null");
      }
      Map<String,Object> initial = new HashMap<String,Object>();
      initial.put(PSRequestInfoBase.KEY_JSESSIONID, req.getSession().getId());

      initial.put(PSRequestInfoBase.KEY_USER, req.getRemoteUser());
      initRequestInfo(initial);
   }


}
