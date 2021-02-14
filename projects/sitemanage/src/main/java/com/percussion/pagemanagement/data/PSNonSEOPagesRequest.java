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
