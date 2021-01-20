/*
 * Copyright 2016 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.cadf.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.MessageFormat;

import org.junit.Test;

import com.ibm.cadf.Messages;
import com.ibm.cadf.exception.CADFException;

public class AttachmentTest
{

    @Test
    public void testAttachmentPositive() throws CADFException, IOException
    {
        String typeURI = "attachURI";
        String content = "content";
        String name = "attachName";
        Attachment attachment = new Attachment(typeURI, content, name);
        assertEquals(true, attachment.isValid());
    }

    @Test
    public void testAttachmentNegative() throws CADFException, IOException
    {

        try
        {
            String typeURI = "attachURI";
            String content = "";
            String name = "attachName";
            Attachment attachment = new Attachment(typeURI, content, name);
            attachment.isValid();
            fail("Attachment object creation should fail as mandatory field content is not passed");
        }
        catch (CADFException ex)
        {

            String message = MessageFormat.format(Messages.MISSING_MANDATORY_FIELDS, "content");
            assertEquals(message, ex.getMessage());
        }

    }
}
