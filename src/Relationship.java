import java.util.Map;

public enum Relationship {
    LESS_THAN,
    MORE_THAN,
    EQUAL_TO;

    public static Relationship parseRel(String relationshipName){
        switch (relationshipName){
            case "<" :
                return Relationship.LESS_THAN;
            case ">":
                return Relationship.MORE_THAN;
            case "=" :
                return Relationship.EQUAL_TO;
            default:
                try {
                    throw new Exception("判断条件错误");
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
        }
    }

    /**
     * 根据提供的数据，所在域，关系，比较的数据，返回匹配信息
     * @param srcData   提供的数据
     * @param field     所在域
     * @param relationship  关系
     * @param condition     比较的数据
     * @return
     */
    public static boolean matchCondition(Map<String,String>srcData,Field field,Relationship relationship,String condition){
        if (null == srcData.get(field.getName()) ||"[NULL]".equals(srcData.get(field.getName()))){
            return false;
        }

        //System.out.println(condition);
        String srcDataValue = srcData.get(field.getName());
        Integer result = null;
        switch (field.getType()) {
            case "int":
                result = Integer.valueOf(srcDataValue).compareTo(Integer.valueOf(condition));
                break;
            case "double":
                result = Double.valueOf(srcDataValue).compareTo(Double.valueOf(condition));
                break;
            case "varchar":
                result = srcDataValue.compareTo(condition);
                break;
            default:
                try {
                    throw new Exception("表中不存在该域");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        switch (relationship){
            case LESS_THAN:
                if (result < 0){
                    return true;
                }else {
                    return false;
                }
            case EQUAL_TO:
                if (result==0){
                    return true;
                }else {
                    return false;
                }
            case MORE_THAN:
                if (result > 0){
                    return true;
                }else {
                    return false;
                }
            default:
                try {
                    throw new Exception("条件限定不匹配");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                    return false;
        }

    }

}
