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
package com.percussion.rxfix;

import java.util.List;

/**
 * Each fixup module implements this interface.
 */
public interface IPSFix
{
   /**
    * Perform a fix on the specified installation of Rhythmyx, which references
    * the passed database definition by default. Any existing results are cleared
    * by calling this method
    * 
    * @param preview No actual changes should occur, just output the list of
    *           anticipated changes using log4j
    * @throws Exception if there is an error performing the fixup
    */
   void fix(boolean preview) throws Exception;
   
   /**
    * Recover results from the performed operations.
    * @return the results, may be empty but never <code>null</code>
    */
   List<PSFixResult> getResults();   
   
   /**
    * Get the operation that this module performs 
    * @return the operation, never <code>null</code> or empty
    */
   String getOperation();

   /**
    * If the fix needs to be run only once and if successful, we don't want to rerun this fix
    * then return true at the end of the successfull process complete.
    * @return
    */
   boolean removeStartupOnSuccess();
}
