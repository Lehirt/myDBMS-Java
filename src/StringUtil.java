import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private final static Pattern relPattern = Pattern.compile("(\\w+)\\s?([<=>])\\s?([^\\s\\;]+)");


    public static List<Map<String,String>> parseWhere(String str){

        List<Map<String,String>> filterList = new LinkedList<>();

        String[] filtStrs = str.trim().split("and");

        for (String filtStr : filtStrs) {
            Map<String,String> filtMap = new LinkedHashMap<>();
            Matcher relMatcher = relPattern.matcher(filtStr);
            relMatcher.find();
            filtMap.put("fieldName",relMatcher.group(1));
            filtMap.put("relationshipName",relMatcher.group(2));
            filtMap.put("condition",relMatcher.group(3));

            filterList.add(filtMap);

        }
        return filterList;
    }

    public static Map<String,Field> parseCreateTable(String fieldsStr){
        String[] lines = fieldsStr.trim().split(",");
        Map<String,Field> fieldMap = new LinkedHashMap<>();

        for (String line : lines) {
            String[] property = line.trim().split(" ");

            Field field = new Field();

            field.setName(property[0]);
            field.setType(property[1]);
            //如果是主键字段后面加*
            if (3 == property.length && "*".equals(property[2])) {
                field.setPrimary(true);
            } else {
                field.setPrimary(false);
            }

            fieldMap.put(property[0],field);
        }
            return fieldMap;
    }


    public static Map<String, String> parseUpdateSet(String Str) {
        Map<String,String> dataMap = new LinkedHashMap<>();
        String[] setStrs = Str.trim().split(",");
        for (String setStr : setStrs) {
            Matcher relMatcher = relPattern.matcher(setStr);
            relMatcher.find();
            dataMap.put(relMatcher.group(1),relMatcher.group(3));
        }
        return dataMap;
    }
}
