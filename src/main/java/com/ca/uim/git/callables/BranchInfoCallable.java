package com.ca.uim.git.callables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.ca.uim.git.model.Branch;

public class BranchInfoCallable implements Callable<Branch>{
	
	Branch branch;
	String repoName;
	String gitRestUrl;
	JSONParser jsonParser=new JSONParser();
	String basicAuth;
	DateFormat formatter= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	
	public BranchInfoCallable(Branch branch,String repoName, String gitRestUrl,String basicAuth) {
		this.branch = branch;
		this.repoName= repoName;
		this.gitRestUrl=gitRestUrl;
		this.basicAuth= basicAuth;
	}

	@Override
	public Branch call() throws Exception {
		try {
		URL branchesInfoUrl = new URL(gitRestUrl+"repos/UIM/"+repoName+"/branches/"+branch.getBranchName());
		HttpURLConnection branchInfoUrlConn = (HttpURLConnection) branchesInfoUrl.openConnection();
		branchInfoUrlConn.setRequestProperty("Authorization", basicAuth);
		JSONObject branchInfo = (JSONObject) jsonParser.parse(readStream(branchInfoUrlConn.getInputStream()));
		JSONObject commitInfo = (JSONObject) branchInfo.get("commit");
		JSONObject commiterInfo = (JSONObject) commitInfo.get("commit");
		JSONObject author = (JSONObject) commiterInfo.get("author");
		branch.setLastCommitedAuthor(author.get("name").toString());
		branch.setLastCommitedDate(author.get("date").toString());
		Date lastCommitedDate = formatter.parse(author.get("date").toString());
		branch.setNoCommitDays((new Date().getTime() - lastCommitedDate.getTime())/(60*60*1000*24));
		}catch (Exception e) {
			e.printStackTrace();
		}
		return branch;
	}
	
	private static String readStream(InputStream in) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
			String nl = "";
			String nextLine ="";
			while ((nextLine = reader.readLine()) != null) {
				sb.append(nl + nextLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

}
