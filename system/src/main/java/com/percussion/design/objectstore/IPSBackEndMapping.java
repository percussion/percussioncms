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

package com.percussion.design.objectstore;


/**
 * The IPSBackEndMapping interface must be implemented by any class which
 * will be used as a back-end mapping in a PSDataMapping object.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public interface IPSBackEndMapping extends IPSReplacementValue
{
   /**
    * Get the columns which must be selected from the back-end(s) in
    * order to use this mapping. The column name syntax is 
    * <code>back-end-table-alias.column-name</code>.
    *
    * @return     the columns which must be selected from the back-end(s)
    *             in order to use this mapping.  If there are no columns, then
    *             <code>null</code> is returned.
    */
   public abstract String[] getColumnsForSelect();
}

