/* Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.util.layertree;

import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.tree.*;

/**
 * A tree model representing <code>{@link gov.nasa.worldwind.layers.Layer}</code> objects and their content.
 * <code>LayerTreeModel</code> initializes itself with a default root node. By default, this root node is of type
 * <code>BasicTreeNode</code> and its text is "Layers". Nodes added under the tree model's root should always be of type
 * <code>{@link LayerTreeNode}</code>. a <code>LayerTreeNode</code> may be of any type.
 * <p/>
 * <code>LayerTreeModel</code> provides operations for performing the following common tasks on a layer tree: <ul>
 * <li>Adding a layer node.</li> <li>Removing all layer nodes.</li> <li>Refreshing the layer nodes from a <code>{@link
 * gov.nasa.worldwind.layers.LayerList}</code>.</li> </ul>
 *
 * @author dcollins
 * @version $Id: LayerTreeModel.java 14917 2011-03-09 17:40:49Z dcollins $
 */
public class LayerTreeModel extends BasicTreeModel
{
    /** The default root name: "Layers". */
    protected static final String DEFAULT_ROOT_NAME = "Layers";

    /** Creates a new <code>LayerTreeModel</code> with the default root node. Otherwise the new model is empty. */
    public LayerTreeModel()
    {
        this.initialize();
    }

    /**
     * Creates a new <code>LayerTreeModel</code> with the default root node and adds a new <code>LayerTreeNode</code>
     * for each <code>Layer</code> in the specified <code>layerList</code>.
     *
     * @param layerList the list of <code>Layer</code> objects to the new model represents.
     *
     * @throws IllegalArgumentException if the <code>layerList</code> is <code>null</code>.
     */
    public LayerTreeModel(LayerList layerList)
    {
        if (layerList == null)
        {
            String message = Logging.getMessage("nullValue.LayersListArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.initialize();
        this.refresh(layerList);
    }

    /** Initializes this tree model with the default root node. */
    protected void initialize()
    {
        this.setRoot(this.createRootNode());
    }

    /**
     * Returns a new root <code>TreeNode</code> for this tree model. Called from <code>initialize</code>.
     *
     * @return a new <code>TreeNode</code>.
     */
    protected TreeNode createRootNode()
    {
        return new BasicTreeNode(DEFAULT_ROOT_NAME);
    }

    /**
     * Adds the specified <code>layerNode</code> to this tree model's root node. Nodes added under this tree model's
     * root should always be of type <code>{@link LayerTreeNode}</code>.
     *
     * @param layerNode the layer node to add.
     *
     * @throws IllegalArgumentException if the <code>layerNode</code> is <code>null</code>.
     */
    public void addLayer(LayerTreeNode layerNode)
    {
        if (layerNode == null)
        {
            String message = Logging.getMessage("nullValue.TreeNodeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.getRoot().addChild(layerNode);
    }

    /**
     * Adds the a new <code>LayerTreeNode</code> created with the specified <code>layer</code> to this tree model's root
     * node. Nodes added under this tree model's root should always be of type <code>{@link LayerTreeNode}</code>.
     *
     * @param layer the layer to add.
     *
     * @return the <code>LayerTreeNode</code> created for the specified <code>layer</code>.
     *
     * @throws IllegalArgumentException if the <code>layer</code> is <code>null</code>.
     */
    public LayerTreeNode addLayer(Layer layer)
    {
        if (layer == null)
        {
            String message = Logging.getMessage("nullValue.LayerIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        LayerTreeNode layerNode = this.createLayerNode(layer);
        if (layerNode == null)
            return layerNode;

        this.addLayer(layerNode);
        return layerNode;
    }

    /**
     * Returns a new root <code>LayerTreeNode</code> for the speciifed <code>layer</code>. Called from
     * <code>addLayer(Layer)</code>.
     *
     * @param layer the <code>Layer</code> to create a new <code>LayerTreeNode</code> for.
     *
     * @return a new <code>LayerTreeNode</code>.
     */
    protected LayerTreeNode createLayerNode(Layer layer)
    {
        return new LayerTreeNode(layer);
    }

    /** Clears this tree model by removing all children of the root node. */
    public void removeAllLayers()
    {
        this.getRoot().removeAllChildren();
    }

    /**
     * Refreshes this tree model's layer nodes with the specified <code>layerList</code>. Clears this tree model by
     * removing all children of the root node, then adds a new <code>LayerTreeNode</code> for each <code>Layer</code> in
     * the specified <code>layerList</code>.
     *
     * @param layerList the list of <code>Layer</code> objects to the new model represents.
     *
     * @throws IllegalArgumentException if the <code>layerList</code> is <code>null</code>.
     */
    public void refresh(LayerList layerList)
    {
        if (layerList == null)
        {
            String message = Logging.getMessage("nullValue.LayersListArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Replace all the layer nodes in the tree with nodes for the current layers.
        this.removeAllLayers();

        for (Layer layer : layerList)
        {
            this.addLayer(layer);
        }
    }
}
