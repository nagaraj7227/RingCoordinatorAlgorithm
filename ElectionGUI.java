/*
 * Name: Vijayalakshmi Balakrishnan
 * ID: 1001434626
 * Reference List:
 * https://www.javatpoint.com/DatagramSocket-and-DatagramPacket
 * https://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html
 * https://www.youtube.com/watch?v=s0JCKUV-XXQ&t=482s
 */

package com.ringelection;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/*
 * This class is for starting up the process and building the GUI for each process.
 */

public class ElectionGUI 
{
	private static JFrame windowFrame;
	public static JTextArea messageTextArea = new JTextArea();
	public static int processesID;
	private static ElectionToken token;
	public static Integer[] portsID = {30000,21000,30010,21010,30020,21020,30030,21030};
	public static int[] processID = {0,1,2,3,4,5,6,7};
	public ArrayList<Integer> ports = new ArrayList<Integer>();
	
	/*
	 *  Main module starts 0 - 7 process as described in book chapter 6.
	 */
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws InterruptedException 
	{
		start(7);
		Thread.currentThread().sleep(2000);
	}
	
	/*
	 *  Initializing the GUI for each process with title number matching with process id.
	 */
	
	private static void initialize() 
	{
		windowFrame = new JFrame();
		windowFrame.setBounds(100, 100, 250, 250);
		windowFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		windowFrame.getContentPane().setLayout(null);
		windowFrame.setTitle("Process: " + processesID);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 10, 215, 150);
		scrollPane.setViewportView(messageTextArea);
		windowFrame.getContentPane().add(scrollPane);
		
		JButton electButton = new JButton("Election");
		electButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					token.tokenPass("Token");
				} catch (IOException | InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		electButton.setBounds(40, 175, 150, 23);
		windowFrame.getContentPane().add(electButton);
	}

	/*
	 *  Initializing each process with window frame and the process id is passed over to the ElectionToken
	 *  so that the process id and ports will be added to the list.
	 */
	 
	
	public static void start(final int id)
	{
		EventQueue.invokeLater(new Runnable() 
		{
			@SuppressWarnings("static-access")
			public void run()
			{
				try {
					ElectionGUI window = new ElectionGUI();
					window.processesID = id;
					initialize();
					window.windowFrame.setVisible(true);
					token = new ElectionToken();
					Thread thread = new Thread(token);
					thread.start();
					
					/*
					 *  When the leader process is crashed, then windowClosing module helps in notifying the other process
					 *  such a way that, that process id and port can be removed from the list.
					 */

					window.windowFrame.addWindowListener(new WindowAdapter() 
					{
						public void windowClosing(WindowEvent arg0) 
						{
							try {
								token.processCrash();
							} catch(Exception e) {
								e.printStackTrace();
							}
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/*
	 *  Constructor to initialize the process id whenever a process starts up.
	 */
	
	public ElectionGUI(int id) 
	{
		processesID = id;
	}
	
	/*
	 *  Constructor to add the ports with their respective process ID.
	 */
	
	public ElectionGUI() 
	{
		ports.addAll(Arrays.asList(portsID));
	}


	/*
	 *  Scan module will help in finding the next set of ports which not crashed.
	 */
		
	public int scan(int processID)
	{
		if(processID >= 7)
		{
			processID = 0;
			for(int i=processID; i<processID+ports.size(); i++)
			{
				if(ports.get(i % 8)!=0)
				{ 
					return ports.get(i % 8);
				}
			}
			return 0;
		}
		else
		{
			processID = processID + 1;
			for(int i=processID; i<processID+ports.size(); i++)
			{
				if(ports.get(i % 8)!=0)
				{ 
					return ports.get(i % 8);
				}
			}
			return 0;
		}
	}
}
