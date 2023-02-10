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

/**
 * 
 */
package com.percussion.pagemanagement.assembler;

import net.sf.json.JSONException;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
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
     * This method is responsible for return the JSONObject with the list
     * properties with tags and their occurrences. First iterate by page and
     * later by PropertyPage.
     * 
     * @param results assumed not <code>null</code>.
     * @param sortOrder
     * @return JSONObject
     * @throws ServletException
     */
    public Map<String, Integer> processTags(List<PSMetadataEntry> results, String sortOrder) throws ServletException
    {
        // Initialize array used for unduplicated tags
        List<ArrayList<String>> arrayPages = inicializeArray(results);

        Map<String, Integer> tagsMap = new TreeMap<>();
        try
        {
            int i = 0;
            for (PSMetadataEntry entryPage : results)
            {
                for (PSMetadataProperty prop : entryPage.getProperties())
                {
                    if (REFERENCES.equals(prop.getName()) && !prop.getStringvalue().isEmpty())
                    {
                        countTags(tagsMap, prop.getStringvalue().trim(), arrayPages.get(i));
                    }
                }
                i++;
            }
            /*
             * List<JSONObject> tagObjects = new ArrayList<JSONObject>(); for
             * (Entry<String, Integer> tagEntry : tagsMap.entrySet()) {
             * JSONObject tagObject = new JSONObject(); tagObject.put(TAG_NAME,
             * tagEntry.getKey()); tagObject.put(TAG_COUNT,
             * tagEntry.getValue()); tagObjects.add(tagObject); }
             */
            // SORT BY ..
            if (COUNT_SORT.equals(sortOrder))
            {
                tagsMap = sortByCountOrder(tagsMap);
            }
            else
            {
                tagsMap = sortByAlphaOrder(tagsMap);
            }

            return tagsMap;
        }
        catch (Exception e)
        {
            throw new ServletException(e);
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
                    int count = ((Integer) tagsMap.get(tag)).intValue();
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

    private Map sortByAlphaOrder(Map<String, Integer> tagsMap) throws JSONException
    {
        List list = new LinkedList(tagsMap.entrySet());
        Collections.sort(list, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Comparable) ((Map.Entry) (o1)).getKey()).compareTo(((Map.Entry) (o2)).getKey());
            }
        });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private Map sortByCountOrder(Map<String, Integer> tagObjects) throws JSONException
    {
        List list = new LinkedList(tagObjects.entrySet());
        Collections.sort(list, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * This method is responsible for return the List with the list of quantity
     * of pages. Returns a list of arrays that will be used not to have
     * duplicate tags.
     * 
     * @param results assumed not <code>null</code>.
     * @return List with the list of quantity of pages
     */
    private List<ArrayList<String>> inicializeArray(List<PSMetadataEntry> results)
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
