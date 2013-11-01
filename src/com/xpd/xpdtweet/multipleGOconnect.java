package com.xpd.xpdtweet;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.HashSet;
import java.util.Set;

import android.util.Log;

import pt.utl.ist.cmov.wifidirect.sockets.SimWifiP2pSocket;



class multipleGOconnect extends Thread {

	private static HashSet<ObjectOutputStream> writers = new HashSet<ObjectOutputStream>();
	private static Set<String> connectedppl;
	int clientID = -1;
	int PORT;
	String Go_name;
	boolean running = true;


	ChatMessage cm;
	String IP;
	SimWifiP2pSocket mCliSocket;
	ObjectInputStream sInput;
	ObjectOutputStream sOutput;
	Learningservice service = null;
	//ClientServiceThread(SimWifiP2pSocket s, int i) {


	public multipleGOconnect(String gO_IP, int gO_PORT, String i,
			Learningservice s, HashSet<ObjectOutputStream> writers1, Set<String> connectedClients) {
		// TODO Auto-generated constructor stub
		IP = gO_IP;
		PORT = gO_PORT;
		Go_name = i;
		service = s;
		writers = writers1;
		connectedppl = connectedClients;
	}

	public void run() {
		System.out.println("opening conn with GO - " + Go_name + ":" +IP + ":"+PORT );
		try {
			mCliSocket = new SimWifiP2pSocket(IP,PORT);
			sOutput = new ObjectOutputStream(mCliSocket.getOutputStream());
			
			synchronized (writers) {
				writers.add(sOutput);
			}
			
			synchronized (connectedppl) {
				connectedppl.add(Go_name);
			}

			sInput  = new ObjectInputStream(mCliSocket.getInputStream());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(writers.size()==1){
				service.sync();
			}
			while(true){
			
				cm = (ChatMessage)sInput.readObject();
				synchronized (service) {
					Log.d("gothread","client"+Go_name);
					service.processInputMessage(cm);
				}
				if (writers.size()>1){
					for (ObjectOutputStream writer : writers) {
							if (!writer.equals(sOutput)){
							writer.writeObject(cm);
							writer.flush();
						}
						else{
							System.out.println("go skipped");
						}
					}
				}
				cm = null;
			}

		} 
		catch (EOFException e){
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		finally {
			// This client is going down!  Remove its name and its print
			// writer from the sets, and close its socket.
			try {
				if (sOutput != null) {
					synchronized (writers) {
						writers.remove(sOutput);
					}
					
					//names.remove(name);
					sOutput.close();
					synchronized (connectedppl) {
						connectedppl.remove(Go_name);
					}
					System.out.println("GO CONNECT:user removed");
					
				}
              
				mCliSocket.close();
				
			} catch (Exception e) 
			{

			}
		}
	}


}

