package gr.ntua.epu.dpap.gassmart;

import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by dimitris on 17/3/2015.
 */
class RequestTask extends AsyncTask<String, String, String> {
    MapsActivity activity;

    RequestTask(MapsActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString();
                out.close();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }

        addStations(responseString);
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Do anything with response..
    }

    /*Add gas stations to the map*/
    public void addStations(String response) {
        final String json = response;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("----" + json);

                    //read the json
                    JSONObject jObject = new JSONObject(json);

                    JSONArray stores = jObject.getJSONArray("stores");
                    for (int i = 0; i < stores.length(); i++) { //foreach store
                        JSONObject store = stores.getJSONObject(i);

                        //add station marker
                        LatLng pt = new LatLng(store.getDouble("lat"), store.getDouble("lng"));
                        activity.mMap.addMarker(new MarkerOptions().position(pt).title(
                                store.getString("name") + '\n' +
                                store.getString("address") + '\n' +
                                store.getString("brand") + '\n' +
                                store.getString("type") + '\n' +
                                "€" + (store.getInt("price")/1000.0) + '\n'
                            ).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        activity.mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                            @Override
                            public View getInfoWindow(Marker arg0) {
                                return null;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                View v = activity.getLayoutInflater().inflate(R.layout.marker, null);
                                TextView info= (TextView) v.findViewById(R.id.info);
                                info.setText(marker.getTitle());

                                return v;
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}