package dialogos.api;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class APIInteraction
{
    public static JSONObject getJSON(String apiRequest)
    {
        try
        {
            download(apiRequest);
            return new JSONObject(new String(download(apiRequest)));
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
}

