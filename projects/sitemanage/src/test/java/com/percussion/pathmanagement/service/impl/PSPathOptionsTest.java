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
package com.percussion.pathmanagement.service.impl;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author JaySeletz
 *
 */
public class PSPathOptionsTest
{

    @Test
    public void testChildTypesSetting() throws Exception
    {
        assertFalse(PSPathOptions.shouldCheckChildTypes());
        
        PSPathOptions.setShouldCheckChildTypes(true);
        assertTrue(PSPathOptions.shouldCheckChildTypes());
        
        PSPathOptions.setShouldCheckChildTypes(false);
        assertFalse(PSPathOptions.shouldCheckChildTypes());
        
        final CountDownLatch latch = new CountDownLatch(1);
        
        final AtomicBoolean otherPass = new AtomicBoolean(false);
        
        Runnable other = new Runnable()
        {
            
            @Override
            public void run()
            {
                if (PSPathOptions.shouldCheckChildTypes())
                    return;
                
                PSPathOptions.setShouldCheckChildTypes(true);
                otherPass.set(PSPathOptions.shouldCheckChildTypes());
                
                try
                {
                    latch.countDown();
                    if (!latch.await(10, TimeUnit.SECONDS))
                    {
                        otherPass.set(false);
                    }
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
        };
        
        Thread thread = new Thread(other);
        thread.setDaemon(true);
        thread.start();
        
        if (!latch.await(10, TimeUnit.SECONDS))
        {
            fail("latch timed out");
        }
        
        assertTrue(otherPass.get());
        assertFalse(PSPathOptions.shouldCheckChildTypes());
    }

}
