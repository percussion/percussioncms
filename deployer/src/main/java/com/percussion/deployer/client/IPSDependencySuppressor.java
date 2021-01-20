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
