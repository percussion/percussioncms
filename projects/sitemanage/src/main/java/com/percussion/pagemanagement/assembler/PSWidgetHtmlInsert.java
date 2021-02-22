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
package com.percussion.pagemanagement.assembler;

import java.util.List;

/**
 * Html that gets inserted into the page requested by
 * the widgets.
 * @author adamgent
 *
 */
public class PSWidgetHtmlInsert
{
    private List<String> top;

    private List<String> bottom;

    private List<String> before;

    private List<String> after;

    public List<String> getTop()
    {
        return top;
    }

    public void setTop(List<String> top)
    {
        this.top = top;
    }

    public List<String> getBottom()
    {
        return bottom;
    }

    public void setBottom(List<String> bottom)
    {
        this.bottom = bottom;
    }

    public List<String> getBefore()
    {
        return before;
    }

    public void setBefore(List<String> before)
    {
        this.before = before;
    }

    public List<String> getAfter()
    {
        return after;
    }

    public void setAfter(List<String> after)
    {
        this.after = after;
    }

}