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
package com.percussion.rxverify.modules;

import com.percussion.rxverify.data.PSInstallation;

import java.io.File;

/**
 * @author dougrand
 * 
 * Define the interface for classes that verify a Rhythmyx installation
 */
public interface IPSVerify
{
   /**
    * Generate entries for a specific directory, recursing into subdirectories
    * as they are found while traversing the file list
    * 
    * @param rxdir The directory, must not be <code>null</code>
    * @param installation stores information about an installation
    * @throws Exception when there is a problem generating the verification
    *            information
    */
   void generate(File rxdir, PSInstallation installation) throws Exception;

   /**
    * Verify the contents of the Rhythmyx directory. This reads the bom info
    * into an internal database, then uses the same mechanism as generate to
    * categorize all the files in the Rhythmyx directory, and then compares the
    * two.
    * 
    * @param rxdir the rhythmyx directory, must not be <code>null</code>
    * @param originalRxDir the original rhythmyx directory that was used before
    *           the upgrade, may be omitted, which means that some checks may
    *           not be performed
    * @param installation the information about an installation, must not be
    *           <code>null</code>
    * @throws Exception when there is a problem using the verification
    *            information
    */
   void verify(File rxdir, File originalRxDir, PSInstallation installation)
         throws Exception;
}
