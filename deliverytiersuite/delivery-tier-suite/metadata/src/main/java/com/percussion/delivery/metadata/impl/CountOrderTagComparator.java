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
package com.percussion.delivery.metadata.impl;

import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author davidpardini
 * 
 */
public class CountOrderTagComparator implements Comparator<JSONObject>
{
    public int compare(JSONObject o1, JSONObject o2)
    {
        JSONObject ob1 = o1;
        JSONObject ob2 = o2;

        int returnCompare = 0;
        try
        {
            int countOb1 = ((Integer) ob1.get(PSMetadataTagsHelper.TAG_COUNT)).intValue();
            int countOb2 = ((Integer) ob2.get(PSMetadataTagsHelper.TAG_COUNT)).intValue();
            if (countOb1 > countOb2)
            {
                returnCompare = -1;
            }
            else if (countOb1 < countOb2)
            {
                returnCompare = 1;
            }
            else
            {
                returnCompare = 0;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return returnCompare;
    }
}