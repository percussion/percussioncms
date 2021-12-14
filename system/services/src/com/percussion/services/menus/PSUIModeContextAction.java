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

package com.percussion.services.menus;


import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
        region = "RXMODEUICONTEXTACTION")
@Table(name="RXMODEUICONTEXTACTION")
public class PSUIModeContextAction implements Serializable{

    @Embeddable
    public class PSUIModeContextActionPK implements Serializable {


        @JoinColumn(name = "ACTIONID")
        private int actionId;


        @JoinColumn(name = "MODEID")
        private int modeId;


        @JoinColumn(name = "UICONTEXTID")
        private int contextId;

        public PSUIModeContextActionPK() {}

        public PSUIModeContextActionPK(int actionId, int modeId, int contextId) {
            this.actionId = actionId;
            this.modeId = modeId;
            this.contextId= contextId;
        }
        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if ( !(other instanceof PSUIModeContextAction.PSUIModeContextActionPK) ) return false;

            final PSUIModeContextAction.PSUIModeContextActionPK pk = (PSUIModeContextAction.PSUIModeContextActionPK) other;

            if ( pk.actionId != actionId &&
                    pk.contextId==contextId &&
                    pk.modeId==modeId &&
                    pk.actionId == actionId) {
                return true;
            }else {
                return false;
            }
        }

        public int hashCode() {
            int result;

            result = 31 * (modeId + actionId + contextId);
            return result;
        }

        public int getActionId() {
            return actionId;
        }

        public void setActionId(int actionId) {
            this.actionId = actionId;
        }

        public int getModeId() {
            return modeId;
        }

        public void setModeId(int modeId) {
            this.modeId = modeId;
        }

        public int getContextId() {
            return contextId;
        }

        public void setContextId(int contextId) {
            this.contextId = contextId;
        }
    }

    @EmbeddedId
    PSUIModeContextActionPK primaryKey;

    @MapsId("actionId")
    @ManyToOne
    private PSActionMenu menu;

    @MapsId("modeId")
    @ManyToOne
    private PSUIMode mode;

    @MapsId("contextId")
    @ManyToOne
    private PSUiContext context;

    public PSUIModeContextAction(int actionId, int modeId, int contextId) {
        this.primaryKey = new PSUIModeContextActionPK(actionId, modeId, contextId);
    }

    public PSActionMenu getMenu() {
        return menu;
    }

    public void setMenu(PSActionMenu menu) {
        this.menu = menu;
    }

    public PSUIMode getMode() {
        return mode;
    }

    public void setMode(PSUIMode mode) {
        this.mode = mode;
    }

    public PSUiContext getContext() {
        return context;
    }

    public void setContext(PSUiContext context) {
        this.context = context;
    }

    public PSUIModeContextAction() { }

    public PSUIModeContextActionPK getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(PSUIModeContextActionPK primaryKey) {
        this.primaryKey = primaryKey;
    }
}
