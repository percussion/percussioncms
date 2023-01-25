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
package com.percussion.cx;

import com.percussion.cx.objectstore.PSNode;

/**
 * This listener defines methods that are called during a search and after
 * a search. This allows the implementers to show information about the search
 * to the end user.
 */
public interface IPSSearchListener
{
   /**
    * Called when the user selects a new action. This allows the implementer
    * to remove any search displays.
    */
   void searchReset();
   
   /**
    * Called when a search is initiated.
    * @param node the search node, which may contain cached results,
    * must never be <code>null</code>.
    */
   void searchInitiated(PSNode node);
   
   /**
    * Called when a search completes.
    * @param node the search node, which contains the results, 
    * must never be <code>null</code>.
    */
   void searchCompleted(PSNode node);
}

