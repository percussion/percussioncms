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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.error.PSException;

/**
 * This interface is designed to be used with the {@link PSItemDefManager} 
 * class. It provides a mechinism to allow users to register to receive
 * messages whenever a content editor is started or stopped.
 *
 * @author paulhoward
 */
public interface IPSItemDefChangeListener
{
   /**
    * Called when a content editor is successfully started.
    * @param def The definition of the editor that started, never <code>
    * null</code>.
    * @param notify propagate a notification to the listener. If a series of
    * changes are being done as a unit, this can be set to <code>false</code>
    * for every call but the last.
    * 
    * @throws PSException If actions taken are not successful. This will 
    * prevent the editor from starting, so it should be done with great care.
    */
   public void registered(PSItemDefinition def, boolean notify) throws PSException;
   
   /**
    * Called when a content editor is shut down.
    * 
    * @param def The definition of the editor that stopped, never <code>
    * null</code>.
    * @param notify propagate a notification to the listener. If a series of
    * changes are being done as a unit, this can be set to <code>false</code>
    * for every call but the last.
    * 
    * @throws PSException If actions taken are not successful. This does not
    * prevent the editor from stopping.
    */
   public void unregistered(PSItemDefinition def, boolean notify) throws PSException;
}
