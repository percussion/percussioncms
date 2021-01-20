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
