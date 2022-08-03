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

import com.opencsv.bean.CsvBindByName;


public class PSPercussionRedirectEntry {

    public PSPercussionRedirectEntry() {
    }

    @CsvBindByName()
    private String subkey;

    @CsvBindByName()
    private String redirectTo;

    @CsvBindByName()
    private String type;

    @CsvBindByName()
    private String createdOn;

    @CsvBindByName()
    private String enabled;

    @CsvBindByName()
    private String condition;

    @CsvBindByName()
    private String site;

    @CsvBindByName()
    private String permanent;

    @CsvBindByName()
    private String id;

    @CsvBindByName()
    private String category;

    @CsvBindByName()
    private String key;

    public PSPercussionRedirectEntry(String subkey, String redirectTo, String type, String createdOn, String enabled, String condition, String site, String permanent, String id, String category, String key) {
        this.subkey = subkey;
        this.redirectTo = redirectTo;
        this.type = type;
        this.createdOn = createdOn;
        this.enabled = enabled;
        this.condition = condition;
        this.site = site;
        this.permanent = permanent;
        this.id = id;
        this.category = category;
        this.key = key;
    }

    public String getSubkey() {
        return subkey;
    }

    public void setSubkey(String subkey) {
        this.subkey = subkey;
    }

    public String getRedirectTo() {
        return redirectTo;
    }

    public void setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getPermanent() {
        return permanent;
    }

    public void setPermanent(String permanent) {
        this.permanent = permanent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSPercussionRedirectEntry{");
        sb.append("subkey='").append(subkey).append('\'');
        sb.append(", redirectTo='").append(redirectTo).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", createdOn='").append(createdOn).append('\'');
        sb.append(", enabled='").append(enabled).append('\'');
        sb.append(", condition='").append(condition).append('\'');
        sb.append(", site='").append(site).append('\'');
        sb.append(", permanent='").append(permanent).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append(", key='").append(key).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
