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

package com.percussion.utils.string;

import com.percussion.security.SecureStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PSFolderStringUtilsTest {

    @Rule
    public TemporaryFolder temporaryFolder = TemporaryFolder.builder().build();


    @Test
    public void testFolderStringUtils() throws IOException {

        File parentA = temporaryFolder.newFolder("parentA");
        File parentB = temporaryFolder.newFolder("parentB");
        File childA = temporaryFolder.newFolder("parentA","childA");

        assertFalse(SecureStringUtils.isChildOfFilePath(parentA.toPath(),parentB.toPath()));
        assertTrue(SecureStringUtils.isChildOfFilePath(parentA.toPath(), childA.toPath()));
        assertFalse(SecureStringUtils.isChildOfFilePath(parentB.toPath(), childA.toPath()));

        assertTrue(SecureStringUtils.isSameFileAs(parentA.toPath(),parentA.toPath()));
        assertFalse(SecureStringUtils.isSameFileAs(parentA.toPath(),childA.toPath()));
        assertFalse(SecureStringUtils.isSameFileAs(parentA.toPath(),parentB.toPath()));
    }
}
