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

package com.percussion.soln.p13n.delivery;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.percussion.soln.p13n.delivery.data.DeliveryListItem;
import com.percussion.soln.p13n.tracking.VisitorProfile;

/**
 * Represents a request from the visitor and is safe to serialize.
 * @author adamgent
 *
 */
public class DeliveryRequest {
    
    private Long listItemId;
    private DeliveryListItem listItem;
    
    private VisitorProfile visitorProfile;
    
    public Long getListItemId() {
        if (getListItem() == null) {
            return listItemId;
        }
        return getListItem().getId();
    }

    public void setListItemId(Long listItemId) {
        this.listItemId = listItemId;
    }

    public VisitorProfile getVisitorProfile() {
        return visitorProfile;
    }

    public void setVisitorProfile(VisitorProfile visitorProfile) {
        this.visitorProfile = visitorProfile;
    }

    @Override
    public String toString() {
        return ToStringBuilder
        .reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public DeliveryListItem getListItem() {
        return listItem;
    }
    
    public void setListItem(DeliveryListItem listItem) {
        this.listItem = listItem;
    }
}
