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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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