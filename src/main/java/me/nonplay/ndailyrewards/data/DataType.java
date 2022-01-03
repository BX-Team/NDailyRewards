package me.nonplay.ndailyrewards.data;

public enum DataType
{
    MYSQL("MYSQL", 0, "MySQL"), 
    SQLITE("SQLITE", 1, "SQLite");
    
    private String s;
    
    private DataType(final String name, final int ordinal, final String s) {
        this.s = s;
    }
    
    public String getName() {
        return this.s;
    }
}
