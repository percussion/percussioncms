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

package com.ibm.cadf.auditlogger;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.ibm.cadf.EventFactory;
import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.model.CADFType;
import com.ibm.cadf.model.Event;
import com.ibm.cadf.model.Identifier;
import com.ibm.cadf.model.Measurement;
import com.ibm.cadf.model.Metric;
import com.ibm.cadf.model.Resource;
import com.ibm.cadf.util.Constants;

public class JsonAuditLoggerTest
{

    @Before
    public void setUp()
    {
        System.setProperty(Constants.API_AUDIT_MAP, "/com/ibm/cadf/cfg/api_audit_map.conf");
    }

    @Test
    public void testJsonAuditing() throws CADFException, IOException
    {

        File file = new File(Constants.JSON_AUDIT_FILES_NAME);
        if (file.exists())
        {
            file.delete();
        }
        AuditLogger auditLogger = AuditLoggerFactory.getAuditLogger(Constants.AUDIT_FORMAT_TYPE_JSON);
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

        // Reason reason = new Reason("File transfer", "10101", null, null);

        Event event = EventFactory.getEventInstance(CADFType.EVENTTYPE.EVENTTYPE_ACTIVITY.name(),
                                                    Identifier.generateUniqueId(), "Send File", "successful",
                                                    initiator, null, target, null, observer, null);

        String metricId = Identifier.generateUniqueId();
        Metric metric1 = new Metric(metricId, "size", "MB");
        Measurement measurement1 = new Measurement("FileData", metric1, null);
        Measurement measurement2 = new Measurement("FileData", metric1, null);

        event.addMeasurement(measurement1);
        event.addMeasurement(measurement2);
        auditLogger.audit(event);

    }

}
