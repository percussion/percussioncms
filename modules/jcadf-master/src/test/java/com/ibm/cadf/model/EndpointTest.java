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
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.MessageFormat;

import org.junit.Test;

import com.ibm.cadf.Messages;
import com.ibm.cadf.exception.CADFException;

public class EndpointTest
{

    @Test
    public void testEndpointPositive() throws CADFException, IOException
    {
        EndPoint endPoint = new EndPoint("http://http://192.168.0.1");
        endPoint.isValid();
        assertEquals(true, endPoint.isValid());
    }

    @Test
    public void testEndpointNegative() throws CADFException, IOException
    {

        try
        {
            EndPoint endPoint = new EndPoint(null);
            endPoint.isValid();
            fail("Endpoint object creation should fail as mandatory field url is not passed");
        }
        catch (CADFException ex)
        {
            String message = MessageFormat.format(Messages.MISSING_MANDATORY_FIELDS, "url");
            assertEquals(message, ex.getMessage());
        }
    }
}
