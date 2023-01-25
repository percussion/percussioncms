/*
 * Copyright 1999-2023 Percussion Software, Inc.
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
package com.percussion.sitemanage.service.impl;

import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublishingJobStatusCallback;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * @author JaySeletz
 *
 */
public class PSPublishingJobStatusCallbackWrapper implements IPSPublishingJobStatusCallback
{
    private List<IPSPublishingJobStatusCallback> callBacks = new ArrayList<>();
    
    @Override
    public void notifyStatus(IPSPublisherJobStatus status)
    {
        for (IPSPublishingJobStatusCallback callBack : callBacks)
        {
            callBack.notifyStatus(status);
        }
    }

    public void addCallBack(IPSPublishingJobStatusCallback callBack)
    {
        Validate.notNull(callBack);
        callBacks.add(callBack);
    }
}
