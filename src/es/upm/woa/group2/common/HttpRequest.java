package es.upm.woa.group2.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;

import com.sun.net.ssl.HttpsURLConnection;

public class HttpRequest {

    private final static String USER_AGENT = "Mozilla/5.0";

    private static HttpURLConnection con;

    // HTTP POST request
    public static void sendPost(String url, JSONObject parameters) throws Exception {

    	//String urlParameters = "name=Jack&occupation=programmer";

        try {

            URL myurl = new URL("http://127.0.0.1:3000/api"+url);
            con = (HttpURLConnection) myurl.openConnection();

            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Connection", "keep-alive");

            OutputStream os = con.getOutputStream();
            System.out.println("parameters.toString()"+parameters.toString());
            os.write(parameters.toString().getBytes("UTF-8"));
            os.close();
            
            StringBuilder content;

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            }

            System.out.println(content.toString());

        } finally {
            
            con.disconnect();
        }

    }

}