/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.ResultList;
import gov.nasa.worldwind.applications.gio.catalogui.ResultTreeCellRenderer;
import gov.nasa.worldwind.applications.gio.catalogui.ResultTreeTable;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.AbstractTreeTableModel;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.TreeTableModel;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.TreeTableNode;
import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: ESGTreeTable.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ESGTreeTable extends ResultTreeTable
{
    public ESGTreeTable(ResultList resultList, AVList tableParams)
    {
        super(resultList, tableParams);
        ResultTreeCellRenderer treeCellRenderer = new ResultTreeCellRenderer(this);
        setTreeCellRenderer(treeCellRenderer);
    }

    protected void nodeExpanded(TreeTableNode node)
    {
        if (node != null && node instanceof ESGResultNode)
        {
            ((ESGResultNode) node).firePropertyChange(ESGKey.ACTION_COMMAND_GET_SERVICE_DATA);
        }
    }

    protected void reloadResultList()
    {
        TreeTableModel ttm = getTreeTableModel();
        if (ttm != null)
        {
            ESGResultListNode root = new ESGResultListNode(getResultList());
            if (ttm instanceof AbstractTreeTableModel)
            {
                ((AbstractTreeTableModel ) ttm).setRoot(root);
                ((AbstractTreeTableModel ) ttm).reload();
            }
        }
    }
}
