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
package com.percussion.monitor.process;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSThumbnailProcessMonitorTest
{

    @Test
    public void test()
    {
        PSThumbnailProcessMonitor mon = new PSThumbnailProcessMonitor();
        assertEquals(0, mon.getCurrentCount());
        mon.incrementCount();
        assertEquals(1, mon.getCurrentCount());
        mon.incrementCount();
        assertEquals(2, mon.getCurrentCount());
        mon.decrementCount();
        assertEquals(1, mon.getCurrentCount());
        mon.incrementCount(5);
        assertEquals(6, mon.getCurrentCount());
        mon.decrementCount(4);
        assertEquals(2, mon.getCurrentCount());
        mon.decrementCount(2);
        assertEquals(0, mon.getCurrentCount());
        mon.decrementCount();
    }

}
