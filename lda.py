#This code generates lda model for a corpus using gensim, nltk libraries.
__author__ = 'Nikhil'

import nltk
import sys,string
#nltk.download('stopwords')
#nltk.download('punkt')
from gensim import corpora, models
from nltk.corpus import stopwords

#default nltk stop words
stop_words = set(stopwords.words("english"))

if __name__ == '__main__':
    words_as_list_in_each_doc = []
    if len(sys.argv) == 3:
        #file consisting of tweets, each line is a tweet and is considered as a document
        tweet_file = sys.argv[1]
        #number of topics to be obtained by lda
        num_of_topics = sys.argv[2]
    with open(tweet_file,'r') as tweets:
        for tweet in tweets:
            # tokenize the tweets using nltk tokenizer
            words = nltk.word_tokenize(tweet.lower())
            #remove punctuation from the words
            words = map(lambda word: word.lower().translate(None, string.punctuation),words)
            #remove stop words from the tweets
            words = filter(lambda word: word not in stop_words,words)
            words_as_list_in_each_doc.append(words)

    #create a dictionary of all the words in the entire corpus
    dictionary = corpora.Dictionary(words_as_list_in_each_doc)

    # Creates the Bag of Word corpus.
    corpus = [dictionary.doc2bow(doc) for doc in words_as_list_in_each_doc]

    #train the lda model based on the number the topics we need to generate
    lda = models.ldamodel.LdaModel(corpus=corpus, id2word=dictionary, num_topics= num_of_topics, update_every=1, chunksize=10000,
                               passes=1)
    #print lda.print_topics(5)
    i = 0
    #generate top topics from the corpus using lda and then retrieve top 'k' words within each topics
    for topic in lda.top_topics(corpus,50):
        print "Words in Topic %d \n" %(i), topic
        #print "\n"'''
        words,score = topic
        for score,word in words:
            print word
        i = i+1
