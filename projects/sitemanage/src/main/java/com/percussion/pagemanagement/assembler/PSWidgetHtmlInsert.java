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
