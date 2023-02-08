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



/**
 * The PSDateLiteralExtractor class is used to extract data from 
 * a pre-defined date literal.
 * 
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDateLiteralExtractor extends PSLiteralExtractor
{
   /**
    * Construct an object from its object store counterpart.
    *
    * @param   source      the object defining the source of this value
    */
   public PSDateLiteralExtractor(
      com.percussion.design.objectstore.PSDateLiteral source)
   {
      super(source);
   }
}

