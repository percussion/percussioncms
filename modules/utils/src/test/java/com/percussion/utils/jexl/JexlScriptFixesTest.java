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

package com.percussion.utils.jexl;

import org.junit.Assert;
import org.junit.Test;

public class JexlScriptFixesTest {

    @Test
    public void fixScript() {

        String testScript = "sdfgsdfg foreach($item in list ) sdfgsdfg";
        String result = JexlScriptFixes.fixScript(testScript,"Unit Test", "fixScript");
        Assert.assertEquals("sdfgsdfg for($item : list) sdfgsdfg", result);
        System.out.println(testScript +" ----> "+result);

        testScript = "if ( !$test )";
        result = JexlScriptFixes.fixScript(testScript,"Unit Test", "fixScript");
        System.out.println(testScript +" ----> "+result);
        Assert.assertEquals("if ( ! $test )", result);


        testScript = "if ( $ref1=$ref2 )";
        result = JexlScriptFixes.fixScript(testScript,"Unit Test", "fixScript");
        System.out.println(testScript +" ----> "+result);
        Assert.assertEquals("if ( $ref1 = $ref2 )", result);


        testScript = "$params=$rx.string.stringToMap(null);";
        result = JexlScriptFixes.fixScript(testScript,"Unit Test", "fixScript");
        System.out.println(testScript +" ----> "+result);
        Assert.assertEquals("$params = $rx.string.stringToMap(null);", result);


        testScript = "sdfgsdfg foreach($item in list ) sdfgsdfg sdfgsdfg foreach($item in list ) sdfgsdfg";
        result = JexlScriptFixes.fixScript(testScript,"Unit Test", "fixScript");
        System.out.println(testScript +" ----> "+result);
        Assert.assertEquals("sdfgsdfg for($item : list) sdfgsdfg sdfgsdfg for($item : list) sdfgsdfg", result);
    }
}
