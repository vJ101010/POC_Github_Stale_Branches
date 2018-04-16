package com.ca.uim.git.restcalls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ca.uim.git.callables.BranchInfoCallable;
import com.ca.uim.git.model.Branch;
import com.ca.uim.git.model.Organization;
import com.ca.uim.git.model.Repo;


public class GitRest {

	public Organization gitRestCall(String userName, String password, String orgName, int noOfDays) throws IOException, ParseException, InterruptedException, ExecutionException, java.text.ParseException {
		String Git_Rest_Url = "https://github-isl-01.ca.com/api/v3/";
		URL url1 = new URL(Git_Rest_Url+"orgs/"+orgName+"/repos?per_page=100&page=1");
		HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
		String userCredentials = userName+":"+password;
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
		urlConnection.setRequestProperty("Authorization", basicAuth);
		String readStream = readStream(urlConnection.getInputStream());
		JSONArray jsonArray = new JSONArray(readStream);
		JSONParser jsonParser = new JSONParser();
		Organization organization = new Organization();
		List<Repo> repos = new ArrayList<Repo>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -noOfDays);
		Date beginingPeriod = cal.getTime();
		for (Object object : jsonArray) {
			Repo repo = new Repo();
			JSONObject jsonObject =   (JSONObject) jsonParser.parse(object.toString());
			String reposUrl = (String) jsonObject.get("branches_url");
			String[] reposSplit = reposUrl.split("/");
			repo.setRepoName(reposSplit[7]);
			repos.add(repo);
		}
		URL url2 = new URL(Git_Rest_Url+"orgs/"+orgName+"/repos?per_page=100&page=2");
		HttpURLConnection urlConnection2 = (HttpURLConnection) url2.openConnection();
		urlConnection2.setRequestProperty("Authorization", basicAuth);
		String readStream2 = readStream(urlConnection2.getInputStream());
		JSONArray jsonArray2 = new JSONArray(readStream2);
		for (Object object : jsonArray2) {
			Repo repo = new Repo();
			JSONObject jsonObject =   (JSONObject) jsonParser.parse(object.toString());
			String reposUrl = (String) jsonObject.get("branches_url");
			String[] reposSplit = reposUrl.split("/");
			repo.setRepoName(reposSplit[7]);
			repos.add(repo);
		}
		
		for (Repo repo : repos) {
			System.out.println(repo.getRepoName());
			URL branchesUrl = new URL(Git_Rest_Url+"repos/"+orgName+"/"+repo.getRepoName()+"/branches?per_page=300");
			HttpURLConnection repourlconn = (HttpURLConnection) branchesUrl.openConnection();
			repourlconn.setRequestProperty("Authorization", basicAuth);
			JSONArray branchesJsonArr = new JSONArray(readStream(repourlconn.getInputStream()));
			List<Branch> branches = new ArrayList<Branch>();
			for (Object object : branchesJsonArr) {
				Branch branch = new Branch();
				JSONObject jsonObject =   (JSONObject) jsonParser.parse(object.toString());
				String branchName = (String) jsonObject.get("name");
				branchName= branchName.replace("#", "%23");
				branch.setBranchName(branchName);
				branches.add(branch);
			}
			
			 ExecutorService executor = Executors.newFixedThreadPool(200);
			 List<Future<Branch>> branchInfoFutures = new ArrayList<Future<Branch>>();
			for (Branch branch : branches) {
				Callable<Branch> branchInfoCallable = new BranchInfoCallable(branch, repo.getRepoName(), Git_Rest_Url, basicAuth,orgName);
				Future<Branch> branchInfoFuture = executor.submit(branchInfoCallable);
				branchInfoFutures.add(branchInfoFuture);
			} 
			
			List<Branch> staleBranches = new ArrayList<>();
			for (Future<Branch> future : branchInfoFutures) {
				staleBranches.add(future.get());
			}
			DateFormat formatter= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			for (Branch branch : staleBranches) {
				Date lastCommitedDate = formatter.parse(branch.getLastCommitedDate());
				if(lastCommitedDate.compareTo(beginingPeriod)>0) {
					//System.out.println("Repo Name::"+repo.getRepoName()+" branch name:"+branch.getBranchName()+" Author :"+branch.getLastCommitedAuthor()+" Last Commited Date :"+ branch.getLastCommitedDate()+ " Number of Uncommited Days::" + branch.getNoCommitDays());
					branches.remove(branch);
				}
			}
			repo.setBranchs(branches);
		}
		organization.setOrganizationName("UIM");
		organization.setRepos(repos);
		return organization;
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
