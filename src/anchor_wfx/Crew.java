package anchor_wfx;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Crew member model for ANCHOR ADS — Manage Vessels & Fleet module.
 */
public class Crew {

    private static int idCounter = 1;

    private final IntegerProperty           id;
    private final StringProperty            name;
    private final StringProperty            role;
    private final StringProperty            contactInfo;
    private final StringProperty            licenseNumber;
    private final ObjectProperty<LocalDate> licenseExpiry;
    /** -1 means unassigned to any vessel */
    //private final IntegerProperty           assignedVesselId;

    // ── New crew constructor (auto-increment ID) ───────────────────────────
    public Crew(String name, String role, String contactInfo,
                String licenseNumber, LocalDate licenseExpiry) {
        this.id               = new SimpleIntegerProperty(idCounter++);
        this.name             = new SimpleStringProperty(name);
        this.role             = new SimpleStringProperty(role);
        this.contactInfo      = new SimpleStringProperty(contactInfo);
        this.licenseNumber    = new SimpleStringProperty(licenseNumber);
        this.licenseExpiry    = new SimpleObjectProperty<>(licenseExpiry);
        //this.assignedVesselId = new SimpleIntegerProperty(-1);
    }

    // ── Edit constructor (preserve existing ID) ────────────────────────────
    public Crew(int id, String name, String role, String contactInfo,
                String licenseNumber, LocalDate licenseExpiry) {
        this.id               = new SimpleIntegerProperty(id);
        this.name             = new SimpleStringProperty(name);
        this.role             = new SimpleStringProperty(role);
        this.contactInfo      = new SimpleStringProperty(contactInfo);
        this.licenseNumber    = new SimpleStringProperty(licenseNumber);
        this.licenseExpiry    = new SimpleObjectProperty<>(licenseExpiry);
        //this.assignedVesselId = new SimpleIntegerProperty(-1);
    }

    public static void resetCounter(int next) { idCounter = next; }

    // ── Getters ────────────────────────────────────────────────────────────
    public int       getId()               { return id.get(); }
    public String    getName()             { return name.get(); }
    public String    getRole()             { return role.get(); }
    public String    getContactInfo()      { return contactInfo.get(); }
    public String    getLicenseNumber()    { return licenseNumber.get(); }
    public LocalDate getLicenseExpiry()    { return licenseExpiry.get(); }
    //public int       getAssignedVesselId() { return assignedVesselId.get(); }
    //public boolean   isAssigned()          { return assignedVesselId.get() != -1; }

    public String getLicenseExpiryDisplay() {
        return licenseExpiry.get() != null ? licenseExpiry.get().toString() : "—";
    }

    /*public String getAssignmentDisplay() {
        return assignedVesselId.get() == -1
            ? "Unassigned"
            : "Vessel #" + assignedVesselId.get();
    }*/

    // ── Setters ────────────────────────────────────────────────────────────
    public void setName(String v)             { name.set(v); }
    public void setRole(String v)             { role.set(v); }
    public void setContactInfo(String v)      { contactInfo.set(v); }
    public void setLicenseNumber(String v)    { licenseNumber.set(v); }
    public void setLicenseExpiry(LocalDate v) { licenseExpiry.set(v); }
    //public void setAssignedVesselId(int v)    { assignedVesselId.set(v); }

    // ── Properties ─────────────────────────────────────────────────────────
    public IntegerProperty           idProperty()               { return id; }
    public StringProperty            nameProperty()             { return name; }
    public StringProperty            roleProperty()             { return role; }
    public StringProperty            contactInfoProperty()      { return contactInfo; }
    public StringProperty            licenseNumberProperty()    { return licenseNumber; }
    public ObjectProperty<LocalDate> licenseExpiryProperty()   { return licenseExpiry; }
    //public IntegerProperty           assignedVesselIdProperty() { return assignedVesselId; }
}