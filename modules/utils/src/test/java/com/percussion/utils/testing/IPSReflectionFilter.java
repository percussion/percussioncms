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
package com.percussion.utils.testing;

/**
 * @author DougRand
 *
 * Implement this class to enable the filtering of accessor methods.
 */
public interface IPSReflectionFilter
{
   /**
    * Return true if the given methodname should be used as an accessor
    * and false otherwise.
    * @param methodname The given methodname, which will never be 
    * <code>null</code>
    * or empty
    * @return
    */
   boolean  acceptMethod(String methodname);
}
