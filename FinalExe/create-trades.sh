# install necessary python libs
pip3 install --upgrade pip setuptools wheel --user > /dev/null
pip3 install shap --user > /dev/null
pip3 install catboost --user > /dev/null
pip3 install pandas --user > /dev/null
pip3 install scikit-learn --user > /dev/null

year=$1
MONTH_PATH_1=$2
MONTH_PATH_2=$3
WRITE_DIR=$4

month="${MONTH_PATH_2##*/}"

#RUN PARSER
java -jar parse-profile.jar $year $MONTH_PATH_2 $WRITE_DIR
outfile_name=$WRITE_DIR"/"$year"-"$month".csv"

#RUN prediction
python3.6 predict.py $outfile_name $year $month $WRITE_DIR
portfolio_file=$WRITE_DIR"/"$year"-"$month"-portfolio.csv"

#RUN trade generator
java -jar create-trades.jar 0 $year $month $MONTH_PATH_2 $WRITE_DIR
trades_file=$WRITE_DIR"/"$year"-"$month"-trades.txt"
cat $trades_file 