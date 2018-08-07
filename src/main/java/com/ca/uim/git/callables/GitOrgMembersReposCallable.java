package com.ca.uim.git.callables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.ca.uim.git.model.Repo;

public class GitOrgMembersReposCallable implements Callable<List<Repo>> {

	String gitRestUrl;
	String orgName;
	String basicAuth;
	JSONParser jsonParser = new JSONParser();
	String user;

	public GitOrgMembersReposCallable(String gitRestUrl, String orgName, String basicAuth, String user) {
		this.gitRestUrl = gitRestUrl;
		this.orgName = orgName;
		this.basicAuth = basicAuth;
		this.user = user;
	}

	@Override
	public List<Repo> call() throws Exception {
		List<Repo> repos = new ArrayList<Repo>();
		try {
			URL userRepos = new URL(gitRestUrl + "users/" + user + "/repos");
			HttpURLConnection branchInfoUrlConn = (HttpURLConnection) userRepos.openConnection();
			branchInfoUrlConn.setRequestProperty("Authorization", basicAuth);
			JSONArray reposInfo = (JSONArray) jsonParser.parse(readStream(branchInfoUrlConn.getInputStream()));
			for (Object object : reposInfo) {
				Repo repo = new Repo();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(object.toString());
				repo.setRepoName((String) jsonObject.get("name"));
				repo.setUserName(user);
				repo.setLastUpdatedTime((String) jsonObject.get("updated_at"));
				if (repo.getLastUpdatedTime().contains("2018-02")) {
					System.out.println(user + ";" + repo.getRepoName() + ";" + jsonObject.get("updated_at"));
					repos.add(repo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return repos;
	}

	private static String readStream(InputStream in) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {
			String nl = "";
			String nextLine = "";
			while ((nextLine = reader.readLine()) != null) {
				sb.append(nl + nextLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

}
