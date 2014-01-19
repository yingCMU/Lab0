package lab0;

public class CommunicationInfra {

	public static void usage(int errCode) {
		switch (errCode) {
		case Constants.INVALID_USAGE:
		default:
			System.out.println("Usage: java CommunicationInfra <config file> <process uid> \n");
			break;
		case Constants.INVALID_CONFIG_FILE:
			System.out.println("Configuration file is incorrectly specified \n");
			break;
		case Constants.INVALID_CONFIG_PARAMS:
			System.out.println("Configuration file has invalid parameters please revisit it \n");
		}
		System.exit(-1);
	}
	
	public static void main(String[] args) {
		/* The infrastructure requires 2 arguments */
		if (args.length != 2) {
			usage(Constants.INVALID_USAGE);
		}
		
		MessagePasser mp = new MessagePasser(args[0], args[1]);
	}
}
