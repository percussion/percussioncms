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

package com.percussion.category.data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author chriswright
 *
 */
@Entity
@Table(name="CT_PAGE_PAGE_CATEGORIES_SET")
public class PSCategoryEntity {

    @Id
    @Column(name="CONTENTID")
    private int id;

    @Column(name="REVISIONID")
    private int revisionId;

    @Column(name="SORTRANK")
    private int sortRank;

    @Basic
    @Column(name="PAGE_CATEGORIES_TREE")
    private String pageCategoriesTree;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(int revisionId) {
        this.revisionId = revisionId;
    }

    public int getSortRank() {
        return sortRank;
    }

    public void setSortRank(int sortRank) {
        this.sortRank = sortRank;
    }

    public String getPageCategoriesTree() {
        return pageCategoriesTree;
    }

    public void setPageCategoriesTree(String pageCategoriesTree) {
        this.pageCategoriesTree = pageCategoriesTree;
    }
}
