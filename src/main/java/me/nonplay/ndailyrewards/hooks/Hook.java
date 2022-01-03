package me.nonplay.ndailyrewards.hooks;

public enum Hook
{
    CITIZENS("CITIZENS", 0, "Citizens");
    
    private String name;
    private boolean e;
    
    private Hook(final String name2, final int ordinal, final String name) {
        this.e = false;
        this.name = name;
    }
    
    public boolean isEnabled() {
        return this.e;
    }
    
    public void enable() {
        this.e = true;
    }
    
    public void disable() {
        this.e = false;
    }
    
    public String getPluginName() {
        return this.name;
    }
}
