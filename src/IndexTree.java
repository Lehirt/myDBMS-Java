import java.io.File;
import java.io.Serializable;
import java.util.*;

public class IndexTree implements Serializable {
    private TreeMap<IndexKey,IndexNode> treeMap;//底层由红黑树实现

    public IndexTree() {
        this.treeMap = new TreeMap<>();
    }

    public TreeMap<IndexKey, IndexNode> getTreeMap() {
        return treeMap;
    }

    public void setTreeMap(TreeMap<IndexKey, IndexNode> treeMap) {
        this.treeMap = treeMap;
    }

    public List<IndexNode> find(Relationship relationship , IndexKey condition){

        List<IndexNode> indexNodeList = new ArrayList<>();
        Map<IndexKey,IndexNode> indexNodeMap = null;

        switch (relationship){
            case LESS_THAN:
                //此方法获得小于key的映射
                indexNodeMap = treeMap.headMap(condition);
                for (IndexNode node : indexNodeMap.values()) {
                    indexNodeList.add(node);
                }
                break;
            case EQUAL_TO:
                //此方法此方法获得等于key的映射
                IndexNode indexNode = treeMap.get(condition);
                indexNodeList.add(indexNode);
                break;
            case MORE_THAN:
                ////此方法此方法获得大于key的映射
                indexNodeMap = treeMap.tailMap(condition);
                if (indexNodeMap.containsKey(condition)){
                    indexNodeMap.remove(condition);
                }
                for (IndexNode node : indexNodeMap.values()) {
                    indexNodeList.add(node);
                }
                break;
            default:
                try {
                    throw new Exception("条件限定不匹配");
                }catch (Exception e){
                    e.printStackTrace();
                }
        }
        return indexNodeList;
    }

    public Set<File> getFiles(Relationship relationship,IndexKey condition){
        Set<File> fileSet = new HashSet<>();
        List<IndexNode> indexNodeList = this.find(relationship,condition);
        for (IndexNode node : indexNodeList) {
            fileSet.addAll(node.getFiles());
        }
        return fileSet;
    }

    public void put(IndexKey indexKey,IndexNode indexNode){
        treeMap.put(indexKey,indexNode);
    }

    public void putIndex(IndexKey indexKey,String filePath,int lineNum){
        IndexNode indexNode = treeMap.get(indexKey);
        //如果没有该结点，添加该结点
        if (null == indexNode){
            treeMap.put(indexKey,new IndexNode());
            //获得新创建的结点
            indexNode = treeMap.get(indexKey);
        }
        Index index = new Index(filePath, lineNum);
        indexNode.addIndex(index);

    }

}
