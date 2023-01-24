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
