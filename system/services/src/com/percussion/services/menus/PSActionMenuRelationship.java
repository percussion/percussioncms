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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE,
        region = "RXMENUACTIONRELATION")
@Table(name="RXMENUACTIONRELATION")
public class PSActionMenuRelationship implements Serializable {

    @Id
    @ManyToOne
    @JoinColumn(name = "ACTIONID")
    private PSActionMenu menu;

    @Id
    @ManyToOne
    @JoinColumn(name = "CHILDACTIONID")
    private PSActionMenu child;


    public PSActionMenuRelationship(){}


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSActionMenuRelationship)) return false;
        PSActionMenuRelationship that = (PSActionMenuRelationship) o;
        return menu.equals(that.menu) && child.equals(that.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menu, child);
    }
}
