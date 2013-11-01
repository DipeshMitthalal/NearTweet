package com.xpd.xpdtweet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.Socket;
import java.util.HashSet;



class ClientServiceThread extends Thread {

	private static HashSet<ObjectOutputStream> writers = new HashSet<ObjectOutputStream>();
	// static HashSet<String> names = new HashSet<String>();
//	private SimWifiP2pSocket clientSocket;
	private Socket clientSocket;
	int clientID = -1;
	boolean running = true;
	Learningservice _service;
	//Socket socket;
	ChatMessage cm;
	
	ObjectInputStream sInput;
	ObjectOutputStream sOutput;
	//ClientServiceThread(SimWifiP2pSocket s, int i) {
		ClientServiceThread(Socket s, int i,Learningservice service) {
		clientSocket = s;
		clientID = i;
		_service = service;
	}

	public void run() {
		System.out.println("Accepted Client : ID - " + clientID );
		try {
			sOutput = new ObjectOutputStream(clientSocket.getOutputStream());
			
			synchronized (writers) {
				writers.add(sOutput);
			}
			
			System.out.println("after"+writers.size());
			sInput  = new ObjectInputStream(clientSocket.getInputStream());
			
			while (true) {
			cm = (ChatMessage)sInput.readObject();
				
				if (cm == null) {
					return;
				}
				if (cm.getType()==ChatMessage.SYNC){
				ChatMessage synack=	_service.clientrequestedsync();
				sOutput.writeObject(synack);
				}else{
				for (ObjectOutputStream writer : writers) {
					
					if(!writer.equals(sOutput)){
					writer.writeObject(cm);
					writer.flush();
				}}}
				//sInput.reset();
				//sOutput.flush();
				cm = null;
			}

		} catch (IOException e) {
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
				System.out.println("server:user removed");
			}
			
				clientSocket.close();
			} catch (IOException e) 
			{

			}
		}
	}

	
}

