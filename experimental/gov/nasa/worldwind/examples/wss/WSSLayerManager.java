/*
Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples.wss;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.examples.kml.KMLApplicationController;
import gov.nasa.worldwind.examples.util.*;
import gov.nasa.worldwind.examples.util.layertree.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.ogc.ows.*;
import gov.nasa.worldwind.ogc.wfs.WFSFeatureType;
import gov.nasa.worldwind.ogc.wss.*;
import gov.nasa.worldwind.upload.HTTPFileUpload;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import java.net.*;
import java.util.logging.Level;

/**
 * @author Lado Garakanidze
 * @version $Id: WSSLayerManager.java 15580 2011-06-08 08:36:06Z garakl $
 */

public class WSSLayerManager extends ApplicationTemplate
{
    protected static final String DEFAULT_WSS_SERVER = "http://www.nasa.network.com/wss?";
//    protected static final String DEFAULT_WSS_SERVER = "http://localhost:8000/wss?";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected LayerTree layerTree;
        protected RenderableLayer hiddenLayer;

        protected HotSpotController hotSpotController;
        protected KMLApplicationController kmlAppController;
        protected BalloonController balloonController;

        public String urlToWSS = DEFAULT_WSS_SERVER;

////        protected static final String DEFAULT_WSS_SERVER = "http://www.nasa.network.com/wss?";
//        protected static final String DEFAULT_WSS_SERVER = "http://localhost:8000/wss?";

        public AppFrame()
        {
            super(true, false, false); // Don't include the layer panel; we're using the on-screen layer tree.

            // Add the on-screen layer tree, refreshing model with the WorldWindow's current layer list. We
            // intentionally refresh the tree's model before adding the layer that contains the tree itself. This
            // prevents the tree's layer from being displayed in the tree itself.
            this.layerTree = new LayerTree();
            this.layerTree.getModel().refresh(this.getWwd().getModel().getLayers());

            // Set up a layer to display the on-screen layer tree in the WorldWindow. This layer is not displayed in
            // the layer tree's model. Doing so would enable the user to hide the layer tree display with no way of
            // bringing it back.
            this.hiddenLayer = new RenderableLayer();
            this.hiddenLayer.addRenderable(this.layerTree);
            this.getWwd().getModel().getLayers().add(this.hiddenLayer);

            // Add a controller to handle input events on the layer selector and on browser balloons.
            this.hotSpotController = new HotSpotController(this.getWwd());

            // Add a controller to handle common KML application events.
            this.kmlAppController = new KMLApplicationController(this.getWwd());

            // Add a controller to display balloons when placemarks are clicked. We override the method addDocumentLayer
            // so that loading a KML document by clicking a KML balloon link displays an entry in the on-screen layer
            // tree.
            this.balloonController = new BalloonController(this.getWwd())
            {
                @Override
                protected void addDocumentLayer(KMLRoot document)
                {
                    addKMLLayer(document);
                }
            };

            // Give the KML app controller a reference to the BalloonController so that the app controller can open
            // KML feature balloons when feature's are selected in the on-screen layer tree.
            this.kmlAppController.setBalloonController(balloonController);

            // Size the World Window to take up the space typically used by the layer panel.
            Dimension size = new Dimension(1000, 600);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);

            this.makeMenu(this);
        }

        /**
         * Adds the specified <code>kmlRoot</code> to this app frame's <code>WorldWindow</code> as a new
         * <code>Layer</code>, and adds a new <code>KMLLayerTreeNode</code> for the <code>kmlRoot</code> to this app
         * frame's on-screen layer tree.
         * <p/>
         * This expects the <code>kmlRoot</code>'s the <code>AVKey.DISPLAY_NAME</code> field to contain a display name
         * suitable for use as a layer name and the <code>AVKey.WFS_URL</code> field to contain URL instance with the
         * GetFeature WFS request the KML was obtained with
         *
         * @param kmlRoot the KMLRoot to add a new layer for.
         */
        protected void addKMLLayer(KMLRoot kmlRoot)
        {
            // Create a KMLController to adapt the KMLRoot to the World Wind renderable interface.
            KMLController kmlController = new KMLController(kmlRoot);

            // Adds a new layer containing the KMLRoot to the end of the WorldWindow's layer list.
            // This retrieve the layer name from the KMLRoot's DISPLAY_NAME field.
            RenderableLayer layer = new RenderableLayer();

            String layerName = (String) kmlRoot.getField(AVKey.DISPLAY_NAME);
            URL wfsURL = (URL) kmlRoot.getField(AVKey.WFS_URL);

            layer.setValue(AVKey.WFS_URL, wfsURL);
            layer.setName(layerName);
            layer.addRenderable(kmlController);
            if (this.layerAlreadyExists(layerName, wfsURL))
                return;

            this.getWwd().getModel().getLayers().add(layer);

            // Adds a new layer tree node for the KMLRoot to the on-screen layer tree, and makes the new node visible
            // in the tree. This also expands any tree paths that represent open KML containers or open KML network
            // links.
            KMLLayerTreeNode layerNode = new KMLLayerTreeNode(layer, kmlRoot);
            this.layerTree.getModel().addLayer(layerNode);
            this.layerTree.makeVisible(layerNode.getPath());
            layerNode.expandOpenContainers(this.layerTree);

            // Listens to refresh property change events from KML network link nodes. Upon receiving such an event this
            // expands any tree paths that represent open KML containers. When a KML network link refreshes, its tree
            // node replaces its children with new nodes created form the refreshed content, then sends a refresh
            // property change event through the layer tree. By expanding open containers after a network link refresh,
            // we ensure that the network link tree view appearance is consistent with the KML specification.
            layerNode.addPropertyChangeListener(AVKey.RETRIEVAL_STATE_SUCCESSFUL, new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent event)
                {
                    if (event.getSource() instanceof KMLNetworkLinkTreeNode)
                    {
                        ((KMLNetworkLinkTreeNode) event.getSource()).expandOpenContainers(layerTree);
                    }
                }
            });
        }

        /**
         * Check for existing layer (if both layers have the same name and the same GetFeature URL)
         *
         * @param layerName A name of the layer
         * @param wfsURL    A URL with the WFS GetFeature request
         *
         * @return <code>true</code> if there is a layer with the same
         */
        protected boolean layerAlreadyExists(String layerName, URL wfsURL)
        {
            if (WWUtil.isEmpty(layerName))
            {
                String message = Logging.getMessage("nullValue.LayerNamesIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            gov.nasa.worldwind.layers.LayerList layers = this.getWwd().getModel().getLayers();
            if (null != layers && layers.size() > 0)
            {
                for (Layer l : layers)
                {
                    if (layerName.equalsIgnoreCase(l.getName())
                        && l.hasKey(AVKey.WFS_URL)
                        && WWURL.areEqual(wfsURL, (URL) l.getValue(AVKey.WFS_URL))
                        )
                    {
                        return true;
                    }
                }
            }

            return false;
        }

        protected void makeMenu(final AppFrame appFrame)
        {
            JMenuBar menuBar = new JMenuBar();
            this.setJMenuBar(menuBar);
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);

            JMenuItem openURLMenuItem = new JMenuItem(new AbstractAction("Connect to WSS ...")
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    try
                    {
                        String wssURL = JOptionPane.showInputDialog(appFrame,
                            "Enter URL of the WSS server", appFrame.urlToWSS);

                        if (!WWUtil.isEmpty(wssURL))
                        {
                            appFrame.urlToWSS = wssURL.trim();
                            new WorkerThread(appFrame.urlToWSS, appFrame).start();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });

            fileMenu.add(openURLMenuItem);

            JMenuItem uploadFileMenuItem = new JMenuItem(new AbstractAction("Upload shape(s) to WSS ...")
            {
                File currentFolder = new File(Configuration.getUserHomeDirectory());
                FileFilter kmlFiles = new FileFilter()
                {
                    @Override
                    public boolean accept(File file)
                    {
                        String ext = WWIO.getSuffix(file.getName());
                        return (ext != null && ("KML".equalsIgnoreCase(ext) || "KMZ".equalsIgnoreCase(ext)));
                    }

                    @Override
                    public String getDescription()
                    {
                        return "Keyhole Markup Language files";
                    }
                };

                public void actionPerformed(ActionEvent actionEvent)
                {
                    try
                    {
                        JFileChooser fc = new JFileChooser(currentFolder);
                        fc.setAcceptAllFileFilterUsed(false);
                        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        fc.setMultiSelectionEnabled(false);
                        fc.addChoosableFileFilter(kmlFiles);

                        fc.setDialogTitle("Select KML or KMZ file");

                        int returnVal = fc.showOpenDialog(appFrame);
                        if (returnVal == JFileChooser.APPROVE_OPTION)
                        {
                            this.currentFolder = fc.getCurrentDirectory();

                            File file = fc.getSelectedFile();

                            HTTPFileUpload uploader = new HTTPFileUpload(new URL(appFrame.urlToWSS));

                            AVList params = new AVListImpl();
                            params.setValue(WSS.Param.AUTH_TOKEN, "World_Wind_Allow_File_Upload");

                            uploader.add(file, file.getName(), params );

                            uploader.send();

                            if (uploader.getTotalFilesUploaded() == uploader.getTotalFilesToUpload())
                            {
                                String message = Logging.getMessage("HTTP.FileUploadedOK", file.getName());
                                JOptionPane.showMessageDialog(null, message, "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });

            fileMenu.add(uploadFileMenuItem);
        }
    }

    /** A <code>Thread</code> that loads a KML file and displays it in an <code>AppFrame</code>. */
    protected static class WorkerThread extends Thread
    {
        /** Indicates the source of the KML file loaded by this thread. Initialized during construction. */
        protected Object urlToWSS;
        /** Indicates the <code>AppFrame</code> the KML file content is displayed in. Initialized during construction. */
        protected AppFrame appFrame;

        /**
         * Creates a new worker thread from a specified <code>kmlSource</code> and <code>appFrame</code>.
         *
         * @param urlToWSS the source of the KML file to load. May be a {@link java.io.File}, a {@link java.net.URL}, or
         *                 an {@link java.io.InputStream}, or a {@link String} identifying a file path or URL.
         * @param appFrame the <code>AppFrame</code> in which to display the KML source.
         */
        public WorkerThread(Object urlToWSS, AppFrame appFrame)
        {
            this.urlToWSS = urlToWSS;
            this.appFrame = appFrame;
        }

        /**
         * Loads this worker thread's KML source into a new <code>{@link gov.nasa.worldwind.ogc.kml.KMLRoot}</code>,
         * then adds the new <code>KMLRoot</code> to this worker thread's <code>AppFrame</code>. The
         * <code>KMLRoot</code>'s <code>AVKey.DISPLAY_NAME</code> field contains a display name created from either the
         * KML source or the KML root feature name.
         * <p/>
         * If loading the KML source fails, this prints the exception and its stack trace to the standard error stream,
         * but otherwise does nothing.
         */
        public void run()
        {
            try
            {
                if (WorldWind.getNetworkStatus().isNetworkUnavailable())
                {
                    String message = Logging.getMessage("NetworkStatus.NetworkUnreachable");
                    Logging.logger().severe(message);
                    throw new WWRuntimeException(message);
                }

                if (WWUtil.isEmpty(this.urlToWSS))
                {
                    String message = Logging.getMessage("nullValue.URLIsNull");
                    Logging.logger().severe(message);
                    throw new IllegalArgumentException(message);
                }

                URL url = WWIO.makeURL(this.urlToWSS);
                if (WWUtil.isEmpty(url))
                {
                    String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource",
                        this.urlToWSS.toString());
                    Logging.logger().severe(message);
                    throw new IllegalArgumentException(message);
                }

                String authToken = new WWURL(url).getQueryParameter(WSS.Param.AUTH_TOKEN);

                url = this.composeGetCapabilitiesRequest(url);
                WSSCapabilities caps = new WSSCapabilities(url);
                caps.parse();

                // extract a base url for GetFeature requests
                String getFeatureBaseUrl = this.extractGetFeatureBaseUrlAsString(caps);
                if (WWUtil.isEmpty(getFeatureBaseUrl))
                {
                    String message = Logging.getMessage("generic.CannotParseCapabilities",
                        "GetFeature/DCP/HTTP/Get/href");
                    Logging.logger().severe(message);
                    throw new IllegalArgumentException(message);
                }

                for (WFSFeatureType featureType : caps.getFeatureTypes())
                {
                    for (String name : ((WSSFeatureType) featureType).getNamedFeatures())
                    {
                        URL urlGetFeature = this.composeGetFeatureRequest(getFeatureBaseUrl, name, authToken);

                        if (this.appFrame.layerAlreadyExists(name, urlGetFeature))
                        {
                            // do not even request it, because it is already exists
                            continue;
                        }

                        final KMLRoot kmlRoot = KMLRoot.create(urlGetFeature);
                        if (kmlRoot == null)
                        {
                            String message = Logging.getMessage("generic.UnrecognizedFeature",
                                urlGetFeature.toString());
                            Logging.logger().severe(message);
                            throw new IllegalArgumentException(message);
                        }

                        kmlRoot.parse();
                        kmlRoot.setField(AVKey.DISPLAY_NAME, name);
                        kmlRoot.setField(AVKey.WFS_URL, urlGetFeature);

                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                appFrame.addKMLLayer(kmlRoot);
                            }
                        });
                    }
                }
            }
            catch (Exception e)
            {
                String message = WWUtil.extractExceptionReason(e);
                Logging.logger().log(Level.SEVERE, message, e);

                JOptionPane.showMessageDialog(null, message, "", JOptionPane.ERROR_MESSAGE);
            }
        }

        protected String extractGetFeatureBaseUrlAsString(WSSCapabilities caps)
        {
            String href = null;

            try
            {
                searchBaseUrl:
                {
                    OWSOperationsMetadata omd = caps.getOperationsMetadata();
                    for (OWSOperation op : omd.getOperations())
                    {
                        if ("GetFeature".equalsIgnoreCase(op.getName()))
                        {
                            for (OWSDCPType dcpType : op.getDCPTypes())
                            {
                                for (OWSRequestMethod method : dcpType.getHTTP().getRequestMethods())
                                {
                                    if ("GET".equalsIgnoreCase(method.getRequestType()))
                                    {
                                        href = method.getHref();
                                        break searchBaseUrl;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (Throwable t)
            {
                Logging.logger().log(Level.FINEST, WWUtil.extractExceptionReason(t), t);
            }

            return href;
        }

        protected URL composeGetFeatureRequest(String baseUrl, String featureName, String authToken)
            throws MalformedURLException, IllegalArgumentException
        {
            if (WWUtil.isEmpty(baseUrl))
            {
                String message = Logging.getMessage("nullValue.URLIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (WWUtil.isEmpty(featureName))
            {
                String message = Logging.getMessage("nullValue.FeatureNameIsNullOrEmpty");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            WWURL wssURL = new WWURL(baseUrl);

            wssURL.setQueryParameter(WSS.Param.SERVICE, WSS.Service.WFS);
            wssURL.setQueryParameter(WSS.Param.VERSION, WSS.Version.V2_0_0);
            wssURL.setQueryParameter(WSS.Param.REQUEST, WSS.Request.GetFeature);
            wssURL.setQueryParameter(WSS.Param.TYPE_NAMES, WSS.TypeNames.ALL);
            wssURL.setQueryParameter(WSS.Param.RESOURCE_ID, featureName);
            if (!WWUtil.isEmpty(authToken))
            {
                wssURL.setQueryParameter(WSS.Param.AUTH_TOKEN, authToken);
            }

            return wssURL.getURL();
        }

        protected URL composeGetCapabilitiesRequest(URL urlToWSS)
            throws MalformedURLException, IllegalArgumentException
        {
            WWURL wwURL = new WWURL(urlToWSS);

            wwURL.setQueryParameter(WSS.Param.SERVICE, WSS.Service.WFS);
            wwURL.setQueryParameter(WSS.Param.VERSION, WSS.Version.V2_0_0);
            wwURL.setQueryParameter(WSS.Param.REQUEST, WSS.Request.GetCapabilities);

            return wwURL.getURL();
        }
    }

    public static void main(String[] args)
    {
        //noinspection UnusedDeclaration
        final AppFrame af = (AppFrame) start("World Wind WSS Layer Manager", AppFrame.class);
    }
}