# install necessary python libs
pip3 install -r requirements.txt | grep -v 'already satisfied'
pip3 install --upgrade pip setuptools wheel --user
pip3 install shap --user
pip3 install catboost --user
pip3 install pandas --user
pip3 install scikit-learn --user

year=$1
MONTH_PATH_1=$2
MONTH_PATH_2=$3
WRITE_DIR=$4

month="${MONTH_PATH_2##*/}"

#RUN PARSER
java -jar parse-profile.jar $year $MONTH_PATH_2 $WRITE_DIR
outfile_name=$WRITE_DIR"/"$year"-"$month".csv"

#RUN prediction
py predict.py $outfile_name $year $month $WRITE_DIR
portfolio_file=$WRITE_DIR"/"$year"-"$month"-portfolio.csv"

#RUN trade generator
java -jar create-trades.jar 0 $year $month $MONTH_PATH_2 $WRITE_DIR
trades_file=$WRITE_DIR"/"$year"-"$month"-trades.txt"
cat $trades_file 