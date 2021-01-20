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
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.ibm.cadf.Messages;
import com.ibm.cadf.exception.CADFException;

public class FederatedCredentialTest
{

    @Test
    public void testFederatedCredentialPositive() throws CADFException, IOException
    {
        String type = "http://docs.oasis-open.org/security/saml/v2.0";
        String token = Identifier.generateUniqueId();
        String identity_provider = Identifier.generateUniqueId();
        String user = Identifier.generateUniqueId();
        List<String> groups = new ArrayList<String>();
        groups.add(Identifier.generateUniqueId());
        FederatedCredential fd = new FederatedCredential(type, token, identity_provider, user, groups);
        assertEquals(true, fd.isValid());
    }

    @Test
    public void testCredentialNegative() throws CADFException, IOException
    {

        try
        {
            String type = "http://docs.oasis-open.org/security/saml/v2.0";
            String token = Identifier.generateUniqueId();
            String identity_provider = null;
            String user = "";
            List<String> groups = new ArrayList<String>();
            groups.add(Identifier.generateUniqueId());
            FederatedCredential fd = new FederatedCredential(type, token, identity_provider, user, groups);
            fd.isValid();
            fail("FederatedCredential object creation should fail as mandatory field identity_provider,user is not passed");
        }
        catch (CADFException ex)
        {
            String message = MessageFormat.format(Messages.MISSING_MANDATORY_FIELDS, "identity_provider,user");
            assertEquals(message, ex.getMessage());
        }

    }
}
