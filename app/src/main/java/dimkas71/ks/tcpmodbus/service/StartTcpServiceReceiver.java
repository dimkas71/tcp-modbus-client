package dimkas71.ks.tcpmodbus.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

public class StartTcpServiceReceiver extends BroadcastReceiver {

    private static final Logger LOGGER = LoggerManager.getLogger(StartTcpServiceReceiver.class);

    public StartTcpServiceReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGGER.i("onReceive: action %s", intent.getAction());

        if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo netInfo = conMan.getActiveNetworkInfo();

            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                
                LOGGER.i("Wifi connected");
                Intent pushIntent = new Intent(context, TCPModubsClientService.class);
                context.startService(pushIntent);
            }
        }
    }
}
