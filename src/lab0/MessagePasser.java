package lab0;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class MessagePasser {
	
	private File config;
	private String name;
	
	public HashMap<String, Node> nodeMap;
	
	public MessagePasser(String configFile, String name) {
		config = new File(configFile);
		if (!config.exists() || config.isDirectory()) {
			CommunicationInfra.usage(Constants.INVALID_CONFIG_FILE);
		}
		this.name = new String(name);
		nodeMap = new HashMap<String, Node>();
	}
	
	@SuppressWarnings("unchecked")
	public int parseConfig() {
		FileInputStream configFileStream = null;
		
		try {
			configFileStream = new FileInputStream(config);
			Yaml yaml = new Yaml();
			Map<String, Object> configMap = (Map<String, Object>) yaml.load(configFileStream);
			if (configMap.isEmpty() || configMap.keySet().size() > Constants.CONFIG_PARAMS) {
				CommunicationInfra.usage(Constants.INVALID_CONFIG_PARAMS);
				return -1;
			}
			
			List<Map<String, Object>> configList = (List<Map<String, Object>>)
					configMap.get("configuration");
			if (configList.size() == 0) {
				CommunicationInfra.usage(Constants.INVALID_CONFIG_PARAMS);
				return -1;
			}
			for (Map<String, Object> iterator : configList) {
				String name = (String) iterator.get("name");
				String ip = (String) iterator.get("ip");
				String port = (String)iterator.get("port");
				
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
			
			if (nodeMap.containsKey(this.name)) {
				CommunicationInfra.usage(Constants.INVALID_CONFIG_PARAMS);
				return -1;
			}
		} catch (FileNotFoundException e) {
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
}
