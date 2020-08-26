
import javax.naming.Name;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    //对表的操作
    private static final Pattern patternCreateTable = Pattern.compile("create\\stable\\s(\\w+)\\s?\\(((?:\\s?\\w+\\s\\w+,?)+)\\)\\s?;");
    private static final Pattern patternAlterTable_add = Pattern.compile("alter\\stable\\s(\\w+)\\sadd\\s(\\w+\\s\\w+)\\s?;");
    private static final Pattern patternDropTable = Pattern.compile("drop\\stable\\s(\\w+)\\s?;");
    //增删查改
    private static final Pattern patternInsert = Pattern.compile("insert\\s+into\\s+(\\w+)\\s(\\(((\\w+,?)+)\\))?\\s+\\w+\\s+\\((([^\\)]+,?)+)\\);?");
    //Pattern patternDelete=Pattern.compile("delete\\sfrom\\s(\\w+)(?:\\swhere\\s(\\w+)\\s?([<=>])\\s?([^\\s\\;]+))?((?:\\s(?:and|or)\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*)?;?");
    private static final Pattern patternDelete = Pattern.compile("delete\\sfrom\\s(\\w+)(?:\\swhere\\s(\\w+\\s?[<=>]\\s?[^\\s\\;]+(?:\\sand\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*))?\\s?;");
    private static final Pattern patternUpdate = Pattern.compile("update\\s(\\w+)\\sset\\s(\\w+\\s?=\\s?[^,\\s]+(?:\\s?,\\s?\\w+\\s?=\\s?[^,\\s]+)*)(?:\\swhere\\s(\\w+\\s?[<=>]\\s?[^\\s\\;]+(?:\\sand\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*))?\\s?;");
    private static final Pattern patternSelect = Pattern.compile("select\\s(\\*|(?:(?:\\w+(?:\\.\\w+)?)+(?:\\s?,\\s?\\w+)*))\\sfrom\\s(\\w+(?:\\s?,\\s?\\w+)*)(?:\\swhere\\s([^\\;]+))?;");



    public static void main(String[] args) {

        User user = new User("user1", "abc");

        //默认进入User1的文件夹
        File userFile = new File("dir" + "/", user.getName());
        //默认进入User1的默认数据库db1
        File dbFile = new File(userFile, "db1");

        Table.init(user.getName(), dbFile.getName());

        Scanner sc = new Scanner(System.in);
        String cmd;
        while (!"exit".equals(cmd=sc.nextLine())){


            Matcher matcherCreateTable = patternCreateTable.matcher(cmd);
            Matcher matcherAlterTable_add = patternAlterTable_add.matcher(cmd);
            Matcher matcherDropTable = patternDropTable.matcher(cmd);

            Matcher matcherInsert = patternInsert.matcher(cmd);
            Matcher matcherDelete = patternDelete.matcher(cmd);
            Matcher matcherUpdate = patternUpdate.matcher(cmd);
            Matcher matcherSelect = patternSelect.matcher(cmd);



            while (matcherSelect.find()){
                if ("*".equals(matcherSelect.group(1)) && null== matcherSelect.group(3)){
                    //暂定只有一张表，且没有选择条件
                    String tableName = matcherSelect.group(2);
                    Table table = Table.getTable(tableName);
                    if (null == table){
                        System.out.println("未找到表 "+ tableName);
                        break;
                    }
                    List<Map<String,String>> datas = table.read();
                    Map<String, Field> fieldMap = table.getFieldMap();

                    for (String fieldName : fieldMap.keySet()) {
                        System.out.printf("\t|\t%s|",fieldName);
                    }
                    System.out.println();

                    for (Map<String, String> data : datas) {
                        for (String fieldValue : data.values()) {
                            System.out.printf("\t|\t%s|",fieldValue);
                        }
                            System.out.println();
                    }


                }
            }


            while (matcherUpdate.find()){
                String tableName = matcherUpdate.group(1);
                String setStr = matcherUpdate.group(2);
                String whereStr = matcherUpdate.group(3);

                Table table = Table.getTable(tableName);
                if (null == table){
                    System.out.println("未找到表 "+ tableName);
                    break;
                }
                Map<String, Field> fieldMap = table.getFieldMap();
                Map<String,String> data = StringUtil.parseUpdateSet(setStr);

                List<SingleFilter> singleFilters = new ArrayList<>();
                if (null == whereStr){
                    table.update(data,singleFilters);
                }else {
                    List<Map<String, String>> filterList = StringUtil.parseWhere(whereStr);
                    for (Map<String, String> filterMap : filterList) {
                        SingleFilter singleFilter = new SingleFilter(fieldMap.get(filterMap.get("fieldName")),
                                filterMap.get("relationshipName"),filterMap.get("condition"));

                        singleFilters.add(singleFilter);
                    }
                    table.update(data,singleFilters);
                }

            }



            while (matcherCreateTable.find()){
                String tableName = matcherCreateTable.group(1);
                String propertys = matcherCreateTable.group(2);
                Map<String,Field> fieldMap = StringUtil.parseCreateTable(propertys);
                System.out.println("创建表");
                System.out.println(Table.creatTable(tableName,fieldMap));
            }

            while (matcherAlterTable_add.find()){
                String tableName = matcherAlterTable_add.group(1);
                String propertys = matcherAlterTable_add.group(2);
                Map<String,Field> fieldMap = StringUtil.parseCreateTable(propertys);
                Table table = Table.getTable(tableName);
                if(table == null){
                    System.out.println("未找到表 "+tableName);
                    break;
                }
                System.out.println(table.addDict(fieldMap));
            }

            while (matcherDropTable.find()){
                String tableName = matcherDropTable.group(1);
                System.out.println(Table.dropTable(tableName));
            }

            while (matcherInsert.find()){
                String tableName = matcherInsert.group(1);
                Table table = Table.getTable(tableName);
                if (null == table){
                    System.out.println("未找到表 "+ tableName);
                    break;
                }
                Map<String, Field> fieldMap = table.getFieldMap();
                Map<String, String> insertData = new HashMap<>();

                String[] fieldValues = matcherInsert.group(5).split(",");
                //如果插入指定的字段
                if(null != matcherInsert.group(2)){
                    String[] fieldNames = matcherInsert.group(3).split(",");
                    //
                    if (fieldValues.length != fieldNames.length){
                        return;
                    }
                    for (int i = 0; i < fieldNames.length;i++){
                        String fieldName = fieldNames[i];
                        String fieldValue = fieldValues[i];
                        //
                        if(!fieldMap.containsKey(fieldName)){
                            return;
                        }
                        insertData.put(fieldName,fieldValue);
                    }
                }else {//否则插入所有字段数据
                    Set<String> fieldNames = fieldMap.keySet();
                    int i = 0;
                    for (String fieldName : fieldNames) {
                        String fieldValue = fieldValues[i];
                        insertData.put(fieldName,fieldValue);
                        i++;
                    }
                }
                System.out.println(table.insert(insertData));
            }

            while (matcherDelete.find()){
                String tableName = matcherDelete.group(1);

                /**
                 *   group(2)
                 *   (\w+\s?[<=>]\s?[^\s\;]+(?:\sand\s(?:\w+)\s?(?:[<=>])\s?(?:[^\s\;]+))*)
                 *
                 *   (\w+)\s?([<=>])\s?([^\s\;]+)
                 */
                String whereStr = matcherDelete.group(2);
                System.out.println(whereStr);
                /**
                 * 没有group(3)可供捕获
                 */
                //System.out.println(matcherDelete.group(3));
                Table table = Table.getTable(tableName);
                if (null == table){
                    System.out.println("未找到表 "+ tableName);
                    break;
                }
                Map<String, Field> fieldMap = table.getFieldMap();

                List<SingleFilter> singleFilters = new ArrayList<>();
                if (null == whereStr) {
                    table.delete(singleFilters);
                }else {
                    List<Map<String, String>> filtList = StringUtil.parseWhere(whereStr);
                    for (Map<String, String> filtMap : filtList) {
                        SingleFilter singleFilter = new SingleFilter(fieldMap.get(filtMap.get("fieldName"))
                                , filtMap.get("relationshipName"), filtMap.get("condition"));

                        singleFilters.add(singleFilter);
                    }
                    table.delete(singleFilters);
                }
            }

        }


    }

}
