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
package com.percussion.rx.design;

import com.percussion.services.catalog.PSTypeEnum;

/**
 * Factory to provide the design model based on the type enum. The models for
 * type enum are registered in design-beans.xml file and the beans are loaded at
 * the time of server startup.
 * 
 * @author bjoginipally
 * 
 */
public interface IPSDesignModelFactory
{
   /**
    * Method to get the design object model for the supplied type enum. See
    * {@link IPSDesignModel} for the supported methods on the returned design
    * model.
    * 
    * @param type The type enum of the design object, must not be
    * <code>null</code>.
    * @return IPSDesignModel Object of the design model, never 
    * <code>null</code>. Throws <code>RunTimeException</code> in case of any
    * error and there is no bean corresponding to the supplied type enum.
    */
   public IPSDesignModel getDesignModel(PSTypeEnum type);
}
