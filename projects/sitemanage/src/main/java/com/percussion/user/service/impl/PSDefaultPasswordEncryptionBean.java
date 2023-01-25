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
package com.percussion.user.service.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.security.IPSPasswordFilter;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSPasswordHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author DavidBenua
 *
 */
@Component("defaultPasswordEncryptionBean")
@Lazy
public class PSDefaultPasswordEncryptionBean implements IPSPasswordFilter
{

    /* (non-Javadoc)
     * @see com.percussion.security.IPSPasswordFilter#encrypt(java.lang.String)
     */
    @Override
    public String encrypt(String password)
    {
        if(StringUtils.isBlank(password))
        {
           return StringUtils.EMPTY; 
        }
        try {
            return  PSPasswordHandler.getHashedPassword(password.trim());
        } catch (PSEncryptionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getAlgorithm() {
        return PSPasswordHandler.ALGORITHM;
    }

    /* (non-Javadoc)
     * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef, java.io.File)
     */
    @Override
    public void init(@SuppressWarnings("unused") IPSExtensionDef def, @SuppressWarnings("unused") File codeRoot)
    {  //does nothing.

    }

    /***
     * Will encrypt the password using the hashing / encryption
     * routine used in the previous version of the software.
     *
     * This is to allow Security Providers to re-encrypt passwords
     * on login after a security update.
     *
     * @param password
     * @return
     */
    @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_SHA1")
    @Override
    @Deprecated
    public String legacyEncrypt(String password) {
        if(StringUtils.isBlank(password))
        {
            return StringUtils.EMPTY;
        }
        return DigestUtils.shaHex(password.trim());
    }

    @Override
    public String getLegacyAlgorithm() {
        return "SHA-1";
    }
}
