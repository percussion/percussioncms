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

package com.percussion.error;

import com.percussion.log.PSLogInformation;

import java.net.URL;
import java.util.Locale;

public interface IPSErrorManager {
    /**
     * Create a formatted message for messages taking only a single
     * argument.
     *
     * @param msgCode   the error string to load
     * @param singleArg the argument to use as the sole argument in
     *                  the error message
     * @return the formatted message
     */
    String createMessage(int msgCode,
                         Object singleArg);

    /**
     * Create a formatted message for messages taking an array of
     * arguments. Be sure to store the arguments in the correct order in
     * the array, where {0} in the string is array element 0, etc.
     *
     * @param msgCode   the error string to load
     * @param arrayArgs the array of arguments to use as the arguments
     *                  in the error message
     * @return the formatted message
     */
    String createMessage(int msgCode,
                         Object[] arrayArgs);

    /**
     * Create a formatted message for messages taking an array of
     * arguments. Be sure to store the arguments in the correct order in
     * the array, where {0} in the string is array element 0, etc.
     *
     * @param msgCode   the error string to load
     * @param arrayArgs the array of arguments to use as the arguments
     *                  in the error message
     * @param loc       the locale to use
     * @return the formatted message
     */
    String createMessage(int msgCode,
                         Object[] arrayArgs,
                         Locale loc);

    /**
     * Create a formatted message for messages taking an array of
     * arguments. Be sure to store the arguments in the correct order in
     * the array, where {0} in the string is array element 0, etc.
     *
     * @param msgCode   the error string to load
     * @param arrayArgs the array of arguments to use as the arguments
     *                  in the error message
     * @param language  the language string to use
     * @return the formatted message
     */
    String createMessage(int msgCode,
                         Object[] arrayArgs,
                         String language);

    String getErrorText(int code, boolean nullNotFound, Locale loc);

    String getErrorText(int code, boolean nullNotFound, String loc);

    URL getErrorURL(PSLogInformation error, Locale loc);

    void init();

    void close();

    String getErrorText(int code);

    String getErrorText(int code, boolean nullNotFound);
}
