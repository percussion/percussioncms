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

        Map<String, Integer> tagsMap = new HashMap<>();
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

            List<PSPair<String, Integer>> tagResultList = new ArrayList<>();
            for (Entry<String, Integer> tagEntry : tagsMap.entrySet())
            {
                tagResultList.add(new PSPair<>(tagEntry.getKey(), tagEntry.getValue()));
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
        List<ArrayList<String>> arrayPages = new ArrayList<>();

        for (int j = 0; j < results.size(); j++)
        {
            ArrayList<String> array = new ArrayList<>();
            arrayPages.add(array);
        }

        return arrayPages;
    }
}
