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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import com.percussion.delivery.metadata.IPSMetadataEntry;
import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.impl.utils.PSPair;
import com.percussion.delivery.metadata.rdbms.impl.PSDbMetadataEntry;

/**
 * This class is responsible for process the tags list metadata and return
 * return the JSONObject with the list properties with tags and their
 * occurrences.
 * 
 * @author davidpardini
 * 
 */
public class PSMetadataTagsHelper
{

    public static final String REFERENCES = "perc:tags";

    public static final String TAG_NAME = "tagName";

    public static final String TAG_COUNT = "tagCount";

    public static final String PROPERTIES = "properties";

    public static final String COUNT_SORT = "count";

    /**
     * This method is responsible for return the list with tags and their
     * occurrences. First iterate by page and later by PropertyPage.
     * 
     * @param results assumed not <code>null</code>.
     * @param sortOrder
     * @return List<PSPair<String, Integer>>
     * @throws ServletException
     */
    public List<PSPair<String, Integer>> processTags(List<IPSMetadataEntry> results, String sortOrder)
            throws ServletException
    {
        // Initialize array used for unduplicated tags
        List<ArrayList<String>> arrayPages = inicializeArray(results);

        Map<String, Integer> tagsMap = new HashMap<String, Integer>();
        try
        {
            int i = 0;
            for (IPSMetadataEntry entryPage : results)
            {
                for (IPSMetadataProperty prop : entryPage.getProperties())
                {
                    if (REFERENCES.equals(prop.getName()) && !prop.getStringvalue().isEmpty())
                    {
                        countTags(tagsMap, prop.getStringvalue().trim(), arrayPages.get(i));
                    }
                }
                i++;
            }

            List<PSPair<String, Integer>> tagResultList = new ArrayList<PSPair<String, Integer>>();
            for (Entry<String, Integer> tagEntry : tagsMap.entrySet())
            {
                tagResultList.add(new PSPair<String, Integer>(tagEntry.getKey(), tagEntry.getValue()));
            }

            // SORT BY ..
            Comparator<PSPair<String, Integer>> comp = new AlphaOrderTagComparator();
            if (COUNT_SORT.equalsIgnoreCase(sortOrder))
            {
                comp = new CountOrderTagComparator();
            }
            Collections.sort(tagResultList, comp);

            return tagResultList;
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    class AlphaOrderTagComparator implements Comparator<PSPair<String, Integer>>
    {
        public int compare(PSPair<String, Integer> o1, PSPair<String, Integer> o2)
        {
            return o1.getFirst().compareTo(o2.getFirst());
        }
    }

    class CountOrderTagComparator implements Comparator<PSPair<String, Integer>>
    {
        public int compare(PSPair<String, Integer> o1, PSPair<String, Integer> o2)
        {
            return o1.getSecond().equals(o2.getSecond()) ? o1.getFirst().compareTo(o2.getFirst()) : o2.getSecond()
                    .compareTo(o1.getSecond());
        }
    }

    /**
     * This method is responsible for return the maps with the tags and their
     * occurrences. First split the stringValue parameter with the tags and add
     * the maps, if the tags already adds 1 to its respective value.
     * 
     * @param tagsMap assumed not <code>null</code>.
     * @param stringvalue assumed not <code>null</code>.
     * @param arrayList assumed not <code>null</code>.
     */
    private void countTags(Map<String, Integer> tagsMap, String stringvalue, ArrayList<String> arrayList)
    {
        String tag = stringvalue.trim().toLowerCase();
        try
        {
            if (tagsMap.containsKey(tag))
            {
                if (!arrayList.contains(tag))
                {
                    int count = tagsMap.get(tag).intValue();
                    count++;
                    tagsMap.put(tag, new Integer(count));
                }
            }
            else
            {
                tagsMap.put(tag, new Integer(1));
                arrayList.add(tag);
            }
        }
        catch (Exception e)
        {
        }
    }

    /**
     * This method is responsible for return the List with the list of quantity
     * of pages. Returns a list of arrays that will be used not to have
     * duplicate tags.
     * 
     * @param results assumed not <code>null</code>.
     * @return List with the list of quantity of pages
     */
    private List<ArrayList<String>> inicializeArray(List<IPSMetadataEntry> results)
    {
        List<ArrayList<String>> arrayPages = new ArrayList<ArrayList<String>>();

        for (int j = 0; j < results.size(); j++)
        {
            ArrayList<String> array = new ArrayList<String>();
            arrayPages.add(array);
        }

        return arrayPages;
    }
}