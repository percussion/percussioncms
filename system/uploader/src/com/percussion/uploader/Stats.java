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
package com.percussion.uploader;

import java.util.Date;

/**
 * A simple helper class that groups some statistical measures together. It
 * is used as a structure, not an object. It is up to the user to decide how
 * to use the fields.
 */
@Deprecated
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

