package qxtr.loader;

import qxtr.model.dataset.DataSet;
import qxtr.model.dataset.DataSetImport;

public interface LoaderInterface {
    public DataSetImport load(DataSet dataSet,LoaderInput input);
}
