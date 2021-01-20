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
package com.percussion.rx.config.test;

import com.percussion.rx.config.impl.PSConfigDeltaFinder;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class PSConfigDeltaFinderTest extends TestCase
{
   public void testDeltaFinder()
   {
      Map<String, Object> newProps = new HashMap<String, Object>();
      newProps.put("name1", "value1");
      newProps.put("name2", "value2");
      newProps.put("name3", "value3");
      Map<String, Object> oldProps = new HashMap<String, Object>();
      oldProps.put("name1", "value1");
      oldProps.put("name2", "value2C");
      oldProps.put("name3", "value3");
      PSConfigDeltaFinder df = new PSConfigDeltaFinder();
      Map<String, Object> delta = df.getConfigDelta(newProps, oldProps);
      // This should yield only one difference that is name2.
      assertTrue(delta.size() == 1);
      assertTrue("value2".equals(delta.get("name2")));
      delta.clear();
      // Remove a name3 from oldProps, as newProps has a this property it should
      // be in delta.
      oldProps.remove("name3");
      delta = df.getConfigDelta(newProps, oldProps);
      assertTrue(delta.size() == 2);
      assertTrue("value2".equals(delta.get("name2")));
      assertTrue("value3".equals(delta.get("name3")));
      delta.clear();
      // Remove the name3 prop from newProps and add it back to oldProps. As we
      // check the new props with old props and bring only the ones different or
      // new, this case should yield only one difference
      newProps.remove("name3");
      oldProps.put("name3", "value3");
      delta = df.getConfigDelta(newProps, oldProps);
      assertTrue(delta.size() == 1);
      assertTrue("value2".equals(delta.get("name2")));
      delta.clear();
      // Add name4 property to newProps, thsi case should bring name2(different
      // from oldProps) and name4(not in oldProps)
      newProps.put("name4", "value4");
      delta = df.getConfigDelta(newProps, oldProps);
      assertTrue(delta.size() == 2);
      assertTrue("value2".equals(delta.get("name2")));
      assertTrue("value4".equals(delta.get("name4")));
      delta.clear();
   }
}
