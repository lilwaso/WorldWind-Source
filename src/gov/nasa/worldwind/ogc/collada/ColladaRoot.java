/*
Copyright (C) 2001, 2011 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.*;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.*;

public class ColladaRoot extends ColladaAbstractObject implements KMLRenderable
{
    /** Reference to the KMLDoc representing the KML or KMZ file. */
    protected ColladaDoc colladaDoc;
    /** The event reader used to parse the document's XML. */
    protected XMLEventReader eventReader;
    /** The input stream underlying the event reader. */
    protected InputStream eventStream;
    /** The parser context for the document. */
    protected ColladaParserContext parserContext;

    /** The Shapes representing the Collada Content. */
    AbstractList<ColladaNodeShape> colladaShapes;

    static int sUniqueID = 0;
    int uniqueID = sUniqueID++;

    public ColladaRoot(File docSource) throws IOException
    {
        super(ColladaConstants.COLLADA_NAMESPACE);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.colladaDoc = new ColladaFile(docSource);

        this.initialize();
    }

    /**
     * Called just before the constructor returns. If overriding this method be sure to invoke
     * <code>super.initialize()</code>.
     *
     * @throws java.io.IOException if an I/O error occurs attempting to open the document source.
     */
    protected void initialize() throws IOException
    {
        this.eventStream = this.getColladaDoc().getKMLStream();
        this.eventReader = this.createReader(this.eventStream);
        if (this.eventReader == null)
            throw new WWRuntimeException(Logging.getMessage("XML.UnableToOpenDocument", this.getColladaDoc()));

        this.parserContext = this.createParserContext(this.eventReader);
    }

    private ColladaDoc getColladaDoc()
    {
        return colladaDoc;
    }

    /**
     * Creates the event reader. Called from the constructor.
     *
     * @param docSource the document source to create a reader for. The type can be any of those supported by {@link
     *                  gov.nasa.worldwind.util.WWXML#openEventReader(Object)}.
     *
     * @return a new event reader, or null if the source type cannot be determined.
     */
    protected XMLEventReader createReader(Object docSource)
    {
        return WWXML.openEventReader(docSource, false);
    }

    /**
     * Invoked during {@link #initialize()} to create the parser context. The parser context is created by the global
     * {@link XMLEventParserContextFactory}.
     *
     * @param reader the reader to associate with the parser context.
     *
     * @return a new parser context.
     */
    protected ColladaParserContext createParserContext(XMLEventReader reader)
    {
        ColladaParserContext ctx = (ColladaParserContext)
            XMLEventParserContextFactory.createParserContext(KMLConstants.COLLADA_MIME_TYPE, this.getNamespaceURI());

        if (ctx == null)
        {
            // Register a parser context for this root's default namespace
            String[] mimeTypes = new String[] {KMLConstants.COLLADA_MIME_TYPE};
            XMLEventParserContextFactory.addParserContext(mimeTypes, new ColladaParserContext(this.getNamespaceURI()));
            ctx = (ColladaParserContext)
                XMLEventParserContextFactory.createParserContext(KMLConstants.COLLADA_MIME_TYPE,
                    this.getNamespaceURI());
        }

        ctx.setEventReader(reader);

        return ctx;
    }

    /**
     * Starts document parsing. This method initiates parsing of the KML document and returns when the full document has
     * been parsed.
     *
     * @param args optional arguments to pass to parsers of sub-elements.
     *
     * @return <code>this</code> if parsing is successful, otherwise  null.
     *
     * @throws javax.xml.stream.XMLStreamException
     *          if an exception occurs while attempting to read the event stream.
     */
    public ColladaRoot parse(Object... args) throws XMLStreamException
    {
        ColladaParserContext ctx = this.parserContext;

        // Create a list of the possible root elements, one for each of the KML namespaces
        List<QName> rootElements = new ArrayList<QName>(ColladaConstants.COLLADA_NAMESPACES.length);
        for (String namespace : ColladaConstants.COLLADA_NAMESPACES)
        {
            rootElements.add(new QName(namespace, "COLLADA"));
        }

        for (XMLEvent event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            // Check the element against each of the possible KML root elements
            for (QName kmlRoot : rootElements)
            {
                if (ctx.isStartElement(event, kmlRoot))
                {
                    super.parse(ctx, event, args);
                    ctx.getEventReader().close();
                    this.closeEventStream();

                    // Listen for property changes in the root feature so that these events can be forwarded
                    // to property change listeners on the KMLRoot.
//                    KMLAbstractFeature feature = this.getFeature();
//                    if (feature != null)
//                        feature.addPropertyChangeListener(this);

                    return this;
                }
            }
        }

        return null;
    }

    /** Closes the event stream associated with this context's XML event reader. */
    protected void closeEventStream()
    {
        try
        {
            this.eventStream.close();
            this.eventStream = null;
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("generic.ExceptionClosingXmlEventReader");
            Logging.logger().warning(message);
        }
    }

    int[] getIntArrayFromString(String floatArrayString)
    {
        String[] arrayOfNumbers = floatArrayString.split(" ");
        int[] ints = new int[arrayOfNumbers.length];

        int i = 0;
        for (String s : arrayOfNumbers)
        {
            ints[i++] = Integer.parseInt(s);
        }

        return ints;
    }

    float[] getFloatArrayFromString(String floatArrayString)
    {
        String[] arrayOfNumbers = floatArrayString.split(" ");
        float[] floats = new float[arrayOfNumbers.length];

        int i = 0;
        for (String s : arrayOfNumbers)
        {
            floats[i++] = Float.parseFloat(s);
        }

        return floats;
    }

    AbstractList<ColladaNodeShape> createScene(ColladaSceneShape mother)
    {
        // assume       <unit name="meter" meter="1"></unit> for now-

        AbstractList<ColladaNodeShape> outShapes = new ArrayList<ColladaNodeShape>();

        ColladaLibraryGeometries geomLib = (ColladaLibraryGeometries) this.getField("library_geometries");
        ColladaLibraryMaterials libraryMaterials = (ColladaLibraryMaterials) this.getField("library_materials");
        ColladaLibraryEffects libraryEffects = (ColladaLibraryEffects) this.getField("library_effects");
        ColladaLibraryImages libraryImages = (ColladaLibraryImages) this.getField("library_images");
        ColladaLibraryVisualScenes visScenes = (ColladaLibraryVisualScenes) this.getField("library_visual_scenes");

        ColladaVisualScene visScene = (ColladaVisualScene) visScenes.getField(
            "visual_scene"); // should filter by URL from sceneURL above

        ArrayList<ColladaNode> nodes = visScene.getNodes();
        for (ColladaNode node : nodes)
        {
            ColladaInstanceGeometry geom = (ColladaInstanceGeometry) node.getField("instance_geometry");
            String urlForGeom = (String) geom.getField("url");
            ColladaBindMaterial bindMaterial = (ColladaBindMaterial) geom.getField("bind_material");
            ColladaTechniqueCommon techniqueCommon = (ColladaTechniqueCommon) bindMaterial.getField("technique_common");
            ArrayList<ColladaInstanceMaterial> materials = techniqueCommon.getMaterials();
            String texture = null;
            File colladaRootFile = ((ColladaFile) this.colladaDoc).colladaFile.getParentFile();

            for (ColladaInstanceMaterial material : materials)
            {
                String name = (String) material.getField("symbol");
                ColladaMaterial materialA = libraryMaterials.getMaterialByName(name);
                ColladaInstanceEffect effect = (ColladaInstanceEffect) materialA.getField("instance_effect");
                ColladaEffect effectA = libraryEffects.getEffectByName((String) effect.getField("url"));
                ColladaProfileCommon profileCommon = (ColladaProfileCommon) effectA.getField("profile_COMMON");
                ColladaTechnique technique = (ColladaTechnique) profileCommon.getField("technique");

                for (ColladaNewParam param : technique.getNewParams())
                {
                    if (param.hasField("surface"))
                    {
                        ColladaSurface surface = (ColladaSurface) param.getField("surface");
                        String imageURL = (String) surface.getField("init_from");
                        ColladaImage image = libraryImages.getImageByName(imageURL);
                        String imageURLB = (String) image.getField("init_from");

                        texture = imageURLB;//getTextureFromImageSource( imageURLB);
                    }
                    else if (param.hasField("sampler2D"))
                    {
                        // this may be more complicated later
                    }
                }
            }

            ColladaGeometry geomA = geomLib.getGeometryByID(urlForGeom);
            ColladaMesh mesh = (ColladaMesh) geomA.getField("mesh");

            ArrayList<ColladaSource> sources = mesh.getSources();
            ArrayList<ColladaTriangles> triangles = mesh.getTriangles();
            ArrayList<ColladaVertices> vertices = mesh.getVertices();

            for (ColladaTriangles triangle : triangles)
            {
                ColladaNodeShape shape = new ColladaNodeShape(this, (String) node.getField("id"), mother);
                shape.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                shape.setTexture(colladaRootFile, texture);

                shape.setHeading(mother.getHeading());
                shape.setPitch(mother.getPitch());
                shape.setRoll(mother.getRoll());

                outShapes.add(shape);

                int count = Integer.parseInt((String) triangle.getField("count"));

                ArrayList<ColladaInput> inputs = triangle.getInputs();
                for (ColladaInput input : inputs)
                {
                    String semantic = (String) input.getField("semantic");
                    ColladaSource sourceForInput = getSourceFromInput(input, sources, vertices);
                    int inputOffset = input.getOffset();

                    float[] floatData = getFloatArrayFromString((String) ((ColladaFloatArray) sourceForInput.
                        getField("float_array")).getField("CharactersContent"));

                    shape.addSource(semantic, inputOffset, floatData);
                }

                ColladaP elementsList = (ColladaP) triangle.getField("p");

                int[] intData = getIntArrayFromString((String) elementsList.getField("CharactersContent"));
                shape.addElementsList(count, intData);
            }
        }

        return outShapes;
    }

    private ColladaSource getSourceFromInput(ColladaInput input, ArrayList<ColladaSource> sources,
        ArrayList<ColladaVertices> inVertices)
    {
        String name = ((String) input.getField("source")).substring(1);

        for (ColladaVertices vertices : inVertices)           // probably need to have a usage object- since we arent preserving field "smeantic" in Vertices
        {
            if (vertices.getField("id").equals(name))
            {
                ColladaInput inputA = (ColladaInput) vertices.getField("input");
                name = ((String) inputA.getField("source")).substring(1);
                break;
            }
        }

        for (ColladaSource source : sources)
        {
            if (source.getField("id").equals(name))
            {
                return source;
            }
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    AbstractList<ColladaNodeShape> createShape(ColladaSceneShape mother)
    {
        return createScene(mother);
    }

    public void parseShape()
    {
        if (colladaShapes == null)
        {
            colladaShapes = createShape(null);
        }
    }

    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        if (colladaShapes != null)
        {
            for (AbstractGeneralShape colladaShape : colladaShapes)
            {
                colladaShape.render(dc);
            }
        }
    }

    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        if (colladaShapes != null)
        {
            for (AbstractGeneralShape colladaShape : colladaShapes)
            {
                colladaShape.render(dc);
            }
        }
    }

    protected XMLEventParserContext getParserContext()
    {
        return this.parserContext;
    }

    /**
     * Finds a named element in the document.
     *
     * @param id the element's identifer. If null, null is returned.
     *
     * @return the element requested, or null if there is no corresponding element in the document.
     */
    public Object getItemByID(String id)
    {
        return id != null ? this.getParserContext().getIdTable().get(id) : null;
    }

    Position position;

    public void setPosition(Double latitude, Double longitude, Double altitude)
    {
        position = new Position(Angle.fromDegreesLatitude(latitude), Angle.fromDegreesLongitude(longitude), altitude);
    }

    public void closeStream()
    {
        try
        {
            this.eventReader.close();
            this.eventReader = null;
            this.parserContext = null;
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }
        this.colladaDoc.closeStream();
    }
}
