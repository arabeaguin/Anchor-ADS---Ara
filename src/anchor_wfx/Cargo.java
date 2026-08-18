package anchor_wfx;

public class Cargo {
    private int id;
    private String description;
    private double weight;
    private double volume;
    private boolean hazardous;
    private String imdgClass;
    private String unNumber;
    private String properShippingName;
    
    public Cargo(int id, String description, double weight, double volume,
                 boolean hazardous, String imdgClass,
                 String unNumber, String properShippingName) {
        this.id                 = id;
        this.description        = description;
        this.weight             = weight;
        this.volume             = volume;
        this.hazardous          = hazardous;
        this.imdgClass          = imdgClass;
        this.unNumber           = unNumber;
        this.properShippingName = properShippingName;
    }

    public Cargo(String description, double weight, double volume,
                 boolean hazardous, String imdgClass,
                 String unNumber, String properShippingName) {
        this.id                 = 0;
        this.description        = description;
        this.weight             = weight;
        this.volume             = volume;
        this.hazardous          = hazardous;
        this.imdgClass          = imdgClass;
        this.unNumber           = unNumber;
        this.properShippingName = properShippingName;
    }

    // Getters
    public int     getId()                 { return id; }
    public String  getDescription()        { return description; }
    public double  getWeight()             { return weight; }
    public double  getVolume()             { return volume; }
    public boolean isHazardous()           { return hazardous; }
    public String  getImdgClass()          { return imdgClass == null ? "—" : imdgClass; }
    public String  getUnNumber()           { return unNumber == null ? "—" : unNumber; }
    public String  getProperShippingName() { return properShippingName == null ? "—" : properShippingName; }

    // Setters
    public void setDescription(String v)        { description = v; }
    public void setWeight(double v)             { weight = v; }
    public void setVolume(double v)             { volume = v; }
    public void setHazardous(boolean v)         { hazardous = v; }
    public void setImdgClass(String v)          { imdgClass = v; }
    public void setUnNumber(String v)           { unNumber = v; }
    public void setProperShippingName(String v) { properShippingName = v; }

    // For table display
    public String getHazardousDisplay() { return hazardous ? "Yes" : "No"; }
}