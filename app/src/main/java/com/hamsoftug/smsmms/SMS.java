package com.hamsoftug.smsmms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;

import java.util.List;

/**
 * Created by USER on 9/15/2017.
 */

public class SMS {

    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;
    private Context context;
    private static final String EXTRAS = "extras";
    private SentListener sentListener;
    private DeliveryListener deliveryListener;

    public SMS(Context context){
        this.context = context;
        this.startListen();
    }

    public interface SentListener {
        void onSent(String num, String msg_num);
        void onFailed(String msg_num, String failed_msg);
        void onError(String msg_num, String error_msg);
    }

    public interface DeliveryListener {
        void onDelivered(String num, String msg_num);
        void onFailed(String msg_num, String failed_msg);
    }

    /*

    public void sendSMS(String phone, String message, String extraValue, SentListener sentListener) {
        this.sentListener = sentListener;

        //Check if the phoneNumber is empty
        if (phone.isEmpty()) {
            sentListener.onError(extraValue,"Phone number not set.");
        } else if(message.isEmpty()){
            sentListener.onError(extraValue,"Message not set");
        } else if(extraValue.isEmpty()){
            sentListener.onError(extraValue,"Extra value not set");
        } else {

            SmsManager sms = SmsManager.getDefault();
            // if message length is too long messages are divided
            List<String> messages = sms.divideMessage(message);
            for (String msg : messages) {

                Intent sent_ = new Intent("SMS_SENT");
                sent_.putExtra(EXTRAS,extraValue);

                PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, sent_, 0);

                Intent delivered = new Intent("SMS_DELIVERED");
                delivered.putExtra(EXTRAS,extraValue);

                PendingIntent deliveredIntent = PendingIntent.getBroadcast(context, 0, delivered, 0);

                sms.sendTextMessage(phone, null, msg, sentIntent, deliveredIntent);

            }
        }
    }

    */

    public void sendSMS(String phone, String message, String extraValue, SentListener sentListener, DeliveryListener deliveryListener) {
        this.sentListener = sentListener;
        this.deliveryListener = deliveryListener;

        //Check if fields are empty
        if (phone.isEmpty()) {
            sentListener.onError(extraValue,"Phone number not set.");
        } else if(message.isEmpty()){
            sentListener.onError(extraValue,"Message not set");
        } else if(extraValue.isEmpty()){
            sentListener.onError(extraValue,"Extra value not set");
        } else {

            SmsManager sms = SmsManager.getDefault();
            // if message length is too long messages are divided
            List<String> messages = sms.divideMessage(message);
            for (String msg : messages) {

                Intent sent_ = new Intent("SMS_SENT");
                sent_.putExtra(EXTRAS,extraValue);

                PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, sent_, 0);

                Intent delivered = new Intent("SMS_DELIVERED");
                delivered.putExtra(EXTRAS,extraValue);

                PendingIntent deliveredIntent = PendingIntent.getBroadcast(context, 0, delivered, 0);

                sms.sendTextMessage(phone, null, msg, sentIntent, deliveredIntent);

            }
        }
    }

    private void startListen(){

        sentStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Unknown Error";
                String num = arg1.getStringExtra(EXTRAS);

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Sent Successfully";
                        sentListener.onSent(num,s);
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        s = "Generic Failure Error";
                        sentListener.onFailed(num,s);
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        s = "Error : No Service Available";
                        sentListener.onFailed(num,s);
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        s = "Error : Null PDU";
                        sentListener.onFailed(num,s);
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        s = "Error : Radio is off";
                        sentListener.onFailed(num,s);
                        break;
                    default:
                        sentListener.onFailed(num,s);
                }

            }
        };

        deliveredStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Message Not Delivered";
                String num = arg1.getStringExtra(EXTRAS);

                switch(getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Delivered Successfully";
                        deliveryListener.onDelivered(num,s);
                        break;
                    case Activity.RESULT_CANCELED:
                        s = "Message Canceled";
                        deliveryListener.onFailed(num,s);
                        break;
                    default:
                        deliveryListener.onFailed(num,s);
                }

            }
        };

        context.registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));
        context.registerReceiver(deliveredStatusReceiver, new IntentFilter("SMS_DELIVERED"));

    }

    public void stopListener(){
        context.unregisterReceiver(sentStatusReceiver);
        context.unregisterReceiver(deliveredStatusReceiver);
    }

}
