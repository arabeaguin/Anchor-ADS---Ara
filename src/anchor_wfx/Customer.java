package anchor_wfx;

import javafx.beans.property.*;

public class Customer {

    private static int idCounter = 1;

    private final IntegerProperty id;
    private final StringProperty fullName;
    private final StringProperty email;
    private final StringProperty phone;
    private final StringProperty address;
    private final StringProperty role; // Shipper / Consignee / Both

    public Customer(String fullName, String email, String phone, String address, String role) {
        this.id       = new SimpleIntegerProperty(idCounter++);
        this.fullName = new SimpleStringProperty(fullName);
        this.email    = new SimpleStringProperty(email);
        this.phone    = new SimpleStringProperty(phone);
        this.address  = new SimpleStringProperty(address);
        this.role     = new SimpleStringProperty(role);
    }

    // Used when editing — preserves existing ID
    public Customer(int id, String fullName, String email, String phone, String address, String role) {
        this.id       = new SimpleIntegerProperty(id);
        this.fullName = new SimpleStringProperty(fullName);
        this.email    = new SimpleStringProperty(email);
        this.phone    = new SimpleStringProperty(phone);
        this.address  = new SimpleStringProperty(address);
        this.role     = new SimpleStringProperty(role);
    }

    public static void resetCounter(int next) { idCounter = next; }

    public int    getId()       { return id.get(); }
    public String getFullName() { return fullName.get(); }
    public String getEmail()    { return email.get(); }
    public String getPhone()    { return phone.get(); }
    public String getAddress()  { return address.get(); }
    public String getRole()     { return role.get(); }

    public void setFullName(String v) { fullName.set(v); }
    public void setEmail(String v)    { email.set(v); }
    public void setPhone(String v)    { phone.set(v); }
    public void setAddress(String v)  { address.set(v); }
    public void setRole(String v)     { role.set(v); }

    public IntegerProperty idProperty()       { return id; }
    public StringProperty  fullNameProperty() { return fullName; }
    public StringProperty  emailProperty()    { return email; }
    public StringProperty  phoneProperty()    { return phone; }
    public StringProperty  addressProperty()  { return address; }
    public StringProperty  roleProperty()     { return role; }

}