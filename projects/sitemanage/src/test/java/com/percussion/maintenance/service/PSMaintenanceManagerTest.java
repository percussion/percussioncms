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
package com.percussion.maintenance.service;

import static org.junit.Assert.*;

import com.percussion.maintenance.service.impl.PSMaintenanceManager;

import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSMaintenanceManagerTest
{
    
    
    @Test
    public void test()
    {
        IPSMaintenanceManager maintenanceManager = new PSMaintenanceManager();
        
        IPSMaintenanceProcess process = new PSMockMaintenanceProcess("Proc1");
        maintenanceManager.startingWork(process);
        assertTrue(maintenanceManager.isWorkInProgress());
        assertFalse(maintenanceManager.hasFailures());
        
        maintenanceManager.workCompleted(process);
        assertFalse(maintenanceManager.isWorkInProgress());
        assertFalse(maintenanceManager.hasFailures());
        
        maintenanceManager.startingWork(process);
        assertTrue(maintenanceManager.isWorkInProgress());
        maintenanceManager.workFailed(process);
        assertFalse(maintenanceManager.isWorkInProgress());
        assertTrue(maintenanceManager.hasFailures());
        maintenanceManager.clearFailures();
        assertFalse(maintenanceManager.hasFailures());
        
        maintenanceManager = new PSMaintenanceManager();
        
        IPSMaintenanceProcess proc1 = new PSMockMaintenanceProcess("Proc1");
        maintenanceManager.startingWork(proc1);
        IPSMaintenanceProcess proc2 = new PSMockMaintenanceProcess("Proc2");
        maintenanceManager.startingWork(proc2);
        assertTrue(maintenanceManager.isWorkInProgress());
        assertFalse(maintenanceManager.hasFailures());
        maintenanceManager.workCompleted(proc2);
        assertTrue(maintenanceManager.isWorkInProgress());
        maintenanceManager.workCompleted(proc1);
        assertFalse(maintenanceManager.isWorkInProgress());
        assertFalse(maintenanceManager.hasFailures());
        
        maintenanceManager.startingWork(proc1);
        maintenanceManager.startingWork(proc2);
        assertTrue(maintenanceManager.isWorkInProgress());
        assertFalse(maintenanceManager.hasFailures());
        
        maintenanceManager.workFailed(proc1);
        assertTrue(maintenanceManager.isWorkInProgress());
        assertTrue(maintenanceManager.hasFailures());
        maintenanceManager.workCompleted(proc2);
        assertFalse(maintenanceManager.isWorkInProgress());
        assertTrue(maintenanceManager.hasFailures());
        maintenanceManager.clearFailures();
        assertFalse(maintenanceManager.hasFailures());
        
    }
}
