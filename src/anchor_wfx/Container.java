package anchor_wfx;

import javafx.beans.property.*;

public class Container {

    private static int idCounter = 1;

    private final StringProperty rawType;

    public String getRawType() {
        return rawType.get();
    }

    public StringProperty rawTypeProperty() {
        return rawType;
    }

    // ── Type Enum ──────────────────────────────────────────────────────────
    public enum Type {
        DRY_20FT("Standard 20ft (Dry)", 28000, 33.2),
        DRY_40FT("Standard 40ft (Dry)", 28500, 67.7),
        HIGH_CUBE_40FT("High Cube 40ft", 28500, 76.4),
        HIGH_CUBE_45FT("High Cube 45ft", 28500, 86.1),
        REEFER_20FT("Reefer 20ft", 27400, 28.1),
        REEFER_40FT("Reefer 40ft", 27700, 67.7),
        OPEN_TOP_20FT("Open Top 20ft", 28130, 32.7),
        OPEN_TOP_40FT("Open Top 40ft", 28000, 66.4),
        FLAT_RACK_20FT("Flat Rack 20ft", 40000, 29.2),
        FLAT_RACK_40FT("Flat Rack 40ft", 40000, 60.0),
        TANK_20FT("Tank 20ft", 26000, 24.9),
        PLATFORM_20FT("Platform 20ft", 34000, 0.0),
        PLATFORM_40FT("Platform 40ft", 40000, 0.0),
        VENTILATED_20FT("Ventilated 20ft", 28000, 32.6),
        HALF_HEIGHT_20FT("Half Height 20ft", 28000, 17.0);

        private final String display;
        private final double maxWeightKg;
        private final double maxVolumeCbm;

        Type(String display, double maxWeightKg, double maxVolumeCbm) {
            this.display = display;
            this.maxWeightKg = maxWeightKg;
            this.maxVolumeCbm = maxVolumeCbm;
        }

        public String getDisplay() {
            return display;
        }

        public double getMaxWeightKg() {
            return maxWeightKg;
        }

        public double getMaxVolumeCbm() {
            return maxVolumeCbm;
        }

        public static Type fromDisplay(String d) {
            for (Type t : values()) {
                if (t.display.equalsIgnoreCase(d.trim())) {
                    return t;
                }
            }
            return DRY_20FT;
        }
    }

    // ── Status Enum ────────────────────────────────────────────────────────
    public enum Status {
        AVAILABLE("Available", "available"),
        IN_USE("In Use", "in_use"),
        MAINTENANCE("Under Maintenance", "under_maintenance");

        private final String display;
        private final String dbValue;  

        Status(String display, String dbValue) {
            this.display = display;
            this.dbValue = dbValue;
        }

        public String getDisplay() {
            return display;
        }

        public String getDbValue() {
            return dbValue;
        } // ← use this when saving to DB

        public static Status fromDisplay(String d) {
            for (Status s : values()) {
                if (s.display.equals(d)) {
                    return s;
                }
            }
            return AVAILABLE;
        }

        public static Status fromName(String dbValue) {
            if (dbValue == null) {
                return AVAILABLE;
            }
            for (Status s : values()) {
                if (s.dbValue.equalsIgnoreCase(dbValue)) {
                    return s; // ← match against dbValue
                }
            }
            return AVAILABLE;
        }
    }

    private final IntegerProperty id;
    private final StringProperty containerNumber;
    private final ObjectProperty<Type> type;
    private final DoubleProperty maxWeightKg;
    private final DoubleProperty maxVolumeCbm;
    private final ObjectProperty<Status> status;

    // ── New container constructor ──────────────────────────────────────────
    public Container(String containerNumber, Type type) {
        this.id = new SimpleIntegerProperty(idCounter++);
        this.containerNumber = new SimpleStringProperty(containerNumber);
        this.type = new SimpleObjectProperty<>(type);
        this.rawType = new SimpleStringProperty(type.getDisplay()); // ← ADD THIS
        this.maxWeightKg = new SimpleDoubleProperty(type.getMaxWeightKg());
        this.maxVolumeCbm = new SimpleDoubleProperty(type.getMaxVolumeCbm());
        this.status = new SimpleObjectProperty<>(Status.AVAILABLE);
    }

    // ── Edit constructor ───────────────────────────────────────────────────
    public Container(int id, String containerNumber, String rawType, double maxWeightKg, double maxVolumeCbm, Status status) {
        this.id = new SimpleIntegerProperty(id);
        this.containerNumber = new SimpleStringProperty(containerNumber);
        this.rawType = new SimpleStringProperty(rawType);
        this.type = new SimpleObjectProperty<>(Type.fromDisplay(rawType));
        this.maxWeightKg = new SimpleDoubleProperty(maxWeightKg);   // ← directly from DB
        this.maxVolumeCbm = new SimpleDoubleProperty(maxVolumeCbm);  // ← directly from DB
        this.status = new SimpleObjectProperty<>(status);
    }

    public static void resetCounter(int next) {
        idCounter = next;
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public int getId() {
        return id.get();
    }

    public String getContainerNumber() {
        return containerNumber.get();
    }

    public String getName() {
        return containerNumber.get();
    }

    public Type getType() {
        return type.get();
    }

    public String getTypeDisplay() {
        return type.get().getDisplay();
    }

    public double getMaxWeightKg() {
        return maxWeightKg.get();
    }

    public double getMaxVolumeCbm() {
        return maxVolumeCbm.get();
    }

    public Status getStatus() {
        return status.get();
    }

    public String getStatusDisplay() {
        return status.get().getDisplay();
    }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setContainerNumber(String v) {
        containerNumber.set(v);
    }

    public void setType(Type v) {
        type.set(v);
        maxWeightKg.set(v.getMaxWeightKg());
        maxVolumeCbm.set(v.getMaxVolumeCbm());
    }

    public void setStatus(Status v) {
        status.set(v);
    }

    // ── Properties ─────────────────────────────────────────────────────────
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty containerNumberProperty() {
        return containerNumber;
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    public DoubleProperty maxWeightKgProperty() {
        return maxWeightKg;
    }

    public DoubleProperty maxVolumeCbmProperty() {
        return maxVolumeCbm;
    }

    public ObjectProperty<Status> statusProperty() {
        return status;
    }

    @Override
    public String toString() {
        return containerNumber.get() + " — " + type.get().getDisplay();
    }
}
