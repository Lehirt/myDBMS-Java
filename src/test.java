
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class test {
    public static void main(String[] args) {

        User user= new User("user1","abc");

        //默认进入User1的文件夹
        File userFile =new File("/idea-java-project/lehirtDBMS/dir"+"/",user.getName());
        //默认进入User1的默认数据库db1
        File dbFile = new File(userFile,"db1");

        Table.init(user.getName(),dbFile.getName());

        String[][] lines = {
                {"id","int","*"},
                {"a","varchar"},
                {"b","varchar"},
        };

        Map<String,Field> fieldMap = new HashMap<>();

        for (String[] line : lines ) {
            Field field = new Field();
            field.setName(line[0]);
            field.setType(line[1]);

            if (3==line.length && "*".equals(line[2])){
            field.setPrimary(true);
            }
            fieldMap.put(line[0],field);
        }

        String result = Table.creatTable("table1",fieldMap);
        System.out.println(result);

    }


}
