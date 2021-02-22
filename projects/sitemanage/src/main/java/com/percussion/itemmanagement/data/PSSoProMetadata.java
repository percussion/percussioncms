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

package com.percussion.itemmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

@XmlRootElement(name="SoProMetadata")
public class PSSoProMetadata extends PSAbstractDataObject
{
    @NotNull
    @NotEmpty
    private String itemId;
    private String metadata;
    
    public PSSoProMetadata() { }
    
    public PSSoProMetadata(String itemId, String metadata) {
        this.itemId = itemId;
        this.metadata = metadata;
    }
    
    /**
     * @return itemId - content id
     */
    public String getItemId()
    {
        return itemId;
    }
    
    /**
     * @param itemId - Set the content id
     */
    public void setItemId(String itemId)
    {
        this.itemId = itemId;
    }
    
    public String getMetadata()
    {
        return metadata;
    }
    
    public void setMetadata(String metadata)
    {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return this.metadata;
    }
}
