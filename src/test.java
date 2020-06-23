
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class test {
    public static void main(String[] args) {

        User user = new User("user1", "abc");

        //默认进入User1的文件夹
        File userFile = new File("/idea-java-project/lehirtDBMS/dir" + "/", user.getName());
        //默认进入User1的默认数据库db1
        File dbFile = new File(userFile, "db1");

        Table.init(user.getName(), dbFile.getName());

/*
        String[][] lines = {
                {"id", "int", "*"},
                {"name", "varchar"},
                {"height", "double"},
                {"sex", "varchar"}
        };

        Map<String, Field> fieldMap = new LinkedHashMap<>();

        for (String[] line : lines) {
            Field field = new Field();
            field.setName(line[0]);
            field.setType(line[1]);

            if (3 == line.length && "*".equals(line[2])) {
                field.setPrimary(true);
            }
            fieldMap.put(line[0], field);
        }

        String result = null;

  */
/*
      //测试创建，获得，添加字典  方法
      result = Table.creatTable("table1", fieldMap);
        System.out.println(result);

        Table table1 = Table.getTable("table1");
        result = table1.addDict(fieldMap);
        System.out.println(result);*//*



        //测试删除表方法
        //      result=Table.dropTable("table1");

        result = Table.creatTable("table1", fieldMap);
        System.out.println("创建表---" + result);

        Table table1 = Table.getTable("table1");

        String[][] srcDataLines = {
                {"1", "张三", "1.7", "man"},
                {"2", "李四", "women"},
                {"3", "王二", "man"},
                {"4", "大黑", "1"},
                {}

        };

        Map<String, Field> dictMap = table1.getFieldMap();

        List<Map<String, String>> insertLines = new ArrayList<>();
        for (String[] srcDataLine : srcDataLines) {

            Iterator<String> fieldNameIterator = dictMap.keySet().iterator();

            Map<String,String> insertData = new LinkedHashMap<>();
            for (String fieldValues : srcDataLine) {
                String name = fieldNameIterator.next();
                insertData.put(name,fieldValues);
            }

            insertLines.add(insertData);
        }
        int i = 1;
        for (Map<String, String> insertLine : insertLines) {

            result=table1.insert(insertLine);
            System.out.println(result + " "+i);
            i=i+1;
        }
*/

        Scanner sc = new Scanner(System.in);
        String cmd;
        while (!"exit".equals(cmd=sc.nextLine())){

            /**
             * 对表的操作
             */
            Pattern patternCreateTable = Pattern.compile("create\\stable\\s(\\w+)\\s?\\(((?:\\s?\\w+\\s\\w+,?)+)\\)\\s?;");
            Matcher matcherCreateTable = patternCreateTable.matcher(cmd);

            Pattern patternAlterTable_add = Pattern.compile("alter\\stable\\s(\\w+)\\sadd\\s(\\w+\\s\\w+)\\s?;");
            Matcher matcherAlterTable_add = patternAlterTable_add.matcher(cmd);

            Pattern patternDropTable = Pattern.compile("drop\\stable\\s(\\w+)\\s?;");
            Matcher matcherDropTable = patternDropTable.matcher(cmd);

            /**
             * 增删查改
             */
            Pattern patternInsert = Pattern.compile("insert\\s+into\\s+(\\w+)\\s(\\(((\\w+,?)+)\\))?\\s+\\w+\\s+\\((([^\\)]+,?)+)\\);?");
            Matcher matcherInsert = patternInsert.matcher(cmd);

            //Pattern patternDelete=Pattern.compile("delete\\sfrom\\s(\\w+)(?:\\swhere\\s(\\w+)\\s?([<=>])\\s?([^\\s\\;]+))?((?:\\s(?:and|or)\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*)?;?");
            Pattern patternDelete = Pattern.compile("delete\\sfrom\\s(\\w+)(?:\\swhere\\s(\\w+\\s?[<=>]\\s?[^\\s\\;]+(?:\\sand\\s(?:\\w+)\\s?(?:[<=>])\\s?(?:[^\\s\\;]+))*))?\\s?;");
            Matcher matcherDelete = patternDelete.matcher(cmd);

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
                System.out.println(table.addDict(fieldMap));
            }

            while (matcherDropTable.find()){
                String tableName = matcherDropTable.group(1);
                System.out.println(Table.dropTable(tableName));
            }

            while (matcherInsert.find()){
                String tableName = matcherInsert.group(1);
                Table table = Table.getTable(tableName);
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
