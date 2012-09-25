package com.negusoft.singleinstance.examples;

import java.io.IOException;

import com.negusoft.singleinstance.SingleInstance;

public class Basic {

	public static void main(String[] args) {
		SingleInstance instance = SingleInstance.request();
		if (instance == null)
			System.out.println("There is already an instance running so we close.");
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
