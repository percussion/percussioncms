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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.utils.service.impl;

import com.percussion.security.ToDoVulnerability;
import com.percussion.security.dao.IPSSecurityItemsDao;
import com.percussion.server.PSServer;
import com.percussion.share.data.PSListWrapper;
import com.percussion.share.data.PSMapWrapper;
import com.percussion.share.data.PSNoContent;
import com.percussion.utils.data.PSLogData;
import com.percussion.utils.data.PSPrivateKeysResponse;
import com.percussion.utils.service.IPSUtilityService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;



@Path("/utility")
@Component("utilityRestService")
@Lazy
@ToDoVulnerability
public class PSUtilityRestService
{
    /**
     * The value of this key represents the string to be encrypted/decrypted in {@link #encryptString(PSMapWrapper)}.
     */
    public static final String STRING_KEY = "string";
    
    /**
     * The value of this key represents the encryption/decryption key to be used in 
     * {@link #encryptString(PSMapWrapper)}, {@link #decryptString(PSMapWrapper)}.
     */
    public static final String KEY_KEY = "key";
    
    private IPSSecurityItemsDao securityItemsDao;
    @Autowired
    public PSUtilityRestService(IPSUtilityService service, IPSSecurityItemsDao securityItemsDao)
    {
        this.service = service;
        this.securityItemsDao = securityItemsDao;
    }

    @GET
    @Path("/privatekeys")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSPrivateKeysResponse getPrivateKeys()
    {
        List<String> keyNames = securityItemsDao.getAvailablePrivateKeys();
        
        return new PSPrivateKeysResponse(keyNames);
    }
    
    @GET
    @Path("/maxInactiveInterval")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent getMaxInactiveInterval()
    {
    	PSNoContent result = new PSNoContent();
    	int sessTimeout = PSServer.getServerConfiguration().getUserSessionTimeout();
    	result.setResult("" + sessTimeout);
    	return result;
    }
    
    /**
     * It is a wrapper method  for the method of the PSUtilityService.
     * 
     * @return A wrapped map containing one key/value pair where the key is {@link #STRING_KEY} and the value is the
     * encrypted string which is never <code>null</code>, may be empty.  Never <code>null</code>.
     */
    @POST
    @Path("/encryptstring")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSMapWrapper encryptString(PSMapWrapper values)
    {
        return encryptDecryptString(values, true);
    }

    /**
     * It is a wrapper method  for the method of the PSUtilityService.
     * 
     * @return A wrapped map containing one key/value pair where the key is {@link #STRING_KEY} and the value is the
     * decrypted string which is never <code>null</code>, may be empty.  Never <code>null</code>.
     */
    @POST
    @Path("/decryptstring")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSMapWrapper decryptString(PSMapWrapper values)
    {       
        return encryptDecryptString(values, false);
    }

    /**
     * Encrypts the supplied list of strings using {@link IPSUtilityService#encryptString(String, String)} method.
     * Uses the default key for encrypting the strings.
     * 
     * @return A wrapped map containing one key/value pair where the key is the supplied string and the value is the
     * encrypted string which is never <code>null</code>, may be empty.  Never <code>null</code>.
     */
    @POST
    @Path("/encryptstrings")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSMapWrapper encryptStrings(PSListWrapper values)
    {
        Validate.notNull(values, "values must not be null for encryptstrings method");
        Validate.notNull(values.getList(), "value list must not be null for encryptstrings method");
        return encryptDecryptStrings(values.getList(), true);
    }
    
    /**
     * Encrypts the supplied list of strings using {@link IPSUtilityService#decryptString(String, String)} method.
     * Uses the default key for decrypting the strings.
     * 
     * @return A wrapped map containing one key/value pair where the key is supplied string and the value is the
     * decrypted string which is never <code>null</code>, may be empty.  Never <code>null</code>.
     */
    @POST
    @Path("/decryptstrings")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSMapWrapper decryptStrings(PSListWrapper values)
    {       
        Validate.notNull(values, "values must not be null for decryptstrings method");
        Validate.notNull(values.getList(), "value list must not be null for decryptstrings method");
        return encryptDecryptStrings(values.getList(), false);
    }

    /**
     * Logs the message to server log.
     * @param logData 
     * @return PSNoContent that just says logged the message.
     */
    @POST
    @Path("/log")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSNoContent log(PSLogData logData)
    {
        Validate.notNull(logData, "logData must not be null for log method");
        service.log(logData.getType(), logData.getCategory(), logData.getMessage());
        return new PSNoContent("Logged the message.");
    }
    
    @GET
    @Path("/saas/flag")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean getSaaSSetting()
    {
        return service.isSaaSEnvironment();
    }
    
    /**
     * Helper method to get values from the map wrapper and then encrypt or decrypt
     * the passed in string.
     * @param values the map wrapper containing the string or key, assumed not <code>null</code>.
     * @param encrypt flag indicating that string should be encrypted.
     * @return A wrapped map containing one key/value pair where the key is {@link #STRING_KEY} and the value is the
     * encrypted/decrypted string which is never <code>null</code>, may be empty.  Never <code>null</code>.
     */
    private PSMapWrapper encryptDecryptString(PSMapWrapper values, boolean encrypt)
    {
        PSMapWrapper mw = new PSMapWrapper();
                
        Map<String, String> entries = values.getEntries();
        String key = null;
        String str = "";
        if(entries != null)
        {
            key = values.getEntries().get(KEY_KEY);
            str = StringUtils.defaultString(values.getEntries().get(STRING_KEY), "");
        }

        if(encrypt)
        {
            mw.getEntries().put(STRING_KEY, service.encryptString(str, key));
        }
        else
        {
            mw.getEntries().put(STRING_KEY, service.decryptString(str, key));
        }
        
        return mw;
    }
    
    
    /**
     * Helper method to encrypt or decrypt the supplied values. Uses default key for encryption or decryption.
     * @param values the list of strings, assumed not <code>null</code>.
     * @param encrypt flag indicating that strings should be encrypted.
     * @return A wrapped map containing one key/value pair where the key is the supplied string and the value is the
     * encrypted/decrypted string which is never <code>null</code>, may be empty.  Never <code>null</code>.
     */
    private PSMapWrapper encryptDecryptStrings(List<String> values, boolean encrypt)
    {
        PSMapWrapper result = new PSMapWrapper();
        Map<String, String> resultEntries = result.getEntries();
        for (String key : values)
        {
            String val = encrypt ? service. encryptString(key, null):service.decryptString(key, null);
            resultEntries.put(key,val);
        }
        return result;
    }

    private IPSUtilityService service;
    
    public static final String SAAS_SETTING_PROPERTY_NAME = "doSAAS";
}