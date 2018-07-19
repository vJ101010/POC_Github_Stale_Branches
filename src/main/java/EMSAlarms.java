import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.simple.JSONObject;

import com.ca.uim.git.model.Branch;

public class EMSAlarms {

	public static void main(String[] args) throws IOException {

		ExecutorService executorService = Executors.newFixedThreadPool(20);
		EmsRestThread emsRest = new EmsRestThread();
		for (int i = 0; i < 50; i++) {
			executorService.execute(emsRest);
		}

	}

}
