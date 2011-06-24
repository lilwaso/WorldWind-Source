/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import java.io.*;

public class ColladaFile implements ColladaDoc
{
    File colladaFile;
    InputStream stream;
    private FileInputStream fileInputStream;

    public ColladaFile(File docSource)
    {
        colladaFile = docSource;
    }

    public InputStream getKMLStream() throws IOException
    {
        try
        {
            fileInputStream = new FileInputStream(this.colladaFile);
            stream =  new BufferedInputStream(fileInputStream,1024*32);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return stream;
    }

    public void closeStream()
    {
        try
        {
            stream.close();
            fileInputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        stream = null;
        fileInputStream = null;
    }
}
