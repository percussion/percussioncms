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

package com.ibm.cadf;

import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.model.CADFType;
import com.ibm.cadf.model.CADFType.EVENTTYPE;
import com.ibm.cadf.model.Event;
import com.ibm.cadf.model.Resource;

public class EventFactory
{

    public static String ERROR_UNKNOWN_EVENTTYPE = "Unknown CADF EventType requested on factory method";

    public static Event getEventInstance(String eventType, String id, String action, String outcome,
                    Resource initiator,
                    String initiatorId, Resource target, String targetId, Resource observer,
                    String observerId) throws CADFException
    {

        if (!CADFType.isValidEventType(eventType))
            throw new CADFException(ERROR_UNKNOWN_EVENTTYPE);

        EVENTTYPE eventTypeEnum = CADFType.EVENTTYPE.valueOf(eventType);
        return new Event(eventTypeEnum.value, id, action, outcome, initiator,
                        initiatorId, target, targetId, observer,
                        observerId);
    }
}
