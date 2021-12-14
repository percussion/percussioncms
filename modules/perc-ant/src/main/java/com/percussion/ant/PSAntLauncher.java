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

package com.percussion.ant;

import org.apache.tools.ant.launch.Launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PSAntLauncher {

    public static void main(String[] args){

        List<String> origArgs = new ArrayList<>(Arrays.asList(args));
        //origArgs.add("--noclasspath");
        //rigArgs.add("--nouserlib");
        //System.setProperty("ant.library.dir","antlib");
        Launcher.main(origArgs.toArray(new String[origArgs.size()]));
    }

}
