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

import com.percussion.pagemanagement.data.PSSEOStatistics.SEO_SEVERITY;
import com.percussion.pathmanagement.data.PSItemByWfStateRequest;

import javax.xml.bind.annotation.XmlRootElement;

import net.sf.oval.constraint.NotNull;

/**
 * This class is posted to the rest service as part of a request to find non-seo pages by path, workflow, and workflow
 * state.  A keyword may also be specified to further refine the seo validations. 
 * 
 * @author peterfrontiero
 */
@XmlRootElement(name = "NonSEOPagesRequest")
public class PSNonSEOPagesRequest extends PSItemByWfStateRequest
{
    /**
     * @return the severity for which all pages will be requested, never <code>null</code>.
     */
    public SEO_SEVERITY getSeverity()
    {
        return severity;
    }

    /**
     * @param severity for which all pages will be requested.
     */
    public void setSeverity(SEO_SEVERITY severity)
    {
        this.severity = severity;
    }

    /**
     * @return the keyword which will be searched for as part of the request.  May consist of one word or a set of
     * words.  May be <code>null</code> or empty.
     */
    public String getKeyword()
    {
        return keyword;
    }

    /**
     * @param keyword the keyword which will be searched for as part of the request.
     */
    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }
    
    /**
     * See {@link #getSeverity()}.
     */
    @NotNull
    private SEO_SEVERITY severity;
    
    /**
     * See {@link #getKeyword()}.
     */
    private String keyword;
   
}
