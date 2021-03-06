/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.core;

/**
 * @author tag
 * @version $Id: Version.java 13266 2010-04-10 02:47:09Z tgaskins $
 */
public class Version
{
    private static final String MAJOR_VALUE = "0";
    private static final String MINOR_VALUE = "0";
    private static final String DOT_VALUE = "1";
    private static final String VERSION_NUMBER = MAJOR_VALUE + "." + MINOR_VALUE + "." + DOT_VALUE;
    private static final String VERSION_NAME = "World Window Alpha";
    private static final String RELEASE_DATE = "4 April 2010";

    public static String getVersion()
    {
        return getVersionName() + " " + VERSION_NUMBER;
    }

    public static String getVersionName()
    {
        return VERSION_NAME;
    }

    public static String getVersionNumber()
    {
        return VERSION_NUMBER;
    }

    public static String getVersionMajorNumber()
    {
        return MAJOR_VALUE;
    }

    public static String getVersionMinorNumber()
    {
        return MINOR_VALUE;
    }

    public static String getVersionDotNumber()
    {
        return DOT_VALUE;
    }

    public static String getReleaseDate()
    {
        return RELEASE_DATE;
    }
}
