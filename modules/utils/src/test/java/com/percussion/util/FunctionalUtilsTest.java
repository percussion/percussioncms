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

package com.percussion.util;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class FunctionalUtilsTest {

    @Test
    public void commaStringToStream() {

        Set<String> expected = new HashSet<>();
        expected.add("test1");
        expected.add("test2");
        Set<String> stringSet = FunctionalUtils.commaStringToStream("test2,test1").collect(Collectors.toSet());
        assertTrue(CollectionUtils.isEqualCollection(expected,stringSet));
        System.out.println(stringSet);
    }
}
