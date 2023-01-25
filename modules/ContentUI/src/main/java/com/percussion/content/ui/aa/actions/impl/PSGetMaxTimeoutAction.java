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
package com.percussion.content.ui.aa.actions.impl;

import com.percussion.content.ui.aa.PSAAClientServlet;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;

import java.util.Map;

/**
 * Retrieves the server's max timeout setting in seconds.
 * Takes no params. Used for keep alive.
 */
public class PSGetMaxTimeoutAction extends PSAAActionBase
{

   // see interface for details
   @SuppressWarnings("unused")
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      Integer timeout = (Integer)params.get(PSAAClientServlet.PARAM_TIMEOUT);
      return new PSActionResponse(String.valueOf(timeout.intValue()),
               PSActionResponse.RESPONSE_TYPE_PLAIN);
   }

}
