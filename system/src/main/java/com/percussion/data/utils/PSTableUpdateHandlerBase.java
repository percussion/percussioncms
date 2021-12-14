/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
