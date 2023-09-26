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
package com.percussion.services.contentmgr.impl.query.nodes;

import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode.Op;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * Basic query node test
 * @author dougrand
 *
 */
public class PSQueryNodeTest extends TestCase
{   
   /**
    * Test serialization
    */
   public void testSimpleToString()
   {
      Calendar cal = new GregorianCalendar();
      cal.set(2004,1,1);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      IPSQueryNode n1 = new PSQueryNodeComparison(
            new PSQueryNodeIdentifier("rx:sys_contentstartdate"),
            new PSQueryNodeValue(cal.getTime()), Op.GT);
      IPSQueryNode n2 = new PSQueryNodeComparison(
            new PSQueryNodeIdentifier("rx:description"),
            new PSQueryNodeValue("%guidelines%"), Op.LIKE);
      IPSQueryNode n3 = new PSQueryNodeConjunction(n1, n2, Op.AND);
      String sval = n3.toString();
      String zone = TimeZone.getDefault().getDisplayName(false,0);
      String test = "qn-conjunction(qn-compare(id(rx:sys_contentstartdate),GT,Sun Feb 01 00:00:00 "+zone+" 2004),AND,qn-compare(id(rx:description),LIKE,%guidelines%))";
      assertEquals(test, sval);
   }
}
