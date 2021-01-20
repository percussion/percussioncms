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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.test.statistics;

import java.util.Properties;
import com.percussion.test.io.DataLoader;
import com.percussion.test.io.TypedDataLoader;

/**
 * Accumulates an average without running overflowing.
 */
public class AverageAccumulator
{
   public AverageAccumulator()
   {
   }

   public void load(DataLoader loader, String name)
   {
      DataLoader ldr = loader.getChildLoader(name, this);
      m_avg = ldr.getDouble("avg");
      m_values = (int)ldr.getLong("values");
   }

   public void store(DataLoader loader, String name)
   {
      DataLoader ldr = loader.getChildLoader(name, this);
      ldr.setDouble("avg", m_avg);
      ldr.setLong("values", m_values);
   }

   public void accumulate(long next)
   {
      accumulate((double)next);
   }

   public void accumulate(double next)
   {
      m_values++;
      m_avg =   ((double)(m_values-1) / (double)(m_values)) * m_avg;
      m_avg +=  next / (double)(m_values);      
   }

   public void accumulate(AverageAccumulator avg)
   {
      // the sum of the sums
      m_values += avg.m_values;

      // the average of the averages
      m_avg = (m_avg + avg.m_avg) / 2;
   }

   public double average()
   {
      return m_avg;
   }

   private long m_values = 0L;
   private double m_avg = 0.0;
}
