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
