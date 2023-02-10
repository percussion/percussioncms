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

package com.percussion.services.menus;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE,
        region = "RXMENUACTIONPROPERTIES")
@Table(name="RXMENUACTIONPROPERTIES")
public class PSActionMenuProperty implements Serializable{

    @Embeddable
    public static class PSActionMenuPropertyPK implements Serializable {

        @Column(name="ACTIONID")
        protected int actionId;
        @Column(name="PROPNAME")
        protected String propertyName;

        public PSActionMenuPropertyPK() {}

        public PSActionMenuPropertyPK(Integer actionId, String propertyName) {
            this.actionId = actionId;
            this.propertyName = propertyName;
        }
        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if ( !(other instanceof PSActionMenuPropertyPK) ) return false;

            final PSActionMenuPropertyPK pk = (PSActionMenuPropertyPK) other;

            if ( pk.actionId != actionId) {
                return false;
            }

            return pk.propertyName.equals(propertyName);
        }

        public int hashCode() {
            int result;
            result = propertyName.hashCode();
            result = 31 * (result + actionId);
            return result;
        }

        public int getActionId() {
            return actionId;
        }

        public void setActionId(int actionId) {
            this.actionId = actionId;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }
    }


    @EmbeddedId
    PSActionMenuPropertyPK primaryKey;

    @MapsId("actionId")
    @ManyToOne
    @JoinColumn(name = "ACTIONID")
    private PSActionMenu menu;

    @Column(name="PROPVALUE")
    private String value;

    @Column(name="DESCRIPTION")
    private String description;

    public PSActionMenuProperty(){
    }

    public PSActionMenuProperty(int actionId, String name, String value) {
        if(this.primaryKey == null){
            this.primaryKey = new PSActionMenuPropertyPK();
        }
        this.primaryKey.actionId = actionId;
        this.primaryKey.propertyName = name;
        this.value = value;
    }

    public PSActionMenu getMenu() {
        return menu;
    }

    public void setMenu(PSActionMenu menu) {
        this.menu = menu;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PSActionMenuPropertyPK getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PSActionMenuPropertyPK primaryKey) {
        this.primaryKey = primaryKey;
    }
}
