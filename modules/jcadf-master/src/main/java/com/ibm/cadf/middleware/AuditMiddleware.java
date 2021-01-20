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

package com.ibm.cadf.middleware;

import java.util.Properties;

import com.ibm.cadf.CADFTaxonomy;
import com.ibm.cadf.CADFTaxonomy.OUTCOME;
import com.ibm.cadf.EventFactory;
import com.ibm.cadf.auditlogger.AuditLogger;
import com.ibm.cadf.auditlogger.AuditLoggerFactory;
import com.ibm.cadf.cfg.Config;
import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.model.CADFType;
import com.ibm.cadf.model.Credential;
import com.ibm.cadf.model.EndPoint;
import com.ibm.cadf.model.Event;
import com.ibm.cadf.model.Host;
import com.ibm.cadf.model.Identifier;
import com.ibm.cadf.model.Resource;
import com.ibm.cadf.util.Constants;
import com.ibm.cadf.util.StringUtil;

public class AuditMiddleware
{

    private Config config;

    private AuditLogger auditLogger;

    public AuditMiddleware(String type)
    {
        config = Config.getInstance();
        auditLogger = AuditLoggerFactory.getAuditLogger(type);

    }

    public void setProperties(Properties properties)
    {
        config.setProperties(properties);
    }

    public void setOutputFilePath(String filePath)
    {
        auditLogger.setOutputFilePath(filePath);
    }

    public void audit(Event event) throws CADFException
    {

        auditLogger.audit(event);
    }

    public Event createEvent(String action, String status, com.ibm.cadf.middleware.AuditContext ctx) throws CADFException
    {
        String actionVal = config.getProperty(action);
        if (StringUtil.isEmpty(actionVal))
        {
            actionVal = CADFTaxonomy.UNKNOWN;
        }

        // Constructing the initiator resource - it should be logged user into the storage platform
        String initiatorId = ctx.getTargetUsername();
        String initiatorTypeURI = config.getProperty(Constants.INITIATOR_TYPE_URI);
        Resource initiator = new Resource(initiatorId);
        initiator.setTypeURI(initiatorTypeURI);
        // Get the storage platform logged in username
        initiator.setName(ctx.getIniatorName());
        Host host = new Host();
        host.setAddress(ctx.getInitiatorIP());
        host.setAgent(ctx.getAgentName());
        initiator.setHost(host);

        // Constructing the target resource
        String targetTypeURI = config.getProperty(Constants.TARGET_TYPE_URI);
        String targetId = Identifier.generateUniqueId();
        Resource target = new Resource(targetId);
        target.setTypeURI(targetTypeURI);
        target.setName(ctx.getTargetName());

        if (!StringUtil.isEmpty(ctx.getPath()))
        {
            target.setId(ctx.getPath());
        }
        if (!StringUtil.isEmpty(ctx.getTargetUsername()))
        {
            // Set credentials
            Credential credential = new Credential(ctx.getTargetUsername());
            target.setCredential(credential);
        }

        // Set addresses
        if (!StringUtil.isEmpty(ctx.getTargetUrl()))
        {
            EndPoint endpoint = new EndPoint(ctx.getTargetUrl());
            endpoint.setName(ctx.getTargetEndpointName());
            target.addAddress(endpoint);
        }

        // Constructing the observer resource.
        String objserverTypeURI = config.getProperty(Constants.OBSERVER_TYPE_URI);
        String observerId = Identifier.generateUniqueId();
        String observername = ctx.getObserverName();
        Resource observer = new Resource(observerId);
        observer.setTypeURI(objserverTypeURI);
        observer.setName(observername);

        // Create an event
        // The default outcome is success
        String outcome = CADFTaxonomy.OUTCOME.SUCCESS.name();
        try
        {
            OUTCOME outcomeEnum = CADFTaxonomy.OUTCOME.valueOf(status);
            outcome = outcomeEnum.value;

        }
        catch (IllegalArgumentException e)
        {
            // If there is no valid status set unknown
            outcome = CADFTaxonomy.OUTCOME.UNKNOWN.value;
        }
String activityType=CADFType.EVENTTYPE.EVENTTYPE_ACTIVITY.name();
        if(ctx.getActivity()!=null && ctx.getActivity().equalsIgnoreCase(CADFType.EVENTTYPE.EVENTTYPE_REVOKE.value)){
            activityType= CADFType.EVENTTYPE.EVENTTYPE_REVOKE.name();
        }

        Event event = EventFactory.getEventInstance(activityType,
                                                    Identifier.generateUniqueId(), actionVal, outcome,
                                                    initiator, null, target, null, observer, null);

        return event;

    }

}
