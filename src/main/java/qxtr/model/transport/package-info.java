@FilterDef(
        name="areaConfiguration",
        parameters=@ParamDef(
                name="areaConfiguration",
                type="qxtr.model.transport.area.AreaConfiguration" ),
        defaultCondition = "areaConfiguration = :areaConfiguration")

@FilterDef(
        name="dataSetImport",
        parameters=@ParamDef(
                name="dataSetImport",
                type="qxtr.model.transport.dataset.DataSetImport" ),
        defaultCondition = "dataSetImport = :dataSetImport")

@FilterDef(
        name="dataSetImports",
        parameters=@ParamDef(
                name="dataSetImports",
                type="qxtr.model.transport.dataset.DataSetImport" ),
        defaultCondition = "dataSetImport in {:dataSetImports}")

@GenericGenerator(
        name = "ds-entity-sequence",
        strategy = "enhanced-sequence",
        parameters = {
                @Parameter(name="prefer_sequence_per_entity", value="true"),
                @Parameter(name="optimizer", value="hilo"),
                @Parameter(name="increment_size", value="10000")})

package qxtr.model.transport;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Parameter;

