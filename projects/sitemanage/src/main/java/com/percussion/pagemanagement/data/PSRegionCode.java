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
