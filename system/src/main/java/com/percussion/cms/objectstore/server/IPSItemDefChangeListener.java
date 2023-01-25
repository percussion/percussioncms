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
