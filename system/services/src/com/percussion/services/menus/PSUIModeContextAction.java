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

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.io.Serializable;


@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE,
        region = "RXMODEUICONTEXTACTION")
@Table(name="RXMODEUICONTEXTACTION")
public class PSUIModeContextAction implements Serializable{

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
