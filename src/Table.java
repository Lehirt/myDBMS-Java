import java.io.*;
import java.util.*;

public class Table {
    private String name;//表名
    private File folder;//表所在的文件夹
    private File dictFile;//数据字典文件
    private LinkedHashSet<File> dataFileSet;
    private File indexFile;//索引文件 (.index结尾的文件)
    private Map<String, Field> fieldMap;//字段映射集
    private Map<String,IndexTree> indexMap;//存放对所有字段的索引树,每个字段分别对应一个索引树
    private static String userName;//用户姓名，切换或者修改用户时 才修改
    private static String dbName;//数据库名字，切换或者修改数据库时 才修改

    private static long lineNumConfine = 10;

    /**
     * 静态方法创建表，使用私有构造函数
     * 构造函数没有返回值
     *
     * @param name 表名
     */
    private Table(String name) {
        this.name = name;
        this.folder = new File("dir"+"/"+userName+"/"+dbName+"/" + name);
        this.dictFile = new File(folder, name + ".dict");
        //this.dataFile = new File(folder+"/data", 1 + ".data");
        this.dataFileSet = new LinkedHashSet<>();
        this.fieldMap = new LinkedHashMap<String, Field>();
        this.indexFile = new File(folder,this.name+".index");
        this.indexMap = new HashMap<>();
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

        File[] dataFiles = new File(table.folder,"data").listFiles();
        if (null != dataFiles && 0!= dataFiles.length){
            for (int i = 1; i <= dataFiles.length; i++) {
                File dataFile = new File(table.folder + "/data",i+".data");
                table.dataFileSet.add(dataFile);
            }
        }
        if (table.indexFile.exists()){
            table.readIndex();
        }else {
            table.buildIndex();
            //table.writeIndex();
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
        File folder = new File("dir"+"/"+userName+"/"+dbName+"/",name);

        Table.deleteFolder(folder);

        return "success";
    }

    /**
     * 判断表是否存在
     * @param name
     * @return
     */
    public static boolean existTable(String name) {
        File folder = new File("dir" + "/" + userName + "/" + dbName + "/", name);
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
     * 插入数据到最后一个数据文件，如果超过数据行数限制，写入下一个文件中
     * @param srcData 未处理的原数据
     * @return
     */
    public String insert(Map<String,String> srcData){
        File lastFile = null;
        int lineNum = 0;
        int fileNum = 0;

        for (File file : dataFileSet) {
            fileNum++;
            lastFile = file;
            lineNum = fileLineNum(lastFile);
        }
        //如果没有一个文件，新建一个1.data
        if (null == lastFile && 0 == fileNum) {
            lastFile = new File(folder + "/data", 1 + ".data");
            dataFileSet.add(lastFile);
            lineNum = 0;
        } else if (lineNumConfine <= fileLineNum(lastFile)) {
            //如果最后一个文件数据行超过限制，新建数据文件
            lastFile = new File(folder + "/data", fileNum + 1 + ".data");
            dataFileSet.add(lastFile);
            lineNum = 0;
        }
        //添加索引
        for (Map.Entry<String, Field> fieldEntry : fieldMap.entrySet()) {
            String dataName = fieldEntry.getKey();
            String dataValue = srcData.get(dataName);
            //如果该字段值为空，不添加入索引树
            if (null == dataValue || "[NULL]".equals(dataValue)) {
                continue;
            }
            String dataType = fieldEntry.getValue().getType();

            IndexTree indexTree = indexMap.get(dataName);
            if (null == indexTree) {
                indexMap.put(dataName, new IndexTree());
                indexTree = indexMap.get(dataName);
            }
            IndexKey indexKey = new IndexKey(dataValue, dataType);
            indexTree.putIndex(indexKey, lastFile.getAbsolutePath(), lineNum);
        }
        writeIndex();
        return insertData(lastFile, srcData);
        }


    /**
     *  在插入时，对语法进行检查，并对空位填充[NULL]
     * @param srcData 未处理的原数据
     * @return
     */
    private  String insertData(File file , Map<String,String> srcData){
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

            file.getParentFile().mkdirs();
            //在数据文件中写入数据
            try (
            FileWriter fw = new FileWriter(file,true);
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

            buildIndex();
            writeIndex();

            return "success";
        }


    private int fileLineNum(File file) {
        int num = 0;
        try (
                FileReader fr = new FileReader(file);
                BufferedReader bf = new BufferedReader(fr);
                ){

            while (null != (bf.readLine())){
                num++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return num;
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
     * 读取指定文件的所有数据和行
     *
     * @param dataFile 数据文件
     * @return 数据列表
     */
    public List<Map<String,String>> readDatasAndLineNum(File dataFile){
        List<Map<String,String>> dataMapList = new ArrayList<>();

        try(
                FileReader fr = new FileReader(dataFile);
                BufferedReader br =new BufferedReader(fr);
        )

        {
            String line = null;
            long lineNum = 1;
            while (null != (line = br.readLine())){
                Map<String,String> dataMap = new LinkedHashMap<>();
                String[] datas = line.split(" ");
                Iterator<String> fieldNames = getFieldMap().keySet().iterator();
                for (String data : datas) {
                    String dataName = fieldNames.next();
                    dataMap.put(dataName,data);
                }
                dataMap.put("[lineNum]",String.valueOf(lineNum));
                dataMapList.add(dataMap);
                lineNum++;
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
            insertData(dataFile,data);
        }
    }

    private void deleteData(File file, List<SingleFilter> singleFilters){
        //读取数据文件
        List<Map<String,String>> srcDatas = readDatas(file);
        List<Map<String,String>> filterDatas = new ArrayList<>(srcDatas);
        for (SingleFilter singleFilter : singleFilters) {
            filterDatas = singleFilter.singleFilterData(filterDatas);
        }
        srcDatas.removeAll(filterDatas);
        writerDatas(file,srcDatas);
        System.out.println("delete success");
    }
    /**
     * 根据给定的过滤器组，查找索引，将指定的文件数据删除
     * @param singleFilters 过滤器组
     */
    public void delete(List<SingleFilter> singleFilters) {
        //deleteData(this.dataFile, singleFilters);
        //此处查找索引
        Set<File> fileSet = findFileSet(singleFilters);
        for (File file : fileSet) {
            deleteData(file,singleFilters);
        }

        buildIndex();
        writeIndex();
    }



    private void updateData(File file, Map<String, String> updateDatas, List<SingleFilter> singleFilters) {
        //读取数据
        List<Map<String, String>> srcDatas = readDatas(file);

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
        writerDatas(file,srcDatas);
    }

    /**
     *
     * @param updateDatas
     * @param singleFilters
     */
    public void update(Map<String, String> updateDatas, List<SingleFilter> singleFilters) {
        //此处查找索引
        Set<File> fileSet = findFileSet(singleFilters);
        for (File file : fileSet) {
            updateData(file,updateDatas,singleFilters);
        }
        buildIndex();
        writeIndex();
    }


    public List<Map<String, String>> read() {

        //索引文件***
        List<Map<String, String>> datas = new ArrayList<>();
        for (File file : dataFileSet) {
            datas.addAll(readDatas(file));
        }
        return datas;
    }


    /**
     * 将索引对象从索引文件读取
     */

    private void readIndex(){
        if (!indexFile.exists()) {
            return;
        }
        try (
                FileInputStream fis = new FileInputStream(indexFile);
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            indexMap = (Map<String, IndexTree>) ois.readObject();
            System.out.println(indexMap);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将索引对象写入索引文件
     */
    private void writeIndex(){
        try (
                FileOutputStream fos = new FileOutputStream(indexFile);

                /**
                 * oos-->对象操作流 <-->该流可以将一个对象写出，或者读取一个对象到程序中，也就是执行了序列化和反序列化操作。
                 * 前提：需要被序列化和反序列化的类必须实现Serializable接口。
                 */
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(indexMap);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 为每个属性建立索引树，如果此属性值为[NULL]索引树将排除此条字段
     */
    public void buildIndex(){
        indexMap = new HashMap<>();
        File[] dataFiles = new File(folder,"data").listFiles();
        //对于每个文件
        for (File dataFile : dataFiles) {
            List<Map<String, String>> datas = readDatasAndLineNum(dataFile);
            //对于每个元组
            for (Map<String, String> data : datas) {
                //对于每个数据字段
                for (Map.Entry<String, Field> fieldEntry : fieldMap.entrySet()) {
                    String dataName = fieldEntry.getKey();
                    String dataValue = data.get(dataName);
                    //如果发现此数据为空，不添加到索引树中
                    if ("[NULL]".equals(dataValue)){
                        continue;
                    }
                    String dataType = fieldEntry.getValue().getType();
                    int lineNum = Integer.valueOf(data.get("[lineNum]"));
                    IndexTree indexTree = indexMap.get(dataName);
                    if (null == indexTree){
                        indexMap.put(dataName,new IndexTree());
                        indexTree = indexMap.get(dataName);
                    }

                    //索引树（红黑树）的indexKey（K）存储（字段值，字段类型）
                    IndexKey indexKey = new IndexKey(dataValue,dataType);
                    indexTree.putIndex(indexKey,dataFile.getAbsolutePath(),lineNum);
                }
            }
        }

        //重新填充dataFileSet
        if(null != dataFiles && 0!= dataFiles.length){
            for (int i = 0; i < dataFiles.length; i++) {
                File dataFile = new File(folder+"/data",i+".data");
                dataFileSet.add(dataFile);
            }
        }

    }

    /**
     * 删除表的索引
     * @return
     */
    public String deleteIndex(){
        if (indexFile.exists()){
            indexFile.delete();
            return "success";
        }
        return "删除索引失败";
    }



    private Set<File> findFileSet(List<SingleFilter> singleFilters){
        Set<File> fileSet = new HashSet<>();
        //此处查找索引
        for (SingleFilter singleFilter : singleFilters) {
            String fieldName = singleFilter.getField().getName();
            String fieldType = singleFilter.getField().getType();
            Relationship relationship = singleFilter.getRelationship();
            String condition = singleFilter.getCondition();

            IndexKey indexKey = new IndexKey(condition,fieldType);
            IndexTree indexTree = indexMap.get(fieldName);
            fileSet.addAll(indexTree.getFiles(relationship,indexKey));
        }
        return fileSet;
    }


    public static void main(String[] args) {
        User user = new User("user1", "abc");
        //默认进入user1用户文件夹
        File userFolder = new File("dir", user.getName());

        //默认进入user1的默认数据库db1
        File dbFolder = new File(userFolder, "db1");
        Table.init(user.getName(), dbFolder.getName());
        Table table1 = Table.getTable("test1");

        table1.readIndex();

        table1.buildIndex();
        table1.writeIndex();
    }

}
