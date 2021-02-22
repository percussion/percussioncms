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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.util.TimeStampUtils;

public class Event extends CADFType
{
    private static final long serialVersionUID = 1L;

    private static final String TYPE_URI_EVENT = CADFType.CADF_VERSION_1_0_0 + "event";

    private String typeURI;

    private String eventType;

    private String id;

    private String eventTime;

    private String action;

    private String outcome;

    private Resource initiator;

    private String initiatorId;

    private Resource target;

    private String targetId;

    private String severity;

    private Reason reason;

    private Resource observer;

    private String observerId;

    private List<Reporterstep> reportersteps;

    private List<Measurement> measurements;

    private List<String> tags;

    private List<Attachment> attachments;

    // Valid cadf:Event record "types"
    public enum EVENT_KEYNAME
    {

        // Event.eventType
        EVENT_KEYNAME_TYPEURI("typeURI"),
        EVENT_KEYNAME_EVENTTYPE("eventType"),
        EVENT_KEYNAME_ID("id"),
        EVENT_KEYNAME_EVENTTIME("eventTime"),
        EVENT_KEYNAME_INITIATOR("initiator"),
        EVENT_KEYNAME_INITIATORID("initiatorId"),
        EVENT_KEYNAME_ACTION("action"),
        EVENT_KEYNAME_TARGET("target"),
        EVENT_KEYNAME_TARGETID("targetId"),
        EVENT_KEYNAME_OUTCOME("outcome"),
        EVENT_KEYNAME_REASON("reason"),
        EVENT_KEYNAME_SEVERITY("severity"),
        EVENT_KEYNAME_MEASUREMENTS("measurements"),
        EVENT_KEYNAME_TAGS("tags"),
        EVENT_KEYNAME_ATTACHMENTS("attachments"),
        EVENT_KEYNAME_OBSERVER("observer"),
        EVENT_KEYNAME_OBSERVERID("observerId"),
        EVENT_KEYNAME_REPORTERCHAIN("reporterchain");

        String value;

        private EVENT_KEYNAME(String value)
        {
            this.value = value;
        }
    }

    public Event()
    {

    }

    public Event(String eventType, String id, String action, String outcome, Resource initiator,
                    String initiatorId, Resource target, String targetId, Resource observer, String observerId)
                    throws CADFException
    {
        super();
        this.typeURI = TYPE_URI_EVENT;
        this.eventType = eventType;
        this.id = id;
        this.action = action;
        this.outcome = outcome;
        this.initiator = initiator;
        this.initiatorId = initiatorId;
        this.target = target;
        this.targetId = targetId;
        this.observer = observer;
        this.observerId = observerId;
        this.eventTime = TimeStampUtils.getCurrentTime();
    }

    public void addReporterstep(Reporterstep reporterstep)
    {
        if (reportersteps == null)
        {
            reportersteps = new ArrayList<>();
        }
        reportersteps.add(reporterstep);
    }

    public void addTag(String name, String value) {
        if (this.tags == null) {
            this.tags = new ArrayList();
        }

        String tag = this.generate_name_value_tag(name, value);
        if (this.isValidTag(tag)) {
            this.tags.add(tag);
        }

    }
    public boolean isValidTag(String value) {
        return StringUtils.isNotEmpty(value);
    }

    private String generate_name_value_tag(String name, String value) throws CADFException {
        if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(value)) {
            String tag = name + "?value=" + value;
            return tag;
        } else {
            throw new CADFException("'Invalid name and/or value. Values cannot be Empty or Null");
        }
    }


    public void addMeasurement(Measurement measurement)
    {
        if (measurements == null)
        {
            measurements = new ArrayList<>();
        }

        measurements.add(measurement);
    }

    public List<Measurement> getMeasurements()
    {
        return measurements;
    }

    public void addAttachment(Attachment attachment)
    {
        if (attachments == null)
        {
            attachments = new ArrayList<>();
        }
        attachments.add(attachment);
    }

    public String getEventType()
    {
        return eventType;
    }

    public void setEventType(String eventType)
    {
        this.eventType = eventType;
    }

    public String getTypeURI()
    {
        return typeURI;
    }

    public void setTypeURI(String typeURI)
    {
        this.typeURI = typeURI;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getEventTime()
    {
        return eventTime;
    }

    public void setEventTime(String eventTime)
    {
        this.eventTime = eventTime;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public String getOutcome()
    {
        return outcome;
    }

    public void setOutcome(String outcome)
    {
        this.outcome = outcome;
    }

    public Resource getInitiator()
    {
        return initiator;
    }

    public void setInitiator(Resource initiator)
    {
        this.initiator = initiator;
    }

    public String getInitiatorId()
    {
        return initiatorId;
    }

    public void setInitiatorId(String initiatorId)
    {
        this.initiatorId = initiatorId;
    }

    public Resource getTarget()
    {
        return target;
    }

    public void setTarget(Resource target)
    {
        this.target = target;
    }

    public String getTargetId()
    {
        return targetId;
    }

    public void setTargetId(String targetId)
    {
        this.targetId = targetId;
    }

    public String getSeverity()
    {
        return severity;
    }

    public void setSeverity(String severity)
    {
        this.severity = severity;
    }

    public Reason getReason()
    {
        return reason;
    }

    public void setReason(Reason reason)
    {
        this.reason = reason;
    }

    public Resource getObserver()
    {
        return observer;
    }

    public void setObserver(Resource observer)
    {
        this.observer = observer;
    }

    public String getObserverId()
    {
        return observerId;
    }

    public void setObserverId(String observerId)
    {
        this.observerId = observerId;
    }

    @Override
    public boolean isValid()
    {
        return StringUtils.isNotEmpty(this.typeURI) && StringUtils.isNotEmpty(this.eventType)
               && StringUtils.isNotEmpty(this.id)
               && StringUtils.isNotEmpty(this.eventTime) && StringUtils.isNotEmpty(this.action)
               && StringUtils.isNotEmpty(this.outcome)
               && (this.initiator != null || StringUtils.isNotEmpty(this.initiatorId))
               && (this.target != null || StringUtils.isNotEmpty(this.targetId))
               && (this.observer != null || StringUtils.isNotEmpty(this.observerId));

    }

}
