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

package com.percussion.sitemanage.importer.utils;

import com.percussion.services.assembly.impl.PSReplacementFilter;
import com.percussion.utils.testing.UnitTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;

@Category(UnitTest.class)
public class TestPSLinkReservedStringFilter {
    private static final String FOO = "foo";
    private static final String BAR = "bar";

    @Test
    public void testFilter() {
        assertTrue(
                "Test Space Failed",
                PSReplacementFilter.filter("/this has spaces").equals(
                        "/this-has-spaces"));
        assertTrue(
                "Test Brackets Failed",
                PSReplacementFilter.filter("/this [has] brackets").equals(
                        "/this-has-brackets"));
        assertTrue(
                "Test Multiple Dashes Failed",
                PSReplacementFilter.filter("/this       [has]     brac--kets").equals(
                        "/this-has-brac-kets"));
        assertTrue(
                "Test Encoded Space Failed",
                PSReplacementFilter.filter("/test/this%20has%20spaces").equals(
                        "/test/this-has-spaces"));
        assertTrue(
                "Test Backslash Failed",
                PSReplacementFilter.filter("\\test\\this has spaces").equals(
                        "/test/this-has-spaces"));

        assertTrue("Test Anchor Failed",
                PSReplacementFilter.filter("\\this has spaces\\and#anchor?malformed")
                        .equals("/this-has-spaces/and-malformed#anchor"));
        
        assertTrue("Test Colon Failed",
                PSReplacementFilter.filter("\\test:a:colon\\this has spaces")
                        .equals("/test-a-colon/this-has-spaces"));
        assertTrue("Test Percent Failed",
                PSReplacementFilter.filter("\\test%a%percent\\this has spaces")
                        .equals("/test-a-percent/this-has-spaces"));
        assertTrue(
                "Test Semicolon Failed",
                PSReplacementFilter.filter(
                        "\\test;a;semicolon\\this has spaces").equals(
                        "/test-a-semicolon/this-has-spaces"));
        assertTrue(
                "Test Asterisk Failed",
                PSReplacementFilter
                        .filter("\\test*a*asterisk\\this has spaces").equals(
                                "/test-a-asterisk/this-has-spaces"));
        assertTrue(
                "Test Question Mark Failed",
                PSReplacementFilter
                        .filter("\\test?a?question?mark\\this has spaces").equals(
                                "/test-a-question-mark/this-has-spaces"));
        assertTrue(
                "Test Less Than Failed",
                PSReplacementFilter
                        .filter("\\test<a<less<than\\this has spaces").equals(
                                "/test-a-less-than/this-has-spaces"));
        assertTrue(
                "Test Greater Than Failed",
                PSReplacementFilter
                        .filter("\\test>a>greater>than\\this has spaces").equals(
                                "/test-a-greater-than/this-has-spaces"));
        assertTrue(
                "Test Pipe Failed",
                PSReplacementFilter
                        .filter("\\test|a|pipe\\this has spaces").equals(
                                "/test-a-pipe/this-has-spaces"));
    }

}
