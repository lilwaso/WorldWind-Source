/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.ogc.kml.gx.GXParserContext;
import gov.nasa.worldwind.util.xml.BasicXMLEventParserContext;
import gov.nasa.worldwind.util.xml.atom.AtomParserContext;
import gov.nasa.worldwind.util.xml.xal.XALParserContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

/**
 * The parser context for KML and KMZ docuements. Specifies the classes used to parse each type of KML element.
 *
 * @author jfb
 * @version $Id:
 */
public class ColladaParserContext extends BasicXMLEventParserContext
{
    /** The key used to identify the coordinates parser in the parser context's parser map. */
    protected static QName COORDINATES = new QName("Coordinates");

    /** The names of elements that contain merely string data and can be parsed by a generic string parser. */
    protected static final String[] StringFields = new String[]
        {
            "author",
            "name",
            "authoring_tool",
            "source_data",
            "init_from",
            "created",
            "modified",
            "up_axis",
        };

    /** The names of elements that contain merely double data and can be parsed by a generic double parser. */
    protected static final String[] DoubleFields = new String[]
        {
            "revision",
        };

    /** The names of elements that contain merely integer data and can be parsed by a generic integer parser. */
    protected static final String[] IntegerFields = new String[]
        {
            "drawOrder",
            "meter",
        };

    /**
     * The names of elements that contain merely boolean integer (0 or 1) data and can be parsed by a generic boolean
     * integer parser.
     */
    protected static final String[] BooleanFields = new String[]
        {
            "extrude",
        };

    /**
     * Creates a parser context instance.
     *
     * @param eventReader      the event reader from which to read events.
     * @param defaultNamespace the default namespace. If null, {@link gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}
     */
    public ColladaParserContext(XMLEventReader eventReader, String defaultNamespace)
    {
        super(eventReader, defaultNamespace != null ? defaultNamespace : ColladaConstants.COLLADA_NAMESPACE);
    }

    /**
     * Creates a parser context instance.
     *
     * @param defaultNamespace the default namespace. If null, {@link gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}
     */
    public ColladaParserContext(String defaultNamespace)
    {
        this(null, defaultNamespace);
    }

    public ColladaParserContext(ColladaParserContext ctx)
    {
        super(ctx);
    }

    /**
     * Loads the parser map with the parser to use for each element type. The parser may be changed by calling {@link
     * #registerParser(javax.xml.namespace.QName, gov.nasa.worldwind.util.xml.XMLEventParser)}.
     */
    protected void initializeParsers()
    {
        super.initializeParsers();

        //this.parsers.put(COORDINATES, new KMLCoordinatesParser());

        this.initializeVersion2dot2Parsers();
        this.initializeCompanionParsers();

        // Set up to handle old versions.
        this.initializeVersion2dot1Parsers();
        this.initializeVersion2dot0Parsers();
    }

    protected void initializeVersion2dot2Parsers()
    {
        this.initializeParsers(ColladaConstants.COLLADA_NAMESPACE);
//        this.initializeParsers(KMLConstants.KML_GOOGLE_2dot2_NAMESPACE);
    }

    protected void initializeParsers(String ns)
    {
        this.parsers.put(new QName(ns, "unit"), new ColladaUnit(ns));
        this.parsers.put(new QName(ns, "material"), new ColladaMaterial(ns));
        this.parsers.put(new QName(ns, "instance_effect"), new ColladaInstanceEffect(ns));
        this.parsers.put(new QName(ns, "technique"), new ColladaTechnique(ns));
        this.parsers.put(new QName(ns, "surface"), new ColladaSurface(ns));
        this.parsers.put(new QName(ns, "format"), new ColladaFormat(ns));
        this.parsers.put(new QName(ns, "sampler2D"), new ColladaSampler2D(ns));
        this.parsers.put(new QName(ns, "source"), new ColladaSource(ns));
        this.parsers.put(new QName(ns, "param"), new ColladaParam(ns));
        this.parsers.put(new QName(ns, "float_array"), new ColladaFloatArray(ns));
        this.parsers.put(new QName(ns, "input"), new ColladaInput(ns));
        this.parsers.put(new QName(ns, "effect"), new ColladaEffect(ns));
        this.parsers.put(new QName(ns, "profile_COMMON"), new ColladaProfileCommon(ns));
        this.parsers.put(new QName(ns, "newparam"), new ColladaNewParam(ns));
        this.parsers.put(new QName(ns, "phong"), new ColladaPhong(ns));
        this.parsers.put(new QName(ns, "diffuse"), new ColladaDiffuse(ns));
        this.parsers.put(new QName(ns, "mesh"), new ColladaMesh(ns));
        this.parsers.put(new QName(ns, "technique_COMMON"), new ColladaTechniqueCommon(ns));
        this.parsers.put(new QName(ns, "technique_common"), new ColladaTechniqueCommon(ns));
        this.parsers.put(new QName(ns, "accessor"), new ColladaAccessor(ns));
        this.parsers.put(new QName(ns, "p"), new ColladaP(ns));
        this.parsers.put(new QName(ns, "texture"), new ColladaTexture(ns));
        this.parsers.put(new QName(ns, "color"), new ColladaColor(ns));
        this.parsers.put(new QName(ns, "geometry"), new ColladaGeometry(ns));
        this.parsers.put(new QName(ns, "vertices"), new ColladaVertices(ns));
        this.parsers.put(new QName(ns, "instance_geometry"), new ColladaInstanceGeometry(ns));
        this.parsers.put(new QName(ns, "instance_material"), new ColladaInstanceMaterial(ns));
        this.parsers.put(new QName(ns, "bind"), new ColladaBind(ns));
        this.parsers.put(new QName(ns, "node"), new ColladaNode(ns));
        this.parsers.put(new QName(ns, "bind_material"), new ColladaBindMaterial(ns));
        this.parsers.put(new QName(ns, "scene"), new ColladaScene(ns));

        this.parsers.put(new QName(ns, "image"), new ColladaImage(ns));
        this.parsers.put(new QName(ns, "asset"), new ColladaAsset(ns));
        this.parsers.put(new QName(ns, "contributor"), new ColladaContributor(ns));

        this.parsers.put(new QName(ns, "library_geometries"), new ColladaLibraryGeometries(ns));
        this.parsers.put(new QName(ns, "library_effects"), new ColladaLibraryEffects(ns));
        this.parsers.put(new QName(ns, "library_images"), new ColladaLibraryImages(ns));
        this.parsers.put(new QName(ns, "library_materials"), new ColladaLibraryMaterials(ns));
        this.parsers.put(new QName(ns, "library_visual_scenes"), new ColladaLibraryVisualScenes(ns));
        this.parsers.put(new QName(ns, "instance_visual_scene"), new ColladaInstanceVisualScene(ns));

        this.parsers.put(new QName(ns, "visual_scene"), new ColladaVisualScene(ns));
        this.parsers.put(new QName(ns, "triangles"), new ColladaTriangles(ns));

        this.addStringParsers(ns, StringFields);
        this.addDoubleParsers(ns, DoubleFields);
        this.addIntegerParsers(ns, IntegerFields);
        this.addBooleanParsers(ns, BooleanFields);
    }

    protected void initializeVersion2dot1Parsers()
    {
        // Just add all the default parsers. // TODO: Check for differences between 2.0 and 2.1
//        this.initializeParsers(KMLConstants.KML_2dot1_NAMESPACE);
    }

    protected void initializeVersion2dot0Parsers()
    {
//        String ns = KMLConstants.KML_2dot0_NAMESPACE;
//
//        // Just add all the default parsers. // TODO: Check for differences between 2.0 and 2.1
//        for (Map.Entry<QName, XMLEventParser> entry : this.parsers.entrySet())
//        {
//            this.parsers.put(new QName(ns, entry.getKey().getLocalPart()), entry.getValue());
//        }
    }

    protected void initializeCompanionParsers()
    {
        this.parsers.putAll(GXParserContext.getDefaultParsers());
        this.parsers.putAll(AtomParserContext.getDefaultParsers());
        this.parsers.putAll(XALParserContext.getDefaultParsers());
    }

//    /**
//     * Get the default coordinates parser.
//     *
//     * @return the default coordinates parser.
//     */
//    public KMLCoordinatesParser getCoordinatesParser()
//    {
//        if (this.coordinatesParser == null)
//            this.coordinatesParser = (KMLCoordinatesParser) this.getParser(COORDINATES);
//
//        return this.coordinatesParser;
//    }
}
