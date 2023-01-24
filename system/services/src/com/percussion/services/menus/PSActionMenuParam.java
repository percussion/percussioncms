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
        region = "RXMENUACTIONPARAM")
@Table(name = "RXMENUACTIONPARAM")
public class PSActionMenuParam implements Serializable{

    @Embeddable
    public static class PSActionMenuParamPK implements Serializable {

        @Column(name="ACTIONID")
        protected int actionId;
        @Column(name="PARAMNAME")
        protected String paramName;

        public PSActionMenuParamPK() {}

        public PSActionMenuParamPK(Integer actionId, String paramName) {
            this.actionId = actionId;
            this.paramName = paramName;
        }
       @Override
       public boolean equals(Object other) {
            if (this == other) return true;
            if ( !(other instanceof PSActionMenuParamPK) ) return false;

            final PSActionMenuParamPK pk = (PSActionMenuParamPK) other;

            if ( pk.actionId != actionId) {
                return false;
            }

           return pk.paramName.equals(paramName);
       }

        public int hashCode() {
            int result;
            result = paramName.hashCode();
            result = 31 * (result + actionId);
            return result;
        }

        public int getActionId() {
            return actionId;
        }

        public void setActionId(int actionId) {
            this.actionId = actionId;
        }

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }
    }

    @EmbeddedId
    PSActionMenuParamPK actionParamPK;

    @MapsId("actionId")
    @ManyToOne
    @JoinColumn(name = "ACTIONID")
    private PSActionMenu menu;

    @Column(name="PARAMVALUE")
    private String paramValue;

    @Column(name="DESCRIPTION")
    private String description;

    public PSActionMenuParam(){}

    public PSActionMenuParam(int actionId, String param, String value) {
        if (this.actionParamPK == null){
            this.actionParamPK = new PSActionMenuParamPK();
        }
        this.actionParamPK.actionId = actionId;
        this.actionParamPK.paramName = param;
        this.paramValue = value;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PSActionMenu getMenu() {
        return menu;
    }

    public void setMenu(PSActionMenu menu) {
        this.menu = menu;
    }

    public PSActionMenuParamPK getActionParamPK() {
        return actionParamPK;
    }

    public void setActionParamPK(PSActionMenuParamPK actionParamPK) {
        this.actionParamPK = actionParamPK;
    }
}
