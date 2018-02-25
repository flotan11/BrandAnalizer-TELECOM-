package imt.brandanalizer;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;

public class ServerConnection {
    ArrayList<File> filesAvailab = new ArrayList<File>();

    public ServerConnection(){
    }

    public void loadClassifierInCache (final Context context, final Cache cache){
        String url = "http://www-rech.telecom-lille.fr/nonfreesift/index.json";

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            for(int i=0; i<response.getJSONArray("brands").length(); i++) {
                                String urlXml = "http://www-rech.telecom-lille.fr/nonfreesift" + response.getJSONArray("brands").getJSONObject(i).getString("classifier");

                                getStringServ(context,response.getJSONArray("brands").getJSONObject(i).getString("classifier"), cache, response.getJSONArray("brands").length(), i);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley","Volley JSON error : "+ error);
                    }
                }
        );

        queue.add(jsonRequest);
    }

    public void getIndexJsonServ(Context context, Cache cache){

    }

    public void getStringServ(final Context context, final String xml, final Cache cache, final int size, final int fichier){
        final String url = "http://www-rech.telecom-lille.fr/nonfreesift/classifiers/" + xml;

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //final DiskBasedCache diskCache = (DiskBasedCache) cache;
                        //File file = diskCache.getFileForKey(url);
                        File file = putFileIntoLocal(context, xml,response);
                        filesAvailab.add(putFileIntoLocal(context, xml,response));
                     /*   try {
                            String filePath = context.getApplicationContext().getFilesDir().getName() + "/" + file.getName();
                            AssetManager assetManager = context.getAssets();
                            InputStream input = assetManager.open(file.getAbsolutePath());
                            byte[] buffer = new byte[input.available()];
                            input.read(buffer);
                            input.close();

                            FileOutputStream output = new FileOutputStream(filePath);
                            output.write(buffer);
                            output.close();
                            filesAvailab.add(file);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley", "Volley string error :" + error);
                    }
                }
        );

        queue.add(stringRequest);
    }

    public static File putFileIntoLocal(Context context, String fileName, String response) {
        Writer writer;
        File outputFile = null;
        File outDir = new File(context.getFilesDir() + File.separator + "EZ_time_tracker");

        if (!outDir.isDirectory()) {
            outDir.mkdir();
        }
        try {
            if (!outDir.isDirectory()) {
                throw new IOException(
                        "Unable to create directory EZ_time_tracker. Maybe the SD card is mounted?");
            }
            outputFile = new File(outDir, fileName);
            writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(response);
            writer.close();
        } catch (IOException e) {
            System.out.println("bytes : " + e);
        }
        return outputFile;
    }
}
