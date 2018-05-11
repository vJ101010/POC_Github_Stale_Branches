package com.ca.uim.git.restcalls;

import com.ca.uim.git.datastore.BranchStore;
import com.ca.uim.git.model.Organization;

/**
 * This is utility that helps to identify the stale branches in repositories
 * under organization. It Generates reports and store data in Excel Spreadsheet
 * under User_Home directory
 *
 */
public class App {
	public static void main(String[] args)
			throws Exception {
		GitRest gitRest = new GitRest();
		Encryptor encryptor = new Encryptor();
		String decryptedPass = encryptor.decrypt(args[1]);
		Organization org = gitRest.gitRestCall(args[0], decryptedPass, args[2], Integer.valueOf(args[3]));
		BranchStore branchStore = new BranchStore();
		branchStore.storeData(org);
		RallyServiceHook rallyServiceHook = new RallyServiceHook();
		 rallyServiceHook.createserviceHook(args[0], decryptedPass, args[2],org);
		System.exit(0);
	}
}
