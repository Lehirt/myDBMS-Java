import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SingleFilter {
    private Field field;
    private String relationShipName;
    private String condition;

    public SingleFilter(Field field,String relationShipName,String condition){
        this.field=field;
        this.relationShipName=relationShipName;
        this.condition=condition;
    }

    /**
     *
     * @param srcDatas 原数据
     * @return  过滤后的数据
     */
    public List<Map<String,String>> singleFilterData(List<Map<String,String>> srcDatas){

        Relationship relationship = Relationship.parseRel(relationShipName);
        List<Map<String,String>> datas = new ArrayList<>();

        //如果没有限定条件，返回原始列表
        if (null == field || null == relationship){
            return srcDatas;
        }

        for (Map<String, String> srcData : srcDatas) {
            //如果条件匹配成功，则新的列表存放该符合要求的数据
            if (Relationship.matchCondition(srcData,field,relationship,condition)){
                datas.add(srcData);
            }else {
                continue;
            }
        }

        return datas;

    }


}
