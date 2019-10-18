package com.company;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.json.simple.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("/home/yaadesh/Desktop/user_data.json"))
        {
            //Read JSON file
            JSONObject userData = (JSONObject) jsonParser.parse(reader);

            HashMap map = buildIndex(userData);
            System.out.println(map);

            String USER_A_KEY = "UserA";
            String USER_B_KEY = "UserB";
            String USER_C_KEY = "UserC";
            String USER_D_KEY = "UserD";

            //System.out.println(userData.get(USER_A_KEY));
            int similarityScore = compareTwoUsers(USER_A_KEY,USER_D_KEY,map,(JSONObject)userData.get(USER_A_KEY));

            System.out.println("Similarity score is: "+similarityScore);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

 // Restructure data such that lookups are easier and scalable.
/*
             /========= Spiderman {UserX,UserW}
Movie ======/ ========= Avengers {UserF}
|
Food ======= Pizza {UserX,UserY}
...
 */

public static HashMap buildIndex(JSONObject userDataJson){
        HashMap<String,HashMap> map = initializeMap();

    Iterator userIterator = userDataJson.keySet().iterator();

        while(userIterator.hasNext()){
            String userKey = (String)userIterator.next();
            JSONObject userValue = (JSONObject) userDataJson.get(userKey);

            Iterator categoryIterator = userValue.keySet().iterator();

            while(categoryIterator.hasNext()){
                String categoryKey = (String) categoryIterator.next();
                //userValue.get(categoryKey)

                JSONArray categoryArray = (JSONArray) userValue.get(categoryKey);


                Iterator valueIterator  = categoryArray.iterator();
                while(valueIterator.hasNext()) {

                    String value = valueIterator.next().toString();
                    if(!map.get(categoryKey).containsKey(value)){
                        HashSet<String> temp = new HashSet<>();
                        temp.add(userKey);
                        map.get(categoryKey).put(value.toLowerCase(),temp);
                    }
                    else{
                        HashSet<String> userSet = (HashSet<String>) map.get(categoryKey).get(value);
                        userSet.add(userKey);
                    }
                }

            }

        }

    return map;

}


public static int compareTwoUsers(String userA, String userB,HashMap<String,HashMap<String,HashSet>> map, JSONObject userTemp){
        JSONObject jsonObjectUserA = userTemp;
    Iterator categoryIterator = jsonObjectUserA.keySet().iterator();

    SimilarityScore similarityScore = new SimilarityScore();
    while(categoryIterator.hasNext()){
        String categoryKey = (String) categoryIterator.next();

        JSONArray categoryArray = (JSONArray) jsonObjectUserA.get(categoryKey);


        Iterator valueIterator  = categoryArray.iterator();

        while(valueIterator.hasNext()) {

            String value = valueIterator.next().toString().toLowerCase();

            if(map.get(categoryKey).get(value).contains(userB)){
                switch(categoryKey){
                    case "sports":
                        similarityScore.addGroupACountAndTag();
                        break;
                    case "movie":
                        similarityScore.addGroupACountAndTag();
                        break;
                    case "food":
                        similarityScore.addGroupBCountAndTag();
                        break;
                    case "cities":
                        similarityScore.addGroupBCountAndTag();
                        break;
                    case "music":
                        similarityScore.addGroupCCountAndTag();
                        break;
                    case "book":
                        similarityScore.addGroupCCountAndTag();
                        break;
                }
            }
        }

    }

    /*
    Total 15 matching tags (at least 5 from group A) - 100%
            4 group A tags or 14+ matching tags - 80%
            2 group A tags or 5 group B tags or 12+ matching tags - 60%
            1 group A tags or 3 group B tags or 10+ matching tags - 40%
            1 group B tags or 5 group C tags or 8+ matching tags - 20%
            less than 5 group C tags matching - 10%
            no tags matching - 0%

       */

    if(similarityScore.getMatchingTags()==0)
        similarityScore.setSimilarityScore(0);
    else if(similarityScore.getMatchingTags()>=15 || similarityScore.getGroupACount()>=5)
        similarityScore.setSimilarityScore(100);
    else if(similarityScore.getMatchingTags()>=14 || similarityScore.getGroupACount()>=4)
        similarityScore.setSimilarityScore(80);
    else if(similarityScore.getMatchingTags()>=12 || similarityScore.getGroupACount()>=2 ||similarityScore.getGroupBCount() >=5)
        similarityScore.setSimilarityScore(60);
    else if(similarityScore.getMatchingTags()>=10 || similarityScore.getGroupACount()>=1 ||similarityScore.getGroupBCount() >=3)
        similarityScore.setSimilarityScore(40);
    else if(similarityScore.getMatchingTags()>=8 || similarityScore.getGroupBCount()>=1 || similarityScore.getGroupCCount()>=5)
        similarityScore.setSimilarityScore(20);
    else if( similarityScore.getGroupCCount()<=5 && similarityScore.getGroupCCount()>0)
        similarityScore.setSimilarityScore(10);

    return similarityScore.getSimilarityScore();


}
public static HashMap initializeMap(){
    HashMap<String, HashMap> map = new HashMap<>();

    map.put("movie",new HashMap());
    map.put("cities",new HashMap());
    map.put("food",new HashMap());
    map.put("sports",new HashMap());
    map.put("books",new HashMap());
    map.put("music",new HashMap());

    return map;
}


    public static class SimilarityScore{
        private int matchingTags=0;
        private int groupACount=0;
        private int groupBCount=0;
        private int groupCCount=0;
        private int similarityScore=-9999;

        public int getGroupACount(){
            return this.groupACount;
        }

        public int getGroupBCount(){
            return this.groupBCount;
        }

        public int getGroupCCount(){
            return this.groupCCount;
        }

        public int getMatchingTags(){
            return this.matchingTags;
        }

        public void addGroupACountAndTag(){
            this.groupACount++;
            this.matchingTags++;
        }

        public void addGroupBCountAndTag(){
            this.groupBCount++;
            this.matchingTags++;
        }

        public void addGroupCCountAndTag(){
            this.groupCCount++;
            this.matchingTags++;
        }

        public void setSimilarityScore(int sim){
            this.similarityScore = sim;
        }
        public int getSimilarityScore(){
            return this.similarityScore;
        }


    }

}

