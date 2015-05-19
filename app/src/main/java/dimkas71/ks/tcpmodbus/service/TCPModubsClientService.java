package dimkas71.ks.tcpmodbus.service;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class TCPModubsClientService extends Service {

    private static final Logger LOGGER = LoggerManager.getLogger(TCPModubsClientService.class);

    private int mPort;
    private InetAddress mAddress;
    private ServerSocket mServerSocket;

    public TCPModubsClientService() {
        LOGGER.i("Constructor: TCPModubsClientService");
        mPort = 502;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LOGGER.i("onBind: %s", intent.getAction());
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LOGGER.i("onCreate: " + TCPModubsClientService.class.getSimpleName());

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();

        LOGGER.i("Ip address: %d", connectionInfo.getIpAddress());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGGER.i("onStartCommand %s", intent.getAction());

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();

        LOGGER.i("Ip address: %d", connectionInfo.getIpAddress());
        LOGGER.i("BSSID address: %s", connectionInfo.getBSSID());
        LOGGER.i("MACADRESS address: %s", connectionInfo.getMacAddress());
        LOGGER.i("Network id address: %d", connectionInfo.getNetworkId());
        LOGGER.i("RSSI address: %d", connectionInfo.getRssi());
        LOGGER.i("SSID address: %s", connectionInfo.getSSID());

        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onDestroy();
    }

}
