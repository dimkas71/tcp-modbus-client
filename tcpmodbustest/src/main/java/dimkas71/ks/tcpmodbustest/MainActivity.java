package dimkas71.ks.tcpmodbustest;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadInputDiscretesRequest;
import net.wimpi.modbus.msg.ReadInputDiscretesResponse;
import net.wimpi.modbus.net.TCPMasterConnection;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private final Logger LOGGER = LoggerManager.getLogger(MainActivity.class);

    private Button mButton;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button)findViewById(R.id.button);
        mButton.setOnClickListener(this);

        mTextView = (TextView)findViewById(R.id.textView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.button:
                new MyAsyncTask().execute();
                break;
        }
    }


    private boolean checkConnection() {

        boolean isWiFiConnected = false;

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();

        if (connectionInfo.getIpAddress() == 0) {
            LOGGER.i("wifi network disconnected: ");
            isWiFiConnected = false;
        } else {
            LOGGER.i("wifi network connected Ip address: %d", connectionInfo.getIpAddress());
            isWiFiConnected = true;
        }

        return isWiFiConnected;
    }


    private InetAddress parseInetAddress(int ipAddress) throws UnknownHostException {

        byte[] inetAddress = new byte[4];

        String ipAddressHex = Integer.toHexString(ipAddress);

        inetAddress[3] = (byte) Integer.parseInt(ipAddressHex.substring(0, 2), 16);
        inetAddress[2] = (byte) Integer.parseInt(ipAddressHex.substring(2, 4), 16);
        inetAddress[1] = (byte) Integer.parseInt(ipAddressHex.substring(4, 6), 16);
        inetAddress[0] = (byte) Integer.parseInt(ipAddressHex.substring(6, 8), 16);

        return InetAddress.getByAddress(inetAddress);
    }

    private final class MyAsyncTask extends AsyncTask<Void, Void, String>  {

        @Override
        protected String doInBackground(Void... params) {
            String result = "";
            if (checkConnection()) {
                try {

                    TCPMasterConnection connection = null;
                    ModbusTCPTransaction transaction = null;
                    ReadInputDiscretesRequest request = null;
                    ReadInputDiscretesResponse response = null;


                    InetAddress address = null;
                    int port = 55555;

                    int ref = 0; //the reference; offset where to satrt reading from
                    int count = 4; //the number of DI's to read
                    int repeat = 1; //a loop fore repeating the transaction


                    final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
                    final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

                    //final InetAddress inetAddress = parseInetAddress(connectionInfo.getIpAddress());

                    final InetAddress inetAddress = InetAddress.getByAddress(new byte[]{(byte) 192, (byte) 168, (byte) 2, (byte) 84});


                    connection = new TCPMasterConnection(inetAddress);
                    connection.setPort(port);
                    connection.connect();

                    request = new ReadInputDiscretesRequest(ref, count);
                    request.setUnitID(15);
                    transaction = new ModbusTCPTransaction(connection);
                    transaction.setRequest(request);


                    int k = 0;

                    do {
                        transaction.execute();
                        response = (ReadInputDiscretesResponse) transaction.getResponse();

                        result = "Digital Input Status = " + response.getDiscretes().toString();
                        System.out.println(result);

                        k++;

                    } while (k < repeat);

                    connection.close();


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            final StringBuilder sb = new StringBuilder();

            sb.append(mTextView.getText() + "\n");
            sb.append(s);
            mTextView.setText(sb.toString());

            super.onPostExecute(s);
        }
    }
}
