package com.ca.uim.git.restcalls;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import com.ca.uim.git.model.Organization;
import com.ca.uim.git.model.Repo;
/**
 * 
 * @author batvi03
 * Class us used for updating the service hook defined in github
 */
public class RallyServiceHook {

	public void createserviceHook(String userName, String password, String orgName, Organization org)
			throws IOException {
		String Git_Rest_Url = "https://github-isl-01.ca.com/api/v3/";
		for (Repo repo : org.getRepos()) {
			System.out.println("Creating Servie Hook on Repo "+ repo.getRepoName());
			URL url = new URL(Git_Rest_Url + "repos/" + orgName + "/" + repo.getRepoName() + "/hooks");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Content-Type", "application/json");
			String input = "{\"name\":\"rally\",\"config\":{\"server\":\"rally1\",\"username\":\"uimbuild@ca.com\",\"workspace\":\"CA Technologies\",\"repository\":\"\",\"password\":\"4Vs!6Ya&4Cz!5Uw\"},\"events\":[\"push\"]}";
			String userCredentials = userName + ":" + password;
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
			urlConnection.setRequestProperty("Authorization", basicAuth);
			OutputStream os = urlConnection.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
				System.out.println("Failed : HTTP error code : "+ urlConnection.getResponseCode());
				//throw new RuntimeException("Failed : HTTP error code : " + urlConnection.getResponseCode());
			}
		}
	}
}
