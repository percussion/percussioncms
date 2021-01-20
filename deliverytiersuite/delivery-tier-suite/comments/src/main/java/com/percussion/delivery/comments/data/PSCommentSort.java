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
