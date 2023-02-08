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

package com.percussion.soln.p13n.delivery.ds;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.p13n.delivery.DeliveryException;
import com.percussion.soln.p13n.delivery.DeliveryRequest;
import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.delivery.data.IDeliveryDataService;
import com.percussion.soln.p13n.delivery.impl.AbstractDeliverySnippetFilterContextFactory;

public class DSDeliveryContextFactory extends AbstractDeliverySnippetFilterContextFactory {
    
    private IDeliveryDataService deliveryDataService;
    private boolean allowListItemInRequest;
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(DSDeliveryContextFactory.class);
    
    public IDeliveryDataService getDeliveryDataService() {
        return deliveryDataService;
    }

    public void setDeliveryDataService(IDeliveryDataService deliveryDataService) {
        this.deliveryDataService = deliveryDataService;
    }

    @Override
    public DeliveryListItem getListItem(DeliveryRequest request)
            throws DeliveryException {
        DeliveryListItem listItem;

        if (request.getListItem() != null && isAllowListItemInRequest()) {
            log.debug("Using List Item in request");
            listItem = request.getListItem();
        } 
        else {
            List<DeliveryListItem> data = getDeliveryDataService().getListItems(Collections.singletonList(request.getListItemId()));
            listItem = data.get(0);
        }
        return listItem;
    }

    
    public boolean isAllowListItemInRequest() {
        return allowListItemInRequest;
    }

    
    public void setAllowListItemInRequest(boolean listItemInRequestAllowed) {
        this.allowListItemInRequest = listItemInRequestAllowed;
    }


}
