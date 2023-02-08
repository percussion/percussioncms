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
package com.percussion.share.service;

import java.util.Collection;

/**
 * Custom utility methods for {@link Collection} objects.
 */
public class PSCollectionUtils
{
    /**
     * Like {@link Collection#contains(Object)} but ignoring string case.
     * 
     * @param strings never <code>null</code>.
     * @param string the role name in question, assumed not blank.
     * 
     * @return <code>true</code> if the string is in the collection
     */
    public static boolean containsIgnoringCase(Collection<String> strings, String string)
    {
        if (string == null)
            return false;
        for (String s : strings)
        {
            if (s != null && s.equalsIgnoreCase(string))
            {
                return true;
            }
        }
        return false;
    }

}
