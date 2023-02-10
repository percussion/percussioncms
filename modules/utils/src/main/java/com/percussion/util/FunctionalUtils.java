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

import org.apache.commons.lang.StringUtils;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FunctionalUtils {

    public static Pattern COMMA_SPLIT = Pattern.compile(",");

    public static Predicate<? super Object> IS_POSITIVE_NUMBER_OR_NOT_NULL = p -> p != null && (p instanceof Number) && (((Number)p).doubleValue() > 0);

    public static Stream<String> commaStringToStream(String string)
    {
        return COMMA_SPLIT.splitAsStream(string).filter(StringUtils::isNotBlank)
                .map(StringUtils::trim);
    }

}
