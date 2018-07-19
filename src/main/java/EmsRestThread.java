import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

public class EmsRestThread implements Runnable {

	@Override
	public void run() {
		String userCredentials = "administrator:interOP@123";
		String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
		URL branchesUrl = null;
		try {
			branchesUrl = new URL("http://10.238.32.55:80/rest/alarms");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpURLConnection repourlconn = null;
		try {
			repourlconn = (HttpURLConnection) branchesUrl.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		repourlconn.setRequestProperty("Authorization", basicAuth);
		try {
			System.out.println(repourlconn.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
