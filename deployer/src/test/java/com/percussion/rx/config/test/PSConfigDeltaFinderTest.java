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
