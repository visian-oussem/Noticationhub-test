package fr.visian.team.notificationapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.microsoft.windowsazure.messaging.NotificationHub;

import java.util.HashSet;
import java.util.Set;

import static com.google.android.gms.gcm.GoogleCloudMessaging.INSTANCE_ID_SCOPE;

/**
 * Created by OOulhaci on 20/06/2017.
 */

class Notifications {
    private static final String PREFS_NAME = "BreakingNewsCategories";
    private GoogleCloudMessaging gcm;
    private NotificationHub hub;
    private Context context;
    private String senderId;
    private InstanceID instanceID;

    Notifications(Context context, String senderId, String hubName, String listenConnectionString) {
        this.context = context;
        this.senderId = senderId;
        this.instanceID = InstanceID.getInstance(context);
        gcm = GoogleCloudMessaging.getInstance(context);
        hub = new NotificationHub(hubName, listenConnectionString, context);
    }

    void storeCategoriesAndSubscribe(Set<String> categories) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        settings.edit().putStringSet("categories", categories).commit();
        subscribeToCategories(categories);
    }

    Set<String> retrieveCategories() {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getStringSet("categories", new HashSet<String>());
    }

    void subscribeToCategories(final Set<String> categories) {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    String regid = instanceID.getToken(senderId, INSTANCE_ID_SCOPE);
                    String templateBodyGCM = "{\"data\":{\"message\":\"$(messageParam)\"}}";

                    hub.registerTemplate(regid, "templateParams",
                            templateBodyGCM, categories.toArray(new String[categories.size()]));
                } catch (IllegalArgumentException e) {
                    Log.e("Notifications", "Failed to register - " + e.getMessage());
                    return e;
                }
                catch (Exception e) {
                    Log.e("Notifications", "Failed - " + e.getMessage());
                    return e;
                }
                return null;
            }

            protected void onPostExecute(Object result) {
                String message = "Subscribed for categories: " + categories.toString();
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }.execute(null, null, null);
    }
}
