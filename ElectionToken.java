/*
 * Name: Vijayalakshmi Balakrishnan
 * ID: 1001434626
 * Reference List:
 * https://www.javatpoint.com/DatagramSocket-and-DatagramPacket
 * https://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html
 * https://www.youtube.com/watch?v=s0JCKUV-XXQ&t=482s
 */

package com.ringelection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/*
 * This class is for selecting the leader in a given set of process and also for identifying which process has crashed.
 */

public class ElectionToken implements Runnable{
	private DatagramSocket datagramSocket;
	private int processID, id;
	private ElectionGUI electionObject;
	
	/*
	 * The below module creates a datagram socket and binds it with the given Port Number.
	 */
	
	@Override
	public void run() {
		try {
			electionObject = new ElectionGUI();
			processID = ElectionGUI.processesID;
			datagramSocket = new DatagramSocket(electionObject.ports.get(processID)); 
			selectLeader();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * The below module creates a datagram packet. This constructor is used to send the packets. Using this DatagramPacket class
	 * a message can be sent or received.
	 */
	
	public void startElection() throws IOException, InterruptedException {
		ArrayList<Integer> ports = electionObject.ports;
		int current = electionObject.ports.get(processID);
		for(int i=0; i<ports.size(); i++)
		{
			if(ports.get(i) != current)
			{
				String token = "#" + processID + electionObject.ports.get(processID); 
				byte[] buf = token.getBytes();
				DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), ports.get(i)); 
				datagramSocket.send(packet);
			}
		}
	}
	
	/*
	 * The below module will pass the token from process 0 to process 7 using DatagramPacket class.
	 */
	
	public void tokenPass(String token) throws IOException, InterruptedException {
		if(token.equals("Token")) 
		{
			ElectionGUI.messageTextArea.append("Sending token from " + this.processID + "\n");
			token = token + processID;
		}
		ElectionGUI.messageTextArea.append(token + "\n");
		byte[] buf = token.getBytes( );
		DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), electionObject.scan(processID)); 
		datagramSocket.send(packet);
	}
	
	/*
	 * Adding the port and process if a crashed process starts up again.
	 */
	
	public void addPort(int addID, int addPort) {
		electionObject.ports.remove(addID);
		electionObject.ports.add(addID, addPort);
	}
	
	/*
	 * When a process crashes, then it sends a unique message with an identifier, so the message can be stripped down to 
	 * identify which process has crashed. And once that process is crashed, the respective socket is closed. 
	 */
	
	public void processCrash() {
		try {
			tokenPass("$" + processID);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			datagramSocket.close();	
		}
	}
	
	/*
	 * The below module will help in identifying the maximum number between the given numbered processes. It also identifes
	 * if the packet has any identifier or not.
	 */
	
	@SuppressWarnings("static-access")
	public void selectLeader() throws IOException, InterruptedException {
		startElection();
		while(true)
		{
			byte[] buf = new byte[256];
			DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
			datagramSocket.receive(datagramPacket);
			String token = new String(buf);
			if(token.substring(0, 5).equals("Token"))
			{
				if(Character.getNumericValue(token.charAt(5)) == processID) 
				{
					electionObject.messageTextArea.append("A round of election is over.\n");
					int leader = 0;
					token = token.trim();
					for(int count = 5 ; count<token.length(); count++)
					{
						int newID = Character.getNumericValue(token.charAt(count));
						if(newID > leader) 
						{
							leader = newID;
						}
					}
					electionObject.messageTextArea.append("Process " + leader + " is elected as Leader\n");
					Thread.currentThread().sleep(1000);
				}
				else
				{
					electionObject.messageTextArea.append("Passing token to next process\n");
					Thread.currentThread().sleep(1000);
					tokenPass(token.trim()+processID);
				}
			}
			else if(token.charAt(0) == '$') 
			{
				id = Character.getNumericValue(token.charAt(1));
				electionObject.ports.remove(id);
				electionObject.ports.add(id, 0);
				ArrayList<Integer> ports = electionObject.ports;
				int current = electionObject.ports.get(processID);
				for(int i=0;i<ports.size();i++)
				{
					if(ports.get(i)!=current)
					{
						String message = "@" + id + processID; 
						byte[] buf1 = message.getBytes();
						DatagramPacket packet = new DatagramPacket(buf1, buf1.length, InetAddress.getLocalHost(), ports.get(i)); 
						datagramSocket.send(packet);
					}
				}
				Thread.currentThread().sleep(3000);
				tokenPass("Token");
			}
			else if(token.charAt(0) == '@')
			{
				electionObject.ports.remove(Character.getNumericValue(token.charAt(1)));
				electionObject.ports.add(Character.getNumericValue(token.charAt(1)),0);
			}
			else if(token.charAt(0) == '#') 
			{
				addPort(Character.getNumericValue(token.charAt(1)),Integer.parseInt(token.substring(2).trim()));
			}
			if(token.charAt(0) != '#' && token.charAt(0) != '$' && token.charAt(0) != '@')
			{
				electionObject.messageTextArea.append("Token received : " + token + "\n");
			}
		}
	}
}
