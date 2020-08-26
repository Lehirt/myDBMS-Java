import java.io.Serializable;
import java.util.Objects;

public class IndexKey implements Serializable ,Comparable{

    private String value;//在索引树中被比较的key值
    private String type;//数据类型，根据该类型选择比较方法


    public IndexKey(String value, String type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    /**
     * treeMap底层构造函数：
     * 传递Comparator具体实现，按照该实现规则进行排序：
     *          public TreeMap(Comparator<? super K> comparator) {this.comparator = comparator;}
     * @param otherValue
     * @return
     */
    @Override
    public int compareTo(Object otherValue) {

        String keyValue = ((IndexKey)otherValue).getValue();
        switch (this.type){
            case "int" :
                return Integer.valueOf(this.value).compareTo(Integer.valueOf(keyValue));
            case "double" :
                return Double.valueOf(this.value).compareTo(Double.valueOf(keyValue));
            case "varchar" :
                return this.value.compareTo(String.valueOf(keyValue));
            default:
                try {
                    throw new Exception("条件限定不匹配");
                }catch (Exception e){
                    e.printStackTrace();
                }
        return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexKey indexKey = (IndexKey) o;
        return Objects.equals(value, indexKey.value) &&
                Objects.equals(type, indexKey.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }
}
