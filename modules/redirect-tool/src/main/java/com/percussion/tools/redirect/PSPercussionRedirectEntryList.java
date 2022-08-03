/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.tools.redirect;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a list of redirect entries from the redirect manager.
 * Intended to be populated by an export redirect csv file from
 * the redirect manager.
 */
public class PSPercussionRedirectEntryList extends ArrayList<PSPercussionRedirectEntry> {

    /**
     * Constructor that takes a path to a redirect file
     * and loads the list with the file contents.
     *
     * @param sourceRedirectFile A valid path to a redirect file.
     */
    public PSPercussionRedirectEntryList(String sourceRedirectFile){

        try {

            List<PSPercussionRedirectEntry> beans = new CsvToBeanBuilder(new FileReader(sourceRedirectFile))
                    .withType(PSPercussionRedirectEntry.class).build().parse();
            this.addAll(beans);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PSPercussionRedirectEntryList(Collection<? extends PSPercussionRedirectEntry> c) {
        super(c);
    }
}
