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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
