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
package com.percussion.services.contentmgr.impl.jsrdata;

import com.percussion.services.contentmgr.data.PSRow;
import com.percussion.utils.jsr170.PSCollectionRangeIterator;

import java.util.Collection;

import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

/**
 * Implementation of a JSR-170 row iterator
 * 
 * @author dougrand
 */
public class PSRowIterator extends PSCollectionRangeIterator<PSRow>
   implements RowIterator
{
   public PSRowIterator(Collection<PSRow> collection) {
      super(collection);
   }

   public Row nextRow()
   {
      return next();
   }
}
