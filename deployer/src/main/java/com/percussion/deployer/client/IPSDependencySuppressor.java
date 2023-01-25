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

import com.percussion.deployer.objectstore.PSDependency;

/**
 * A pluggable class for PSExportJob to determine if a given dependency should
 * be suppressed from the exported file.  Typically, this is used to suppress 
 * dependencies that have been added by the addMissingDependencies process that
 * the caller of the job does not want included.
 */
public interface IPSDependencySuppressor
{

   /**
    * Determines if the specified dependency should be suppressed from 
    * the dependency tree being assembled.  It is the responsibility of the
    * caller to enforce the suppression.
    * 
    * @param dependency the dependency to consider, never <code>null</code>
    * 
    * @return <code>true</code> if the dependency should be suppressed;
    * <code>false</code> otherwise.
    */
   public boolean suppress(PSDependency dependency);

}
