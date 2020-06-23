
import java.io.*;
import java.util.*;

public class Table {
    private String name;//表名
    private File folder;//表所在的文件夹
    private File dictFile;//数据字典文件
    private File dataFile;//数据文件
    private Map<String, Field> fieldMap;//字段映射集
    private static String userName;//用户姓名，切换或者修改用户时 才修改
    private static String dbName;//数据库名字，切换或者修改数据库时 才修改


    /**
     * 静态方法创建表，使用私有构造函数
     * 构造函数没有返回值
     *
     * @param name 表名
     */
    private Table(String name) {
        this.name = name;
        this.folder = new File("/idea-java-project/lehirtDBMS/dir"+"/"+userName+"/"+dbName+"/" + name);
        this.dictFile = new File(folder, name + ".dict");
        this.dataFile = new File(folder+"/data", 1 + ".data");
        this.fieldMap = new LinkedHashMap<String, Field>();
    }


    /**
     * 初始化表，定义需要初始化的用户和数据库
     *
     * @param userName 用户名
     * @param dbName   数据库
     */
    public static void init(String userName, String dbName) {

        /*
         * 不能使用类实例this引用静态变量
         *
         * */
        Table.userName = userName;
        Table.dbName = dbName;
    }

    /**
     * 创建一个新的表文件
     * @param name  表名
     * @param fieldMap  表属性
     * @return  如果表存在返回失败的信息，否则返回success
     */
    public static String creatTable(String name, Map<String, Field> fieldMap) {
        if (existTable(name)) {
            return "表 " + name + " 已经存在于数据库中";
        }

        Table table = new Table(name);

        //创建所指定的文件目录，即使没有文件也创建，有则不必创建
        table.dictFile.getParentFile().mkdirs();

        //将字段映射值附加入数据字典文件
        table.addDict(fieldMap);

        return "success";

    }

    /**
     * 根据表名获取表
     * @param name  表名
     * @return  如果不存在此表返回null, 否则返回对应Table对象
     */
    public static Table getTable(String name) {
        if (name == null || "".equals(name) || !existTable(name)) {
            return null;
        }
        Table table = new Table(name);
        try (
                FileReader fr = new FileReader(table.dictFile);
                BufferedReader bfr = new BufferedReader(fr);
        ) {
            String line = null;
            while ((line = bfr.readLine()) != null) {
                String[] fieldValues = line.split(" ");
                Field field = new Field();
                field.setName(fieldValues[0]);
                field.setType(fieldValues[1]);
                ;
                if (3 == fieldValues.length && "*".equals(fieldValues[2])) {
                    field.setPrimary(true);
                }
                //将字段的名字作为key
                table.fieldMap.put(fieldValues[0], field);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }




    public Map<String,Field> getFieldMap(){
        return fieldMap;
    }


    private static void deleteFolder(File file){
        if (file.isFile()){
            file.delete();
        }else if (file.isDirectory()) {

            File[] files = file.listFiles();

            for (int i = 0;i<files.length;i++){
                Table.deleteFolder(files[i]);//迭代删除文件
            }
            file.delete();//删除文件夹
        }

    }


    /**
     * 根据表名删除表
     * @param name  表名
     * @return  如果不存在此表返回null, 删除成功返回success
     */
    public static String dropTable(String name) {
        if (!existTable(name)) {
            return "错误：不存在表" + name;
        }
        File folder = new File("/idea-java-project/lehirtDBMS/dir"+"/"+userName+"/"+dbName+"/",name);

        Table.deleteFolder(folder);

        return "success";
    }

    /**
     * 判断表是否存在
     * @param name
     * @return
     */
    public static boolean existTable(String name) {
        File folder = new File("/idea-java-project/lehirtDBMS/dir" + "/" + userName + "/" + dbName + "/", name);
        return folder.exists();
    }

    /**
     * 在字典文件中写入创建的字段信息,然后将新增的字段map追加到this.fieldMap
     * @param fields  字段列表，其中map的name为列名，type为数据类型，primaryKey为是否作为主键
     * @return
     */
    public String addDict(Map<String, Field> fields) {

        Set<String> names = fields.keySet();
        for (String name : names) {
            if (fieldMap.containsKey(name)) {
                return "错误，存在重复添加的字段" + name;
            }
        }

        writerDict(fields,true);
        fieldMap.putAll(fields);

        return "success";
    }

    /**
     * 在数据文件没有此字段的数据的前提下，可以删除此字段
     * @param fieldName
     * @return
     */
    public String deleteDict(String fieldName){
        if (!fieldMap.containsKey(fieldName)){
            return "错误：不存在该字段"+fieldName;
        }

        writerDict(fieldMap,false);

        return "success";
    }

    /**
     * 提供字段写入文件的方法
     * @param fields  字段映射集
     * @param append 是否在文件结尾追加
     */
    private void writerDict(Map<String, Field> fields , boolean append) {
        try (
                FileWriter fw = new FileWriter(dictFile,append);
                PrintWriter pw = new PrintWriter(fw);
        ) {
            //for (String s : fieldMap.keySet())
            for (Field field : fields.values()) {
                String name = field.getName();
                String type = field.getType();
                //若字段是主键，则加 * 标志
                if (field.isPrimary()) {
                    pw.println(name + " " + type + " " + "*");
                } else {//非主键
                    pw.println(name + " " + type + " "+"^");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 对空位填充fillStr,填充后的字段按照数据字段顺序排序
     * @param data 原始数据
     * @param fillStr   要填充的字符串
     * @return  被指定字符串填充后的数据
     */
    public Map<String,String> fillData(Map<String,String> data , String fillStr){
        //data是初始数据集合；
        //fillData是最终要写入文件的数据集合
        Map<String , String> fillData = new LinkedHashMap<>();
        for (String key : data.keySet()) {

            if ((String)data.get(key) == null || "".equals((String)data.get(key))){
                data.put(key,fillStr);
            }
            fillData.put(key,(String) data.get(key));
        }

      /*  for (Map.Entry<String, Field> fieldEntry : fieldMap.entrySet()) {
            //  String fieldKey = fieldEntry.getKey();
            if (data.get(fieldEntry.getKey())==null || "".equals(data.get(fieldEntry.getKey()))){
                fillData.put(fieldEntry.getKey(),fillStr);
            }else {
                fillData.put(fieldEntry.getKey(),data.get(fieldEntry.getKey()));
            }
        }*/

        return fillData;
    }

    /**
     * 利用正则表达式判断数据data是否与数据字段相符
     * @param data
     * @return
     */
    private boolean checkType(Map<String,String> data){
            //Iterator<String> dataIterator = data.values().iterator();
            Iterator<Field> fieldIterator = fieldMap.values().iterator();
            //while (dataIterator.hasNext())
            while (fieldIterator.hasNext()){
                Field field = fieldIterator.next();
                String datavalue = data.get(field.getName());
                if ("[NULL]".equals(datavalue)){
                    continue;
                }

                switch (field.getType()){
                    case "int" :
                        if (!datavalue.matches("^(-|\\+)?\\d+$")){
                            return false;
                        }
                        break;
                    case "double" :
                        if (!datavalue.matches("^(-|\\+)?\\d*\\.?\\d+$")){
                            return false;
                        }
                        break;
                    case "varchar":
                        break;
                    default:
                        return false;
                }
            }
        return true;
        }

    /**
     * 在插入时，对语法进行检查，并对空位填充[NULL] ,默认插入为追加方式
     * @param srcData 未处理的原数据
     * @return
     */
    public String insert(Map<String,String> srcData){
            return insert(srcData,true);
        }


    /**
     *  在插入时，对语法进行检查，并对空位填充[NULL]
     * @param srcData 未处理的原数据
     * @param append 是否追加
     * @return
     */
    private  String insert(Map<String,String> srcData,boolean append){
            if (srcData.size()>fieldMap.size() || 0 ==srcData.size()){
                return "错误：插入数据错误，请检查数据";
            }

            //遍历数据，判断主键不能为空
            Set<Map.Entry<String, Field>> entries = fieldMap.entrySet();
            for (Map.Entry<String, Field> fieldEntry : entries) {
                String key = fieldEntry.getKey();
                Field field = fieldEntry.getValue();
                if (field.isPrimary()){
                    if (null == srcData.get(key) || "[NULL]".equals(srcData.get(key))){
                        return "错误：字段 "+key+" 是主键，不能为空";
                    }
                }
            }

            Map<String, String> insterData = fillData(srcData, "[NULL]");

            if (!checkType(insterData)){
                return "错误：请检查插入的类型";
            }

            dataFile.getParentFile().mkdirs();
            //在数据文件中写入数据
            try (
            FileWriter fw = new FileWriter(dataFile,append);
            BufferedWriter bw = new BufferedWriter(fw);
                    )

            {

                Set<String> fields = insterData.keySet();
                for (String field : fields) {
                    bw.write(insterData.get(field) + " ");
                    bw.flush();
                }
                bw.newLine();

            }catch (IOException e){
                e.printStackTrace();
                return "写入数据异常";
            }
            return "success";
        }

    /**
     * 读取指定文件的所有数据
     * @param dataFile 数据文件
     * @return  数据列表
     */
    public List<Map<String,String>> readDatas(File dataFile){
        List<Map<String,String>> dataMapList = new ArrayList<>();

        try (
                FileReader fr = new FileReader(dataFile);
                BufferedReader bfr = new BufferedReader(fr);
                ){

            String line = null;
            while (null != (line=bfr.readLine())){
                Map<String,String> dataMap = new LinkedHashMap<>();
                String[] datas = line.split(" ");

                Iterator<String> fieldsNames = getFieldMap().keySet().iterator();
                for (String data : datas) {
                    String dataName = fieldsNames.next();
                    dataMap.put(dataName,data);
                }
                dataMapList.add(dataMap);

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataMapList;
    }

    /**
     * 将给定数据列表写入给定数据文件
     * @param dataFile 数据文件
     * @param datas 数据列表
     */
    private void writerDatas(File dataFile,List<Map<String,String>> datas){
        if (dataFile.exists()){
            dataFile.delete();
        }
        for (Map<String, String> data : datas) {
            insert(data);
        }
    }

    private void deleteData(File dataFile , List<SingleFilter> singleFilters){
        //读取数据文件
        List<Map<String,String>> srcDatas = readDatas(dataFile);
        List<Map<String,String>> filterDatas = new ArrayList<>(srcDatas);
        for (SingleFilter singleFilter : singleFilters) {
            filterDatas = singleFilter.singleFilterData(filterDatas);
        }
        srcDatas.removeAll(filterDatas);
        writerDatas(dataFile,srcDatas);
        System.out.println("delete success");
    }
    /**
     * 根据给定的过滤器组，查找索引，将指定的文件数据删除
     * @param singleFilters 过滤器组
     */
    public void delete(List<SingleFilter> singleFilters) {
        //此处查找索引
        deleteData(this.dataFile, singleFilters);
    }



    private void updateData(File File, Map<String, String> updateDatas, List<SingleFilter> singleFilters) {
        //读取数据
        List<Map<String, String>> srcDatas = readDatas(File);

        /**
         *  将srcDatas集合的元素放置在filterDatas集合中。
         *  虽然filterDatas和srcDatas是两个不同的集合，但是他们此刻拥有对同一位置元素对象的引用
         */
        List<Map<String,String>> filterDatas = new ArrayList<>(srcDatas);

        //循环过滤
        for (SingleFilter singleFilter : singleFilters) {
            filterDatas = singleFilter.singleFilterData(srcDatas);
        }

        //遍历过滤后的数据，将数据的值更新为updateDatas对应的值
        for (Map<String, String> filterData : filterDatas) {
            for (Map.Entry<String, String> setData : updateDatas.entrySet()) {
                //put(): 旧值被setData替换
                filterData.put(setData.getKey(),setData.getValue());
            }
        }
        System.out.println("srcDatas"+srcDatas);
        System.out.println("filterDatas"+filterDatas);
        writerDatas(File,srcDatas);
    }

    /**
     *
     * @param updateDatas
     * @param singleFilters
     */
    public void update(Map<String, String> updateDatas, List<SingleFilter> singleFilters) {
        //此处查找索引
        updateData(this.dataFile,updateDatas,singleFilters);
    }


    public List<Map<String, String>> read() {
        return readDatas(this.dataFile);
    }
}
