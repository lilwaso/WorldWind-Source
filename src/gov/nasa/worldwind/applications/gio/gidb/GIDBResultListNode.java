/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.catalogui.ResultList;
import gov.nasa.worldwind.applications.gio.catalogui.ResultModel;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.catalogui.AVListNode;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.AbstractTreeTableNode;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * @author dcollins
 * @version $Id: GIDBResultListNode.java 5551 2008-07-18 23:50:47Z dcollins $
 */
public class GIDBResultListNode extends AbstractTreeTableNode
{
    private ResultList resultList;
    private String sortKey;
    private boolean isSortChildren;

    public GIDBResultListNode(ResultList resultList)
    {
        if (resultList == null)
        {
            String message = "nullValue.ResultListIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.resultList = resultList;

        setAllowsChildren(true);
        setLeaf(false);
        setSortChildren(true);
        setSortKey(CatalogKey.TITLE);
        update();
    }

    public Object getValue(String key)
    {
        return null;
    }

    public boolean isSortChildren()
    {
        return this.isSortChildren;
    }

    public void setSortChildren(boolean sortChildren)
    {
        this.isSortChildren = sortChildren;
    }

    public String getSortKey()
    {
        return this.sortKey;
    }

    public void setSortKey(String sortKey)
    {
        this.sortKey = sortKey;
    }

    public void update()
    {
        List<AVListNode> children = new ArrayList<AVListNode>();
        for (ResultModel result : this.resultList)
            if (result instanceof GIDBResultModel)
                children.add(new GIDBResultNode((GIDBResultModel) result));
        if (this.isSortChildren)
            Collections.sort(children, new AVListNode.KeyComparator(this.sortKey));
        setChildren(children);
    }
}