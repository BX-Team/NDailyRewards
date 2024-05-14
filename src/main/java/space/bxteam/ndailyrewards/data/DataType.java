package space.bxteam.ndailyrewards.data;

public enum DataType
{
    MYSQL("MYSQL", 0, "MySQL"), 
    SQLITE("SQLITE", 1, "SQLite");
    
    private final String s;
    
    DataType(final String name, final int ordinal, final String s) {
        this.s = s;
    }
    
    public String getName() {
        return this.s;
    }
}
