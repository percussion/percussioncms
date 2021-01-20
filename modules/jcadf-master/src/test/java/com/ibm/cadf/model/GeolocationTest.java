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

public class GeolocationTest
{

    @Test
    public void testCredentialPositive() throws CADFException, IOException
    {
        Geolocation geolocation = new Geolocation(Identifier.generateUniqueId(),
                        "34N 40' 50.12", "34S 40' 50.12", "0", "1", "toronto", "ontario", "ca");
        assertEquals(true, geolocation.isValid());
    }
}
