package qxtr.loader;

import qxtr.model.DataSet;
import qxtr.model.DataSetImport;

public interface LoaderInterface {
    public DataSetImport load(DataSet dataSet,LoaderInput input);
}
