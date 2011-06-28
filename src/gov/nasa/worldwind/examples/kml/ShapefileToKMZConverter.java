/*
 * Copyright (C) 2001, 2011 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.examples.kml;

import gov.nasa.worldwind.formats.shapefile.Shapefile;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import javax.xml.stream.XMLStreamException;
import java.io.*;

/**
 * Utility to convert a Shapefile to KMZ. This application launches a file chooser and allows the user to select a
 * Shapefile, or a directory of Shapefiles. Each file is converted to a KMZ file, and saved to the same directory as
 * the source file. Progress is printed to stdout.
 *
 * @author pabercrombie
 * @version $Id: ShapefileToKMZConverter.java 15632 2011-06-15 19:49:35Z pabercrombie $
 */
public class ShapefileToKMZConverter
{
    /**
     * Convert a file or directory to KMZ.
     *
     * @param file file to convert.
     *
     * @throws IOException        if an exception occurs reading the input file or writing the output file
     * @throws XMLStreamException if an exception is thrown writing KML
     */
    protected static void convertToKMZ(File file) throws IOException, XMLStreamException
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!file.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!file.canRead())
        {
            String message = Logging.getMessage("generic.FileNoReadPermission");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (file.isDirectory())
            convertDirectory(file, new String[]{".shp"});
        else
            convertFile(file);
    }

    /**
     * Convert a directory of files to KMZ.
     *
     * @param dir      directory to convert
     * @param suffixes All files matching these suffixes will be converted
     */
    protected static void convertDirectory(File dir, final String[] suffixes)
    {
        System.out.printf("===== Converting Directory %s\n", dir.getPath());

        File[] files = dir.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                for (String suffix : suffixes)
                {
                    if (file.getPath().endsWith(suffix))
                        return true;
                }

                return false;
            }
        });

        if (files != null)
        {
            for (File file : files)
            {
                try
                {
                    convertFile(file);
                }
                catch (Exception e)
                {
                    System.out.printf("Exception converting %s, skipping file\n", file.getPath());
                    e.printStackTrace();
                }
            }
        }

        File[] directories = dir.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.isDirectory();
            }
        });

        if (directories != null)
        {
            for (File directory : directories)
            {
                convertDirectory(directory, suffixes);
            }
        }
    }

    /**
     * Convert a file to KMZ. The KMZ file will have the same name as the source shapefile, and the ".kmz" extension.
     *
     * @param file file to convert.
     *
     * @throws IOException        if an exception occurs reading the input file or writing the output file
     * @throws XMLStreamException if an exception is thrown writing KML
     */
    protected static void convertFile(File file) throws IOException, XMLStreamException
    {
        System.out.printf("Converting File %s\n", file.getPath());

        File newFile = new File(WWIO.replaceSuffix(file.getPath(), ".kmz"));

        KMZDocumentBuilder kmzBuilder = new KMZDocumentBuilder(new FileOutputStream(newFile));

        Shapefile shapeFile = new Shapefile(file);

        kmzBuilder.writeObject(shapeFile);

        kmzBuilder.close();
    }

    /**
     * Example entry point.
     * 
     * @param args no command line arguments
     */
    public static void main(String[] args)
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file or directory to convert");
        fileChooser.setApproveButtonText("Convert");
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(true);

        int status = fileChooser.showOpenDialog(null);
        if (status != JFileChooser.APPROVE_OPTION)
            return;

        File[] files = fileChooser.getSelectedFiles();
        if (files == null)
        {
            System.out.println("No files selected");
            return;
        }

        for (File file : files)
        {
            try
            {
                convertToKMZ(file);
            }
            catch (IOException e)
            {
                System.out.printf("Exception converting input file %s, skipping file\n", file.getPath());
                e.printStackTrace();
            }
            catch (XMLStreamException e)
            {
                System.out.printf("Exception converting input file %s, skipping file\n", file.getPath());
                e.printStackTrace();
            }
        }
    }
}
