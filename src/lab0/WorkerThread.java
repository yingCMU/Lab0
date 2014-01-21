package lab0;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

public class WorkerThread implements Runnable {
	private static Logger log = Logger.getLogger(WorkerThread.class);  
	
	private MessagePasser mp;
	private Socket socket;
	
	public WorkerThread(MessagePasser mp, Socket socket) {
		this.mp = mp;
		this.socket = socket;
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		log.debug(mp.getName()+ " workerThread");
		ObjectInputStream objIpStream = null;
		Message msg = null;
		String src = null;
		
		try {
			objIpStream = new ObjectInputStream(socket.getInputStream());
			while(true) {
				/*
				 * If there are no bytes buffered on the socket, 
				 * or all buffered bytes have been consumed by read, then all
				 */
				log.debug("-- waiting for incoming msg");
				msg = (Message)objIpStream.readObject();
				assert msg instanceof Message;
				src = new String(msg.getSrc());
				log.debug("--put msg into rcvqueue");
				mp.getRcvQueue().add(msg);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			System.err.println("connection terminated from " + src +
					" Posible Reason: " + e.getMessage());
		}
		 catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (objIpStream != null) {
				try {
					socket.close();
					objIpStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
