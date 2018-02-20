package com.jinhyeon.myapp;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.PriorityQueue;

import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;


/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	private static final String FACEBOOK_CLIENT_ID = "**";
	private static final String REDIRECT_URL = "**";
	private static final String FACEBOOK_CLIENT_SECRET_KEY = "**";
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	private static final String USER_AGENT = "Mozila/5.0";

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		
		return "home";
	}
	
	@RequestMapping(value ="/facebookSignin")
	public String getFacebookSigninCode(HttpSession session) {
		String facebookUrl = "https://www.facebook.com/v2.12/dialog/oauth?"
				+ "client_id="+FACEBOOK_CLIENT_ID
				+ "&redirect_uri="+REDIRECT_URL
				+"&scope=public_profile,email,user_location";
		
		return "redirect:"+facebookUrl;
	}
	
	@RequestMapping(value = "/facebookAccessToken")
	public void getFacebookSignIn(String code, HttpSession session, String state) throws Exception {
		System.out.println("state : " + state);
		System.out.println("code : " + code);
		System.out.println("session : " + session);
		
		String accessToken = requestFacebookAccessToken(session, code);
		System.out.println(accessToken);
//		return "redirect:"+REDIRECT_URL;
	}
	
	private String requestFacebookAccessToken(HttpSession session, String code) throws Exception {
		String facebookUrl = "https://graph.facebook.com/v2.12/oauth/access_token?"+
								"client_id="+FACEBOOK_CLIENT_ID+
								"&redirect_uri="+REDIRECT_URL+
								"&client_secret="+FACEBOOK_CLIENT_SECRET_KEY+
								"&code="+code;
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		try {
            HttpGet httpget = new HttpGet(facebookUrl);
            System.out.println("Executing request " + httpget.getRequestLine());

            // Create a custom response handler
            ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                public String handleResponse(
                        final HttpResponse response) throws ClientProtocolException, IOException {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                }

            };
            String responseBody = httpClient.execute(httpget, responseHandler);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(responseBody);
            String facebookAccessToken = (String) jsonObject.get("access_token");
//            System.out.println(facebookAccessToken);
            session.setAttribute("facebookAccessToken", facebookAccessToken);
            return facebookAccessToken;
        } finally {
            httpClient.close();
        }		
	}
}
