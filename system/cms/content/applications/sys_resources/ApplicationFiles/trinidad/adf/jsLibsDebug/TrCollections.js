/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/**
 * Utilities for working with collections
 */
var TrCollections = new Object();

/**
 * A static util function that removes an array of values
 * from another array.
 * @param {Array} toRemove Array that contains the values to remove
 * @param {Array} allValues Array from that the values will be removed
 */
TrCollections.removeValuesFromArray = function(
  toRemove,
  allValues
  )
{
  if(toRemove && allValues)
  {
    for(i=0; i<toRemove.length; i++)
    {
      var value = toRemove[i];
      for(j=0;j<allValues.length; j++)
      {
        if(allValues[j].toLowerCase() == value.toLowerCase())
        {
          allValues.splice(j,1);
        }
      }
    }
  }
}

