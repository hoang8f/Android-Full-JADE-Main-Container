package info.hoang8f.jade.agent;

/**
 * This interface implements the logic of the chat client running on the user
 * terminal.
 * 
 * @author Michele Izzo - Telecomitalia
 */

public interface SimpleAgentInterface {
	public void handleSpoken(String s);
	public String[] getParticipantNames();
    public void onHostChanged(String host);
    public void onAgentNameChanged(String name);
}