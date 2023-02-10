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
package com.percussion.services.contentmgr.impl.query;


/**
 * Maps field names to hibernate properties. Used to transform field names
 * to hibernate reference for query building. Presented as an interface to
 * enable testing.
 * 
 * @author dougrand
 *
 */
public interface IPSPropertyMapper
{
   /**
    * Transform the input name to an output name
    * @param propname the property name, never <code>null</code> or empty
    * @return the transformed name, could be identical to the input name.
    */
   String translateProperty(String propname);
}
