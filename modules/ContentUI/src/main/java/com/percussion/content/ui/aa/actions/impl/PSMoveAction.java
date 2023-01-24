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

import com.percussion.cas.PSModifyRelatedContent;
import com.percussion.content.ui.aa.PSAAObjectId;
import com.percussion.content.ui.aa.actions.PSAAClientActionException;
import com.percussion.content.ui.aa.actions.PSActionResponse;
import com.percussion.error.PSException;
import com.percussion.server.IPSRequestContext;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * An action that will move a slot item up or down by one position or move
 * the item to a specified position.
 * <p>
 * Expects the following parameters:
 * </p>
 * <table border="1" cellspacing="0" cellpadding="5">
 * <thead>  
 * <th>Name</th><th>Allowed Values</th><th>Details</th> 
 * </thead>
 * <tbody>
 * <tr>
 * <td>objectId</td><td>The object id string</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>mode</td><td>up, down, reorder</td><td>Required</td>
 * </tr>
 * <tr>
 * <td>index</td><td>The desired position to move to</td>
 * <td>Only required if in reorder mode</td>
 * </tr>
 * </tbody>
 * </table>
 */
public class PSMoveAction extends PSAAActionBase
{

   // see base class for details
   public PSActionResponse execute(Map<String, Object> params)
            throws PSAAClientActionException
   {
      PSAAObjectId objectId = getObjectId(params);
      String mode = (String)getParameter(params, "mode");
      String index = (String)getParameter(params, "index");
      
      int idx = -1;      
      
      // Validate mode
      if(StringUtils.isBlank(mode))
         throw new PSAAClientActionException(
                  "Missing required mode parameter.");
      if(!(mode.equals("up") || mode.equals("down") || mode.equals("reorder")))
         throw new PSAAClientActionException(
                  "Invalid mode! Must be 'up', 'down' or 'reorder'.");
      
      // Validate index
      if(mode.equals("reorder"))
      {
         if(StringUtils.isBlank(index))
            throw new PSAAClientActionException(
                     "index parameter required when using reorder mode.");
         try
         {
            idx = Integer.parseInt(index);   
         }
         catch(NumberFormatException nfe)
         {
            throw new PSAAClientActionException(
                     "Invalid format! index must be an integer.");
         }
      }
      IPSRequestContext request = getRequestContext();
      try
      {
         if(mode.equals("up"))
         {
            PSModifyRelatedContent.moveUp(
                     Integer.parseInt(objectId.getRelationshipId()), request);
         }
         else if(mode.equals("down"))
         {
            PSModifyRelatedContent.moveDown(
                     Integer.parseInt(objectId.getRelationshipId()), request);
         }
         else if(mode.equals("reorder"))
         {
            PSModifyRelatedContent.reorder(
                     Integer.parseInt(objectId.getRelationshipId()), idx, request);
         }
      }
      catch(PSException e)
      {
         throw new PSAAClientActionException(e);
      }      
      return new PSActionResponse(SUCCESS, PSActionResponse.RESPONSE_TYPE_PLAIN);
   }   

}
