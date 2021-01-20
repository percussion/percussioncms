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
package com.percussion.share.dao;

import java.util.List;

import com.percussion.share.data.IPSItemSummary;

public class MockItemSummary implements IPSItemSummary {

    @Override
    public Category getCategory()
    {
        throw new UnsupportedOperationException("getCategory is not yet supported");
    }

    @Override
    public List<String> getFolderPaths()
    {
        throw new UnsupportedOperationException("getFolderPaths is not yet supported");
    }

    @Override
    public String getIcon()
    {
        throw new UnsupportedOperationException("getIcon is not yet supported");
    }

    @Override
    public String getId()
    {
        throw new UnsupportedOperationException("getId is not yet supported");
    }

    @Override
    public String getName()
    {
        throw new UnsupportedOperationException("getName is not yet supported");
    }

    @Override
    public String getType()
    {
        throw new UnsupportedOperationException("getType is not yet supported");
    }

    @Override
    public boolean isFolder()
    {
        throw new UnsupportedOperationException("isFolder is not yet supported");
    }

    @Override
    public void setCategory(@SuppressWarnings("unused") Category category)
    {
        throw new UnsupportedOperationException("setCategory is not yet supported");
    }

    @Override
    public void setFolderPaths(@SuppressWarnings("unused") List<String> paths)
    {
        throw new UnsupportedOperationException("setFolderPaths is not yet supported");
    }

    @Override
    public void setIcon(@SuppressWarnings("unused") String icon)
    {
        throw new UnsupportedOperationException("setIcon is not yet supported");
    }

    @Override
    public void setId(@SuppressWarnings("unused") String id)
    {
        throw new UnsupportedOperationException("setId is not yet supported");
    }

    @Override
    public void setName(@SuppressWarnings("unused") String name)
    {
        throw new UnsupportedOperationException("setName is not yet supported");
    }

    @Override
    public void setType(@SuppressWarnings("unused") String type)
    {
        throw new UnsupportedOperationException("setType is not yet supported");
    }

    @Override
    public String getLabel()
    {
        throw new UnsupportedOperationException("getLabel is not yet supported");
    }

    @Override
    public void setLabel(@SuppressWarnings("unused") String label)
    {
        throw new UnsupportedOperationException("setLabel is not yet supported");
    }

    @Override
    public boolean isRevisionable()
    {
        throw new UnsupportedOperationException("isRevisionable is not yet supported");
    }

    @Override
    public void setRevisionable(@SuppressWarnings("unused") boolean revisionable)
    {
        throw new UnsupportedOperationException("setRevisionable is not yet supported");
    }

    @Override
    public boolean isPage()
    {
        throw new UnsupportedOperationException("isPage is not yet supported");
    }
    public boolean isResource()
    {
        throw new UnsupportedOperationException("isResource is not yet supported");
    }

}
