package info.hoang8f.jade.maincontainer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import jade.android.AgentContainerHandler;
import jade.android.RuntimeCallback;
import jade.android.RuntimeService;
import jade.android.RuntimeServiceBinder;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private RuntimeServiceBinder runtimeServiceBinder;
    private ServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button BtnStart;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BtnStart = (Button) findViewById(R.id.btn_start_main_container);
        BtnStart.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_main_container:
                startMainContainer();
                break;
            default:
                //Do nothing
        }
    }

    /**
     * Create JADE Main Container here
     */
    private void startMainContainer() {
        //Check runtime service
        if (runtimeServiceBinder == null) {
            //Create Runtime Service Binder here
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {
                    runtimeServiceBinder = (RuntimeServiceBinder) service;
                    Log.i(TAG, "Gateway successfully bound to RuntimeService");

                    runtimeServiceBinder.createMainAgentContainer(new RuntimeCallback<AgentContainerHandler>() {
                        @Override
                        public void onSuccess(AgentContainerHandler agentContainerHandler) {
                            Log.i(TAG, "Main-Container created...");
                            Log.i(TAG, "Platform:" + agentContainerHandler.getAgentContainer().getPlatformName());
                            Log.i(TAG, "Container:" + agentContainerHandler.getAgentContainer().getName());
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.i(TAG, "Failed to create Main Container");
                        }
                    });
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {

                }
            };
            Log.i(TAG, "Binding Gateway to RuntimeService...");
            bindService(new Intent(getApplicationContext(), RuntimeService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

}
