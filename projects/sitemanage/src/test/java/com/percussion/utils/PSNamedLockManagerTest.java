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
package com.percussion.utils;

import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;

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
