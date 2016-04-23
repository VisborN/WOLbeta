package com.dudkovlad.wolbeta;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.Switch;
import android.view.Gravity;
import android.content.Context;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import java.net.InterfaceAddress;
import java.util.Enumeration;
import java.net.NetworkInterface;
import java.net.InetSocketAddress;
/**
 * Created by vlad on 26.07.2014.
 */

public class Send_Magic_Package extends Thread {

    String Mac, ip;

    boolean send_, stop, waschecked, lastisChecked;
    Network_setup mainactivity;
    //public Handler mHandler;
    Message msg;
    InetSocketAddress serv_addr;

    long start;


    Send_Magic_Package (Network_setup mainactivity_) {
        // Создаём новый второй поток
        super("Второй поток");
        stop = false;
        send_ = false;

        mainactivity = mainactivity_;
        if (mainactivity.close.isChecked()) {
            waschecked = true;
            start = System.nanoTime();
        }
        lastisChecked = mainactivity.close.isChecked();
        start(); // Запускаем поток
    }

    // Точка входа второго потока
    public void run() {
        try {

            LOG("begin");
            if(mainactivity.send.isChecked())
                send();
            while(true)
            {
                if (send_) {
                    send();

                    send_=!send_;
                }
                if (mainactivity.close.isChecked() && waschecked)
                {
                    if (mainactivity.Seconds_to_close.getText().toString().isEmpty()) {
                        if ((System.nanoTime() - start) / 1000000000.0 > 5)
                            mainactivity.finish();
                    }
                    else if ((System.nanoTime()-start) / 1000000000.0>Double.valueOf(mainactivity.Seconds_to_close.getText().toString()))
                        mainactivity.finish();
                }
                else waschecked = false;
                yield();

            }

        } catch (Exception e) {

            LOG(mainactivity.res.getString(R.string.second_thread_exterminate)+"2\n"+e.toString()+"\n");
        }
    }

    void LOG(String some)
    {
        msg = new Message();
        msg.what = 23;
        msg.obj = some;
        mainactivity.mHandler.sendMessage(msg);
    }


    void send()
    {
        try {
            LOG(mainactivity.res.getString(R.string.sending_magic_package)+"\n");

            Mac = mainactivity.MAC_address.getText().toString();
            ip = mainactivity.IP_or_domain.getText().toString();

            byte [] Magic_package = new byte[102];
            byte [] MAC = new byte [6];
            byte temp;
            MAC = String_to_byte_mac(Mac);
            for (int i = 0; i < 6; i++) {
                Magic_package[i] = (byte)127;
            }


            for (int i = 6; i < 102; i++)
            {
                Magic_package[i] = MAC[i%6];
            }
            if(ip.isEmpty()) {

                serv_addr = new InetSocketAddress("255.255.255.255" , 2304);
            }
            else serv_addr = new InetSocketAddress (ip, 2304);

            DatagramSocket sock = new DatagramSocket();
            sock.setBroadcast(true);

            DatagramPacket pack = new DatagramPacket(Magic_package, Magic_package.length, serv_addr);


            for (int i = 5; i > 0; i--) {

                sock.send(pack);



            }

            sock.close();


            LOG(mainactivity.res.getString(R.string.magic_packages_sended)+"\n");
        } catch (Exception e) {
            LOG(mainactivity.res.getString(R.string.second_thread_exterminate)+"3\n"+e.toString()+"\n");
        }

    }


    byte [] String_to_byte_mac(String mac)
    {
        byte[] macout = new byte []{2,2,2,2,2,2};
        char [] temp = new char [2];
        for(int i = 0, j = 0, s = 0; i < mac.length()&& s < 6; i++)
        {
            temp [j] = mac.charAt(i);
            if (('0'<=temp[j] && temp[j]<= '9') ||( 'a'<=temp[j]&&temp[j]<='f')||( 'A'<=temp[j]&&temp[j]<='F')) {
                if (j == 0) j++;
                else
                {
                    j=0;
                    macout[s]= two_char_to_byte(temp);
                    s++;
                }
            }
        }
        LOG("\n");
        for(int i=0;i<6;i++)
            LOG(" "+((Byte)macout[i]).toString());
        LOG(mac+" =");

        return macout;
    }



    byte two_char_to_byte(char two[])
    {
        byte out=(byte)0;
        byte[] temp= new byte [2];
        for (int i = 0; i < 2; i++)
            switch (two[i])
            {
                case '0':temp[i]=0;
                    break;
                case '1':temp[i]=1;
                    break;
                case '2':temp[i]=2;
                    break;
                case '3':temp[i]=3;
                    break;
                case '4':temp[i]=4;
                    break;
                case '5':temp[i]=5;
                    break;
                case '6':temp[i]=6;
                    break;
                case '7':temp[i]=7;
                    break;
                case '8':temp[i]=8;
                    break;
                case '9':temp[i]=9;
                    break;
                case 'a':
                case 'A':temp[i]=10;
                    break;
                case 'b':
                case 'B':temp[i]=11;
                    break;
                case 'c':
                case 'C':temp[i]=12;
                    break;
                case 'd':
                case 'D':temp[i]=13;
                    break;
                case 'e':
                case 'E':temp[i]=14;
                    break;
                case 'f':
                case 'F':temp[i]=15;
                    break;
            }
        temp[0]=(byte)(((int)temp[0])*16);
        out = (byte)((int)temp[0] | (int)temp [1]);



        return out;

    }

}
