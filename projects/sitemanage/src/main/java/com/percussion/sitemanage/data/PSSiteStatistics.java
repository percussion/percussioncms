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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.sitemanage.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "statistics")
public class PSSiteStatistics extends PSAbstractDataObject
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private long binary;

    private long css;

    private long linksInternal;

    private long pages;

    private long templates;

    public long getBinary()
    {
        return binary;
    }

    public void setBinary(long binary)
    {
        this.binary = binary;
    }

    public long getCss()
    {
        return css;
    }

    public void setCss(long css)
    {
        this.css = css;
    }

    public long getLinksInternal()
    {
        return linksInternal;
    }

    public void setLinksInternal(long linksInternal)
    {
        this.linksInternal = linksInternal;
    }

    public long getPages()
    {
        return pages;
    }

    public void setPages(long pages)
    {
        this.pages = pages;
    }

    public long getTemplates()
    {
        return templates;
    }

    public void setTemplates(long templates)
    {
        this.templates = templates;
    }
}
