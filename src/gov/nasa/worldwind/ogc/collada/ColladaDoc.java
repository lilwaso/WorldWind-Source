/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import java.io.*;

public interface ColladaDoc
{
    /**
      * Returns an {@link java.io.InputStream} to the associated KML document within either a KML file or stream or a KMZ file or
      * stream.
      * <p/>
      * Implementations of this interface do not close the stream; the user of the class must close the stream.
      *
      * @return an input stream positioned to the head of the KML document.
      *
      * @throws java.io.IOException if an error occurs while attempting to create or open the input stream.
      */
     InputStream getKMLStream() throws IOException;

    void        closeStream();
}
