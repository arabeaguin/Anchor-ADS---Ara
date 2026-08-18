package anchor_wfx;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Permission {

    // module -> [canView, canAdd, canEdit, canDelete]
    private static final Map<String, boolean[]> permissions = new HashMap<>();

    public static void load(String role) {
        permissions.clear();
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                "SELECT module, can_view, can_add, can_edit, can_delete "
                + "FROM role_permissions WHERE role = ?"
            );
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                permissions.put(rs.getString("module"), new boolean[]{
                    rs.getInt("can_view")   == 1,
                    rs.getInt("can_add")    == 1,
                    rs.getInt("can_edit")   == 1,
                    rs.getInt("can_delete") == 1
                });
            }
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean canView(String module)   { return check(module, 0); }
    public static boolean canAdd(String module)    { return check(module, 1); }
    public static boolean canEdit(String module)   { return check(module, 2); }
    public static boolean canDelete(String module) { return check(module, 3); }

    private static boolean check(String module, int index) {
        boolean[] perms = permissions.get(module);
        return perms != null && perms[index];
    }
}