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
package com.percussion.delivery.metadata.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.Validate;

/**
 * Responsible for calculating a hash over a value. It uses SHA-1 by default
 * and UTF-8 to convert the string value.
 * 
 * @author miltonpividori
 *
 */
public class PSHashCalculator
{
    private static final String HEXES = "0123456789ABCDEF";
    private static final String HASH_ALGORITHM = "SHA-1";
    private static final String CONTENT_ENCODING = "UTF-8";

    private MessageDigest digest;
    
    /**
     * Creates a new PSHashCalculator instance using the algorithm specified in
     * {@link #HASH_ALGORITHM} value. It may throw an exception if the algorithm
     * is not specified.
     */
    public PSHashCalculator()
    {
        try
        {
            digest = MessageDigest.getInstance(HASH_ALGORITHM);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Calculates a hash of the given value. The hash algorithm used is specified
     * by {@link #HASH_ALGORITHM}. The value encoding is specified by
     * {@link #CONTENT_ENCODING}.
     * <p>
     * This method is thread-safe.
     * 
     * @param value The content to generate a hash value of. Cannot be
     * <code>null</code>, maybe empty.
     * @return A hash value according to the hash algorithm specified in
     * {@link #HASH_ALGORITHM}.
     */
    public synchronized String calculateHash(String value)
    {
        Validate.notNull(value, "Value cannot be null");
        
        digest.reset();
        byte[] hashResult = null;
        
        try
        {
            hashResult = digest.digest(value.getBytes(CONTENT_ENCODING));
            return getHex(hashResult);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private String getHex(byte[] raw)
    {
        if (raw == null)
            return null;
        
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        
        for (final byte b : raw)
        {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        
        return hex.toString();
    }
}
