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

package com.percussion.soln.segment.rx;

import static java.util.Collections.*;

import java.io.StringReader;
import java.util.List;


public class SegmentAliasUtil {
    
    public static List<String> parseAliases(String input) {
        if (input == null) throw new IllegalArgumentException("input cannot be null");
        if ("".equals(input)) return emptyList();
        StringReader sr = new StringReader(input);
        SegmentAliasFieldLexer lexer = new SegmentAliasFieldLexer(sr);
        SegmentAliasFieldParser parser = new SegmentAliasFieldParser(lexer);
        try {
            parser.startRule();
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad input: " + input, e);
        }
        return parser.getAliases();
    }

}
