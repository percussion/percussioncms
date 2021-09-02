/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
