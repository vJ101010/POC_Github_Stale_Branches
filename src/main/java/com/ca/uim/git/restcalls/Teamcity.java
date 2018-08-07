package com.ca.uim.git.restcalls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
/**
 * 
 * @author batvi03
 *	Class is used for getting roles assigned for user in teamcity
 */
public class Teamcity {
	static Teamcity teamcity = new Teamcity();

	public static void main(String[] args) throws IOException, InterruptedException {

		Map<String, String> userRestCall = new HashMap<String, String>();
		String Teamcity_Rest_Url = "http://build.dev.fco/teamcity/app/rest/";
		// Getting first 100 repositories , as git uses pagination
		System.out.println("******Getting Users***********");
		String userCredentials = "uimbuild:T3sti9144@@";
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
		URL url1 = new URL(Teamcity_Rest_Url + "users");
		HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
		urlConnection.setRequestProperty("Authorization", basicAuth);
		urlConnection.setRequestProperty("Content-Type", "application/json");
		String readStream = readStream(urlConnection.getInputStream());
		System.out.println(readStream);
		Document doc = convertStringToDocument(readStream);
		System.out.println(doc);
		doc.getDocumentElement().normalize();
		System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
		NodeList nList = doc.getElementsByTagName("user");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			// System.out.println("\nCurrent Element :" + nNode.getNodeName());
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				System.out.println("PMF Key : " + eElement.getAttribute("username"));
				System.out.println("Href : " + eElement.getAttribute("href"));
				userRestCall.put(eElement.getAttribute("username"), eElement.getAttribute("href"));
			}
		}

		int index=0;
		for (Entry<String, String> userRestEntry : userRestCall.entrySet()) {
			// teamcity.getUserDetails(userRestEntry.getValue());
			String[] split = userRestEntry.getValue().split("users/");
			teamcity.getUserDetails(index++,userRestEntry.getKey(), split[1]);
		}

	}

	private void getUserDetails(int index, String pmfKey, String restUrlForUserDetails) throws IOException, InterruptedException {
		try {
			//Thread.sleep(5000);
			String Teamcity_Rest_Url = "http://build.dev.fco/teamcity/app/rest/";
			// Getting first 100 repositories , as git uses pagination
			String userCredentials = "uimbuild:T3sti9144@@";
			String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
			URL url1 = new URL(Teamcity_Rest_Url + "users/" + restUrlForUserDetails);
			HttpURLConnection urlConnection = (HttpURLConnection) url1.openConnection();
			urlConnection.setRequestProperty("Authorization", basicAuth);
			urlConnection.setRequestProperty("Content-Type", "application/json");
			String readStream = readStream(urlConnection.getInputStream());
			Document doc = convertStringToDocument(readStream);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("role");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				// System.out.println("\nCurrent Element :" + nNode.getNodeName());
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					System.out.println(index+ ";"+
							pmfKey + ";" + eElement.getAttribute("roleId") + ";" + eElement.getAttribute("scope")+";");

				}
			}
		} catch (Exception e) {
			System.out.println("Exception Occured");
			e.printStackTrace();
		}
	}

	private static Document convertStringToDocument(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
