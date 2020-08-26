import java.io.File;
import java.io.Serializable;
import java.util.*;

public class IndexNode implements Serializable {
    private List<Index> indexList;//被索引树选择出来的索引链表

    public IndexNode() {
        this.indexList = new ArrayList<>();
    }

    /**
     * 索引以链表存储在indexNode中，indexKey相等即在同一个indexNode中，再次遍历indexList获得查询的值。
     * @param index
     */
    public void addIndex(Index index){
        indexList.add(index);
    }

    public Iterator<Index> indexIterator(){
        return indexList.iterator();
    }

    public Set<File> getFiles(){
        Set<File> fileSet =new HashSet<>();
        Iterator<Index> indexIterator = indexIterator();
        for (Index index : indexList) {
            File file = new File(index.getFilePath());
            fileSet.add(file);
        }
        return fileSet;
    }

}
