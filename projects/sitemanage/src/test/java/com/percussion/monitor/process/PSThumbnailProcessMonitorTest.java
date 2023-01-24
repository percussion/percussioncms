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
