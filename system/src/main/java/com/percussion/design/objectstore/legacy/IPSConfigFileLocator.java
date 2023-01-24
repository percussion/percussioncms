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

package com.percussion.design.objectstore.legacy;

import java.io.File;

/**
 * Interface to provide file locations of each of the configuration files used
 * by an {@link IPSComponentConverter}.
 */
public interface IPSConfigFileLocator
{
   /**
    * Get the location of the server configuration file.
    * 
    * @return The file, never <code>null</code>.
    */
   public File getServerConfigFile();

   /**
    * Get the location of the Spring beans configuration file.
    * 
    * @return The file, never <code>null</code>.
    */
   public File getSpringConfigFile();


}
