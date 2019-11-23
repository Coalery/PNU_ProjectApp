package busanunivproject.app.coalery.teamproject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private RadioButton[] chRadio = new RadioButton[3];
    private int[] chVals = new int[3];
    private int selectedIndex;

    private TextView valueText;
    private TextView urlText;
    private boolean isGet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectedIndex = 0;

        valueText = findViewById(R.id.valueText);

        chRadio[0] = findViewById(R.id.radio_ch1);
        chRadio[1] = findViewById(R.id.radio_ch2);
        chRadio[2] = findViewById(R.id.radio_ch3);

        chRadio[0].setOnClickListener(new View.OnClickListener() { public void onClick(View view) {
            selectedIndex = 0;
            valueText.setText(String.valueOf(chVals[0]));
        }});
        chRadio[1].setOnClickListener(new View.OnClickListener() { public void onClick(View view) {
            selectedIndex = 1;
            valueText.setText(String.valueOf(chVals[1]));
        }});
        chRadio[2].setOnClickListener(new View.OnClickListener() { public void onClick(View view) {
            selectedIndex = 2;
            valueText.setText(String.valueOf(chVals[2]));
        }});

        urlText = findViewById(R.id.URL);
        Button button = findViewById(R.id.getButton);
        button.setOnClickListener(new View.OnClickListener() { public void onClick(View view) {
            getData(urlText.getText().toString() + "get_data.php");
        }});

        Button upButton = findViewById(R.id.upButton);
        Button downButton = findViewById(R.id.downButton);

        upButton.setOnClickListener(new View.OnClickListener() { public void onClick(View view) {
            if(!isGet)
                return;
            if(chVals[selectedIndex] >= 9 )
                return;
            chVals[selectedIndex] ++;
            valueText.setText(String.valueOf(chVals[selectedIndex]));
           setData(urlText.getText().toString() + "update_data.php","m_name=m_input" + (selectedIndex + 1) + "&m_input=" + chVals[selectedIndex]);
        }});

        downButton.setOnClickListener(new View.OnClickListener() { public void onClick(View view) {
            if(!isGet)
                return;
            if(chVals[selectedIndex] <= 0)
                return;
            chVals[selectedIndex] --;
            valueText.setText(String.valueOf(chVals[selectedIndex]));
            setData(urlText.getText().toString() + "update_data.php","m_name=m_input" + (selectedIndex + 1) + "&m_input=" + chVals[selectedIndex]);
        }});
    }

    public void apply() {
        if(!isGet)
            return;
        valueText.setText(String.valueOf(chVals[selectedIndex]));
    }

    public void setData(String url, String query) {
        class SetData extends AsyncTask<String, Void, String> {
            @Override
            protected  String doInBackground(String... params) {
                String uri = params[0];
                String postParameters = params[1];

                Log.v("URI", uri);
                Log.v("PostParameters", postParameters);

                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();

                    con.setReadTimeout(5000);
                    con.setConnectTimeout(5000);
                    con.setRequestMethod("POST");
                    con.connect();

                    OutputStream os = con.getOutputStream();
                    os.write(postParameters.getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    int responseStatusCode = con.getResponseCode();
                    Log.d("HTTP Response Code", "response code - " + responseStatusCode);

                    InputStream inputStream;
                    if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                        inputStream = con.getInputStream();
                    }
                    else{
                        inputStream = con.getErrorStream();
                    }


                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;

                    while((line = bufferedReader.readLine()) != null){
                        sb.append(line);
                    }

                    bufferedReader.close();

                    return sb.toString().trim();
                } catch(Exception e) {
                    e.printStackTrace();
                    return new String("Error: " + e.getMessage());
                }
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Log.d("Post Response", result);
            }
        }
        SetData s = new SetData();
        s.execute(url, query);
    }

    public void getData(String url) {
        class GetDataJSON extends AsyncTask<String, Void, String> {
            @Override
            protected  String doInBackground(String... params) {
                String uri = params[0];

                BufferedReader br = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = br.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch(Exception e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if(br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "스트림을 닫던 중, 예외 발생", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if(result == null)
                    Log.v("Test", "result is null");
                else
                    Log.v("Test", result);
                processJSON(result);
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

    public void processJSON(String result) {
        if(result == null)
            return;
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray values = jsonObject.getJSONArray("result");

            for(int i=0; i<values.length(); i++) {
                JSONObject c = values.getJSONObject(i);
                String m_name = c.getString("m_name");
                int m_input = Integer.parseInt(c.getString("m_input"));

                if(m_name.equals("m_input1"))
                    chVals[0] = m_input;
                else if(m_name.equals("m_input2"))
                    chVals[1] = m_input;
                else if(m_name.equals("m_input3"))
                    chVals[2] = m_input;
                isGet = true;
                apply();
            }
        } catch(JSONException e) {
            Toast.makeText(getApplicationContext(), "JSON 을 파싱하던 중, 예외가 발생하였습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        } catch (NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "문자열에서 정수형으로 형변환 중, 예외가 발생하였습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
    }

}
