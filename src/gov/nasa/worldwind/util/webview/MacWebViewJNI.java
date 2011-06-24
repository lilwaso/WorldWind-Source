/*
Copyright (C) 2001, 2011 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.util.webview;

import gov.nasa.worldwind.avlist.AVList;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * @author dcollins
 * @version $Id: MacWebViewJNI.java 15517 2011-05-26 21:47:27Z dcollins $
 */
public class MacWebViewJNI
{
    static
    {
        System.loadLibrary("webview");
    }

    public static native long allocWebViewWindow(Dimension frameSize);

    public static native void releaseWebViewWindow(long webViewWindowPtr);

    public static native void setHTMLString(long webViewWindowPtr, String htmlString);

    public static native void setHTMLStringWithBaseURL(long webViewWindowPtr, String htmlString, URL baseURL);

    public static native void setHTMLStringWithResourceResolver(long webViewWindowPtr, String htmlString,
        WebResourceResolver resourceResolver);

    public static native Dimension getFrameSize(long webViewWindowPtr);

    public static native void setFrameSize(long webViewWindowPtr, Dimension size);

    public static native Dimension getContentSize(long webViewWindowPtr);

    public static native Dimension getMinContentSize(long webViewWindowPtr);

    public static native void setMinContentSize(long webViewWindowPtr, Dimension size);

    public static native URL getContentURL(long webViewWindowPtr);

    public static native void setUpdateListener(long webViewWindowPtr, PropertyChangeListener listener);

    public static native long getUpdateTime(long webViewWindowPtr);

    public static native AVList[] getLinks(long webViewWindowPtr);

    public static native void goBack(long webViewWindowPtr);

    public static native void goForward(long webViewWindowPtr);

    public static native void sendEvent(long webViewWindowPtr, InputEvent event);

    public static native boolean canDisplayInTexture(long webViewWindowPtr);

    public static native void displayInTexture(long webViewWindowPtr, int target);
}
