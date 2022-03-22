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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.error;

import com.percussion.log.PSLogInformation;

import java.net.URL;
import java.util.Locale;

public class PSErrorManagerDefaultImpl implements IPSErrorManager {
    @Override
    public String createMessage(int msgCode, Object singleArg) {
        return null;
    }

    @Override
    public String createMessage(int msgCode, Object[] arrayArgs) {
        return null;
    }

    @Override
    public String createMessage(int msgCode, Object[] arrayArgs, Locale loc) {
        return null;
    }

    @Override
    public String createMessage(int msgCode, Object[] arrayArgs, String language) {
        return null;
    }

    @Override
    public String getErrorText(int code, boolean nullNotFound, Locale loc) {
        return null;
    }

    @Override
    public String getErrorText(int code, boolean nullNotFound, String loc) {
        return null;
    }

    @Override
    public URL getErrorURL(PSLogInformation error, Locale loc) {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void close() {

    }

    @Override
    public String getErrorText(int code) {
        return null;
    }

    @Override
    public String getErrorText(int code, boolean nullNotFound) {
        return null;
    }
}
