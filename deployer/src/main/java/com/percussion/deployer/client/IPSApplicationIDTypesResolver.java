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

package com.percussion.deployer.client;

import java.util.Iterator;

/**
 * A pluggable class for PSFolderContentsDescriptorBuilder to delegate the
 * typing of undefined application ID types. In the standard MSM client,
 * undefined application ID types are typed through user-interaction with a
 * dialog.
 * 
 * @author James Schultz
 */
public interface IPSApplicationIDTypesResolver
{
   /**
    * Defines each undefined ID type mapping in the supplied iterator, by
    * calling the mapping's <code>setType</code> method.
    * 
    * @param undefinedMappings an iterator of
    *           <code>PSApplicationIDTypeMapping</code>, each representing an
    *           undefined ID type
    */
   public void defineIdTypes(Iterator undefinedMappings);

}
