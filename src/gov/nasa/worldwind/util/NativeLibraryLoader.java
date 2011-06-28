/*
Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util;

import gov.nasa.worldwind.exception.WWRuntimeException;

import java.io.File;

/**
 * @author Lado Garakanidze
 * @version $Id: NativeLibraryLoader.java 14274 2010-12-18 22:59:39Z garakl $
 */

public class NativeLibraryLoader
{
    public static void loadLibrary(String libName) throws WWRuntimeException, IllegalArgumentException
    {
        if (WWUtil.isEmpty(libName))
        {
            String message = Logging.getMessage("nullValue.LibraryIsNull");
//            Logging.logger().finest(message);
            throw new IllegalArgumentException(message);
        }

        // try first to load it by it's short name and let JVM and OS to do it's work
        try
        {
            System.loadLibrary(libName);
//            Logging.logger().finest(Logging.getMessage("generic.LibraryLoadedOK", libName));
        }
        catch (java.lang.UnsatisfiedLinkError ule)
        {
            String message = Logging.getMessage("generic.LibraryNotLoaded", libName, ule.getMessage());
//            Logging.logger().finest(message);
            throw new WWRuntimeException(message);
        }
        catch (Throwable t)
        {
            String message = Logging.getMessage("generic.LibraryNotLoaded", libName, t.getMessage());
//            Logging.logger().finest(message);
            throw new WWRuntimeException(message);
        }
    }

    /**
     * Attempt to locate and load a dynamic library specified by a "libraryName" parameter
     *
     * @param libraryName The "libraryName" could be a library name or a full file name (but not a path)
     * @param paths       The "paths" parameter is optional and specifies paths where to look, and it will also search
     *                    sub-directories
     *
     * @throws WWRuntimeException       if attempt to load filed
     * @throws IllegalArgumentException if the libraryName parameter is null or empty
     */
    public static void findAndLoadLibrary(String libraryName, String[] paths)
        throws WWRuntimeException, IllegalArgumentException
    {
        // let's get a full name of the lib and try to load by it's full name

        String libFullName = makeFullLibName(libraryName);
        if (WWUtil.isEmpty(libFullName))
        {
            String message = Logging.getMessage("nullValue.LibraryIsNull");
            Logging.logger().finest(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            System.loadLibrary(libFullName);
//            Logging.logger().finest(Logging.getMessage("generic.LibraryLoadedOK", libFullName));

            return; // GOOD! Leaving now
        }
        catch (java.lang.UnsatisfiedLinkError ule)
        {
        }

        if (!WWUtil.isEmpty(paths))
        {
            for (String path : paths)
            {
                try
                {
                    String library = path + ((path.endsWith(File.separator)) ? "" : File.separator) + libFullName;
                    if (new File(library).exists())
                    {
                        System.load(library);
//                        String message = Logging.getMessage("generic.LibraryLoadedOK", library);
//                        Logging.logger().finest(message);

                        return;
                    }
                }
                catch (java.lang.UnsatisfiedLinkError ule)
                {
                    Logging.logger().finest(ule.getMessage());
                }
            }
        }

        // no joy if we are here
        String reason = Logging.getMessage("generic.FileNotFound", libraryName);
        String message = Logging.getMessage("generic.LibraryNotLoaded", libraryName, reason);
//        Logging.logger().finest( message );
        throw new WWRuntimeException(message);
    }

    public static void load(String libName, String[] paths) throws WWRuntimeException, IllegalArgumentException
    {
        if (WWUtil.isEmpty(libName))
        {
            String message = Logging.getMessage("nullValue.LibraryIsNull");
            Logging.logger().finest(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            // try first to load it by it's short name and let JVM and OS to do it's work
            loadLibrary(libName);

            return;
        }
        catch (IllegalArgumentException iae)
        {
            throw iae;
        }
        catch (Throwable t)
        {
        }

        findAndLoadLibrary(libName, paths);
    }

    protected static String makeFullLibName(String libName)
    {
        if (WWUtil.isEmpty(libName))
            return null;

        if (gov.nasa.worldwind.Configuration.isWindowsOS())
        {
            if (!libName.toLowerCase().endsWith(".dll"))
                return libName + ".dll";
        }
        else if (gov.nasa.worldwind.Configuration.isMacOS())
        {
            if (!libName.toLowerCase().endsWith(".jnilib") && !libName.toLowerCase().startsWith("lib"))
                return "lib" + libName + ".jnilib";
        }
        else if (gov.nasa.worldwind.Configuration.isUnixOS())  // covers Solaris and Linux
        {
            if (!libName.toLowerCase().endsWith(".so") && !libName.toLowerCase().startsWith("lib"))
                return "lib" + libName + ".so";
        }
        return libName;
    }
}
