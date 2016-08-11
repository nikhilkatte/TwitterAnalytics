/**
 * Created by Nikhil katte
 */
package com.vam.twitter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class TwitterAnalytics {
	/**
	 *
	 * @author Vardhaman Metpally
	 */
	public static final int PROFILE_INFO = 0;
	public static final int FOLLOWER_INFO = 1;
	public static final int FRIEND_INFO = 2;
	public static final int STATUSES_INFO = 3;
	public static final int USERS_SEARCH = 4;
	public static final int FRIEND_ID = 5;
	public static final int SEARCH_QUERIES = 6;
	public static final int RETWEETS = 7;
	public static String USER_TIMELINE = "/statuses/user_timeline";
	public static String FOLLOWERS = "/followers/list";
	public static String FRIENDS = "/friends/list";
	public static String USER_PROFILE = "/users/show";


	public static final String CONSUMER_SECRET = "SHOc5IQirfF1DxzOvcKf1aCvAijtvk3RrnH3fIM63U";
	public static final String CONSUMER_KEY = "IPMvFiL1gky14xrPo9l0Q";
	public static final String REQUEST_TOKEN_URL = "https://twitter.com/oauth/request_token";
	public static final String AUTHORIZE_URL = "https://twitter.com/oauth/authorize";
	public static final String ACCESS_TOKEN_URL = "https://twitter.com/oauth/access_token";


	//file handlers to store the collected user information
	BufferedWriter OutFileWriter;
	OAuthTokenSecret OAuthTokens;

	 // name of the file containing a list of users
	final String DEF_FILENAME = "politician.txt";

	//Terms can be either users or query terms
	ArrayList<String> Terms = new ArrayList<String>();
	OAuthConsumer Consumer;

	/**
	 * Creates a OAuthConsumer with the current consumer & user access tokens and secrets
	 * @return consumer
	 */
	public OAuthConsumer GetConsumer()
	{
		OAuthConsumer consumer = new DefaultOAuthConsumer(CONSUMER_KEY,CONSUMER_SECRET);

		consumer.setTokenWithSecret(OAuthTokens.getAccessToken(),OAuthTokens.getAccessSecret());
		return consumer;
	}

	//OAuth authentication for the appplication

	public OAuthTokenSecret GetUserAccessKeySecret()
	{
		try {

			if(CONSUMER_KEY.isEmpty())
			{
				System.out.println("Register your application and copy the consumer key into the configuration file.");
				return null;
			}
			if(CONSUMER_SECRET.isEmpty())
			{
				System.out.println("Register your application and copy the consumer secret into the configuration file.");
				return null;
			}
			OAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,CONSUMER_SECRET);
			OAuthProvider provider = new DefaultOAuthProvider(REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTHORIZE_URL);
			String authUrl = provider.retrieveRequestToken(consumer, OAuth.OUT_OF_BAND);
			System.out.println("Now visit:\n" + authUrl + "\n and grant your app authorization");
			System.out.println("Enter the PIN code and hit ENTER when you're done:");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String pin = br.readLine();
			System.out.println("Fetching your access token from Twitter");
			provider.retrieveAccessToken(consumer,pin);
			String accesstoken = consumer.getToken();
			String accesssecret  = consumer.getTokenSecret();
			OAuthTokenSecret tokensecret = new OAuthTokenSecret(accesstoken,accesssecret);
			return tokensecret;
		} catch (OAuthNotAuthorizedException ex) {
			ex.printStackTrace();
		} catch (OAuthMessageSignerException ex) {
			ex.printStackTrace();
		} catch (OAuthExpectationFailedException ex) {
			ex.printStackTrace();
		} catch (OAuthCommunicationException ex) {
			ex.printStackTrace();
		} catch(IOException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
	/**
	 * Reads the file and loads the users in the file to be crawled
	 * @param filename
	 */
	public void ReadUsers(String filename)
	{
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
			String temp = "";
			while((temp = br.readLine())!=null)
			{
				if(!temp.isEmpty())
				{
					Terms.add(temp);
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		finally{
			try {
				br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Load the User Access Token, and the User Access Secret
	 */
	public void LoadTwitterToken()
	{
		OAuthTokens =  this.GetUserAccessKeySecret();
	}


	/**
	 * Retrieves the rate limit status of the application
	 * @return
	 */
	public JSONObject GetRateLimitStatus()
	{
		try{
			URL url = new URL("https://api.twitter.com/1.1/application/rate_limit_status.json");
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setReadTimeout(5000);
			Consumer.sign(huc);
			huc.connect();
			BufferedReader bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
			StringBuffer page = new StringBuffer();
			String temp= "";
			while((temp = bRead.readLine())!=null)
			{
				page.append(temp);
			}
			bRead.close();
			return (new JSONObject(page.toString()));
		} catch (JSONException ex) {
			Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
		} catch (OAuthCommunicationException ex) {
			Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
		}  catch (OAuthMessageSignerException ex) {
			Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
		} catch (OAuthExpectationFailedException ex) {
			Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
		}catch(IOException ex)
		{
			Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}


	/**
	 * Initialize the file writer
	 * @param outFilename name of the file
	 */
	public void InitializeWriters(String outFilename) {
		try {
			File fl = new File(outFilename);
			if(!fl.exists())
			{
				fl.createNewFile();
			}
			/**
			 * Use UTF-8 encoding when saving files to avoid
			 * losing Unicode characters in the data
			 */
			OutFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilename,true),"UTF-8"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Close the opened filewriter to save the data
	 */
	public void CleanupAfterFinish()
	{
		try {
			OutFileWriter.close();
		} catch (IOException ex) {
			Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Writes the retrieved data to the output file
	 * @param data containing the retrived information in JSON
	 * @param user name of the user currently being written
	 */
	public void WriteToFile(String user, String data)
	{
		try
		{
			OutFileWriter.write(data);
			OutFileWriter.flush();
			OutFileWriter.newLine();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Retrives the profile information of the user
	 * @param username of the user whose profile needs to be retrieved
	 * @return the profile information as a JSONObject
	 */
	public JSONObject GetProfile(String username)
	{
		BufferedReader bRead = null;
		JSONObject profile = null;
		try {
			System.out.println("Processing profile of "+username);
			boolean flag = true;
			URL url = new URL("https://api.twitter.com/1.1/users/show.json?screen_name="+username);
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setReadTimeout(5000);
			// Step 2: Sign the request using the OAuth Secret
			Consumer.sign(huc);
			huc.connect();
			if(huc.getResponseCode()==404||huc.getResponseCode()==401)
			{
				System.out.println(huc.getResponseMessage());
			}
			else
				if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503)
				{
					try {
						huc.disconnect();
						System.out.println(huc.getResponseMessage());
						Thread.sleep(3000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
				else
					// Step 3: If the requests have been exhausted, then wait until the quota is renewed
					if(huc.getResponseCode()==429)
					{
						huc.disconnect();
						try {
							Thread.sleep(this.GetWaitTime("/users/show/:id"));
						} catch (InterruptedException ex) {
							Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
						}
						flag = false;
					}
			if(!flag)
			{
				//recreate the connection because something went wrong the first time.
				huc.connect();
			}
			StringBuilder content=new StringBuilder();
			if(flag)
			{
				bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
				String temp= "";
				while((temp = bRead.readLine())!=null)
				{
					content.append(temp);
				}
			}
			huc.disconnect();
			try {
				profile = new JSONObject(content.toString());
			} catch (JSONException ex) {
				ex.printStackTrace();
			}
		} catch (OAuthCommunicationException ex) {
			ex.printStackTrace();
		} catch (OAuthMessageSignerException ex) {
			ex.printStackTrace();
		} catch (OAuthExpectationFailedException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return profile;
	}

	public JSONObject GetFriendsid(String username)
	{
		BufferedReader bRead = null;
		JSONObject profile = null;
		// JSONArray followers = new JSONArray();
		try {
			System.out.println(" friends id for the user= "+username);
			long cursor = -1;
			while(true)
			{
				if(cursor==0)
				{
					break;
				}
				// Step 1: Create the APi request using the supplied username
				URL url = new URL("https://api.twitter.com/1.1/friends/ids.json?user_id="+username+"&cursor="+cursor);
				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				// huc.setReadTimeout(5000);
				// Step 2: Sign the request using the OAuth Secret
				Consumer.sign(huc);
				huc.connect();
				if(huc.getResponseCode()==400||huc.getResponseCode()==404)
				{
					System.out.println(huc.getResponseMessage());
					break;
				}
				else
					if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503||huc.getResponseCode()==504)
					{
						try{
							System.out.println(huc.getResponseMessage());
							huc.disconnect();
							Thread.sleep(3000);
							continue;
						} catch (InterruptedException ex) {
							Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
					else
						// Step 3: If the requests have been exhausted, then wait until the quota is renewed
						if(huc.getResponseCode()==429)
						{
							huc.disconnect();
							try {
								Thread.sleep(this.GetWaitTime("/followers/list"));
							} catch (InterruptedException ex) {
								Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
							}
							continue;
						}
				// Step 4: Retrieve the followers list from Twitter
				bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
				StringBuilder content = new StringBuilder();
				String temp = "";
				while((temp = bRead.readLine())!=null)
				{
					content.append(temp);
				}
				try {
					profile = new JSONObject(content.toString());
					// Step 5: Retrieve the token for the next request
					cursor = profile.getLong("next_cursor");

				} catch (JSONException ex) {
					Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		} catch (OAuthCommunicationException ex) {
			ex.printStackTrace();
		} catch (OAuthMessageSignerException ex) {
			ex.printStackTrace();
		} catch (OAuthExpectationFailedException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return profile;
	}

	public JSONArray GetReTweets(String username)
		{
			BufferedReader bRead = null;
			JSONArray retweets = new JSONArray();
			try {
				System.out.println(" followers user = " + username);
				long cursor = -1;
				while (true) {
					if (cursor == 0) {
						break;
					}
					// Step 1: Create the APi request using the supplied username
					URL url = new URL("https://api.twitter.com/1.1/statuses/retweets/" + username + "&cursor=" + cursor + "&count=1000000");
					//URL url = new URL("https://api.twitter.com/1.1/followers/ids.json?screen_name="+username+"&cursor="+cursor);
					HttpURLConnection huc = (HttpURLConnection) url.openConnection();
					huc.setReadTimeout(5000);
					// Step 2: Sign the request using the OAuth Secret
					Consumer.sign(huc);
					huc.connect();
					if (huc.getResponseCode() == 400 || huc.getResponseCode() == 404) {
						System.out.println(huc.getResponseMessage());
						break;
					} else if (huc.getResponseCode() == 500 || huc.getResponseCode() == 502 || huc.getResponseCode() == 503 || huc.getResponseCode() == 504) {
						try {
							System.out.println(huc.getResponseMessage());
							huc.disconnect();
							Thread.sleep(3000);
							continue;
						} catch (InterruptedException ex) {
							Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
						}
					} else
						// Step 3: If the requests have been exhausted, then wait until the quota is renewed
						if (huc.getResponseCode() == 429) {
							huc.disconnect();
							try {
								Thread.sleep(this.GetWaitTime("/statuses/retweets/" + username));
							} catch (InterruptedException ex) {
								Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
							}
							continue;
						}
					// Step 4: Retrieve the retweets from Twitter
					bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
					StringBuilder content = new StringBuilder();
					String temp = "";
					while ((temp = bRead.readLine()) != null) {
						content.append(temp);
					}
					try {
						JSONObject jobj = new JSONObject(content.toString());
						// Step 5: Retrieve the token for the next request
						cursor = jobj.getLong("next_cursor");
						JSONArray idlist = jobj.getJSONArray("users");
						if (idlist.length() == 0) {
							break;
						}
						for (int i = 0; i < idlist.length(); i++) {
							retweets.put(idlist.getJSONObject(i));
						}
					} catch (JSONException ex) {
						Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			} catch (OAuthCommunicationException ex) {
				ex.printStackTrace();
			} catch (OAuthMessageSignerException ex) {
				ex.printStackTrace();
			} catch (OAuthExpectationFailedException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return retweets;
		}

	/**
	 * Retrieves the id's of followers of a user
	 * @param username the name of the user whose followers need to be retrieved
	 * @return a list of user objects corresponding to the followers of the user
	 */
	public JSONArray GetFollowers(String username)
	{
		BufferedReader bRead = null;
		JSONArray followers = new JSONArray();
		try {
			System.out.println(" followers user = "+username);
			long cursor = -1;
			while(true)
			{
				if(cursor==0)
				{
					break;
				}
				// Step 1: Create the APi request using the supplied username
				// URL url = new URL("https://api.twitter.com/1.1/friends/ids.json?user_id="+username+"&cursor="+cursor);
				URL url = new URL("https://api.twitter.com/1.1/followers/list.json?screen_name="+username+"&cursor=" + cursor);
				//URL url = new URL("https://api.twitter.com/1.1/followers/ids.json?screen_name="+username+"&cursor="+cursor);
				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				huc.setReadTimeout(5000);
				// Step 2: Sign the request using the OAuth Secret
				Consumer.sign(huc);
				huc.connect();
				if(huc.getResponseCode()==400||huc.getResponseCode()==404)
				{
					System.out.println(huc.getResponseMessage());
					break;
				}
				else
					if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503||huc.getResponseCode()==504)
					{
						try{
							System.out.println(huc.getResponseMessage());
							huc.disconnect();
							Thread.sleep(3000);
							continue;
						} catch (InterruptedException ex) {
							Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
					else
						// Step 3: If the requests have been exhausted, then wait until the quota is renewed
						if(huc.getResponseCode()==429)
						{
							huc.disconnect();
							try {
								Thread.sleep(this.GetWaitTime("/followers/list"));
							} catch (InterruptedException ex) {
								Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
							}
							continue;
						}
				// Step 4: Retrieve the followers list from Twitter
				bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
				StringBuilder content = new StringBuilder();
				String temp = "";
				while((temp = bRead.readLine())!=null)
				{
					content.append(temp);
				}
				try {
					JSONObject jobj = new JSONObject(content.toString());
					// Step 5: Retrieve the token for the next request
					cursor = jobj.getLong("next_cursor");
					JSONArray idlist = jobj.getJSONArray("users");
					if(idlist.length()==0)
					{
						break;
					}
					for(int i=0;i<idlist.length();i++)
					{
						followers.put(idlist.getJSONObject(i));
					}
				} catch (JSONException ex) {
					Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		} catch (OAuthCommunicationException ex) {
			ex.printStackTrace();
		} catch (OAuthMessageSignerException ex) {
			ex.printStackTrace();
		} catch (OAuthExpectationFailedException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return followers;
	}

	/**
	 * Retrieved the status messages of a user
	 * @param username the name of the user whose status messages need to be retrieved
	 * @return a list of status messages
	 */
	public JSONArray GetStatuses(String username)
	{
		BufferedReader bRead = null;
		//Get the maximum number of tweets possible in a single page 200
		int tweetcount = 200;
		//Include include_rts because it is counted towards the limit anyway.
		boolean include_rts = true;
		JSONArray statuses = new JSONArray();
		try {
			System.out.println("Processing status messages of "+username);
			long maxid = 0;
			while(true)
			{
				URL url = null;
				if(maxid==0)
				{
					url = new URL("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=" + username+"&include_rts="+include_rts+"&count="+tweetcount);
				}
				else
				{
					//use max_id to get the tweets in the next page. Use max_id-1 to avoid getting redundant tweets.
					url = new URL("https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=" + username+"&include_rts="+include_rts+"&count="+tweetcount+"&max_id="+(maxid-1));
				}
				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				huc.setReadTimeout(5000);
				Consumer.sign(huc);
				huc.connect();
				if(huc.getResponseCode()==400||huc.getResponseCode()==404)
				{
					System.out.println(huc.getResponseCode());
					break;
				}
				else
					if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503)
					{
						try {System.out.println(huc.getResponseCode());
						Thread.sleep(3000);
						} catch (InterruptedException ex) {
							Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
					else
						// Step 3: If the requests have been exhausted, then wait until the quota is renewed
						if(huc.getResponseCode()==429)
						{
							huc.disconnect();
							try {
								Thread.sleep(this.GetWaitTime("/statuses/user_timeline"));
							} catch (InterruptedException ex) {
								Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
							}
							continue;
						}
				bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getInputStream()));
				StringBuilder content = new StringBuilder();
				String temp = "";
				while((temp = bRead.readLine())!=null)
				{
					content.append(temp);
				}
				try {
					JSONArray statusarr = new JSONArray(content.toString());
					if(statusarr.length()==0)
					{
						break;
					}
					for(int i=0;i<statusarr.length();i++)
					{
						JSONObject jobj = statusarr.getJSONObject(i);
						statuses.put(jobj);
						//Get the max_id to get the next batch of tweets
						if(!jobj.isNull("id"))
						{
							maxid = jobj.getLong("id");
						}
					}
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
			System.out.println(statuses.length());
		} catch (OAuthCommunicationException ex) {
			ex.printStackTrace();
		} catch (OAuthMessageSignerException ex) {
			ex.printStackTrace();
		} catch (OAuthExpectationFailedException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return statuses;
	}

	/**
	 * Retrieves the friends of a user
	 * @param username the name of the user whose friends need to be fetched
	 * @return a list of user objects who are friends of the user
	 */
	public JSONArray GetFriends(String username)
	{
		BufferedReader bRead = null;
		JSONArray friends = new JSONArray();
		try {
			System.out.println("Processing friends of "+username);
			long cursor = -1;
			while(true)
			{
				if(cursor==0)
				{
					break;
				}
				// Step 1: Create the APi request using the supplied username
				URL url = new URL("https://api.twitter.com/1.1/friends/ids.json?screen_name="+username+"&cursor="+cursor);
				HttpURLConnection huc = (HttpURLConnection) url.openConnection();
				huc.setReadTimeout(5000);
				//Step 2: Sign the request using the OAuth Secret
				Consumer.sign(huc);
				huc.connect();
				if(huc.getResponseCode()==400||huc.getResponseCode()==401)
				{
					System.out.println(huc.getResponseMessage());
					break;
				}
				else
					if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503)
					{
						try {
							System.out.println(huc.getResponseMessage());
							Thread.sleep(3000);
							continue;
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
					}
					else
						// Step 3: If the requests have been exhausted, then wait until the quota is renewed
						if(huc.getResponseCode()==429)
						{
							huc.disconnect();
							try {
								Thread.sleep(this.GetWaitTime("/friends/list"));
							} catch (InterruptedException ex) {
								Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
							}
							continue;
						}
				// Step 4: Retrieve the friends list from Twitter
				bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getContent()));
				StringBuilder content = new StringBuilder();
				String temp = "";
				while((temp = bRead.readLine())!=null)
				{
					content.append(temp);
				}
				try {
					JSONObject jobj = new JSONObject(content.toString());
					// Step 5: Retrieve the token for the next request
					cursor = jobj.getLong("next_cursor");
					JSONArray userlist = jobj.getJSONArray("users");
					if(userlist.length()==0)
					{
						break;
					}
					for(int i=0;i<userlist.length();i++)
					{
						friends.put(userlist.get(i));
					}
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
				huc.disconnect();
			}
		} catch (OAuthCommunicationException ex) {
			ex.printStackTrace();
		} catch (OAuthMessageSignerException ex) {
			ex.printStackTrace();
		} catch (OAuthExpectationFailedException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return friends;
	}

	/**
	 * Retrieves the wait time if the API Rate Limit has been hit
	 * @param api the name of the API currently being used
	 * @return the number of milliseconds to wait before initiating a new request
	 */
	public long GetWaitTime(String api)
	{
		JSONObject jobj = this.GetRateLimitStatus();
		if(jobj!=null)
		{
			try {
				if(!jobj.isNull("resources"))
				{
					JSONObject resourcesobj = jobj.getJSONObject("resources");
					JSONObject apilimit = null;
					if(api.equals(USER_TIMELINE))
					{
						JSONObject statusobj = resourcesobj.getJSONObject("statuses");
						apilimit = statusobj.getJSONObject(api);
					}
					else
						if(api.equals(FOLLOWERS))
						{
							JSONObject followersobj = resourcesobj.getJSONObject("followers");
							apilimit = followersobj.getJSONObject(api);
						}
						else
							if(api.equals(FRIENDS))
							{
								JSONObject friendsobj = resourcesobj.getJSONObject("friends");
								apilimit = friendsobj.getJSONObject(api);
							}
							else
								if(api.equals(USER_PROFILE))
								{
									JSONObject userobj = resourcesobj.getJSONObject("users");
									apilimit = userobj.getJSONObject(api);
								}

								else if(api.equals(SEARCH_QUERIES))
								{

									JSONObject statusobj = resourcesobj.getJSONObject("statuses");
									apilimit = statusobj.getJSONObject(api);

								}
								else if(api.equals(RETWEETS)) {
									JSONObject retweetsobj = resourcesobj.getJSONObject("statuses");
									apilimit = retweetsobj.getJSONObject(api);
								}
					int numremhits = apilimit.getInt("remaining");
					if(numremhits<=1)
					{
						long resettime = apilimit.getInt("reset");
						resettime = resettime*1000; //convert to milliseconds
						return resettime;
					}
				}
			} catch (JSONException ex) {
				ex.printStackTrace();
			}
		}
		return 0;
	}

	   /**
	 * Creates an OR search query from the supplied terms
	 * @param queryTerms
	 * @return a String formatted as term1 OR term2
	 */
	public String CreateORQuery(ArrayList<String> queryTerms)
	{
		String OR_Operator = " OR ";
		StringBuffer querystr = new StringBuffer();
		int count = 1;
		for(String term:queryTerms)
		{
			if(count==1)
			{
				querystr.append(term);
			}
			else
			{
				querystr.append(OR_Operator).append(term);
			}
		}
		return querystr.toString();
	}

	  /**
	 * Fetches tweets matching a query
	 * @param query for which tweets need to be fetched
	 * @return an array of status objects
	 */
	public JSONArray GetSearchResults(String query)
	{
		try{
			//construct the request url
			String URL_PARAM_SEPERATOR = "&";
			StringBuilder url = new StringBuilder();
			url.append("https://api.twitter.com/1.1/search/tweets.json?q=");
			//query needs to be encoded
			url.append(URLEncoder.encode(query, "UTF-8"));
			url.append(URL_PARAM_SEPERATOR);
			url.append("count=100");
			URL navurl = new URL(url.toString());
			HttpURLConnection huc = (HttpURLConnection) navurl.openConnection();
			huc.setReadTimeout(5000);
			Consumer.sign(huc);
			huc.connect();
			if(huc.getResponseCode()==400||huc.getResponseCode()==404||huc.getResponseCode()==429)
			{
				System.out.println(huc.getResponseMessage());
				try {
					huc.disconnect();
					Thread.sleep(this.GetWaitTime("/friends/list"));
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			}
			if(huc.getResponseCode()==500||huc.getResponseCode()==502||huc.getResponseCode()==503)
			{
				System.out.println(huc.getResponseMessage());
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			BufferedReader bRead = new BufferedReader(new InputStreamReader((InputStream) huc.getInputStream()));
			String temp;
			StringBuilder page = new StringBuilder();
			while( (temp = bRead.readLine())!=null)
			{
				page.append(temp);
			}
			JSONTokener jsonTokener = new JSONTokener(page.toString());
			try {
				JSONObject json = new JSONObject(jsonTokener);
				JSONArray results = json.getJSONArray("statuses");
				return results;
			} catch (JSONException ex) {
				Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
			}
		} catch (OAuthCommunicationException ex) {
			Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
		} catch (OAuthMessageSignerException ex) {
			Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
		} catch (OAuthExpectationFailedException ex) {
			Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
		}catch(IOException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}
public static void main(String[] args) {
		TwitterAnalytics t = new TwitterAnalytics();
		t.LoadTwitterToken();
		t.Consumer = t.GetConsumer();
		ArrayList<String> queryterms = new ArrayList<String>();
		System.out.println(t.GetRateLimitStatus());
		//uncomment one of the line based on the requirement
		// int apicode = PROFILE_INFO;
		//  int apicode = FOLLOWER_INFO;
		// int apicode = STATUSES_INFO;
		//int apicode = SEARCH_QUERIES;
		//int apicode = RETWEETS;
		int apicode = FRIEND_ID;
		String infilename = t.DEF_FILENAME;
		// String outfilename = t.DEF_OUTFILENAME;
		if(args!=null)
		{
			if(args.length>2)
			{
				apicode = Integer.parseInt(args[2]);
				//outfilename = args[1];
				infilename = args[0];
			}
			if(args.length>1)
			{
				//outfilename = args[1];
				infilename = args[0];
			}
			else
				if(args.length>0)
				{
					infilename = args[0];
				}
		}
		// t.InitializeWriters(outfilename);
		t.ReadUsers(infilename);
		if(apicode!=PROFILE_INFO&&apicode!=FOLLOWER_INFO&&apicode!=FRIEND_INFO&&apicode!=STATUSES_INFO&&apicode!=FRIEND_ID&&apicode!=SEARCH_QUERIES&&apicode!=RETWEETS)
		{
			System.out.println("Invalid API type: Use 0 for Profile, 1 for Followers, 2 for Friends, and 3 for Statuses, 4 for Users Search , 5 for Friend ids , 6 for Searching on Twitter using terms, 7 for Retweets by a user");
			System.exit(0);
		}
		if(t.Terms.size()>0)
		{
			//TO-DO: Print the possible API types and get user selection to crawl the users.
			t.LoadTwitterToken();

			for(String user:t.Terms)
			{
				final String dirname ="Output\\"+user;
				File fl = new File(dirname);
				if(!fl.exists())
				{
					fl.mkdir();
				}
				else {
					System.out.println("Directory already exists");
				}
			}
			//term could be user or a search query
			for(String term:t.Terms)
			{

				final String outfilename = "Output\\"+term+"\\"+term+"_retweets.json";
				t.InitializeWriters(outfilename);

				if(apicode==PROFILE_INFO)
				{
					JSONObject jobj = t.GetProfile(term);
					if(jobj!=null&&jobj.length()!=0)
					{
						t.WriteToFile(term, jobj.toString());
					}
					// System.out.println(jobj.toString());
				}
				else
					if(apicode==FOLLOWER_INFO)
					{
						JSONArray statusarr = t.GetFollowers(term);
						if(statusarr.length()>0)
						{
							t.WriteToFile(term, statusarr.toString());
						}
					}

					else
					if(apicode==FRIEND_INFO)
					{
						JSONArray statusarr = t.GetFriends(term);
						if(statusarr.length()>0)
						{
							t.WriteToFile(term, statusarr.toString());
						}
					}
					else
						if(apicode==FRIEND_ID)
						{
							JSONObject jobj = t.GetFriendsid(term);
							if(jobj!=null&&jobj.length()!=0)
							{
								t.WriteToFile(term, jobj.toString());
							}
						}

						else
						if(apicode == RETWEETS)
						{
							JSONArray statusarr = t.GetReTweets(term);
							if(statusarr.length()>0)
							{
								t.WriteToFile(term, statusarr.toString());
							}
						}

							else
								if(apicode == SEARCH_QUERIES)
								{
									JSONArray results = t.GetSearchResults(t.CreateORQuery(queryterms));
									if(results!=null)
									{
										t.WriteToFile(term, results.toString());
									}
								}

							else
								if(apicode == STATUSES_INFO)
								{
									JSONArray statusarr = t.GetStatuses(term);

									//JSONArray statusarr = new JSONArray(content.toString());
									if(statusarr.length()==0)
									{
										break;
									}
									for(int i=0;i<statusarr.length();i++)
									{
										try {
											JSONObject jobj = statusarr.getJSONObject(i);
											t.WriteToFile(term, jobj.toString());
										} catch (JSONException ex) {
											Logger.getLogger(TwitterAnalytics.class.getName()).log(Level.SEVERE, null, ex);
										}
									}
								}
			}
		}
		//now you can close the files as all the threads have finished
		t.CleanupAfterFinish();
	}
}


