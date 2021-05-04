/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
