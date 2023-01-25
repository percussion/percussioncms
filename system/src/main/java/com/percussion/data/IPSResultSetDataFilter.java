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

