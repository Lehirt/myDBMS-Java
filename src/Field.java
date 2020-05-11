public class Field {
   private   String name;
   private   String type;
   private   boolean primary;


    public boolean isPrimary(){
        return primary ;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean getPrimary(){
        return primary;
    }

}
