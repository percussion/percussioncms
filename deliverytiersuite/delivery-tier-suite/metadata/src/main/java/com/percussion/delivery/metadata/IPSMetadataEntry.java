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

package com.percussion.delivery.metadata;

import java.util.Set;

public interface IPSMetadataEntry
{

    /**
     * @return the name
     */
    public String getName();

    /**
     * @param name the name to set
     */
    public void setName(String name);

    /**
     * @return the folder
     */
    public String getFolder();

    /**
     * @param folder the folder to set
     */
    public void setFolder(String folder);

    /**
     * @return the page path
     */
    public String getPagepath();

    /**
     * @param path the pagepath to set
     */
    public void setPagepath(String path);

    /**
     * @return the linktext
     */
    public String getLinktext();

    /**
     * @param linktext the linktext to set
     */
    public void setLinktext(String linktext);

    /**
     * @return the type
     */
    public String getType();

    /**
     * @param type the type to set
     */
    public void setType(String type);

    /**
     * @return the site
     */
    public String getSite();

    /**
     * @param site the site to set
     */
    public void setSite(String site);

    /**
     * @return the properties. This returns a cloned set of properties changing
     *         the value of these directly will not affect the property values
     *         in the entry. To change property values on the entry you must
     *         passed the properties back to the entries
     *         {@link #setProperties(Set)} method.
     */
    public Set<IPSMetadataProperty> getProperties();

    /**
     * @param properties the properties to set
     */
    public void setProperties(Set<IPSMetadataProperty> properties);

    public void addProperty(IPSMetadataProperty prop);

    public void clearProperties();

}
