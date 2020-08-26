import java.io.Serializable;

/**
 * 该索引的基本表现类型就是   文件路径+行数
 */
public class Index implements Serializable {
    private String filePath;
    private int lineNum;

    public Index(String filePath, int lineNum) {
        this.filePath = filePath;
        this.lineNum = lineNum;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLineNum() {
        return lineNum;
    }
}
