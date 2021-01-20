/*
 * Copyright 2016 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.cadf.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.util.TimeStampUtils;

public class ReporterstepTest
{

    @Test
    public void testReporterstepPositive() throws CADFException, IOException
    {

        Resource reporter = new Resource(Identifier.generateUniqueId());
        reporter.setTypeURI("storage");
        reporter.setName("storageadmin");
        Reporterstep reporterstep = new Reporterstep("modifier", reporter, Identifier.generateUniqueId(),
                        TimeStampUtils.getCurrentTime());
        assertEquals(true, reporterstep.isValid());
    }

    @Test
    public void testReporterstepNegative() throws CADFException, IOException
    {
        Resource reporter = new Resource(Identifier.generateUniqueId());
        reporter.setTypeURI("storage");
        reporter.setName("storageadmin");
        Reporterstep reporterstep = new Reporterstep("role1", reporter, Identifier.generateUniqueId(),
                        TimeStampUtils.getCurrentTime());
        // passed invalid role while creating reporterstep object, validation should fail
        assertEquals(false, reporterstep.isValid());
    }
}
