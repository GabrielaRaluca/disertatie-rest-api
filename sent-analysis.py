import flask
import pandas as pd
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from flask import request, jsonify
from vaderSentiment.vaderSentiment import SentimentIntensityAnalyzer 
from sklearn.metrics.pairwise import cosine_similarity

# function to print sentiments 
# of the sentence. 
def sentiment_scores(sentence): 
  
    # Create a SentimentIntensityAnalyzer object. 
    sid_obj = SentimentIntensityAnalyzer() 
  
    # polarity_scores method of SentimentIntensityAnalyzer 
    # oject gives a sentiment dictionary. 
    # which contains pos, neg, neu, and compound scores. 
    sentiment_dict = sid_obj.polarity_scores(sentence) 
      
    print("Overall sentiment dictionary is : ", sentiment_dict) 
    print("sentence was rated as ", sentiment_dict['neg']*100, "% Negative") 
    print("sentence was rated as ", sentiment_dict['neu']*100, "% Neutral") 
    print("sentence was rated as ", sentiment_dict['pos']*100, "% Positive") 
  
    print("Sentence Overall Rated As", end = " ") 
  
    # decide sentiment as positive, negative and neutral 
    if sentiment_dict['compound'] >= 0.05 : 
        print("Positive") 
  
    elif sentiment_dict['compound'] <= - 0.05 : 
        print("Negative") 
  
    else : 
        print("Neutral") 

    return jsonify({'score': sentiment_dict['compound']})

app = flask.Flask(__name__)
app.config["DEBUG"] = True

def create_label_vectors(body):

	variables = body[0].keys()

	df = pd.DataFrame(body, columns = variables)
	Mean = df.groupby(by="userId",as_index=False)['score'].mean()
	Rating_avg = pd.merge(df,Mean,on='userId')
	Rating_avg['avg']=Rating_avg['score_x']-Rating_avg['score_y']
	get_user_similarity(Rating_avg)
	return df

def get_user_similarity(matrix):
	final=pd.pivot_table(matrix,values='score_x',index='userId',columns='label')
	print(final)
	final_scores = final.fillna(0)
	print(final_scores)

	cosine = cosine_similarity(final_scores)
	np.fill_diagonal(cosine, 0)
	similarities =pd.DataFrame(cosine,index=final_scores.index)
	similarities.columns=final_scores.index
	

	#TODO test this 
	similar_users = find_n_neighbours(similarities, 5)
	print(similarities)


def find_n_neighbours(df,n):
    order = np.argsort(df.values, axis=1)[:, :n]
    df = df.apply(lambda x: pd.Series(x.sort_values(ascending=False)
           .iloc[:n].index, 
          index=['top{}'.format(i) for i in range(1, n+1)]), axis=1)
    return df	


@app.route('/similarity', methods=['POST'])
def get_user_profiles():
    data = request.json 
    response = create_label_vectors(data['scores'])
    return response


@app.route('/sentiment', methods=['POST'])
def analyse():
    data = request.json 
    print(data)

    sentence = data['description']
    return sentiment_scores(sentence)

app.run()