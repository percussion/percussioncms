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
package com.percussion.data.utils;

import com.percussion.data.IPSTableChangeListener;
import com.percussion.data.PSUpdateHandler;
import com.percussion.design.objectstore.PSBackEndTable;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSUpdatePipe;
import com.percussion.server.IPSHandlerInitListener;
import com.percussion.server.IPSRequestHandler;
import com.percussion.util.PSCollection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base class that looks for update events from update handlers and causes
 * subclass specific actions to be taken. This class is instantiated and added
 * as a listener on {@link com.percussion.server.PSServer}.
 * 
 * @author dougrand
 */
public abstract class PSTableUpdateHandlerBase
      implements
         IPSHandlerInitListener,
         IPSTableChangeListener
{
   /**
    * Table names that are of interested for this update handler. Initialized
    * in the ctor, never empty
    */
   protected final List<String> m_tables = new ArrayList<String>();

   /**
    * @param tables the array of tables that we're interested in, never
    *           <code>null</code> or empty
    */
   public PSTableUpdateHandlerBase(String tables[]) {
      if (tables == null || tables.length == 0)
      {
         throw new IllegalArgumentException("Must have tables specified");
      }
      for (String table : tables)
      {
         m_tables.add(table.toLowerCase());
      }
   }

   public void initHandler(IPSRequestHandler requestHandler)
   {
      if (requestHandler instanceof PSUpdateHandler)
      {
         // see if the update handler contains a table we care about.
         PSUpdateHandler uh = (PSUpdateHandler) requestHandler;
         PSDataSet ds = uh.getDataSet();
         PSPipe pipe = ds.getPipe();
         if (!(pipe instanceof PSUpdatePipe))
            return;

         PSUpdatePipe upPipe = (PSUpdatePipe) pipe;

         PSCollection tableCol = upPipe.getBackEndDataTank().getTables();

         boolean added = false;
         Iterator tables = tableCol.iterator();
         while (tables.hasNext() && !added)
         {
            PSBackEndTable table = (PSBackEndTable) tables.next();
            if (m_tables.contains(table.getTable().toLowerCase()))
            {
               uh.addTableChangeListener(this);
            }
         }
      }
   }

   public void shutdownHandler(IPSRequestHandler requestHandler)
   {
      // Noop

   }
}
