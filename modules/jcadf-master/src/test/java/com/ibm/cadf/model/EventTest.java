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

import com.ibm.cadf.EventFactory;
import com.ibm.cadf.exception.CADFException;

public class EventTest
{

    @Test
    public void testEventPositive() throws CADFException, IOException
    {

        String initiatorId = Identifier.generateUniqueId();
        Resource initiator = new Resource(initiatorId);
        initiator.setTypeURI("/testcase");
        initiator.setName("AuditLoggerTest");

        String targetId = Identifier.generateUniqueId();
        Resource target = new Resource(targetId);
        target.setTypeURI("/configurator");
        target.setName("Configuration Component");

        String observerId = Identifier.generateUniqueId();
        Resource observer = new Resource(observerId);
        observer.setTypeURI("/management");
        observer.setName("Management Component");

        Event event = EventFactory.getEventInstance(CADFType.EVENTTYPE.EVENTTYPE_ACTIVITY.name(),
                                                    Identifier.generateUniqueId(), "Send File", "successful",
                                                    initiator, null, target, null, observer, null);
        assertEquals(true, event.isValid());
    }

    @Test
    public void testEventNegative() throws CADFException, IOException
    {

        String initiatorId = Identifier.generateUniqueId();
        Resource initiator = new Resource(initiatorId);
        initiator.setTypeURI("/testcase");
        initiator.setName("AuditLoggerTest");

        String targetId = Identifier.generateUniqueId();
        Resource target = new Resource(targetId);
        target.setTypeURI("/configurator");
        target.setName("Configuration Component");

        String observerId = Identifier.generateUniqueId();
        Resource observer = new Resource(observerId);
        observer.setTypeURI("/management");
        observer.setName("Management Component");

        Event event = EventFactory.getEventInstance(CADFType.EVENTTYPE.EVENTTYPE_ACTIVITY.name(), "", "Send File", "successful",
                                                    initiator, null, target, null, observer, null);
        event.isValid();
        assertEquals(false, event.isValid());
    }
}
