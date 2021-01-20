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
package com.percussion.uploader;

import java.util.Date;

/**
 * A simple helper class that groups some statistical measures together. It
 * is used as a structure, not an object. It is up to the user to decide how
 * to use the fields.
 */
public class Stats
{
   public int rowsUpdated = 0;
   public int rowsInserted = 0;
   public int rowsSkipped = 0;
   public int rowsFailed = 0;
   public int rowsDePublished = 0;
   public int docsProcessed = 0;
   public int errors = 0;
   public int warnings = 0;

   public Date startTime = null;
   public String startTimeString = null;

   public Date finishTime = null;
   public String finishTimeString = null;

   /**
    * @return The sum of all row statistics.
    */
   public int getRowsProcessed()
   {
      return rowsUpdated + rowsInserted + rowsSkipped + rowsDePublished
         + rowsFailed;
   }
}

