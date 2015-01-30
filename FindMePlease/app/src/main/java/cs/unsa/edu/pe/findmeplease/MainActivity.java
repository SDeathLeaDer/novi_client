package cs.unsa.edu.pe.findmeplease;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    Button btnShowLocation;
    public EditText phoneTxt;
    private CheckBox chkSendPosition;
    private CheckBox chkSendSMS;
    private boolean chkP=true,chkS=false;
    GPSTracker gps;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        if(event.getKeyCode()==66||event.getKeyCode()==24)
        {
            Log.i("Key pressed",String.valueOf(event.getKeyCode()));
            requestHelp();
            return true;
        }
        else
            return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wake();

        if(savedInstanceState!=null)
        {
            chkP=savedInstanceState.getBoolean("chkSendPosition");
            chkS=savedInstanceState.getBoolean("chkSendSMS");
        }

        chkSendPosition=(CheckBox)findViewById(R.id.sendPosition);
        chkSendSMS=(CheckBox)findViewById(R.id.sendSMS);

        phoneTxt=(EditText)findViewById(R.id.phoneText);

        chkSendPosition.setChecked(chkP);
        chkSendSMS.setChecked(chkS);

        if(android.os.Build.VERSION.SDK_INT>9)
        {
            StrictMode.ThreadPolicy policy=new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        btnShowLocation=(Button)findViewById(R.id.simulateButton);
        chkSendPosition=(CheckBox)findViewById(R.id.sendPosition);

        gps=new GPSTracker(MainActivity.this);

        btnShowLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                requestHelp();
            }
        });
    }

    public void onStop()
    {
        super.onStop();
        if(gps!=null)
            gps.stopUsingGPS();
    }

    public void wake()
    {
        Window window=this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    public void requestHelp()
    {
        if(gps.canGetLocation())
        {
            double latitude=gps.getLatitude();
            double longitude=gps.getLongitude();
            double accuracy=gps.getAccuracy();

            String url="";
            if(!chkSendPosition.isChecked())
                url="http://csunsa.herobo.com/auxiliame/notify.php?lat=-16.4042458&lng=-71.5246088&ac=20";
            else
                url="http://csunsa.herobo.com/auxiliame/notify.php?lat="+latitude+"&lng="+longitude+"&ac="+accuracy;

            if(chkSendSMS.isChecked())
                sendSMS(phoneTxt.getText().toString(),"Ayuda!");

            String result=GET(url);
            Toast.makeText(MainActivity.this,result,Toast.LENGTH_SHORT).show();

            if(result!="")
            {
                Vibrator v=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }
        }
        else
            gps.showSettingsAlert();
    }

    public boolean isConnected()
    {
        ConnectivityManager connMgr=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connMgr.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    private void sendSMS(String phoneNumber,String message)
    {
        SmsManager sms=SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber,null,message,null,null);
    }

    //todo change to async
    public String GET(String url)
    {
        String result="";

        if(!isConnected())
        {
            result="No tiene conexión a internet!";
        }
        else
        {
            try
            {
                HttpClient httpclient=new DefaultHttpClient();
                HttpResponse httpResponse=httpclient.execute(new HttpGet(url));

                StatusLine statusLine=httpResponse.getStatusLine();
                if(statusLine.getStatusCode()==HttpStatus.SC_OK)
                {
                    ByteArrayOutputStream out=new ByteArrayOutputStream();
                    httpResponse.getEntity().writeTo(out);
                    out.close();
                    result=out.toString();
                }
                else
                {
                    httpResponse.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id=item.getItemId();

        if(id==R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }
}
