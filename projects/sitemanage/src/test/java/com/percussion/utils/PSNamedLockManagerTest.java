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
package com.percussion.utils;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author JaySeletz
 *
 */
public class PSNamedLockManagerTest
{

    @Test
    public void testSingleThread()
    {
        PSNamedLockManager mgr = new PSNamedLockManager(5000);
        String name1 = "name1";
        String name2 = "name2";
        assertFalse(mgr.isLocked(name1));
        assertFalse(mgr.haveLock(name1));
        assertFalse(mgr.isLocked(name2));
        assertFalse(mgr.haveLock(name2));
        
        assertTrue(mgr.getLock(name1));
        assertTrue(mgr.isLocked(name1));
        assertTrue(mgr.haveLock(name1));
        assertFalse(mgr.isLocked(name2));
        assertFalse(mgr.haveLock(name2));
        
        assertTrue(mgr.getLock(name2));
        assertTrue(mgr.isLocked(name2));
        assertTrue(mgr.haveLock(name2));
        assertTrue(mgr.isLocked(name1));
        assertTrue(mgr.haveLock(name1));
        
        mgr.releaseLock(name1);
        assertFalse(mgr.isLocked(name1));
        assertFalse(mgr.haveLock(name1));
        assertTrue(mgr.isLocked(name2));
        assertTrue(mgr.haveLock(name2));
        
        mgr.releaseLock(name2);
        assertFalse(mgr.isLocked(name1));
        assertFalse(mgr.haveLock(name1));
        assertFalse(mgr.isLocked(name2));
        assertFalse(mgr.haveLock(name2));
        
        assertFalse(mgr.releaseLock(name1));

    }

    @Ignore
    public void testMultipleThreads() throws Exception
    {
        PSNamedLockManager mgr = new PSNamedLockManager(5);
        String name1 = "name1";
        
        Locker locker1 = new Locker("locker1", mgr);
        Locker locker2 = new Locker("locker2", mgr);
        
        locker1.start();
        locker2.start();
        
        locker1.setLock(name1);
        Thread.sleep(20);
        assertTrue(mgr.isLocked(name1));
        locker2.setLock(name1);
        Thread.sleep(20);
        
        assertTrue(locker1.didLock(name1));
        assertFalse(locker2.didLock(name1));
        
        locker1.releaseLock(name1);
        Thread.sleep(20);
        assertFalse(mgr.isLocked(name1));
        
        locker2.setLock(name1);
        Thread.sleep(20);
        assertTrue(mgr.isLocked(name1));
        assertTrue(locker2.didLock(name1));
        locker2.releaseLock(name1);
        Thread.sleep(20);
        assertFalse(mgr.isLocked(name1));
    }
    
    private class Locker extends Thread
    {
        String lockName = null;
        String lastLock = null;
        String releaseLock = null;
        PSNamedLockManager lockMgr;
        
        public Locker(String name, PSNamedLockManager lockMgr)
        {
            super(name);
            setDaemon(true);
            this.lockMgr = lockMgr;
        }

        @Override
        public void run()
        {
            while(true)
            {
                if (lockName != null)
                {
                    boolean didLock = lockMgr.getLock(lockName);
                    if (didLock && lockMgr.haveLock(lockName))
                        lastLock = lockName;
                    lockName = null;
                }
                
                if (releaseLock != null)
                {
                    lockMgr.releaseLock(releaseLock);
                    releaseLock = null;
                }
                
                try
                {
                    sleep(5);
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            }
        }
        
        public void setLock(String name)
        {
            lockName = name;
        }
        
        public boolean didLock(String name)
        {
            boolean didLock = name.equals(lastLock);
            if (didLock)
                lastLock = null;
            
            return didLock;
        }
        
        public void releaseLock(String name)
        {
            String last = releaseLock;
            if (last != null)
                throw new IllegalStateException("Still waiting to release lock: " + last);
            
            releaseLock = name;
        }
    }
}
