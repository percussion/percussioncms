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

package com.percussion.HTTPClient;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Some utility methods for digesting info using MD5.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 * @since	V0.3-3
 */
@Deprecated
class MD5
{
    private static final char[] hex = {
	'0', '1', '2', '3', '4', '5', '6', '7',
	'8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
    };

    /**
     * Turns array of bytes into string representing each byte as
     * unsigned hex number.
     *
     * @param hash	array of bytes to convert to hex-string
     * @return	generated hex string
     */
    public static final String toHex(byte hash[])
    {
	StringBuilder buf = new StringBuilder(hash.length * 2);

	for (int idx=0; idx<hash.length; idx++)
	    buf.append(hex[(hash[idx] >> 4) & 0x0f]).append(hex[hash[idx] & 0x0f]);

	return buf.toString();
    }

    /**
     * Digest the input.
     *
     * @param input the data to be digested.
     * @return the md5-digested input
     */
    @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
	public static final byte[] digest(byte[] input)
    {
	try
	{
	    MessageDigest md5 = MessageDigest.getInstance("MD5");
	    return md5.digest(input);
	}
	catch (NoSuchAlgorithmException nsae)
	{
	    throw new Error(nsae.toString());
	}
    }

    /**
     * Digest the input.
     *
     * @param input1 the first part of the data to be digested.
     * @param input2 the second part of the data to be digested.
     * @return the md5-digested input
     */
    @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5")
	@Deprecated
    public static final byte[] digest(byte[] input1, byte[] input2)
    {
	try
	{
	    MessageDigest md5 = MessageDigest.getInstance("MD5");
	    md5.update(input1);
	    return md5.digest(input2);
	}
	catch (NoSuchAlgorithmException nsae)
	{
	    throw new Error(nsae.toString());
	}
    }

    /**
     * Digest the input.
     *
     * @param input the data to be digested.
     * @return the md5-digested input as a hex string
     */
    public static final String hexDigest(byte[] input)
    {
	return toHex(digest(input));
    }

    /**
     * Digest the input.
     *
     * @param input1 the first part of the data to be digested.
     * @param input2 the second part of the data to be digested.
     * @return the md5-digested input as a hex string
     */
    public static final String hexDigest(byte[] input1, byte[] input2)
    {
	return toHex(digest(input1, input2));
    }

    /**
     * Digest the input.
     *
     * @param input the data to be digested.
     * @return the md5-digested input as a hex string
     */
    public static final byte[] digest(String input)
    {
	try
	    { return digest(input.getBytes("8859_1")); }
	catch (UnsupportedEncodingException uee)
	    { throw new Error(uee.toString()); }
    }

    /**
     * Digest the input.
     *
     * @param input the data to be digested.
     * @return the md5-digested input as a hex string
     */
    public static final String hexDigest(String input)
    {
	try
	    { return toHex(digest(input.getBytes("8859_1"))); }
	catch (UnsupportedEncodingException uee)
	    { throw new Error(uee.toString()); }
    }
}
