package lse;

import java.io.*;
import java.util.*;

/**
 * This class builds an index of keywords. Each keyword maps to a set of pages in
 * which it occurs, with frequency of occurrence in each page.
 *
 */
public class LittleSearchEngine {
    
    /**
     * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
     * an array list of all occurrences of the keyword in documents. The array list is maintained in 
     * DESCENDING order of frequencies.
     */
    HashMap<String,ArrayList<Occurrence>> keywordsIndex;
    
    /**
     * The hash set of all noise words.
     */
    HashSet<String> noiseWords;
    
    /**
     * Creates the keyWordsIndex and noiseWords hash tables.
     */
    public LittleSearchEngine() {
        keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
        noiseWords = new HashSet<String>(100,2.0f);
    }
    
    /**
     * Scans a document, and loads all keywords found into a hash table of keyword occurrences
     * in the document. Uses the getKeyWord method to separate keywords from other words.
     * 
     * @param docFile Name of the document file to be scanned and loaded
     * @return Hash table of keywords in the given document, each associated with an Occurrence object
     * @throws FileNotFoundException If the document file is not found on disk
     */
    public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
    throws FileNotFoundException {
        /** COMPLETE THIS METHOD **/
        HashMap<String, Occurrence> mapDocFile = new HashMap<String, Occurrence>(100,2f);
        try(Scanner sc = new Scanner(new File(docFile)))
        {
        while (sc.hasNextLine()) 
        {
           //get line
           String line = sc.nextLine();
           
           //split line by space,now you have an array of words in THAT line
           String[] lineSplit=line.trim().split("\\s+");
           
           //set up a for loop for each word in document
           for(int i=0; i<lineSplit.length; i++)
           {
               String currWord=lineSplit[i];
               
               //call getkeyWord on this word
               String str=getKeyword(currWord);
               
               /*
                * if the word is keyword, that is,
                * getKeyWord returns anything 
                * that is NOT null, then add 
                * to hash table
                */
               if(str!=null)
               {
                   /*
                    * traverse HashTable for any
                    * existing occurrences of str
                    * 
                    * if already existing, then just update
                    */
                  if(mapDocFile.containsKey(str))
                  {
                      Occurrence occur=mapDocFile.get(str);
                      occur.frequency++;
                  }
                  //if the str is not contained in hash table already
                  else
                  {
                      Occurrence occ=new Occurrence(docFile,1);
                      mapDocFile.put(str,occ);
                  }
                   
               }
           }
        }
    }
    
    catch (FileNotFoundException e) 
    {
        //throw exception if no document in that name
        throw new FileNotFoundException("File not Found!");
    }
        

        
        
    return mapDocFile;
        
                
    
    }
    
    /**
     * Merges the keywords for a single document into the master keywordsIndex
     * hash table. For each keyword, its Occurrence in the current document
     * must be inserted in the correct place (according to descending order of
     * frequency) in the same keyword's Occurrence list in the master hash table. 
     * This is done by calling the insertLastOccurrence method.
     * 
     * @param kws Keywords hash table for a document
     */
    public void mergeKeywords(HashMap<String,Occurrence> kws) 
    {
        //iterate through the hashtable from one doc file, take each word from it
        for(String kwsWord: kws.keySet())
        { 
            //get the occurrence object at that word
            Occurrence occur = kws.get(kwsWord);
            
            //now grab the arraylist of occurrences for that word in the MASTER Hash-TABLE
            ArrayList<Occurrence> list = keywordsIndex.get(kwsWord);
            
            //if the arraylist for that word is EMPTY in the master hashtable, i.e., that word ain't in the hash table,
            //then create a new arraylist
            if(list==null)
            {    
                list = new ArrayList<Occurrence>();
                
                //add the key-value pair of word and list to the hashtable
                keywordsIndex.put(kwsWord, list);
            }
            
            //add the word occurences from docFile hashtable to the list in the MASTER hashtable
            list.add(occur);
            
            //call the insertLastOccurrence method on this list with the newly added word(we're technically adding occurrences), 
            //insertLastOccurrence will organize the items aptly. 
            this.insertLastOccurrence(list);
                    
        }
        
    }
    
    /**
     * Given a word, returns it as a keyword if it passes the keyword test,
     * otherwise returns null. A keyword is any word that, after being stripped of any
     * trailing punctuation(s), consists only of alphabetic letters, and is not
     * a noise word. All words are treated in a case-INsensitive manner.
     * 
     * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
     * NO OTHER CHARACTER SHOULD COUNT AS PUNCTUATION
     * 
     * If a word has multiple trailing punctuation characters, they must all be stripped
     * So "word!!" will become "word", and "word?!?!" will also become "word"
     * 
     * See assignment description for examples
     * 
     * @param word Candidate word
     * @return Keyword (word without trailing punctuation, LOWER CASE)
     */
    public String getKeyword(String word) {
        /** COMPLETE THIS METHOD **/
        //method to check if word is keyword, and returns the keyword equivalent if it is
        
        //takes the word and gets rid of white-spaces and lower-cases it
        word=word.trim().toLowerCase();
        
        //empty word 
        if(word==null || word.length()==0)
        {
            return null;
        }
         
        //iterate through individual characters of the word and see if a punctuation is hit
        int index=-1;
        for(int i=0; i<word.length(); i++)
        {
            char ch=word.charAt(i);
            //when punctuation is hit
            if(!Character.isLetter(ch))
            {
                index=i;
                break;
            }
        }
        
        //the word contains no punctuation
        if(index==-1)
        {
            //check if word is a noise-word, if it isn't then return that word
            if(!noiseWords.contains(word))
            {
                return word;
            }
            else
            {
                return null;
            }
        }
        
        /*the word does contain punctuation!!
         * case 1) punctuation is in the middle of the word: return null
         * case 2) punctuation is at the end, trailing:  cut out trailing and then return word
         */
        String wordAfterPunctuation=word.substring(index+1);
        String wordBeforePunctuation=word.substring(0,index);
        
        
        
        boolean hasAlphaAftPunc=checkForAlphabet(wordAfterPunctuation);
        
        //case 1: middle
        if(hasAlphaAftPunc)
        {
            //this is wrong format for word, return null
            return null;
        }
        //case 2: trailing 
        else
        {
            /* first check for noiseWord, if not noise word, 
             * then return the word without 
             * trailing characters
             */
            if(!(noiseWords.contains(wordBeforePunctuation)))
            {
                return wordBeforePunctuation;
            }
            else
            {
                //this is a noiseWord, return null;
                return null;
            }
            
        }
        
    }
    

    //private helper method: returns boolean value, checks is alphabet
    private static boolean checkForAlphabet(String theWord)
    {
        for(int j=0; j<theWord.length(); j++)
        {
            char ch=theWord.charAt(j);
            if(ch>='a' && ch<='z')
            {
                return true;
            }
            
        }
        return false;
        
    }
    
    
    /**
     * Inserts the last occurrence in the parameter list in the correct position in the
     * list, based on ordering occurrences on descending frequencies. The elements
     * 0..n-2 in the list are already in the correct order. Insertion is done by
     * first finding the correct spot using binary search, then inserting at that spot.
     * 
     * @param occs List of Occurrences
     * @return Sequence of mid point indexes in the input list checked by the binary search process,
     *         null if the size of the input list is 1. This returned array list is only used to test
     *         your code - it is not used elsewhere in the program.
     */
    public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) 
    {
        /** COMPLETE THIS METHOD **/
        ArrayList<Integer> freCollec=new ArrayList<Integer>();
        
        if(occs.isEmpty())
        {
            return null;
        }

        
        //get the frequency at last item of occs
        Occurrence lastItem=occs.get(occs.size()-1);
        occs.remove(occs.size()-1);
        int lastItemFrequency=lastItem.frequency;
        
        int lo=0;
        int hi=occs.size()-1;
        int mid=(lo+hi)/2;
        
        while(lo<=hi)
        {
            mid=(lo+hi)/2;
            if(lastItemFrequency>occs.get(mid).frequency)
            {
               hi=mid-1;    
               freCollec.add(mid);
            }
            else if(lastItemFrequency==occs.get(mid).frequency)
            {
                freCollec.add(mid);
                break;
            }
            else
            {
                lo=mid+1;
                freCollec.add(mid);
                mid=mid+1;
            }
            
        }
    
            occs.add(mid, lastItem);
            return freCollec;
        }
        
         

    
    
    /**
     * This method indexes all keywords found in all the input documents. When this
     * method is done, the keywordsIndex hash table will be filled with all keywords,
     * each of which is associated with an array list of Occurrence objects, arranged
     * in decreasing frequencies of occurrence.
     * 
     * @param docsFile Name of file that has a list of all the document file names, one name per line
     * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
     * @throws FileNotFoundException If there is a problem locating any of the input files on disk
     */
    public void makeIndex(String docsFile, String noiseWordsFile) 
    throws FileNotFoundException {
        // load noise words to hash table
        Scanner sc = new Scanner(new File(noiseWordsFile));
        while (sc.hasNext()) {
            String word = sc.next();
            noiseWords.add(word);
        }
        
        // index all keywords
        sc = new Scanner(new File(docsFile));
        while (sc.hasNext()) {
            String docFile = sc.next();
            HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
            mergeKeywords(kws);
        }
        sc.close();
    }
    
    /**
     * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
     * document. Result set is arranged in descending order of document frequencies. 
     * 
     * Note that a matching document will only appear once in the result. 
     * 
     * Ties in frequency values are broken in favor of the first keyword. 
     * That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2 also with the same 
     * frequency f1, then doc1 will take precedence over doc2 in the result. 
     * 
     * The result set is limited to 5 entries. If there are no matches at all, result is null.
     * 
     * See assignment description for examples
     * 
     * @param kw1 First keyword
     * @param kw1 Second keyword
     * @return List of documents in which either kw1 or kw2 occurs, arranged in descending order of
     *         frequencies. The result size is limited to 5 documents. If there are no matches, 
     *         returns null or empty array list.
     */
    public ArrayList<String> top5search(String kw1, String kw2) {
        
        ArrayList<Occurrence> list1 = keywordsIndex.get(kw1);
        ArrayList<Occurrence> list2= keywordsIndex.get(kw2);
        ArrayList<String> answer=new ArrayList<String>();
        
    

        //if list1 and list2 null
        if (list1 == null && list2 == null) {
            return null;
        }
        
        //if list1 null and list2 not null
        if (list1 == null && list2 != null) {
            
            int counter=0; 
            while(counter<5 && counter<list2.size())
            {
                answer.add(list2.get(counter).document);
                counter++;
            }
            return answer;
        }
        
        //if list1 not null and list2 null
        if (list1 != null && list2 == null) {
            
            int counter=0; 
            while(counter<5 && counter<list1.size())
            {
                answer.add(list1.get(counter).document);
                counter++;
            }
            return answer;
            
            
        }
        
        /*
         * if both list1 and list2 aren't null, merge both of them to
         * create a an arraylist with
         * occurrence objects
         * in decreasing order of frequency
        */
        else 
        {
            ArrayList<Occurrence> result = new ArrayList<>();
            int list1Count=0;
            int list2Count=0;
            while(list1Count<list1.size() && list2Count<list2.size())
            {
                //frequency at list1 is greater
                if(list1.get(list1Count).frequency > list2.get(list2Count).frequency)
                {
                    result.add(list1.get(list1Count));
                    list1Count++;
                    
                }
                
                //frequency at list2 is greater
                else if(list2.get(list2Count).frequency > list1.get(list1Count).frequency)
                {
                    result.add(list2.get(list2Count));
                    list2Count++;
                }
                
                //frequency at both is the same, add both, but add list1 first
                else
                {
                    result.add(list1.get(list1Count));
                    result.add(list2.get(list2Count));
                    list1Count++;
                    list2Count++;
                    
                }
                
            }
            
            //must add remaining 
            if(list1Count<list1.size())
            {
                while(list1Count<list1.size())
                {
                    result.add(list1.get(list1Count));
                    list1Count++;
                }
            }
            
            if(list2Count<list2.size())
            {
                while(list2Count<list2.size())
                {
                    result.add(list2.get(list2Count));
                    list2Count++;
                }
            }
            
            
            
            /*
             * we now have an arraylist with occurrence objects of kw1 and kw2 in decreasing order of frequencies
             * we must print out the first five! 
             * (or less if less)
             */
    
            
            int count=0;
            while(count<5 && count<result.size())
            {
                if (!answer.contains(result.get(count).document))
                {
                    answer.add(result.get(count).document);
                }
                count++;
            }
            
            return answer;
            
            
            
        }
        
    }
    
}