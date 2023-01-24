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
package com.percussion.gwt.pkgmgtui.client;

import java.util.Arrays;

public class PSConstants
{
    private PSConstants(){}

   /**
    * Default members margin
    */
   private static final int membersMargin = 10;
    /**
     * Default resize options for the dialogs
     */
    private static final String[] dialogResizeOptions = { "L", "B", "R", "T",
            "BL", "BR", "TL", "TR" };

    public static int getMembersMargin() {
        return membersMargin;
    }

    public static String[] getDialogResizeOptions() {
        /* Return a copy to avoid vulnerability */
        return Arrays.copyOf(dialogResizeOptions,dialogResizeOptions.length);
    }
}
