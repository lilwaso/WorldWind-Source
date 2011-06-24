/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.util.xml.*;

/**
 * The abstract base class for most KML classes. Provides parsing and access to the <i>id</i> and <i>targetId</i> fields
 * of KML elements.
 *
 * @author tag
 * @version $Id: KMLAbstractObject.java 15352 2011-04-22 04:37:08Z tgaskins $
 */
public abstract class KMLAbstractObject extends AbstractXMLEventParser
{
    protected KMLAbstractObject()
    {
        super();
    }

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected KMLAbstractObject(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Returns the id of this object, if any.
     *
     * @return the id of this object, or null if it's not specified in the element.
     */
    public String getId()
    {
        return (String) this.getField("id");
    }

    /**
     * Returns the target-id of this object, if any.
     *
     * @return the targetId of this object, or null if it's not specified in the element.
     */
    public String getTargetId()
    {
        return (String) this.getField("targetId");
    }

    @Override
    public KMLRoot getRoot()
    {
        XMLEventParser root = super.getRoot();
        return root instanceof KMLRoot ? (KMLRoot) root : null;
    }
}
