/*
 * Copyright 1999-2022 Percussion Software, Inc.
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

package com.percussion.tools.redirect;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static com.percussion.tools.redirect.TestRedirectList.createTempFolder;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class TestJSONConversion {

    @Before
    public void setup() throws IOException {
        createTempFolder();
    }

    @Test
    public void testJSONConversion() throws IOException {
        PSPercussionRedirectEntryList list = new PSPercussionRedirectEntryList(
                TestRedirectList.getRedirectSourceFilePath());

        PSJSONRedirectConverter cvt = new PSJSONRedirectConverter();

        String result;
        for(PSPercussionRedirectEntry e : list){
            if(e.getCategory().equalsIgnoreCase("VANITY")){
                result = cvt.convertVanityRedirect(e);
                assertNotNull(result);
                System.out.println(result);
            }else if(e.getCategory().equalsIgnoreCase("REGEX")){
                result = cvt.convertRegexRedirect(e);
                assertNotNull(result);
                System.out.println(result);
            }else if(e.getCategory().equalsIgnoreCase("AUTOGEN")){
                result = cvt.convertVanityRedirect(e);
                assertNotNull(result);
                System.out.println(result);
            }
            else{
                fail("No category! " + e.toString());
            }
        }
    }
}
