package anchor_wfx;

import java.sql.*;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class OptionQueries {

    public ObservableList<Cargo> getCargoListFromDatabase() {
        ObservableList<Cargo> cargoList = FXCollections.observableArrayList();
        System.out.println("Fetching cargo list..");
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            String sql = "SELECT * FROM cargo";
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                int haz = rs.getInt("is_hazardous");
                boolean hazardous = false;

                if (haz == 1) {
                    hazardous = true;
                }

                Cargo cargo = new Cargo(
                        rs.getInt("cargo_id"),
                        rs.getString("description"),
                        rs.getDouble("weight_kg"),
                        rs.getDouble("volume_cbm"),
                        hazardous,
                        rs.getString("imdg_class"),
                        rs.getString("un_number"),
                        rs.getString("proper_shipping_name")
                );

                cargoList.add(cargo);
            }

            System.out.println("Fetching cargo...");
            System.out.println("Rows fetched: " + cargoList.size());

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return cargoList;
    }

    public ObservableList<Crew> getCrewListFromDatabase() {
        ObservableList<Crew> crewList = FXCollections.observableArrayList();

        System.out.println("getting CrewList");
        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            String sql = "SELECT * FROM crew";
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                java.sql.Date expiryDate = rs.getDate("license_expiry");
                LocalDate expiry = (expiryDate != null) ? expiryDate.toLocalDate() : null; // ✅ null-safe

                Crew crew = new Crew(
                        rs.getInt("crew_id"),
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("contact_info"),
                        rs.getString("license_number"),
                        expiry
                );

                crewList.add(crew);
            }

            System.out.println("Fetching crew...");
            System.out.println("Rows fetched: " + crewList.size());
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return crewList;
    }

    public ObservableList<Vessel> getVesselListFromDatabase() {
        System.out.println("fetching vesselList from database");
        ObservableList<Vessel> vesselList = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM vessel");

            while (rs.next()) {
                String status = rs.getString("status");

                Vessel vessel = new Vessel(
                        rs.getInt("vessel_id"),
                        rs.getString("name"),
                        rs.getString("vessel_type"),
                        rs.getDouble("capacity_weight"),
                        rs.getDouble("capacity_volume"),
                        Vessel.Status.fromDb(status),
                        rs.getString("registration_number")
                );

                vesselList.add(vessel);
            }

            System.out.println("Loading vessels...");
            System.out.println("Loaded vessels: " + vesselList.size());

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return vesselList;
    }

    public void loadCrewAssignments(List<Vessel> vesselList, List<Crew> crewList) {
        for (Vessel v : vesselList) {
            v.getAssignedCrew().clear();
        }

        try {
            Connection con = DBConnection.getConnection();

            String sql = """
                SELECT vc.vessel_id, vc.crew_id
                FROM vessel_crew vc
            """;

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            for (Vessel v : vesselList) {
                v.getAssignedCrew().clear();
            }
            while (rs.next()) {
                int vesselId = rs.getInt("vessel_id");
                int crewId = rs.getInt("crew_id");

                Vessel vessel = vesselList.stream()
                        .filter(v -> v.getId() == vesselId)
                        .findFirst().orElse(null);

                Crew crew = crewList.stream()
                        .filter(c -> c.getId() == crewId)
                        .findFirst().orElse(null);

                if (vessel != null && crew != null) {
                    vessel.getAssignedCrew().add(crew);
                }
            }

            con.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public ObservableList<Port> getPortListFromDatabase() {
        ObservableList<Port> portList = FXCollections.observableArrayList();
        portList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM port");

            while (rs.next()) {
                portList.add(new Port(
                        rs.getInt("port_id"),
                        rs.getString("name"),
                        rs.getString("country"),
                        rs.getString("city")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return portList;
    }

    public ObservableList<Route> getRouteListFromDatabase() {
        ObservableList<Route> routeList = FXCollections.observableArrayList();
        routeList.clear();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM route");

            while (rs.next()) {
                routeList.add(new Route(
                        rs.getInt("route_id"),
                        rs.getInt("origin_port_id"),
                        rs.getInt("destination_port_id"),
                        rs.getInt("transit_days")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return routeList;
    }

    public ObservableList<Container> getContainerListFromDatabase() {
        ObservableList<Container> containerList = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM container");

            while (rs.next()) {
                Container.Status status = Container.Status.fromName(rs.getString("status"));
                containerList.add(new Container(
                        rs.getInt("container_id"),
                        rs.getString("container_number"),
                        rs.getString("container_type"),
                        rs.getDouble("max_weight"),
                        rs.getDouble("max_volume"),
                        status
                ));
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return containerList;
    }

    public ObservableList<Booking> getBookingListFromDatabase() {
        ObservableList<Booking> bookingList = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM booking");

            while (rs.next()) {
                Booking.Status status = Booking.Status.fromName(rs.getString("status"));

                java.sql.Date createdDate = rs.getDate("booking_date"); // ← changed
                LocalDate created = (createdDate != null) ? createdDate.toLocalDate() : LocalDate.now();

                bookingList.add(new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("shipper_id"),
                        rs.getInt("consignee_id"),
                        rs.getInt("cargo_id"),
                        status,
                        rs.getString("notes") != null ? rs.getString("notes") : "",
                        created
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return bookingList;
    }

    public ObservableList<Shipment> getShipmentListFromDatabase() {
        ObservableList<Shipment> list = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM shipment");

            while (rs.next()) {
                Shipment.Status status = Shipment.Status.fromName(rs.getString("status"));

                java.sql.Date depDate = rs.getDate("departure_date");
                java.sql.Date arrDate = rs.getDate("arrival_date");

                list.add(new Shipment(
                        rs.getInt("shipment_id"),
                        rs.getInt("booking_id"),
                        rs.getInt("vessel_id"),
                        rs.getInt("route_id"),
                        rs.getInt("container_id"),
                        status,
                        depDate != null ? depDate.toLocalDate() : null,
                        arrDate != null ? arrDate.toLocalDate() : null
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public ObservableList<Invoice> getInvoiceListFromDatabase() {
        ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM invoice");

            while (rs.next()) {
                Invoice.Status status = Invoice.Status.fromName(rs.getString("status"));
                java.sql.Date invoice_date = rs.getDate("invoice_date");

                invoiceList.add(new Invoice(
                        rs.getInt("invoice_id"),
                        rs.getInt("shipment_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("freight_rate_id"),
                        rs.getDouble("subtotal"),
                        rs.getDouble("total_amount"),
                        status,
                        invoice_date != null ? invoice_date.toLocalDate() : null
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return invoiceList;
    }

    public ObservableList<Payment> getPaymentListFromDatabase() {
        ObservableList<Payment> paymentList = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM payment");

            while (rs.next()) {
                java.sql.Date payment_date = rs.getDate("payment_date");

                paymentList.add(new Payment(
                        rs.getInt("payment_id"),
                        rs.getInt("invoice_id"),
                        rs.getDouble("amount_paid"),
                        payment_date != null ? payment_date.toLocalDate() : null,
                        rs.getString("receipt_number")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return paymentList;
    }

    public ObservableList<FreightRate> getFreightRateListFromDatabase() {
        ObservableList<FreightRate> freightRateList = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM freight_rate");

            while (rs.next()) {
                freightRateList.add(new FreightRate(
                        rs.getInt("rate_id"),
                        rs.getString("name"),
                        rs.getDouble("base_amount")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return freightRateList;
    }

    public ObservableList<Surcharge> getSurchargeListFromDatabase() {
        ObservableList<Surcharge> surchargeList = FXCollections.observableArrayList();

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM surcharge");

            while (rs.next()) {
                surchargeList.add(new Surcharge(
                        rs.getInt("surcharge_id"),
                        rs.getString("name"),
                        rs.getDouble("default_amount")
                ));
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return surchargeList;
    }

    public ObservableList<SettingsView.Employee> getUserListFromDatabase() {
        ObservableList<SettingsView.Employee> userList = FXCollections.observableArrayList();

        System.out.println("Fetching users from database...");

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM users");

            while (rs.next()) {
                String roleStr = rs.getString("role");
                String statusStr = rs.getString("status");

                SettingsView.EmployeeRole role = switch (roleStr) {
                    case "super_admin" ->
                        SettingsView.EmployeeRole.SUPER_ADMIN;
                    case "account_manager" ->
                        SettingsView.EmployeeRole.ACCOUNT_MANAGER;
                    case "operations_officer" ->
                        SettingsView.EmployeeRole.OPERATIONS_OFFICER;
                    case "billing_officer" ->
                        SettingsView.EmployeeRole.BILLING_OFFICER;
                    case "fleet_manager" ->
                        SettingsView.EmployeeRole.FLEET_MANAGER;
                    case "customer_relations" ->
                        SettingsView.EmployeeRole.CUSTOMER_OFFICER;
                    default ->
                        SettingsView.EmployeeRole.AUDITOR;
                };

                boolean active = "active".equals(statusStr);

                SettingsView.Employee emp = new SettingsView.Employee(
                        rs.getString("employee_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        role,
                        active
                );

                userList.add(emp);
            }

            System.out.println("Users fetched: " + userList.size());
            con.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return userList;
    }

    public boolean insertUser(SettingsView.Employee emp) {
        String sql = """
        INSERT INTO users (employee_id, username, full_name, email, password, role, status)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, emp.getEmployeeId());
            ps.setString(2, emp.getUsername());
            ps.setString(3, emp.getFullName());
            ps.setString(4, emp.getEmail());
            ps.setString(5, emp.getPassword());
            ps.setString(6, roleToDb(emp.getRole()));
            ps.setString(7, emp.isActive() ? "active" : "inactive");

            ps.executeUpdate();
            con.close();

            System.out.println("User inserted: " + emp.getUsername());
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String roleToDb(SettingsView.EmployeeRole role) {
        return switch (role) {
            case SUPER_ADMIN ->
                "super_admin";
            case ACCOUNT_MANAGER ->
                "account_manager";
            case OPERATIONS_OFFICER ->
                "operations_officer";
            case BILLING_OFFICER ->
                "billing_officer";
            case FLEET_MANAGER ->
                "fleet_manager";
            case CUSTOMER_OFFICER ->
                "customer_relations";
            case AUDITOR ->
                "read_only";
        };
    }

    public String generateNextEmployeeId() {
        String sql = """
        SELECT employee_id FROM users
        WHERE employee_id LIKE 'EMP-%'
        ORDER BY employee_id DESC
        LIMIT 1
    """;

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                String last = rs.getString("employee_id"); // e.g. "EMP-0105"
                int num = Integer.parseInt(last.replace("EMP-", ""));
                con.close();
                return String.format("EMP-%04d", num + 1);
            }

            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return "EMP-0100";
    }

    public boolean updateUser(SettingsView.Employee emp) {
        String sql = """
        UPDATE users
        SET full_name = ?,
            email     = ?,
            role      = ?,
            status    = ?
        WHERE employee_id = ?
    """;

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, emp.getFullName());
            ps.setString(2, emp.getEmail());
            ps.setString(3, roleToDb(emp.getRole()));
            ps.setString(4, emp.isActive() ? "active" : "inactive");
            ps.setString(5, emp.getEmployeeId());

            int rows = ps.executeUpdate();
            con.close();

            System.out.println("User updated: " + emp.getUsername() + " (" + rows + " row affected)");
            return rows > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateUserPassword(SettingsView.Employee emp) {
        String sql = "UPDATE users SET password = ? WHERE employee_id = ?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, emp.getPassword());
            ps.setString(2, emp.getEmployeeId());

            int rows = ps.executeUpdate();
            con.close();

            System.out.println("Password updated for: " + emp.getUsername());
            return rows > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateAdminCredentials(String employeeId, String newUsername, String newPassword) {
        String sql = "UPDATE users SET username = ?, password = ? WHERE employee_id = ?";

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, newUsername);
            ps.setString(2, newPassword);
            ps.setString(3, employeeId);

            int rows = ps.executeUpdate();
            con.close();

            System.out.println("Admin credentials updated: " + employeeId);
            return rows > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(SettingsView.Employee emp) {
        String sql = "DELETE FROM users WHERE employee_id = ?";

        try (
                Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, emp.getEmployeeId());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public SettingsView.Employee getUserByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = ?";

    try (
        Connection con = DBConnection.getConnection();
        PreparedStatement ps = con.prepareStatement(sql)
    ) {
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String roleStr = rs.getString("role");
            String statusStr = rs.getString("status");

            SettingsView.EmployeeRole role = switch (roleStr) {
                case "super_admin"        -> SettingsView.EmployeeRole.SUPER_ADMIN;
                case "account_manager"    -> SettingsView.EmployeeRole.ACCOUNT_MANAGER;
                case "operations_officer" -> SettingsView.EmployeeRole.OPERATIONS_OFFICER;
                case "billing_officer"    -> SettingsView.EmployeeRole.BILLING_OFFICER;
                case "fleet_manager"      -> SettingsView.EmployeeRole.FLEET_MANAGER;
                case "customer_relations" -> SettingsView.EmployeeRole.CUSTOMER_OFFICER;
                default                   -> SettingsView.EmployeeRole.AUDITOR;
            };

            boolean active = "active".equals(statusStr);

            return new SettingsView.Employee(
                rs.getString("employee_id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("password"),
                role,
                active
            );
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}

}
