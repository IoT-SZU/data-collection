package cn.ac.futurenet.data_collection.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by chenlin on 28/03/2018.
 */
public class FileTransfer {
    private String url = null;

    public FileTransfer(String url) {
        this.url = url;
    }

    public void sendFile(String dirname, String filename, String data, OnSendDataListener callback) {
        String sendData = String.format("dirname=%s&filename=%s&data=%s",
                URLEncoder.encode(dirname),
                URLEncoder.encode(filename),
                URLEncoder.encode(data));
        String info = post(sendData);
        callback.onFinished("true".equals(info) ? null : info, dirname, filename, data);
    }

    private String post(String data) {
        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();

            // Set connection property
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(20000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Send data
            PrintWriter pw = new PrintWriter(conn.getOutputStream());
            pw.print(data);
            pw.flush();
            pw.close();

            // Get return data
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder tmp = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                tmp.append(line);
            }
            in.close();
            return tmp.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "IOException";
        }
    }

    public interface OnSendDataListener {
        void onFinished(String err, String dirname, String filename, String data);
    }

}
