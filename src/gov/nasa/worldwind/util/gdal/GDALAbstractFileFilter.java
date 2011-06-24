/*
Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.gdal;

import gov.nasa.worldwind.util.*;

import java.io.File;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * @author Lado Garakanidze
 * @version $Id: GDALAbstractFileFilter.java 15568 2011-06-07 06:42:05Z garakl $
 */

abstract class GDALAbstractFileFilter implements java.io.FileFilter
{
    protected HashSet<String> listFolders = new HashSet<String>();
    protected final String searchPattern;

    protected GDALAbstractFileFilter(String searchPattern)
    {
        if (null == searchPattern || searchPattern.length() == 0)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.searchPattern = searchPattern;

        listFolders.clear();
    }

    protected boolean isHidden(String path)
    {
        if (!WWUtil.isEmpty(path))
        {
            String[] folders = path.split(Pattern.quote(File.separator));
            if (!WWUtil.isEmpty(folders))
            {
                for (String folder : folders)
                {
                    if (!WWUtil.isEmpty(folder) && folder.startsWith("."))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String[] getFolders()
    {
        String[] folders = new String[listFolders.size()];
        return this.listFolders.toArray(folders);
    }
}