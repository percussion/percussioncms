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
