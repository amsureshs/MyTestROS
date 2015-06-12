package com.relianceit.relianceorder;

import android.app.Application;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.relianceit.relianceorder.models.ROSCustomer;
import com.relianceit.relianceorder.models.ROSNewOrder;
import com.relianceit.relianceorder.models.ROSReturnOrder;
import com.relianceit.relianceorder.models.ROSUser;
import com.relianceit.relianceorder.util.AppDataManager;
import com.relianceit.relianceorder.util.Constants;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class AppController extends Application {

	public static final String TAG = AppController.class.getSimpleName();
	 
    private RequestQueue mRequestQueue;

    private static AppController mInstance;
    private ROSCustomer rosCustomer;
    private ROSNewOrder selectedOrder;
    private ROSReturnOrder selectedReturnOrder;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        fillUserUsingSavedData();
    }
 
    public static synchronized AppController getInstance() {
        return mInstance;
    }
 
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
 
        return mRequestQueue;
    }
 
    public <T> void addToRequestQueue(Request<T> req, String tag) {

        req.setRetryPolicy(new DefaultRetryPolicy(Constants.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
 
    public <T> void addToRequestQueue(Request<T> req) {
        req.setRetryPolicy(new DefaultRetryPolicy(Constants.DEFAULT_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
 
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

	public String getDeviceId() {
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();
        Log.i(TAG, "Device Id: " + deviceId);
        return deviceId;
    }

   public void fillUserUsingSavedData() {
       ROSUser user = ROSUser.getInstance();
       user.setUsername(AppDataManager.getData(getApplicationContext(), Constants.DM_USERNAME_KEY));

       String encodedToken = AppDataManager.getData(getApplicationContext(), Constants.DM_ACCESS_TOKEN_KEY);
       if (encodedToken != null) {
           byte[] data = Base64.decode(encodedToken, Base64.DEFAULT);
           try {
               String token = new String(data, "UTF-8");
               user.setAccessToken(token);
           } catch (UnsupportedEncodingException e) {
               e.printStackTrace();
           }
       }

       //user.setAccessToken("A336CC74-7923-43E3-B69B-72E4FC91F7B1");
       user.setDeviceToken(getDeviceId());
   }

    public ROSCustomer getRosCustomer() {
        return rosCustomer;
    }

    public void setRosCustomer(ROSCustomer rosCustomer) {
        this.rosCustomer = rosCustomer;
    }

    public ROSNewOrder getSelectedOrder() {
        return selectedOrder;
    }

    public void setSelectedOrder(ROSNewOrder selectedOrder) {
        this.selectedOrder = selectedOrder;
    }

    public ROSReturnOrder getSelectedReturnOrder() {
        return selectedReturnOrder;
    }

    public void setSelectedReturnOrder(ROSReturnOrder selectedReturnOrder) {
        this.selectedReturnOrder = selectedReturnOrder;
    }
}
