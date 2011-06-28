package gov.nasa.worldwind.ogc.collada;

import java.io.IOException;

public interface ColladaFilePathResolver
{
    String resolveFilePath(String path) throws IOException;
}
