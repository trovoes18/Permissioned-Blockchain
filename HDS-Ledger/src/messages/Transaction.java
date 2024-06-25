package messages;


public abstract class Transaction {

    private final String type;

    public Transaction(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
