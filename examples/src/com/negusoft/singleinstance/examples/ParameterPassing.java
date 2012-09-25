package com.negusoft.singleinstance.examples;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import com.negusoft.singleinstance.SingleInstance;

public class ParameterPassing {
	
	private static final String DEFAULT_PARAMETER = "HELLO WORLD!";
	
	private static String parameter;

	public static void main(String[] args) {
		SingleInstance.RequestDelegate request = null;
		if (args.length > 0)
			parameter = args[0];
		else
			parameter = DEFAULT_PARAMETER;
		
		request = new SingleInstance.RequestDelegate() {
			@Override public void requestAction(Socket socket) {
				try {
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					writer.write(parameter);
					writer.newLine();
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		
		SingleInstance.ResponseDelegate response = new SingleInstance.ResponseDelegate() {
			@Override public void responseAction(Socket socket) {
				
				BufferedReader reader;
				try {
					InputStreamReader rAux = new InputStreamReader(socket.getInputStream());
					reader = new BufferedReader(rAux);
					String parameter = reader.readLine();
					System.out.println("Param received: \"" + parameter + "\"");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}};
		
		SingleInstance instance = SingleInstance.request(request, response);
		if (instance == null)
		{
			System.out.println("There is already an instance running so we close.");
			System.out.println("But we sent it the param we received.");
		}
		else
		{
			System.out.println("There is no instance currently running so we can go ahead:");
			System.out.println("Doing some cool stuff, press ENTER key to stop...");
			try {
				System.in.read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			instance.dropInstance();
			System.out.println("Finished, now another instance can run.");
		}
	}

}
