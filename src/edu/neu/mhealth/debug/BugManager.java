package edu.neu.mhealth.debug;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class BugManager {

	private static BugManager bugManager;
	
	private List<Bug> bugs;
	
	
	private BugManager() {
		if (bugs == null) {
			bugs = new ArrayList<Bug>();
		}
	}
	
	public static BugManager getBugManager() {
		if (bugManager == null) {
			bugManager = new BugManager();
		}
		
		return bugManager;
	}
	
	public void addBug(Bug bug) {
		bugs.add(bug);
	}
	
	public void moveBugs() {
		Iterator iterator = bugs.iterator();
		while (iterator.hasNext()) {
			Bug bug = (Bug) iterator.next();
			bug.crawl();
		}
	}
}
