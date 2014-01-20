package lab0;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.yaml.snakeyaml.Yaml;

public class MessagePasser {
	
	private File config;
	private long configLastModifiedTime;
	private String name;
	private AtomicInteger id;
	private BlockingQueue<Message> rcvQueue;
	
	public HashMap<String, Node> nodeMap = null;
	public ArrayList<Rule> sendRules = null;
	public ArrayList<Rule> rcvRules = null;
	
	public MessagePasser(String configFile, String name) {
		config = new File(configFile);
		if (!config.exists() || config.isDirectory()) {
			CommunicationInfra.usage(Constants.INVALID_CONFIG_FILE);
		}
		this.name = new String(name);
		nodeMap = new HashMap<String, Node>();
		
		parseConfig();
		id = new AtomicInteger(-1);
		setRcvQueue(new LinkedBlockingQueue<Message>());
		
		/* create server thread for this message passer object */
		ServerThread server = new ServerThread(this);
		new Thread(server).start();
	}
	
	@SuppressWarnings("unchecked")
	private int generateSendRules() {
		FileInputStream configFileStream = null;
		ArrayList<Rule> sendRules = null;
		
		try {
			configFileStream = new FileInputStream(config);
			Yaml yaml = new Yaml();
			sendRules = new ArrayList<Rule>();
			
			Map<String, Object> configMap = (Map<String, Object>) yaml.load(configFileStream);
			if (configMap.isEmpty() || configMap.keySet().size() > Constants.CONFIG_PARAMS) {
				return -1;
			}
			
			/* read the send rules from configuration file
			 *    - can have an empty send rules section or unspecified altogether
			 */
			List<Map<String, Object>> sendRuleList = (List<Map<String, Object>>)
					configMap.get("sendRules");
			for (Map<String, Object> iterator : sendRuleList) {
				String action = (String)iterator.get("action");
				if (action == null || !(action.equalsIgnoreCase("drop") 
						|| action.equalsIgnoreCase("duplicate") 
						|| action.equalsIgnoreCase("delay"))) {
					return -1;
				}
				Rule newRule = new Rule(action);
				newRule.setDest((String)iterator.get("dest"));
				newRule.setSrc((String)iterator.get("src"));
				newRule.setKind((String)iterator.get("kind"));
				newRule.setSeqnum((Integer)iterator.get("seqNum"));
				sendRules.add(newRule);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (configFileStream != null) {
				try {
					configFileStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					return -1;
				}
			}
		}
		
		/* if parsing the configuration worked update send rules */
		this.sendRules = sendRules;
		return 0;
	}
	
	@SuppressWarnings("unchecked")
	private int generateRcvRules() {
		FileInputStream configFileStream = null;
		ArrayList<Rule> rcvRules = null;
		
		try {
			configFileStream = new FileInputStream(config);
			Yaml yaml = new Yaml();
			rcvRules = new ArrayList<Rule>();
			
			Map<String, Object> configMap = (Map<String, Object>) yaml.load(configFileStream);
			if (configMap.isEmpty() || configMap.keySet().size() > Constants.CONFIG_PARAMS) {
				return -1;
			}
			
			/* read the receive rules from configuration file
			 *    - can have an empty receive rules section or unspecified altogether
			 */
			List<Map<String, Object>> rcvRuleList = (List<Map<String, Object>>)
					configMap.get("receiveRules");
			for (Map<String, Object> iterator : rcvRuleList) {
				String action = (String)iterator.get("action");
				if (action == null || !(action.equalsIgnoreCase("drop") 
						|| action.equalsIgnoreCase("duplicate") 
						|| action.equalsIgnoreCase("delay"))) {
					return -1;
				}
				Rule newRule = new Rule(action);
				newRule.setDest((String)iterator.get("dest"));
				newRule.setSrc((String)iterator.get("src"));
				newRule.setKind((String)iterator.get("kind"));
				newRule.setSeqnum((Integer)iterator.get("seqNum"));
				rcvRules.add(newRule);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (configFileStream != null) {
				try {
					configFileStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					return -1;
				}
			}
		}
		
		/* if parsing the configuration worked update receive rules */
		this.rcvRules = rcvRules;
		return 0;
	}
	
	@SuppressWarnings("unchecked")
	public int parseConfig() {
		FileInputStream configFileStream = null;
		configLastModifiedTime = config.lastModified();
		
		try {
			configFileStream = new FileInputStream(config);
			Yaml yaml = new Yaml();
			Map<String, Object> configMap = (Map<String, Object>) yaml.load(configFileStream);
			if (configMap.isEmpty() || configMap.keySet().size() > Constants.CONFIG_PARAMS) {
				CommunicationInfra.usage(Constants.INVALID_CONFIG_PARAMS);
				return -1;
			}
			
			/* parse configuration parameter from configuration file */
			List<Map<String, Object>> configList = (List<Map<String, Object>>)
					configMap.get("configuration");
			if (configList == null || configList.size() == 0) {
				CommunicationInfra.usage(Constants.INVALID_CONFIG_PARAMS);
				return -1;
			}
			for (Map<String, Object> iterator : configList) {
				String name = (String) iterator.get("name");
				String ip = (String) iterator.get("ip");
				Integer port = (Integer)iterator.get("port");
				
				if (name == null || ip == null || port == null) {
					CommunicationInfra.usage(Constants.INVALID_CONFIG_PARAMS);
					return -1;
				}
				
				Node newNode = new Node(name, ip, port);
				if (nodeMap.containsKey(name)) {
					CommunicationInfra.usage(Constants.INVALID_CONFIG_PARAMS);
					return -1;
				}
				nodeMap.put(name, newNode);
			}
			
			if (!nodeMap.containsKey(this.name)) {
				CommunicationInfra.usage(Constants.INVALID_CONFIG_PARAMS);
				return -1;
			}
			
			if (generateSendRules() < 0) {
				CommunicationInfra.usage(Constants.INVALID_CONFIG_PARAMS);
				return -1;
			}
			
			if (generateRcvRules() < 0) {
				CommunicationInfra.usage(Constants.INVALID_CONFIG_PARAMS);
				return -1;
			}
			
		}  catch (FileNotFoundException e) {
			e.printStackTrace();
			CommunicationInfra.usage(Constants.INVALID_CONFIG_FILE);
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
			CommunicationInfra.usage(Constants.INVALID_CONFIG_FILE);
			return -1;
		} finally {
			if (configFileStream != null) {
				try {
					configFileStream.close();
				} catch (IOException e) {
					e.printStackTrace();
					CommunicationInfra.usage(Constants.INVALID_CONFIG_FILE);
					return -1;
				}
			}
		}
		
		return 0;
	}
	
	public BlockingQueue<Message> getRcvQueue() {
		return rcvQueue;
	}

	public void setRcvQueue(BlockingQueue<Message> rcvQueue) {
		this.rcvQueue = rcvQueue;
	}
	
	public void send(Message message) {
		ObjectOutputStream objOpStream = null;
		Socket clientSocket = null;
		if (message.getDest() == null) {
			return;
		}
		
		message.setId(id.incrementAndGet());
		message.setSrc(this.name);
		/* TODO: Rule checking  -- only 1 person should be writing to a given stream */
		try {
			clientSocket = new Socket(nodeMap.get(message.getDest()).getIp(), 
					nodeMap.get(message.getDest()).getPort());
			objOpStream = new ObjectOutputStream(clientSocket.getOutputStream());
			objOpStream.writeObject(message);
			objOpStream.flush();
			objOpStream.reset();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (objOpStream != null) {
					objOpStream.close();
				}
				
				if (clientSocket != null) {
					clientSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ServerThread implements Runnable {
		private MessagePasser mp;
		
		public ServerThread(MessagePasser mp) {
			this.mp = mp;
		}

		@Override
		public void run() {
			ServerSocket serverSock = null;
			try {
				serverSock = new ServerSocket(mp.nodeMap.get(mp.name).getPort());
				while (true) {
					/* receiver is blocked -- blocking server implementation */
					Socket newConnection = serverSock.accept();
					new WorkerThread(mp, newConnection);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if ( serverSock != null) {
						serverSock.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}