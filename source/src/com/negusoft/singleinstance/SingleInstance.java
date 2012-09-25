/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.negusoft.singleinstance;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * <p>This is a utility to control the creation of instances across the local system.
 * This means we can check if there is already an instance of certain program running 
 * at the moment. It is also possible to implement actions to be performed when attempting 
 * to create a new instance if there was already one active.</p>
 * <p>A socket based mechanism is used to establish the instances. You can select the port number 
 * on which you want to work and a listening socket will take it. Like this, the new instance
 * will fail to listen on that port number, meaning that there is another one running. Since 
 * it is a socket based system, it allows passing data between the actual instance and the one 
 * attempting to establish. This can be useful, for example, to pass a file url to a program
 * that will be opened in the currently running instance, if there is one.
 * </p>
 * 
 * @author NEGU Soft
 *
 */
public class SingleInstance {
	
	public static final int DEFAULT_PORT = 3273;
	private static final long DROP_TIMEOUT = 500;
	
	private int port;
	private ServerSocket serverSocket;
	private ResponseDelegate response;
	private Thread thread;
	
	private SingleInstance() {
		this(DEFAULT_PORT, null);
	}
	
	private SingleInstance(int port) {
		this(port, null);
	}
	
	private SingleInstance(int port, ResponseDelegate response) {
		this.port = port;
		this.response = response;
	}
	
	private void establishInstance() throws IOException {
		this.serverSocket = new ServerSocket(this.port);

		this.thread = new Thread(new Runnable() {
			@Override public void run() {
				while (true) {
					try {
						//If another instance tries to establish -> respond
						Socket socket = serverSocket.accept();
						if (response != null)
							response.responseAction(socket);
						socket.close();
					} catch (IOException e) {
						break;
					}
				}
				//Set variables to null before exiting
				serverSocket = null;
			}});
		this.thread.start();
	}
	
	/**
	 * Free the instance son that a new instance can be established
	 */
	public void dropInstance() {
		if (this.serverSocket == null)
			return;
		
		//close the ServerSocket
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//wait fot the thread to finish
		try {
			this.thread.join(DROP_TIMEOUT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get the port where the instance is established
	 * @return port number
	 */
	public int getPort() {
		return this.port;
	}
	
	/**
	 * Request an instance representation using the default port
	 * @return an instance representation or NULL if there is one already one running
	 */
	public static SingleInstance request() {
		return request(DEFAULT_PORT, null, null);
	}
	
	/**
	 * Request an instance representation
	 * @param port the port where the instance will be established
	 * @return an instance representation or NULL if there is one already one running
	 */	
	public static SingleInstance request(int port) {
		return request(port, null, null);
	}
	
	/**
	 * Request an instance representation using the default port
	 * @param request action to be performed if there is already an instance running
	 * @param response action to perform when new instances are requested while this one is running
	 * @return an instance representation or NULL if there is one already one running
	 */
	public static SingleInstance request(RequestDelegate request, ResponseDelegate response) {
		return request(DEFAULT_PORT, request, response);
	}
	
	/**
	 * Request an instance representation
	 * @param port the port where the instance will be established
	 * @param request action to be performed if there is already an instance running
	 * @param response action to perform when new instances are requested while this one is running
	 * @return an instance representation or NULL if there is one already one running
	 */
	public static SingleInstance request(int port, RequestDelegate request, ResponseDelegate response) {
		//Try to establish the instance
		SingleInstance result = new SingleInstance(port, response);
		try {
			result.establishInstance();
			return result;
		} catch (IOException e) {
		}
		
		//If failed connect to the current instance and notify
		try {
			Socket socket = new Socket(InetAddress.getLocalHost(), port);
			if (request != null)
				request.requestAction(socket);
		} catch (Exception e) {
		}
		
		return null;
	}
	
	/**
	 * Request to be performed to the currently running instance
	 * @author NEGU Soft
	 */
	public static interface RequestDelegate {
		public void requestAction(Socket socket);
	}
	
	/**
	 * Response for the new instance attempts
	 * @author NEGU Soft
	 */
	public static interface ResponseDelegate {
		public void responseAction(Socket socket);
	}
}
