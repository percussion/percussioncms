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
