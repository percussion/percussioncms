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

package com.percussion.utils.service.impl;

import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.ToDoVulnerability;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.service.IPSUtilityService;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.nio.charset.StandardCharsets;

public class PSUtilityService implements IPSUtilityService
{
    @Deprecated
    @ToDoVulnerability


    private static final Log log = LogFactory.getLog(PSUtilityService.class);
    private IPSSystemProperties systemProps = null;
    public PSUtilityService()
    {

    }

    //see interface
    public String encryptString(String str, String key)
    {

        if (str == null)
            throw new IllegalArgumentException("str may not be null");

        try {
            return PSEncryptor.getInstance().encrypt(str);
        } catch (PSEncryptionException e) {
            log.error("Error encrypting text: " + e.getMessage(),e);
            return "";
        }

    }

    //see interface
    public String decryptString(String str, String key)
    {
        String ret;

        if (str == null)
            throw new IllegalArgumentException("str may not be null");

        if (key == null || key.length() == 0)
        {
            key = PSLegacyEncrypter.DEFAULT_KEY();
        }
        try{
            ret = PSEncryptor.getInstance().decrypt(str);

        }catch(PSEncryptionException ex){
            ret =  PSLegacyEncrypter.getInstance().decrypt(str, key);
        }

        return ret;
    }

    @Override
    public void log(String type, String category, String message)
    {
        if(StringUtils.isBlank(message)){
            //Nothing to log simply return.
            return;
        }
        LogTypeEnum ltype = LogTypeEnum.info;
        try{
            ltype = LogTypeEnum.valueOf(type);
        }
        catch (Exception e) {
            //Invalid type enum supplied ignoring the exception and treating it as info
        }
        LogCategoryEnum lcategory = LogCategoryEnum.General;
        try{
            lcategory = LogCategoryEnum.valueOf(category);
        }
        catch (Exception e) {
            //Invalid category enum supplied treating it as general
        }
        String logMsg = lcategory.toString() + " : " + message;
        switch (ltype)
        {
            case debug:
                log.debug(logMsg);
                break;
            case error:
                log.error(logMsg);
                break;
            default :
                log.info(logMsg);
                break;
        }
    }

    /**
     * Set the system properties on this service. This service will always use
     * the the values provided by the most recently set instance of the
     * properties.
     * 
     * @param systemProps the system properties
     */
    public void setSystemProps(IPSSystemProperties systemProps)
    {
        this.systemProps = systemProps;
    }

    @Override
    public boolean isSaaSEnvironment()
    {
        boolean isSaaS = false;
        String saasProp = systemProps.getProperty(SAAS_FLAG_SERVER_PROP);
        if(StringUtils.isNotBlank(saasProp) && (saasProp.equalsIgnoreCase("true") || saasProp.equalsIgnoreCase("yes")))
            isSaaS = true;
        return isSaaS;
    }


}
