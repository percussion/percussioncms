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
package com.percussion.services.contentmgr;

/**
 * Thie enumeration is used to specify options to the load process
 */
public enum PSContentMgrOption {
   /**
    * If this is provided, the input translations, output translations, 
    * field validations and initial values are processed as appropriate.
    */
   PROCESS_FIELDS, 
   /**
    * If this is provided, then children should be loaded on first access, not
    * up front.
    */
   LAZY_LOAD_CHILDREN,
   /**
    * Load the smallest amount of data possible.
    */
   LOAD_MINIMAL;
}
