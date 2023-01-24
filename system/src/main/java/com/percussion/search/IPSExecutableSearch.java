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
