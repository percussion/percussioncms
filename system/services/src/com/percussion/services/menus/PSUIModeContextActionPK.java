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

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import java.io.Serializable;
import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSUIModeContextActionPK)) return false;
        PSUIModeContextActionPK that = (PSUIModeContextActionPK) o;
        return getActionId() == that.getActionId() && getModeId() == that.getModeId() && getContextId() == that.getContextId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getActionId(), getModeId(), getContextId());
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
