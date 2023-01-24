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
package com.percussion.services.system;

/**
 * Error numbers for use with the bundle
 * <code>PSSystemErrorStringBundle.properties</code>
 */
public interface IPSSystemErrors
{
   /**
    * Missing shared property.
    * <p>
    * The arguments passed in for this message are: <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>The id of the missing shared property</TD>
    * </TR>
    * </TABLE>
    */
   public static final int MISSING_SHARED_PROPERTY = 1;

   /**
    * Couldn't read a folder
    * <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>The content id</TD>
    * </TR>
    * </TABLE>
    */
   public static final int ERROR_DETERMINING_FOLDER_READ = 4;
}
