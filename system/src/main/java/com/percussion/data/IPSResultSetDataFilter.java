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
package com.percussion.data;

import java.util.List;

/**
 * The IPSResultSetDataFilter is used to create a result set filter to accept 
 * possible result set rows based on criterion set by this object. The 
 * implementing classes would get called from the <code>
 * PSFilterResultSetWrapper</code> class calls "accept" each time the result 
 * set advances using the <code> next</code> method. The accept method will 
 * move the cursor position foward  until the end or the current row passes 
 * the accept method.
 */
public interface IPSResultSetDataFilter
{
   /**
    * Called to determine if the current row is "acceptable" based on the 
    * criterion. If return <code>false</code>, the calling class will move the 
    * current cursor of the result set forward until this method returns <code>
    * true</code>
    * 
    * @param data the execution data to operate on, not <code>null</code>.
    * 
    * @param vals the list of values to check against, what it checks them
    *    against is up to the implementing class
    * 
    * @return <code>true</code> if the result row is to be accepted in the
    *    creation of the xml data, otherwise <code>false</code>
    */
   public boolean accept(PSExecutionData data, Object[] vals);
   
   /**
    * Return the list of columns for the specified filter.
    */
   public List getColumns();
}

