package info.hoang8f.jade.agent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import info.hoang8f.jade.maincontainer.MainActivity;
import info.hoang8f.jade.utils.Constants;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;
import jade.util.leap.Set;
import jade.util.leap.SortedSetImpl;

public class ReceivedMessageAgent extends Agent implements SimpleAgentInterface {

    private static final String TAG = "SendMessageAgent";
    private static final long serialVersionUID = 1594371294421614291L;
    private Set participants = new SortedSetImpl();
    private Codec codec = new SLCodec();
    private Context context;
    private String ipAddress = "133.19.63.184";
    private String agentName = "android-agent";

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Context) {
                context = (Context) args[0];
            }
        }

        // Add initial behaviours
        addBehaviour(new SendMessage(this, 3000));
        addBehaviour(new ParticipantsManager(this));

        // Activate the GUI
        registerO2AInterface(SimpleAgentInterface.class, this);

        Intent broadcast = new Intent();
        broadcast.setAction("jade.demo.agent.SEND_MESSAGE");
        Log.i(TAG, "###Sending broadcast " + broadcast.getAction());
        context.sendBroadcast(broadcast);

        //Get ipAddress and agentName
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFS_FILE_NAME, Context.MODE_PRIVATE);
        ipAddress = sharedPreferences.getString(Constants.PREFS_HOST_ADDRESS, ipAddress);
        agentName = sharedPreferences.getString(Constants.PREFS_AGENT_NAME, agentName);
    }

    protected void takeDown() {
    }

    class SendMessage extends TickerBehaviour {

        public SendMessage(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            Log.i(TAG, "###on Tick");
            ACLMessage message = new ACLMessage(ACLMessage.CONFIRM);
            message.setLanguage(codec.getName());
            String convId = "C-" + myAgent.getLocalName();
            message.setConversationId(convId);
            message.setContent("hello! I am from android mobile");
            AID dummyAid = new AID();
            dummyAid.setName(agentName + "@" + ipAddress + ":1099/JADE");
            dummyAid.addAddresses("http://" + ipAddress + ":7778/acc");
            message.addReceiver(dummyAid);
            myAgent.send(message);
            Log.i(TAG, "###Send message:" + message.getContent());
            exportLog("Send message:" + message.getContent());
        }

    }

    class ParticipantsManager extends CyclicBehaviour {
        private static final long serialVersionUID = -4845730529175649756L;

        ParticipantsManager(Agent a) {
            super(a);
        }

        public void onStart() {
            //Start cyclic
        }

        public void action() {
            // Listening for incomming
            ACLMessage msg = myAgent.receive();
            if (msg != null) {
                try {
                    //Get message
                    String message = msg.getContent();
                    Log.i(TAG, "###Incomming message:" + message);
                    exportLog("Incomming message:" + message);

                } catch (Exception e) {
                    Logger.println(e.toString());
                    e.printStackTrace();
                }
            } else {
                block();
            }
        }
    } // END of inner class ParticipantsManager

    public void handleSpoken(String s) {

    }

    public String[] getParticipantNames() {
        String[] pp = new String[participants.size()];
        return pp;
    }

    public void onHostChanged(String host) {
        ipAddress = host;
    }

    public void onAgentNameChanged(String name) {
        agentName = name;
    }

    private void exportLog(String log) {
        MainActivity mainActivity = (MainActivity)context;
        mainActivity.exportLogConsole(log);
    }
}
