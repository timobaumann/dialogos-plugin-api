package dialogos.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class APIInteraction
{
    public static Object getJSON(String apiRequest)
    {
        try
        {
            String load = new String(download(apiRequest));
            if (isJSONObjectValid(load))
            {
                return new JSONObject(load);
            }
            if (isJSONArrayValid(load))
            {
                return new JSONArray(load);
            }
        } catch (IOException exp)
        {
            exp.printStackTrace();
        }

        return null;
    }

    private static byte[] download(String url) throws IOException
    {
        URLConnection conn = new URL(url).openConnection();
        InputStream in = new BufferedInputStream(conn.getInputStream());

        int length = conn.getContentLength();
        byte[] data = new byte[length];

        int offset = 0;

        int bytesRead;
        while (offset < length)
        {
            bytesRead = in.read(data, offset, data.length - offset);

            if (bytesRead == -1)
            {
                break;
            }

            offset += bytesRead;
        }

        in.close();

        return data;
    }

    private static boolean isJSONObjectValid(String jsonString)
    {
        try
        {
            new JSONObject(jsonString);
        } catch (JSONException ex)
        {
            return false;
        }
        return true;
    }

    private static boolean isJSONArrayValid(String jsonString)
    {
        try
        {
            new JSONArray(jsonString);
        } catch (JSONException ex)
        {
            return false;
        }
        return true;
    }
}

