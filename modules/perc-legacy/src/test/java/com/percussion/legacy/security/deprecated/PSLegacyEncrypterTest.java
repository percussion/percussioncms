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
package com.percussion.legacy.security.deprecated;

import com.percussion.security.PSEncryptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Test case for the {@link PSLegacyEncrypter} class
 */
@Deprecated
public class PSLegacyEncrypterTest
{

   @Rule
   public TemporaryFolder temporaryFolder = TemporaryFolder.builder().build();
   private String rxdeploydir;

   @Before
   public void setup(){
      rxdeploydir = System.getProperty("rxdeploydir");
      System.setProperty("rxdeploydir",temporaryFolder.getRoot().getAbsolutePath());
   }

   @After
   public void teardown(){
      //Reset the deploy dir property if it was set prior to test
      if(rxdeploydir != null)
         System.setProperty("rxdeploydir",rxdeploydir);
   }

   /**
    * Test encrypt/decrypt
    *
    * @throws Exception if the test fails
    */
   @Test
   public void testEncrypt() throws Exception
   {
      if (rxdeploydir == null)
         rxdeploydir = temporaryFolder.getRoot().getAbsolutePath();

      testKey("jass is the way", "demo");
      testKey(PSLegacyEncrypter.getInstance(rxdeploydir + PSEncryptor.SECURE_DIR).OLD_SECURITY_KEY(), "demo");
      testKey(PSLegacyEncrypter.getInstance(rxdeploydir + PSEncryptor.SECURE_DIR).OLD_SECURITY_KEY(), "");
      testKey("a", "myPass");
      testKey(getKey("foo", 4), "foo");
      testKey(getKey("foo", 13), "foo");
      testKey(getKey("foo", 14), "foo");
      testKey(getKey("foo", 15), "foo");
      testKey(getKey("foo", 16), "foo");
      testKey(getKey("foo", 18), "foo");
      testKey("MaSaLa-MiTsUbIsHi-RaDiO-louisiana", "Balt");
   }
   
   /**
    * Test conversion of <code>BigInteger</code> to padded byte array
    * 
    * @throws Exception if the test fails
    */
   @Test
   public void testConvert() throws Exception
   {
      // test 8-byte array
      testToByteArray(new BigInteger("72057594037927936"));
      
      // test byte arrays of size < 8
      testToByteArray(new BigInteger("1"));
      testToByteArray(new BigInteger("0"));
      testToByteArray(new BigInteger("-255"));
      
      // test byte arrays of size > 8
      testToByteArray(new BigInteger("18519084246547628289"));
      testToByteArray(new BigInteger("-4703847398623097585407"));
    }
   
   /**
    * Generate a key based on the supplied seed and size.
    * 
    * @param seed The value to use to get the bytes to fill, assumed not 
    * <code>null</code> or empty.
    * @param size The size in bytes fo the returned string.
    * 
    * @return the key, never <code>null</code> or empty.
    */
   private String getKey(String seed, int size)
   {
      byte[] bytes = new byte[size];
      Arrays.fill(bytes, seed.getBytes()[0]);
      return new String(bytes);
   }
   
   /**
    * Attempts to encrypt and descrypt the supplied pwd
    * 
    * @param key The key to use, assumed not <code>null</code> or empty.
    * @param pwd The pwd to encrypt, assumed not <code>null</code>.
    *
    */
   private void testKey(String key, String pwd)
   {
      String enc = PSLegacyEncrypter.getInstance(rxdeploydir + PSEncryptor.SECURE_DIR).encrypt(pwd, key);
      Assert.assertNotEquals(pwd, enc);
      System.out.println(enc);
      assertEquals(pwd, PSLegacyEncrypter.getInstance(
              rxdeploydir + PSEncryptor.SECURE_DIR).decrypt(enc, key,null));
      assertEquals(enc, PSLegacyEncrypter.getInstance(
              rxdeploydir + PSEncryptor.SECURE_DIR).encrypt(pwd, key));
   }
   
   /**
    * Attempts to convert the supplied <code>BigInteger</code> to a byte array
    * which has been padded if necessary, verifying that the array has been
    * padded correctly and the resulting array converts back to a
    * <code>BigInteger</code> which is equivalent to the original.
    * 
    * @param bigInt The <code>BigInteger</code> to convert, assumed not
    * <code>null<code>.
    *
    */
   private void testToByteArray(BigInteger bigInt)
   {
      byte[] convertedBytes = PSLegacyEncrypter.getInstance(rxdeploydir + PSEncryptor.SECURE_DIR).toByteArray(bigInt);
      assertEquals(0, (convertedBytes.length % PSLegacyEncrypter.BYTE_ARRAY_MULTIPLE));
      BigInteger convertedInt = new BigInteger(convertedBytes);
      Assert.assertEquals(convertedInt, bigInt);
   }
}

