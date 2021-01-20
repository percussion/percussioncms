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

public class MeasurementTest
{

    @Test
    public void testMeasurementPositive() throws CADFException, IOException
    {
        String metricId = Identifier.generateUniqueId();
        Metric metric1 = new Metric(metricId, "size", "MB");
        Measurement measurement = new Measurement("FileData", metric1, null);
        assertEquals(true, measurement.isValid());
    }

    @Test
    public void testMeasurementNegative() throws CADFException, IOException
    {
        String metricId = Identifier.generateUniqueId();
        Metric metric1 = new Metric(metricId, "size", "MB");
        Measurement measurement = new Measurement(null, metric1, null);
        assertEquals(false, measurement.isValid());
    }
}
