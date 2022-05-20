/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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
