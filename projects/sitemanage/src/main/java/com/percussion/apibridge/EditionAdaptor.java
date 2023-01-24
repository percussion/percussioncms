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

package com.percussion.apibridge;

import com.percussion.rest.editions.IEditionsAdaptor;
import com.percussion.rest.editions.PublishResponse;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.publishing.IPSPublishingWs;
import org.springframework.beans.factory.annotation.Autowired;

@PSSiteManageBean
public class EditionAdaptor implements IEditionsAdaptor {

    @Autowired
    private IPSPublishingWs pubWs;

    public EditionAdaptor(){
        //Default ctor
    }

    private IPSGuid getEditionGuidFromId(String id){
        return PSGuidUtils.makeGuid(Long.parseLong(id), PSTypeEnum.EDITION);
    }

    @Override
    public PublishResponse publish(String id) {

       return loadPublishResponseFromStatus(pubWs.getPublishingJobStatus(
               pubWs.startPublishingJob(
                       getEditionGuidFromId(id),null)));
    }

    private PublishResponse loadPublishResponseFromStatus(IPSPublisherJobStatus status){
        PublishResponse ret =  new PublishResponse();

        ret.setDelivered(String.valueOf(status.countItemsDelivered()));
        ret.setFailures(String.valueOf(status.countFailedItems()));
        ret.setJobid(status.getJobId());
        ret.setStatus(status.getState().getDisplayName());
        return ret;
    }
}
