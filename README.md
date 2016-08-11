<h2>TwitterAnalytics using Twitter RestApis </h2>

The goal of this project is to be able to perform Twitter Analytics using the Twitter Rest APIs

The reason for this project is that I want to perform two things using Twitter tweets:

Sentiment Analysis of Tweets.

I wanted to estimate US Congressmen's stance on a particular political issue/bill. For example, I want to capture Donald Trump's political ideology using his tweets and friends information on Twitter. Furthermore, if we make use of the text information of users’ tweets, especially retweets, to better understand political figure’s emphasis on different topics. Hypothetically,If Donald Trump follows Hillary CLinton on twitter and if he retweets lots of postive tweets of Hillary clinton related to Iran Nuke bill, we can predict that Donald Trump's stance on Iran Nuke bill topic is positive. Similarly, I try to establish every congressman's stance on a particular topic using his/her twitter interaction.

Twitter Crawler

This application does the following using Twitter REST APIs:

Authenticates my twitter application(MiningInfo) for Analytics on Twitter using OAuth
Returns entire Twitter profile information based on the username/twitter-id provided
Returns Twitter followers info (names,twitter ids, etc) for each twitter user/twitter-id provided.
Returns Twitter Statuses for each twitter user provided
Returns Twitter friend's ids for each twitter user provided
Returns Tweets based on the keyword search on Twitter.
Returns retweets by a user
crawls through each and every US congressmen's Twitter account and 1. Returns the US congressmen followers for each US congressmen 2. Tweets for each US congressmen 3. Retweets for each US congressmen.

Sentiment Analysis using Nltk/Scikit

This application predicts the sentiments of Tweets as 'positive' or 'negative' using natural language processing and machine learning techniques:

I use the following for my Sentiment classification:

I performed the following preprocessing to my tweets :

 1.Lowercase all characters
 2. Apply stemming using Porter Stemmer.
 3. Strip punctuations
 4. Replace #xyz with xyz
 5. Replace a word contains www. or http(s):// with URL so that we can treat all the urls the same
 6. Replace a word contains @someuser with AT_USER so that we can treat all the users the same
 7. Ignore words that don't start with an alphabet
 8. Use the stop words list to filter out low value words such as 'the', 'is' and 'on'.
 9. add all the positive tweets to positive_tweet file, where each line is a preprocessed tweet
 10.add all the negative tweets to negative_tweet file, where each line is a preprocessed tweet
Generated semi-supervised labelled data using emoticons, common positive/negative words and manual annotation.

To improve the accuracy, I considered only the most k informative terms(unigrams + bigrams) as features instead of entire corpus as features. Selection of most informative features is done using Chi-sqaure test

 The size of k choosen is : 500','1000','2500','5000','10000', '15000' 
I run my sentiment classification on the following classifiers:

 a. MultinomailNB scikit classifier
 b. Logistic Regression scikit classifier
 c. Linear Support Vector Machine scikit classifier
Explore US politician's view on a subject based on his/her Social Interaction

The goal is to explore 487 U.S. politicians stance/view on a particular topic/subject based on their twitter interaction.

Of the entire set, 265 people are Republicans and 222 people are Democrats. I get all politicians tweets/retweets .Due to the API limit, I could collect only around 3,300 most recent tweets for each user. This results in a dataset of 487 U.S. politicians and 713,830 tweets.

Out of these tweets, I generated the top tweeted topics using LDA(Latent Dirichlet Allocation) using gensim and nltk libraries. Results for top 5 topics using LDA are:

       Topic    Top words
       1        House, Senate
       2        Jobs, Congress
       3        Energy, Support
       4        Bill, Vote
       5        Budget, Obamacare
Now for these topics, I establish each politician's stance based on his/her tweets for that particular topic using sentiment analysis as described above
