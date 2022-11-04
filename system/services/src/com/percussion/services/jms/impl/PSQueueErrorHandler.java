/*
 * Copyright 1999-2022 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.services.jms.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.util.PSBaseBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

@Service
@PSBaseBean("sys_queueErrorHandler")
public class PSQueueErrorHandler implements ErrorHandler {

    private Logger log = LogManager.getLogger(IPSConstants.PUBLISHING_LOG);
    private List<IPSNotificationListener> listeners = new ArrayList<>();

    public List<IPSNotificationListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<IPSNotificationListener> listeners) {
        this.listeners = listeners;
    }

    public void addListener(IPSNotificationListener l){
       if(!listeners.contains(l))
           listeners.add(l);
    }

    @Override
    public void handleError(Throwable t) {
        for(IPSNotificationListener l : listeners){
           //TODO: Notify
            try {
                l.notifyEvent(new PSNotificationEvent(
                        PSNotificationEvent.EventType.JMS_ERROR,
                        t));
            } catch (PSNotFoundException | PSDataServiceException e) {
                log.error("An unexpected JMS error happened. Error: {}",
                        PSExceptionUtils.getMessageForLog(e));
            }
        }
    }
}
