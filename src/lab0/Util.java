package lab0;

import java.util.ArrayList;



public class Util {
	static Rule matchRule(ArrayList<Rule> rules,Message m){
		Rule myRule = null;
		/* to be changed for runtime update
		synchronized (config) {
			rules = config.getSendRules();
		}
		*/
		assert rules != null;
		for (Rule r : rules) {
			if (r.match(m)) {
				myRule = r;
				break;
			}
		}
		return myRule;
	}
}
