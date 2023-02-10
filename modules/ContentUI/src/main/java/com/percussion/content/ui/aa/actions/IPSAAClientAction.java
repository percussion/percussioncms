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
package com.percussion.content.ui.aa.actions;

import java.util.Map;

/**
 * An aa client action does some action on the Rhythmyx server or
 * retrieves information for the the AA client to use. Actions are expected
 * to be in the com.percussion.content.ui.aa.actions.impl package and must have 
 * the naming convention of PSXXXAction. This naming convention is required
 * as reflection is used to instantiate the action instance. The client
 * must send the descriptive name between the PS and Action so that the
 * action factory can find and instantiate the action.
 */
public interface IPSAAClientAction
{
   
   /**
    * This is where the work of the action is executed.
    * @param params a map of parameters that the action will need
    * to do its job. Usually is just some of the parameters from the
    * clients servlet request. May be <code>null</code> or empty if
    * parameters are not needed.
    * @return an action response object that contains the response data
    * and the response return type. Never <code>null</code>.
    * @throws PSAAClientActionException 
    */
   public PSActionResponse execute(Map<String, Object> params)
      throws PSAAClientActionException; 
   
   
   
   /**
    * Success string constant
    */
   public String SUCCESS = "success";
   
   /**
    * Object id parameter constant
    */
   public String OBJECT_ID_PARAM = "objectId"; 
   
   
   
}
