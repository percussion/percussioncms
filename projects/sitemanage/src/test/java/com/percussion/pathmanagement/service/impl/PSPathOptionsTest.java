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
