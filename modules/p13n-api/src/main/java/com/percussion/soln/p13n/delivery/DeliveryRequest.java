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
