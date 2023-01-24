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

import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.server.PSServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Properties;

/**
 * Retrieve the server properties for Rhythmyx.
 * Returns a JSON object with a property for each corresponding
 * server property.
 */
public class PSGetServerPropertiesAction extends PSAAActionBase
{

   /* (non-Javadoc)
    * @see com.percussion.content.ui.aa.actions.IPSAAClientAction#execute(java.util.Map)
    */
   public PSActionResponse execute(@SuppressWarnings("unused") Map<String, Object> params)
            throws PSAAClientActionException
   {
      Properties props = PSServer.getServerProps();
      JSONObject result = new JSONObject();      
      try
      {
         for(Object key : props.keySet())
         {
            String value = (String)props.get(key);
            result.put((String)key, value);
         }
      }
      catch (JSONException e)
      {
         throw new PSAAClientActionException(e);
      }
      return new PSActionResponse(result.toString(), PSActionResponse.RESPONSE_TYPE_JSON);
   }

}
