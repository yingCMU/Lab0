package lab0;

import org.apache.log4j.Logger;

public class Test {

	private static Logger log = Logger.getLogger(Test.class); 
	public static void main(String[] args) {
		/* The infrastructure requires 2 arguments */
		
		String file = "C:\\Users\\lily\\Lab0\\src\\lab0\\config";
		String name = "alice";
		MessagePasser server = new MessagePasser(file, name );
		Message msg1 = new Message("alice", "PROMPT", "This is the first message of 18842");
		try {
			Thread.sleep(2000);
			MessagePasser client = new MessagePasser(file, "bob" );
			
			client.send(msg1);
			//server.receive();
			log.info("end1------------");
			Message msg2 = new Message("alice", "PROMPT", "This is the 2nd message of 18842");
			Message msg3 = new Message("alice", "PROMPT", "3rd message of 18842");
			
			client = new MessagePasser(file, "cat" );
			
			client.send(msg2);
			Thread.sleep(2000);
			//server.receive();
			log.info("end2----------------");
			client = new MessagePasser(file, "dad" );
			
			client.send(msg3);
			Thread.sleep(2000);
			server.receive();
			server.receive();
			server.receive();
			server.receive();
			log.info("end3---------------");
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
