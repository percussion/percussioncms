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
package com.percussion.delivery.comments.data;

/**
 * A small data class to hold sortby info for a comment result set.
 * @author erikserating
 */
public class PSCommentSort
{
   
   /**
    * The sort by field value. Initialized in the ctor.
    */
   private SORTBY sortby;
   
   /**
    * Indicates an ascending sort direction if set to <code>true</code>.
    * Initialized in the ctor.
    */
   private boolean ascending = true;
   
   /**
    * Ctor
    * @param sortby sort by option, cannot be <code>null</code>.
    * @param isAscending <code>true</code> indicates an ascending sort order.
    */
   public PSCommentSort(SORTBY sortby, boolean isAscending)
   {
      if(sortby == null)
         throw new IllegalArgumentException("sortby cannot be null.");
      this.sortby = sortby;
      this.ascending = isAscending;
   }
   
   /**
    * @return the sortby field, never <code>null</code>.
    */
   public SORTBY getSortBy()
   {
      return sortby;
   }
   
   /**
    * @return <code>true</code> indicates an ascending sort direction.
    */
   public boolean isAscending()
   {
      return ascending;
   }
   
   /**
    * Enumeration of sort field options.
    */
   public enum SORTBY
   {
      CREATEDDATE,
      EMAIL,
      USERNAME
   }
   
}
