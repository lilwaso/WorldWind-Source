/*
Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.gdal;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.util.*;

import java.io.File;

/**
 * @author Lado Garakanidze
 * @version $Id: GDALLibraryFinder.java 15568 2011-06-07 06:42:05Z garakl $
 */
class GDALLibraryFinder extends GDALAbstractFileFilter
{
    protected final String libExtension;

    public GDALLibraryFinder()
    {
        super("gdal");
        this.libExtension = Configuration.isWindowsOS() ? ".dll" : (Configuration.isMacOS() ? ".jnilib" : ".so");
    }

    public boolean accept(File pathname)
    {
        String filename;
        String dir;
        if (null != pathname
            && !isHidden(pathname.getAbsolutePath())
            && null != (dir = pathname.getParent())
            && !this.listFolders.contains(dir)                  // skip already discovered
            && null != (filename = pathname.getName())          // get folder name
            && !filename.startsWith(".")
            && null != (filename = filename.toLowerCase())      // change to lower case
            && filename.contains(this.searchPattern)
            && filename.endsWith(this.libExtension)
            && this.canLoad(pathname.getAbsolutePath())
            )
        {
            this.listFolders.add(dir);
            return true;
        }
        Thread.yield();
        return false;
    }

    /**
     * Attempts to load the specified filename from the local file system as a dynamic library. The filename argument
     * must be a complete path name.
     *
     * @param pathToLibrary - the file to load
     *
     * @return TRUE if the file is loadable library
     */
    protected boolean canLoad(String pathToLibrary)
    {
        try
        {
            System.load(pathToLibrary);

            return true;
        }
        catch (Throwable t)
        {
            Logging.logger().finest(WWUtil.extractExceptionReason(t));
        }
        return false;
    }
}
