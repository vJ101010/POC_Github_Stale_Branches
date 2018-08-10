package com.ca.uim.git.restcalls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
/**
 * 
 * @author batvi03
 *	Class is used for fetching the stale branhces in github based on number of offset days
 */
public class GitRest {

	public Organization gitRestCall(String userName, String password, String orgName, int noOfDays)
			throws IOException, ParseException, InterruptedException, ExecutionException, java.text.ParseException {
		String Git_Rest_Url = "https://github-isl-01.ca.com/api/v3/";
		// Getting first 100 repositories , as git uses pagination
		System.out.println("******Getting Repositories***********");
		String userCredentials = userName + ":" + password;
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
		String readStream = getReposinFirstPage(orgName, Git_Rest_Url, basicAuth);
		JSONArray jsonArray = new JSONArray(readStream);
		JSONParser jsonParser = new JSONParser();
		Organization organization = new Organization();
		List<Repo> repos = new ArrayList<Repo>();
		Calendar cal = adjustCalender(noOfDays);
		Date beginingPeriod = cal.getTime();
		getRepositories(jsonArray, jsonParser, repos);
		// Getting first 100 repositories , as git uses pagination
		String readStream2 = getReposInSecondPage(orgName, Git_Rest_Url, basicAuth);
		JSONArray jsonArray2 = new JSONArray(readStream2);
		getRepositories(jsonArray2, jsonParser, repos);

		System.out.println("******Getting Repositories***********");
		System.out.println("Total Repositories::" + repos.size());
		for (Repo repo : repos) {
			// Rest URL to get Branches of each repository
			URL branchesUrl = new URL(
					Git_Rest_Url + "repos/" + orgName + "/" + repo.getRepoName() + "/branches?per_page=300");
			HttpURLConnection repourlconn = (HttpURLConnection) branchesUrl.openConnection();
			repourlconn.setRequestProperty("Authorization", basicAuth);
			JSONArray branchesJsonArr = new JSONArray(readStream(repourlconn.getInputStream()));
			List<Branch> branches = new ArrayList<Branch>();
			for (Object object : branchesJsonArr) {
				Branch branch = new Branch();
				JSONObject jsonObject = (JSONObject) jsonParser.parse(object.toString());
				String branchName = (String) jsonObject.get("name");
				// Handling special cases which Git rest url doesn't support
				branchName = branchName.replace("#", "%23");
				branchName = branchName.replace("%", "%25");
				branch.setBranchName(branchName);
				branches.add(branch);
			}

			List<Future<Branch>> branchInfoFutures = getBranchInfo(orgName, Git_Rest_Url, basicAuth, repo, branches);

			List<Branch> staleBranches = new ArrayList<>();
			for (Future<Branch> future : branchInfoFutures) {
				staleBranches.add(future.get());
			}

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
			for (Branch branch : staleBranches) {
				LocalDate lastCommitedDate = LocalDate.parse(branch.getLastCommitedDate(),formatter);
				/*
				 * Verifying if the last commited day is less than the input day provided
				 */
				/* (lastCommitedDate.compareTo(beginingPeriod) > 0) {
					branches.remove(branch);
				}*/
				/*
				 * Fetching the git commit history based on the input
				 */
				if(lastCommitedDate.compareTo(LocalDate.now().minusDays(15)) < 0) {
					branches.remove(branch);
				}
				
			}
			repo.setBranchs(branches);
		}
		organization.setOrganizationName(orgName);
		organization.setRepos(repos);
		return organization;
	}

	private List<Future<Branch>> getBranchInfo(String orgName, String Git_Rest_Url, String basicAuth, Repo repo,
			List<Branch> branches) {
		// Fetching Branch Info Parallely Using multi threading programm
		ExecutorService executor = Executors.newFixedThreadPool(200);
		List<Future<Branch>> branchInfoFutures = new ArrayList<Future<Branch>>();
		for (Branch branch : branches) {
			Callable<Branch> branchInfoCallable = new BranchInfoCallable(branch, repo.getRepoName(), Git_Rest_Url,
					basicAuth, orgName);
			Future<Branch> branchInfoFuture = executor.submit(branchInfoCallable);
			branchInfoFutures.add(branchInfoFuture);
		}
		return branchInfoFutures;
	}

	private String getReposinFirstPage(String orgName, String Git_Rest_Url, String basicAuth)
			throws MalformedURLException, IOException {
		URL url1 = new URL(Git_Rest_Url + "orgs/" + orgName + "/repos?per_page=100&page=1");
		HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
		urlConnection.setRequestProperty("Authorization", basicAuth);
		String readStream = readStream(urlConnection.getInputStream());
		return readStream;
	}

	private String getReposInSecondPage(String orgName, String Git_Rest_Url, String basicAuth)
			throws MalformedURLException, IOException {
		URL url2 = new URL(Git_Rest_Url + "orgs/" + orgName + "/repos?per_page=100&page=2");
		HttpURLConnection urlConnection2 = (HttpURLConnection) url2.openConnection();
		urlConnection2.setRequestProperty("Authorization", basicAuth);
		String readStream2 = readStream(urlConnection2.getInputStream());
		return readStream2;
	}

	private void getRepositories(JSONArray jsonArray, JSONParser jsonParser, List<Repo> repos) throws ParseException {
		for (Object object : jsonArray) {
			Repo repo = new Repo();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(object.toString());
			String reposUrl = (String) jsonObject.get("branches_url");
			String[] reposSplit = reposUrl.split("/");
			repo.setRepoName(reposSplit[7]);
			repos.add(repo);
		}
	}

	private Calendar adjustCalender(int noOfDays) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.DATE, -noOfDays);
		return cal;
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
