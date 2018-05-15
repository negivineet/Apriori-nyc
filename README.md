# Apriori-nyc
Implementation of Apriori Algorithm on NYC-311 Dataset
You will need 311 - DATASET ( download online - available for free )

1. java AP -g < $F > 1col.txt 
This mode ("generate") will take an input file and generate 1 column
2. java AP -v 0.09 0.31 < small.txt 
This mode ("verbose") will show implementation of A-priori on a small data set
3. java AP -m minsup minconf $F 1col.txt  
This mode ("Master") will show implementation of A-priori on a full data set along with generated column in step 1.
