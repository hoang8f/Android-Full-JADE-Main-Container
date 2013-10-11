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

import info.hoang8f.jade.agent.SimpleAgent;
import jade.android.AgentContainerHandler;
import jade.android.AgentHandler;
import jade.android.RuntimeCallback;
import jade.android.RuntimeService;
import jade.android.RuntimeServiceBinder;
import jade.wrapper.StaleProxyException;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private RuntimeServiceBinder runtimeServiceBinder;
    private ServiceConnection serviceConnection;
    private AgentContainerHandler mainContainerHandler;
    Button btnStart;
    Button btnAgent1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnStart = (Button) findViewById(R.id.btn_start_main_container);
        btnStart.setOnClickListener(this);
        btnAgent1 = (Button) findViewById(R.id.btn_agent1);
        btnAgent1.setOnClickListener(this);
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
                bindService();
                break;
            case R.id.btn_agent1:
                createAgent();
                break;
            default:
                //Do nothing
        }
    }

    /**
     * Create JADE Main Container here
     */
    private void bindService() {
        //Check runtime service
        if (runtimeServiceBinder == null) {
            //Create Runtime Service Binder here
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {
                    runtimeServiceBinder = (RuntimeServiceBinder) service;
                    Log.i(TAG, "###Gateway successfully bound to RuntimeService");
                    startMainContainer();
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    Log.i(TAG, "###Gateway unbound from RuntimeService");
                }
            };
            Log.i(TAG, "###Binding Gateway to RuntimeService...");
            bindService(new Intent(getApplicationContext(), RuntimeService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            startMainContainer();
        }
    }

    private void startMainContainer() {
        runtimeServiceBinder.createMainAgentContainer(new RuntimeCallback<AgentContainerHandler>() {
            @Override
            public void onSuccess(AgentContainerHandler agentContainerHandler) {
                mainContainerHandler = agentContainerHandler;
                Log.i(TAG, "###Main-Container created...");
                Log.i(TAG, "###Container:" + agentContainerHandler.getAgentContainer().getName());
                btnStart.setEnabled(false);
                Log.i(TAG, "###mainContainerHandler:" + mainContainerHandler);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.i(TAG, "###Failed to create Main Container");
            }
        });
    }

    private void createAgent() {
        if (mainContainerHandler !=null) {
            mainContainerHandler.createNewAgent("android-agent", SimpleAgent.class.getName(),
                    new Object[] {getApplicationContext()}, new RuntimeCallback<AgentHandler>() {
                @Override
                public void onSuccess(AgentHandler agentHandler) {
                    try {
                        Log.i(TAG, "###Success to create agent: " + agentHandler.getAgentController().getName());
                        agentHandler.getAgentController().start();
                    } catch (StaleProxyException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.i(TAG, "###Failed to created an Agent");
                }
            });

        } else {
            Log.e(TAG, "###Can't get Main-Container to create agent");
        }
    }

}
