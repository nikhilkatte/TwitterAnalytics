import collections
__author__ = 'Nikhil katte'
import sys, math

import nltk
#nltk.download('punkt')
from nltk.collocations import BigramCollocationFinder
from nltk.metrics import BigramAssocMeasures
from nltk.probability import FreqDist, ConditionalFreqDist

import sklearn
from sklearn.svm import LinearSVC
from sklearn.naive_bayes import MultinomialNB
from sklearn.linear_model import LogisticRegression
from nltk.classify.scikitlearn import SklearnClassifier

#breaks up the sentences into lists of individual words (as selected by the input mechanism) and appends 'pos' or 'neg' after each list
def select_best_features_from_all_features(file,best_words,type):
    fea = []
    with open(file,'r') as sentences:
        for sentence in sentences:
            #use nltk word tokenizer
            words = nltk.word_tokenize(sentence)
            words = [best_word_features(words,best_words), type]
            fea.append(words)
    return fea

#creates lists of all positive and negative words
def words_in_class(file):
    word_type = []
    with open(file,'r') as sentences:
        for sentence in sentences:
            #use nltk word tokenizer
            words = nltk.word_tokenize(sentence)
            word_type.extend(words)
    return word_type

#Feature extraction using all words as features
def bag_of_words(words):
    return dict([(word,True)] for word in words)

# Combine words and bigrams and compute words and bigrams information scores
def create_word_bigram_scores(pos_words, neg_words):

    #print pos_words
    #Retreive all occuring bi grams
    pos_bigram_finder = BigramCollocationFinder.from_words(pos_words)
    neg_bigram_finder = BigramCollocationFinder.from_words(neg_words)

    #Retreive best bigrams
    posBigrams = pos_bigram_finder.nbest(BigramAssocMeasures.chi_sq, 5000)
    negBigrams = neg_bigram_finder.nbest(BigramAssocMeasures.chi_sq, 5000)

    #combining unigrams and top 5000 bigrams to form words
    pos = pos_words + posBigrams
    neg = neg_words + negBigrams

    #Computing word frequency Distribution using nltk library
    word_fd = FreqDist()

    #Computing conditional probablity of each word in postive corpus and negative corpus
    cond_word_fd = ConditionalFreqDist()
    for word in pos:
        word_fd[word] += 1
        cond_word_fd['pos'][word] += 1

    for word in neg:
        word_fd[word] +=1
        cond_word_fd['neg'][word] +=1

    pos_word_count = cond_word_fd['pos'].N()
    neg_word_count = cond_word_fd['neg'].N()
    total_word_count = pos_word_count + neg_word_count

    #calculating information score for terms using chi sqaure test
    word_scores = {}
    for word, freq in word_fd.iteritems():
        pos_score = BigramAssocMeasures.chi_sq(cond_word_fd['pos'][word], (freq, pos_word_count), total_word_count)
        neg_score = BigramAssocMeasures.chi_sq(cond_word_fd['neg'][word], (freq, neg_word_count), total_word_count)
        word_scores[word] = pos_score + neg_score

    return word_scores

    #we should extract the most informative words based on the information score
def find_best_words(word_scores, number):
    best_val = sorted(word_scores.iteritems(), key= lambda (word,score): score,reverse=True)[:number]
    best_words = set(map(lambda (word,score): word,best_val))
    return best_words

# Now use the most informative words and bigrams as machine learning features
# If the word vector consists of a word, return a dic with true
def best_word_features(words,best_words):
    return dict([(word, True) for word in words if word in best_words])

# Train classifier and return the  classification accuracy
def evaluate(classifier,trainFeatures,testFeatures):

    classifier = SklearnClassifier(classifier)
    classifier.train(trainFeatures)
    #initiates referenceSets and testSets
    referencesets = collections.defaultdict(set)
    testsets = collections.defaultdict(set)

    #puts correctly labeled sentences in referenceSets and the predictively labeled version in testsets
    for i, (features, label) in enumerate(testFeatures):
		referencesets[label].add(i)
		predicted = classifier.classify(features)
		testsets[predicted].add(i)

	#prints metrics to show how well the feature selection did
    #print 'train on %d instances, test on %d instances' % (len(trainFeatures), len(testFeatures))
    print 'accuracy:', nltk.classify.util.accuracy(classifier, testFeatures)
    print 'pos precision:', nltk.metrics.precision(referencesets['pos'], testsets['pos'])
    print 'pos recall:', nltk.metrics.recall(referencesets['pos'], testsets['pos'])
    print 'neg precision:', nltk.metrics.precision(referencesets['neg'], testsets['neg'])
    print 'neg recall:', nltk.metrics.recall(referencesets['neg'], testsets['neg'])

if __name__ == '__main__':
    if len(sys.argv) == 3:
        pos_tweet_file = sys.argv[1]
        neg_tweet_file = sys.argv[2]

        #extract positive/negative words from postive/negative tweets file
        pos_words = words_in_class(pos_tweet_file)
        neg_words= words_in_class(neg_tweet_file)

        #consider all unigrams + top 5000 informative positive/negative bigrams based on chi-sqaured test
        word_scores = create_word_bigram_scores(pos_words, neg_words)


        #best_words = find_best_words(word_scores, number)

        #numbers of features to select. I selected this number in random in order to get the best metrics
        dimention = ['500','1000','2500','5000','10000', '15000']

        for d in dimention:
            #we should extract the most informative words based on the information score
            best_words = find_best_words(word_scores,int(d))
            posFeatures = select_best_features_from_all_features(pos_tweet_file,best_words,'pos')
            negFeatures = select_best_features_from_all_features(neg_tweet_file,best_words,'neg')

            #selects 3/4 of the features for training and 1/4 for testing
            posCutoff = int(math.floor(len(posFeatures)*3/4))
            negCutoff = int(math.floor(len(negFeatures)*3/4))
            trainFeatures = posFeatures[:posCutoff] + negFeatures[:negCutoff]
            testFeatures = posFeatures[posCutoff:] + negFeatures[negCutoff:]

            #Evalutaion of sentiment analysis on 3 classifiers with features as top d (unigrams + bi-grams)
            print 'MultinomiaNB classifier`s metrics for %d features are' %(int(d)),evaluate(MultinomialNB(),trainFeatures,testFeatures)
            print 'LogisticRegression classifier`s metrics for %d features are' %(int(d)),evaluate(LogisticRegression(),trainFeatures,testFeatures)
            print 'LinearSVC`s metrics for %d features are' %(int(d)),evaluate(LinearSVC(),trainFeatures,testFeatures)




