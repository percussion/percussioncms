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

package com.percussion.services.publisher.data;


import static org.apache.commons.lang.Validate.isTrue;

import java.util.Iterator;

/**
 * A container for the content list result processing.
 * @author adamgent
 *
 */
public final class PSContentListResults
{

   private Iterator<PSContentListItem> m_iterator;
   public long m_estimatedSize = 0;
   
   
   public PSContentListResults(Iterator<PSContentListItem> iterator,
         long estimatedSize)
   {
      super();
      m_iterator = iterator;
      this.m_estimatedSize = estimatedSize;
   }

   public long getEstimatedSize()
   {
      return m_estimatedSize;
   }

   public Iterator<PSContentListItem> iterator() {
      Iterator<PSContentListItem> it = m_iterator;
      isTrue(it != null, "Iterator should not have been called yet or has been called more than once.");
      m_iterator = null;
      return it;
   }
}
