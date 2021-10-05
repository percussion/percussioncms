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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.search;


/**
 * Interface for executing searches against the Rhythmyx server.  Instances of 
 * classes implementing this interface may be obtained thru the 
 * {@link PSExecutableSearchFactory}.
 */
public interface IPSExecutableSearch
{
   /**
    * Executes a standard search based on the criteria specified when 
    * constructing this object.  This method may be called more than once.
    *    
    * @return the search response, never <code>null</code>, may be empty.
    *
    * @throws PSSearchException if an error happens executing search.
    */
   public PSWSSearchResponse executeSearch() 
      throws PSSearchException;
}
