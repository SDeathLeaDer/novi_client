package cs.unsa.edu.pe.findmeplease;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.Set;

public class MyBroadcastReceiver extends BroadcastReceiver
{
    private BluetoothAdapter mBluetoothAdapter=null;

    @Override
    public void onReceive(Context ctx,Intent intent)
    {
        Log.d("Z","Received: Bluetooth Connected");
        mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter!=null)
        {
            Set<BluetoothDevice> pairedDevices=mBluetoothAdapter.getBondedDevices();
            if(pairedDevices.size()>0)
            {
                for(BluetoothDevice device:pairedDevices)
                {
                    Log.d("Z",device.getName()+"\n"+device.getAddress());

                    if(device.getName().compareTo("AB Shutter 3")==0)
                    {
                        Log.d("Z","Connected!");
                        Intent i=new Intent(ctx,MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(i);
                    }
                }
            }
            return;
        }
    }
}