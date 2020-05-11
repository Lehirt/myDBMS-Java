import com.sun.net.httpserver.Authenticator;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Table {

    //表名
    private String name;
    //数据字典文件
    private File dictFile;
    //数据文件
    private File dataFile;
    //字段映射集
    private Map<String , Field > fieldMap;
    //用户姓名，切换或者修改用户时 才修改
    private static String userName;
    //数据库名字，切换或者修改数据库时 才修改
    private static String dbName;
    private Table table;


    /**
     * 静态方法创建表，使用私有构造函数
     * 构造函数没有返回值
     *
     * @param name 表名
     */
    public Table(String name){
        this.name = name;
        this.dictFile = new File("/idea-java-project/lehirtDBMS/dir"+"/"+userName+"/"+dbName+"/",name+".dict");
        this.dataFile = new File("/idea-java-project/lehirtDBMS/dir"+"/"+userName+"/"+dbName+"/",name+".data");
        this.fieldMap = new HashMap<String, Field>();
    }




    /**
     *  初始化表，定义需要初始化的用户和数据库
     * @param userName 用户名
     * @param dbName  数据库
     */
    public static void init(String userName ,String dbName){

        /*
        * 不能使用类实例this引用静态变量
        *
        * */
        Table.userName = userName;
        Table.dbName = dbName;
    }




    public static String creatTable(String name , Map<String,Field> fieldMap){
        if(exist(name)){
            return "表 "+name+" 已经存在于数据库中";
        }

       Table table =new Table(name);

        //创建所指定的文件目录，即使没有文件也创建，有则不必创建
       table.dictFile.getParentFile().mkdirs();

       //将字段映射值附加入数据字典文件
        table.addDict(fieldMap);

        return  "success";

    }



    private String addDict(Map<String,Field> fields) {
        try(
            FileWriter fw = new FileWriter(dictFile);
            PrintWriter pw = new PrintWriter(fw,true);
        ) {
            //for (String s : fieldMap.keySet())
            for (Field field : fields.values()) {
                String name = field.getName();
                String type = field.getType();
                //若字段是主键，则加 * 标志
                if (field.isPrimary()){
                    pw.println(name +" "+ type +" "+ "*" );
                }else  {
                    pw.println(name+" "+type+" ");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
            fieldMap.putAll(fields);
            return "success";
    }



    public static Table getTable(String name){

        if (name==null || "".equals(name) || !exist(name)){
            return null;
        }

        Table table = new Table(name);

        try (
             FileReader fr = new FileReader(table.dictFile);
             BufferedReader bfr=new BufferedReader(fr);
         ){
            String line ;
            while ( (line = bfr.readLine())!=null){
                String[] fieldValues = line.split(" ");
                Field field = new Field();
                field.setName(fieldValues[0]);
                field.setType(fieldValues[1]); ;
                if (3==fieldValues.length && "*".equals(fieldValues[2])) {
                     field.setPrimary(true);
                }
                //将字段的名字作为key
                table.fieldMap.put(fieldValues[0],field);

            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }


        return table;
    }

    public static boolean exist(String name){
        File file = new File("/idea-java-project/lehirtDBMS/dir"+"/"+userName+"/"+dbName+"/",name);
        return file.exists();
    }



}
