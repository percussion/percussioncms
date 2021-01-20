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
package com.percussion.pagemanagement.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotNull;

/**
 * 
 * Region code is everything but the defining
 * region HTML elements (usually a div with a special class attribute).
 * Thus the regions start tag and end tag are not included in 
 * {@link #getTemplateCode()}.
 * <p>
 * Region code can be a mixture of template language (velocity), 
 * HTML fragments or just plain text. 
 * 
 * @author adamgent
 *
 */
@XmlRootElement
public class PSRegionCode extends PSRegionNode
{


    private static final long serialVersionUID = 1L;

    @NotNull
    private String templateCode;
    

    @Override
    public void accept(IPSRegionNodeVisitor visitor)
    {
        visitor.visit(this);
    }


    @XmlElement
    public String getTemplateCode()
    {
        return templateCode;
    }

    public void setTemplateCode(String code)
    {
        this.templateCode = code;
    }
    
    
    

    
}
