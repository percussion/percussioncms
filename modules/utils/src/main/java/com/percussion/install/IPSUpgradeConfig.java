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

package com.percussion.install;

import java.util.Iterator;

/**
 * This interface defines an access method that returns the list of modules 
 * required to be run on a Rhythmyx install. The configuration document may 
 * define multiple modules depending the version of Rhythmyx. The required set 
 * of modules depends on the previous installation of Rhythmyx.
 */
public interface IPSUpgradeConfig
{
   /**
    * Returns list of modules that needs to be run on the existing installation 
    * to upgrade to the current version.
    * @return module list as Java <code>Iterator</code>
    */
   Iterator getModuleList();

}
