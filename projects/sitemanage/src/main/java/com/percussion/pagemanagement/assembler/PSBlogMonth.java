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

public class PSBlogMonth 
{

    private String month;

    private Integer count;

    /**
     * @param month
     * @param count
     */
    public PSBlogMonth(String month, Integer count)
    {
        super();
        this.month = month;
        this.count = count;
    }

    /**
     * @return the month
     */
    public String getMonth()
    {
        return month;
    }

    /**
     * @param month the month to set
     */
    public void setMonth(String month)
    {
        this.month = month;
    }

    /**
     * @return the count
     */
    public Integer getCount()
    {
        return count;
    }

    /**
     * @param count the number of counts to set
     */
    public void setCount(Integer count)
    {
        this.count = count;
    }

}
