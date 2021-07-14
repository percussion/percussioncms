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
package com.percussion.user.service.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.security.IPSPasswordFilter;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSPasswordHandler;
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
