/*
Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util.webview;

import java.net.URL;

/**
 * @author pabercrombie
 * @version $Id: WebResourceResolver.java 14988 2011-03-17 03:16:49Z dcollins $
 */
public interface WebResourceResolver
{
    URL resolve(String address);
}
