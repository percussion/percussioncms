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
package com.percussion.services.contentmgr.impl.query.nodes;

import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode.Op;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
      String test = "qn-conjunction(qn-compare(id(rx:sys_contentstartdate),GT,Sun Feb 01 00:00:00 EST 2004),AND,qn-compare(id(rx:description),LIKE,%guidelines%))";
      assertEquals(test,sval);
   }
}
