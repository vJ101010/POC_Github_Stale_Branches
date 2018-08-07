package com.ca.uim.git.restcalls.fork.organizations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ca.uim.git.callables.GitOrgMembersReposCallable;
import com.ca.uim.git.model.Repo;

public class ForkOrgs {

	public static void main(String[] args) throws IOException, ParseException {
		ForkOrgs forkOrgs = new ForkOrgs();
		JSONParser jsonParser = new JSONParser();
		String Git_Rest_Url = "https://github-isl-01.ca.com/api/v3/";
		String userCredentials = args[0] + ":" + args[1];
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
		List<String> users = new ArrayList<String>();
		JSONArray jsonArray = null;
		for (int pntr = 1; pntr < 5; pntr++) {
			jsonArray = forkOrgs.getOrgUsers(args, jsonParser, Git_Rest_Url, basicAuth, pntr);
			forkOrgs.getUsers(users, jsonArray, jsonParser);
		}
		List<Future<List<Repo>>> reposFututre = new ArrayList<Future<List<Repo>>>();
		ExecutorService executorService = Executors.newFixedThreadPool(30);
		for (String user : users) {
			GitOrgMembersReposCallable gitOrgMembersReposCallable = new GitOrgMembersReposCallable(Git_Rest_Url,
					args[2], basicAuth, user);
			Future<List<Repo>> repo = executorService.submit(gitOrgMembersReposCallable);
			reposFututre.add(repo);
		}

		System.out.println(reposFututre.size());
	}

	private JSONArray getOrgUsers(String[] args, JSONParser jsonParser, String Git_Rest_Url, String basicAuth, int pntr)
			throws MalformedURLException, IOException, ParseException {
		String memurl = "/members?per_page=100&page=" + pntr;
		URL branchesInfoUrl = new URL(Git_Rest_Url + "orgs/" + args[2] + memurl);
		HttpURLConnection branchInfoUrlConn = (HttpURLConnection) branchesInfoUrl.openConnection();
		branchInfoUrlConn.setRequestProperty("Authorization", basicAuth);
		JSONArray jsonArray = (JSONArray) jsonParser.parse(readStream(branchInfoUrlConn.getInputStream()));
		return jsonArray;
	}

	private List<String> getUsers(List<String> users, JSONArray jsonArray, JSONParser jsonParser)
			throws ParseException {
		for (Object object : jsonArray) {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(object.toString());
			String user = (String) jsonObject.get("login");
			System.out.println(user);
			users.add(user);
		}
		return users;
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
		// System.out.println(sb.toString());
		return sb.toString();
	}

}
