package anchor_wfx;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DashboardQueries {

    public int getActiveShipments() {
        int count = 0;

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            String sql = "SELECT COUNT(*) AS totalActiveShipments FROM shipment WHERE status IN ('pending_departure', 'departed', 'in_transit')";
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                count = rs.getInt("totalActiveShipments");
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    public int getPendingInvoices() {
        int pending = 0;

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            String sql1 = "SELECT COUNT(*) AS unpaid FROM invoice WHERE status = 'unpaid'";
            ResultSet rs = st.executeQuery(sql1);

            while (rs.next()) {
                pending += rs.getInt("unpaid");
            }

            String sql2 = "SELECT COUNT(*) AS partially_paid FROM invoice WHERE status = 'partially_paid'";

            rs = st.executeQuery(sql2);
            while (rs.next()) {
                pending += rs.getInt("partially_paid");
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pending;
    }

    public int getTotalCustomers() {
        int totalCustomers = 0;

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            String sql = "SELECT COUNT(*) AS totalCustomers FROM customer";
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                totalCustomers = rs.getInt("totalCustomers");
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalCustomers;
    }

    public int getDeliveredThisMonth() {
        int delivers = 0;

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            String sql = "SELECT COUNT(*) AS delivered FROM shipment\n"
                    + "WHERE status = 'delivered' \n"
                    + "AND arrival_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01')\n"
                    + "AND arrival_date < DATE_FORMAT(CURDATE() + INTERVAL 1 MONTH, '%Y-%m-01')";

            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                delivers = rs.getInt("delivered");
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return delivers;
    }

    public int[] getVesselStatus() {
        int[] counts = {0, 0, 0};

        try {
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            String sql = "SELECT status, COUNT(*) AS counts FROM vessel GROUP BY status";
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                switch (rs.getString("status")) {
                    case "active" ->
                        counts[0] = rs.getInt("counts");
                    case "docked" ->
                        counts[1] = rs.getInt("counts");
                    case "under_maintenance" ->
                        counts[2] = rs.getInt("counts");
                }
            }

            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    public List<String[]> getLatestBookingsAsArray() {
        List<String[]> list = new ArrayList<>();
        String sql = """
        SELECT b.booking_id, c1.name AS shipper, c2.name AS consignee, b.status
        FROM booking b
        JOIN customer c1 ON b.shipper_id = c1.customer_id
        JOIN customer c2 ON b.consignee_id = c2.customer_id
        ORDER BY b.booking_id DESC
    """;
        try (Connection conn = DBConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    "#ANC" + rs.getInt("booking_id"),
                    rs.getString("shipper"),
                    rs.getString("consignee"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return list;
    }
}
