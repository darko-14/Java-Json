package ReceiptScannerPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* 
 * main() -> getURLConnection(url) 
 * main() -> parseResponse(response)
 * parseResponse() -> sortArray(jsonarray)
 * sortArray() -> displayResult()
 */ 

public class ReceiptScanner {

	private static HttpURLConnection connection;
	
	public static void main(String[] args) {
		
		String url = "https://interview-task-api.mca.dev/qr-scanner-codes/alpha-qr-gFpwhsQ8fkY1";
		StringBuffer response =	getURLConnection(url);
		
		parseResponse(response.toString());
	
	}	
	
	private static void parseResponse(String response) {
		
		JSONArray receipts = new JSONArray(response);
		JSONArray domestic = new JSONArray();
		JSONArray imported = new JSONArray();
		
		for(int i=0; i<receipts.length(); i++) {
			JSONObject receipt = receipts.getJSONObject(i);
			if(receipt.getBoolean("domestic")) {
				domestic.put(receipt);
			}else {
				imported.put(receipt);
			}
		}
		double domesticCost = sortArray(domestic);
		double importedCost = sortArray(imported);
		System.out.println("Domestic cost: $"+domesticCost);
		System.out.println("Imported cost: $"+importedCost);
		System.out.println("Domestic count: "+domestic.length());
		System.out.println("Imported count: "+imported.length());
	}

	private static double sortArray(JSONArray arr) {
		List<JSONObject> receiptsList = new ArrayList<JSONObject>();
	    for (int i = 0; i < arr.length(); i++) {
	    	receiptsList.add(arr.getJSONObject(i));
	    }
		Collections.sort(receiptsList, new Comparator<JSONObject>() {
			private static final String KEY_NAME = "name";

			@Override
			public int compare(JSONObject a, JSONObject b) {
				String valA = new String();
	            String valB = new String();

	            try {
	                valA = (String) a.get(KEY_NAME);
	                valB = (String) b.get(KEY_NAME);
	            } 
	            catch (JSONException e) {
	                e.printStackTrace();
	            }

	            return valA.compareTo(valB);
			}
			
		});
		
		return displayResult(receiptsList);
		
	}

	private static double displayResult(List<JSONObject> receiptsList) {
		
		double totalCost = 0;
		
		if(receiptsList.get(0).getBoolean("domestic")) {
			System.out.println(". Domestic");
		}else {
			System.out.println(". Imported");
		}
		
		for(int i=0; i<receiptsList.size(); i++) {
			JSONObject receipt = receiptsList.get(i);
			String name = receipt.getString("name");
			double price = receipt.getDouble("price");
			String description = receipt.getString("description");
			totalCost += price;
			
			System.out.println("... "+name);
			System.out.println("    Price: $"+price);
			System.out.println("    "+description.substring(0, 10)+"...");
			if(receipt.has("weight")) {
				long weight = receipt.getLong("weight");
				System.out.println("    Weight: "+weight+"g");
			}else {
				System.out.println("    Weight: N/A");
			}
		}
		return totalCost;
	}

	public static StringBuffer getURLConnection(String url_string) {
		BufferedReader reader;
		String line;
		StringBuffer responseContent = new StringBuffer();
        try {        
        	
        	URL url = new URL(url_string);
        	connection = (HttpURLConnection) url.openConnection();
        	connection.setRequestMethod("GET");
        	connection.setConnectTimeout(5000);
        	connection.setReadTimeout(5000);
        	
        	int status = connection.getResponseCode();
        	
        	if(status > 200) {
        		reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        		while((line = reader.readLine()) != null) {
        			responseContent.append(line);
        		}
        		reader.close();
        	}else {
        		reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        		while((line = reader.readLine()) != null) {
        			responseContent.append(line);
        		}
        		reader.close();
        	}
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	connection.disconnect();
        }
        return responseContent;
        
	}
}
