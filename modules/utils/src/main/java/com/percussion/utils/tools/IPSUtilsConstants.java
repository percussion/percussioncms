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
package com.percussion.utils.tools;

/**
 * General constants referenced by Utils classes
 */
public interface IPSUtilsConstants
{
   /**
    * The standard name of the preferred encoding for Rhythmyx. This encoding is
    * guaranteed to be acceptable for XML parsers and HTTP servers, and should
    * be some kind of Unicode so that we can be sure all characters are
    * representable. This is the standard name for {@link #RX_JAVA_ENC}.
    */
   public static final String RX_STANDARD_ENC = "UTF-8";


   /**
    * The standard name of the preferred encoding for Rhythmyx. This encoding is
    * guaranteed to be acceptable for Sun's Java methods which take a character
    * encoding, and should be some kind of Unicode so that we can be sure all
    * characters are representable.This is the Java name for 
    * {@link #RX_STANDARD_ENC}.
    */
   public static final String RX_JAVA_ENC = "UTF-8";
   public static final String SERVER_LOG = "Server";
   public static final String  UNIT_TEST_LOG = "Unit Tests";
}

