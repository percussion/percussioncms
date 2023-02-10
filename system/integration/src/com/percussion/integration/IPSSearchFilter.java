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
package com.percussion.integration;

/**
 * This interface allows users of the portal connector SDK to filter search
 * results. 
 */
public interface IPSSearchFilter
{
   /**
    * A search result object is supplied to the implementer of this method. The
    * implementation will do the filtering and return the same type of object
    * that was supplied with the filtered result.
    * 
    * @param searchResult a search result object that needs to be filtered,
    *    never <code>null</code>.
    * @return the filtered search result object, never <code>null</code>.
    */
   public Object filter(Object searchResult);
}
