package dimkas71.ks.tcpmodbus.service;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.os.Process;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import net.wimpi.modbus.ModbusCoupler;
import net.wimpi.modbus.net.ModbusTCPListener;
import net.wimpi.modbus.procimg.SimpleDigitalIn;
import net.wimpi.modbus.procimg.SimpleDigitalOut;
import net.wimpi.modbus.procimg.SimpleInputRegister;
import net.wimpi.modbus.procimg.SimpleProcessImage;
import net.wimpi.modbus.procimg.SimpleRegister;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TCPModubsClientService extends Service {

    private static final Logger LOGGER = LoggerManager.getLogger(TCPModubsClientService.class);

    private int mPort;

    private ModbusTCPListener mListener;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (checkConnection()) {
                try {
                    startListener();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    LOGGER.e(e.getMessage());
                }
            }

        }
    }




    public TCPModubsClientService() {
        LOGGER.i("Constructor: TCPModubsClientService");
        mPort = 55555;
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

        final HandlerThread handlerThread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();
        mServiceLooper = handlerThread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);


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

    private void startListener() throws UnknownHostException {

        if (mListener != null && mListener.isListening()) {
            return;
        }

        SimpleProcessImage spi = null;
        spi = new SimpleProcessImage();

        spi.addDigitalOut(new SimpleDigitalOut(true));
        spi.addDigitalOut(new SimpleDigitalOut(false));
        spi.addDigitalIn(new SimpleDigitalIn(false));
        spi.addDigitalIn(new SimpleDigitalIn(true));
        spi.addDigitalIn(new SimpleDigitalIn(false));
        spi.addDigitalIn(new SimpleDigitalIn(true));
        spi.addRegister(new SimpleRegister(251));
        spi.addInputRegister(new SimpleInputRegister(55));

        ModbusCoupler.getReference().setProcessImage(spi);
        ModbusCoupler.getReference().setMaster(false);
        ModbusCoupler.getReference().setUnitID(15);

        mListener = new ModbusTCPListener(4);

        final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        mListener.setAddress(parseInetAddress(wifiInfo.getIpAddress()));
        LOGGER.i("Start listening....");
        mListener.setPort(mPort);
        mListener.start();

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

    private void stopListener() {
        LOGGER.i("Stop Listenning....");
        if (mListener != null) {
            if (mListener.isListening()) {
                mListener.stop();
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGGER.i("onStartCommand %s", intent.getAction());
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;

        mServiceHandler.sendMessage(msg);
        return START_STICKY;


    }

    @Override
    public void onDestroy() {
        stopListener();
        super.onDestroy();
    }

}
