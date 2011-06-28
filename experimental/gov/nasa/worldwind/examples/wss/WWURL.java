/*
Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples.wss;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.util.*;

import java.net.*;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Lado Garakanidze
 * @version $Id: WWURL.java 15420 2011-05-12 07:30:09Z garakl $
 */

public class WWURL
{
    protected URL baseURL = null;
    protected AVList queryParams = new AVListImpl();

    protected WWURL(Object o) throws MalformedURLException, IllegalArgumentException
    {
        if (WWUtil.isEmpty(o))
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        URL url = (o instanceof URL) ? (URL) o : ((o instanceof String) ? new URL((String) o) : null);
        if (null == url)
        {
            String message = Logging.getMessage("generic.MalformedURL", url);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String query = url.getQuery();
        if (!WWUtil.isEmpty(query))
        {
            for (String param : query.split("&"))
            {
                try
                {
                    String[] pair = param.split("=");
                    if (null != pair && pair.length > 0)
                    {
                        String name = URLDecoder.decode(pair[0], "UTF-8");
                        String value = (pair.length > 1) ? URLDecoder.decode(pair[1], "UTF-8") : null;
                        this.setQueryParameter(name, value);
                    }
                }
                catch (Exception e)
                {
                    Logging.logger().log(Level.FINEST, e.getMessage(), e);
                }
            }
        }

        this.baseURL = new URL(url.getProtocol(), url.getHost(), url.getPort(), url.getPath());
    }

    public URL getURL()
    {
        return WWIO.makeURL(this.toString());
    }

    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer(this.baseURL.toString());
        sb.append('?');
        for (Map.Entry<String, Object> e : this.queryParams.getEntries())
        {
            sb.append(e.getKey());
            Object value = e.getValue();
            if (!WWUtil.isEmpty(value))
            {
                sb.append("=").append(e.getValue());
            }
            sb.append('&');
        }

        if (sb.charAt(sb.length() - 1) == '&')
        {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public WWURL setQueryParameter(String name, String value)
    {

        if (!WWUtil.isEmpty(name))
        {
            synchronized (this)
            {
                try
                {
                    // search for existing keys (case insensitive)
                    List<String> keysToRemove = new ArrayList<String>();
                    for (Map.Entry<String, Object> e : this.queryParams.getEntries())
                    {
                        if (name.equalsIgnoreCase(e.getKey()))
                        {
                            keysToRemove.add(e.getKey());
                        }
                    }

                    if (!keysToRemove.isEmpty())
                    {
                        for (String key : keysToRemove)
                        {
                            this.queryParams.removeKey(key);
                        }
                    }
                }
                finally
                {
                    this.queryParams.setValue(name, value);
                }
            }
        }
        return this;
    }

    public String getQueryParameter(String name)
    {
        try
        {
            if (!WWUtil.isEmpty(name))
            {
                if (this.queryParams.hasKey(name))
                {
                    return this.queryParams.getStringValue(name);
                }

                for (Map.Entry<String, Object> e : this.queryParams.getEntries())
                {
                    if (name.equalsIgnoreCase(e.getKey()))
                    {
                        return this.queryParams.getStringValue(e.getKey());
                    }
                }
            }
        }
        catch (Exception e)
        {
            Logging.logger().finest(e.getMessage());
        }

        return null;
    }

    public AVList getQueryParameters()
    {
        return this.queryParams.copy();
    }

    public static boolean areEqual(URL urlA, URL urlB)
    {
        boolean areEqual;

        try
        {
            areEqual = (null != urlA && null != urlB && areEqual(new WWURL(urlA), new WWURL(urlB)));
        }
        catch (Throwable t)
        {
            areEqual = false;
        }

        return areEqual;
    }

    public static boolean areEqual(WWURL urlA, WWURL urlB)
    {
        return (null != urlA && null != urlB && urlA.toString().equalsIgnoreCase(urlB.toString()));
    }
}
