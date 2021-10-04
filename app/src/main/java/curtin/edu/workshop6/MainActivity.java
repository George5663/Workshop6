package curtin.edu.workshop6;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String result;
    private ProgressBar progressBar;
    private TextView textArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textArea = findViewById(R.id.textArea);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        Button downloadBtn = findViewById(R.id.downloadBtn);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Task().execute();
            }
        });
    }

    private void showProgress(int currBytes)
    {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(currBytes);
    }

    private class Task extends AsyncTask<Void, Integer, String> {
        private int totalBytes;
        @Override
        protected String doInBackground(Void... voids) {
            String urlString = Uri.parse("https://10.0.2.2:8000/testwebservice/rest")
                    .buildUpon()
                    .appendQueryParameter("method", "thedata.getit")
                    .appendQueryParameter("api_key", "01189998819991197253")
                    .appendQueryParameter("format", "json")
                    .build().toString();
            try {
                URL url = new URL(urlString);

                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

                DownloadUtils.addCertificate(MainActivity.this, conn);

                if(conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    throw new RuntimeException();
                }
                else
                {
                    InputStream input = conn.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    totalBytes = conn.getContentLength();
                    int currBytes = 0;
                    byte[] buffer = new byte[1024];
                    int bytesRead = input.read(buffer);
                    while(bytesRead > 0)
                    {
                        baos.write(buffer, 0, bytesRead);
                        bytesRead = input.read(buffer);
                        currBytes += bytesRead;
                        publishProgress(currBytes);
                    }
                    baos.close();

                    result = baos.toString();

                    JSONObject jBase = new JSONObject(result);
                    JSONArray jFactions = jBase.getJSONArray("factions");
                    StringBuilder builder = new StringBuilder();
                    for(int i = 0; i < jFactions.length(); i++)
                    {
                        JSONObject faction = (JSONObject) jFactions.get(i);
                        String factionName = (String) faction.getString("name");
                        int factionStrength = (int) faction.getInt("strength");
                        String factionRelationship = (String) faction.getString("relationship");
                        builder.append(factionName).append(" Strength: ").append(factionStrength).append(" Relationship: ").append(factionRelationship).append("\n");
                    }
                    result = builder.toString();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
                System.exit(0);
            } catch (RuntimeException e) {
                System.out.println("Response code isn't valid");
                System.exit(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String resultIn) {
            textArea.setText(resultIn);
        }

        @Override
        public void onProgressUpdate(Integer... params)
        {
            showProgress(params[0]);
        }
    }
}

