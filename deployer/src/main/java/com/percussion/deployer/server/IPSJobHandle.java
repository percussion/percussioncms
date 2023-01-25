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

 
package com.percussion.deployer.server;

/**
 * Handle to the currently executing job, used to update status.
 */
public interface IPSJobHandle
{
   /**
    * Updates the current status using the supplied message
    * 
    * @param message The message, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>message</code> is invalid.
    */
   public void updateStatus(String message);
   
   /**
    * Determines if the current job has been cancelled.
    * 
    * @return <code>true</code> if the job is cancelled and the executing code
    * should stop at an appropriate point, cleanup as required, and return,
    * <code>false</code> otherwise.
    */
   public boolean isCancelled();
}
