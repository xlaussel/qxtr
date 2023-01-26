package qxtr.loader;

import qxtr.model.transport.dataset.DataSet;
import qxtr.model.transport.dataset.DataSetImport;

public interface LoaderInterface {
    public DataSetImport load(DataSet dataSet,LoaderInput input);
}
