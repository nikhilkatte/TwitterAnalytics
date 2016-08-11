#this code reads tweets for the given user as a json file and then returns the txt document where each line represents a tweet text
__author__ = 'Nikhil katte'
import sys
import codecs
import json

if __name__ == '__main__':
    if len(sys.argv) == 3:
        json_file = sys.argv[1]
        txt_file = sys.argv[3]
    output = codecs.open(txt_file,'w','utf-8')
    with codecs.open(json_file,'r','utf-8') as input:
        for line in input:
            #read each json object
            tweet = json.load(line)
            #extract the id, even though I am not using it anywhere
            id = tweet['id']
            #if the tweet is a retweet, extract its text
            if 'retweeted_status' in tweet:
               	text = tweet['retweeted_status']['text']
            else:
              #extract just the text of the tweet
    	        text = tweet['text']
            output.write(text)
