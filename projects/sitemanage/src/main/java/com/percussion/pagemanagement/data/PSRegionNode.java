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
package com.percussion.pagemanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.Objects;

/**
 * Represents a node in the region tree.
 * Currently a node is either a region or template code.
 * @author adamgent
 *
 */
public abstract class PSRegionNode extends PSAbstractDataObject
{
    
    
    private static final long serialVersionUID = 1L;
    
    private PSRegionOwnerType ownerType;
    
    
    /**
     * Visitor pattern to avoid casting.
     * @param visitor visitor.
     */
    public abstract void accept(IPSRegionNodeVisitor visitor);

    /*
     * TODO Not sure if this is needed really.
     * Ideally merged region should extend this class but it does not.
     */
    public PSRegionOwnerType getOwnerType()
    {
        return ownerType;
    }

    public void setOwnerType(PSRegionOwnerType ownerType)
    {
        this.ownerType = ownerType;
    }

    
    public static enum PSRegionOwnerType {
        PAGE, TEMPLATE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSRegionNode)) return false;
        PSRegionNode that = (PSRegionNode) o;
        return getOwnerType() == that.getOwnerType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOwnerType());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSRegionNode{");
        sb.append("ownerType=").append(ownerType);
        sb.append('}');
        return sb.toString();
    }
}
