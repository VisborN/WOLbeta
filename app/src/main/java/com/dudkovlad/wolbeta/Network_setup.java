package com.dudkovlad.wolbeta;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;



public class Network_setup extends Activity implements OnClickListener {
    LinearLayout MainLayout;
    EditText MAC_address, IP_or_domain, Seconds_to_close;
    TextView messages;
    Button Send_button;
    Resources res;
    Switch close, send;
    SharedPreferences mSettings;
    Context context;
    Send_Magic_Package thread2;
    Handler mHandler= new  Handler () {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ADD_NEW_LINE:
                    messages.setText((String)msg.obj+messages.getText());
                    break;

                default:
					/* о неизвестных сообщениях нужно узнать как можно раньше */
                    messages.setText("unusual massage\n"+messages.getText());
                    break;
            }
        }
    };

    public static final int ADD_NEW_LINE = 23;

    public static final String APP_PREF = "mysettings";

    String MAC_address_text, IP_or_domain_text, Seconds_to_close_text;

    boolean Close_auto, Send_auto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettings = getSharedPreferences(APP_PREF, this.MODE_MULTI_PROCESS);
        res = getResources();
        load();
        context = this;
        setMainLayout();
        setContentView(MainLayout);
        thread2 = new Send_Magic_Package(this);
    }

    void setMainLayout ()
    {
        MainLayout = new LinearLayout (this);
        MainLayout.setOrientation(LinearLayout.VERTICAL);

        Send_button = new Button (this);
        Send_button.setText(res.getString(R.string.send_button));
        Send_button.setHeight(LayoutParams.WRAP_CONTENT);
        Send_button.setWidth(LayoutParams.FILL_PARENT);
        Send_button.setOnClickListener(this);
        MainLayout.addView(Send_button);

        MAC_address = new EditText (this);
        MAC_address.setHint (res.getString(R.string.host_mac_address));
        if(!MAC_address_text.isEmpty())
            MAC_address.setText(MAC_address_text);
        MAC_address.setMinHeight(LayoutParams.WRAP_CONTENT);
        MAC_address.setMinWidth(LayoutParams.WRAP_CONTENT);
        MainLayout.addView(MAC_address);

        IP_or_domain = new EditText (this);
        IP_or_domain.setHint (res.getString(R.string.IP));
        if(!IP_or_domain_text.isEmpty())
            IP_or_domain.setText(IP_or_domain_text);
        IP_or_domain.setMinHeight(LayoutParams.WRAP_CONTENT);
        IP_or_domain.setMinWidth(LayoutParams.WRAP_CONTENT);
        MainLayout.addView (IP_or_domain);

        MainLayout.addView (CreateMySwitch(res.getString(R.string.auto_send), 200, Send_auto));
        MainLayout.addView (CreateMySwitch(res.getString(R.string.auto_close), 201, Close_auto));

        Create_or_change_edit_text_sec_to_close ();

        messages = new TextView (this);
        messages.setMinHeight(LayoutParams.WRAP_CONTENT);
        messages.setMinWidth(LayoutParams.WRAP_CONTENT);
        messages.setLines (6);
        MainLayout.addView(messages);
    }



    void Create_or_change_edit_text_sec_to_close () {
        if (close.isChecked()) {
            if (Seconds_to_close == null) {
                Seconds_to_close = new EditText(this);
                Seconds_to_close.setHint(res.getString(R.string.sec_to_close));
                if (!Seconds_to_close_text.isEmpty())
                    Seconds_to_close.setText(Seconds_to_close_text);
                Seconds_to_close.setMinHeight(LayoutParams.WRAP_CONTENT);
                Seconds_to_close.setMinWidth(LayoutParams.WRAP_CONTENT);
                MainLayout.addView(Seconds_to_close, 5);
            }else MainLayout.addView(Seconds_to_close, 5);

        } else {
            if(Seconds_to_close!=null)
                MainLayout.removeView(Seconds_to_close);

        }
    }




    LinearLayout CreateMySwitch(String text_add, int whatSwitch, boolean isChecked)
    {
        LinearLayout out = new LinearLayout(this);
        out.setOrientation(LinearLayout.HORIZONTAL);
        out.setMinimumWidth(LayoutParams.MATCH_PARENT);

        TextView _textView = new TextView (this);
        _textView.setText(text_add);
        _textView.setMinHeight(LayoutParams.MATCH_PARENT);/*
        _textView.setWidth(LayoutParams.WRAP_CONTENT);*/
        out.addView(_textView);

        Switch button = new Switch (this);
        button.setText(" ");
        button.setHint(((Integer) whatSwitch).toString());/*
        button.setWidth(LayoutParams.WRAP_CONTENT);
        button.setHeight(LayoutParams.WRAP_CONTENT);*/
        button.setGravity(Gravity.RIGHT);
        if (isChecked)
            button.toggle();
        if (whatSwitch==200)
            send = button;
        if(whatSwitch==201) {
            close = button;
            button.setOnClickListener(this);
        }
        out.addView (button);

        return out;
    }



    public void onClick(View v) {
        if(((Switch)v).getHint().equals("201"))
            Create_or_change_edit_text_sec_to_close();
        else
            thread2.send_=true;
        //Toast.makeText(this, ((Byte)((Integer)(((int)((byte)0x0a))<<4)).byteValue()).toString()+" "+((Byte)(byte)(0x0a<<4)).toString()+" "+((Byte)(byte)0xa0).toString(), Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onResume() {
        super.onResume();
        load();

    }

    void load()
    {
        Seconds_to_close_text               = mSettings.getString   ("APP_PREF_SECONDS", "");
        Close_auto                          = mSettings.getBoolean  ("APP_PREF_CLOSE_AUTO", false);
        Send_auto                           = mSettings.getBoolean  ("APP_PREF_SEND_AUTO", false);
        MAC_address_text                    = mSettings.getString   ("APP_PREF_MAC", "");
        IP_or_domain_text                   = mSettings.getString   ("APP_PREF_IP", "");

        if (MAC_address_text.equals("go")) {
            MAC_address_text = "6C-F0-49-0F-22-E0";
            Send_auto = true;
            Close_auto = true;
            Seconds_to_close_text = "2";
        }
    }

    void save()
    {
        Editor editor = mSettings.edit();
        if(Seconds_to_close!=null)
            editor.putString    ("APP_PREF_SECONDS", Seconds_to_close.getText().toString());
        editor.putBoolean   ("APP_PREF_CLOSE_AUTO", close.isChecked());
        editor.putBoolean   ("APP_PREF_SEND_AUTO", send.isChecked());
        editor.putString    ("APP_PREF_MAC", MAC_address.getText().toString());
        editor.putString    ("APP_PREF_IP", IP_or_domain.getText().toString());

        editor.apply();
    }

    @Override
    protected void onPause() {
        save();
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }





}
