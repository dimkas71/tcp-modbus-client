package dimkas71.ks.tcpmodbus;

import android.app.Application;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

/**
 * Created by Администратор on 19.05.15.
 */
public class MyApplication extends Application {
    private static final Logger LOGGER = LoggerManager.getLogger(MyApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();
        LOGGER.i("MyApplication:onCreate value={}", 123);

    }
}
