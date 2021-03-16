
import sys
import numpy as np
import pandas as pd
from sklearn.impute import SimpleImputer
# import data

def value_to_float(x):
    if type(x) == int or type(x) == float: return x
    if 'K' in x:
        return value_to_float(x.replace('K', '')) * 1000
    elif 'M' in x:
        return value_to_float(x.replace('M', '')) * 1000000
    elif 'B' in x:
        return value_to_float(x.replace('B', '')) * 1000000000
    elif '%' in x:
        return value_to_float(x.replace('%', ''))
    try:
        return float(x.replace(',', '').replace('$', '').replace(' ', ''))
    except ValueError:
        return np.nan

def read_data(path):
    # deal with test data
    test = pd.read_csv(path)
    test=test[int(test['DCV'])>1000000]
    del test['Last Split']
    del test['Fiscal Year Ends']
    del test['Most recent quarter']
    test=test.replace('none', np.nan)
    test=test.dropna(thresh=30)
    del test['Price/Earnings']
    del test['Annual Dividend']
    del test['Dividend Yield']
    del test['Debt/Equity']
    test_companies=test['Company']
    del test['Company']
    test=test.applymap(value_to_float)
    my_imputer = SimpleImputer()
    test_filled = pd.DataFrame(my_imputer.fit_transform(test))
    test_filled.columns=test.columns
    test_filled.index=test.index
    return test_filled,test_companies

#pip install catboost

# Cat Boost Regressor
from catboost import CatBoostRegressor
def predict(path,year,month,outputFolder):
    cat = CatBoostRegressor()
    cat_model = cat.load_model("trained_model")#path of trained model
    X_test,test_companies=read_data(path)
    cat_pred = cat_model.predict(X_test)
    pred_result=pd.DataFrame(test_companies)
    pred_result["result"] =cat_pred
    first_20_result=pred_result.sort_values('result',ascending=False)
    first_20_result = first_20_result.head(20)
    # Saving the results in a csv file
    first_20_result.to_csv(outputFolder+"/"+year+"-"+month+"-portfolio.csv", index = False, header = True)


if __name__ == "__main__":
   predict(sys.argv[1],sys.argv[2],sys.argv[3],sys.argv[4])