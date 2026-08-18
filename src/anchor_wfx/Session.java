package anchor_wfx;

public class Session {
    private static String user;
    private static String role;

    public static String getUser() { return user; }
    public static void setUser(String u) { user = u; }

    public static String getRole() { return role; }
    public static void setRole(String r) { role = r; }

    public static boolean isSuperAdmin()         { return "super_admin".equalsIgnoreCase(role); }
    public static boolean isAccountManager()     { return "account_manager".equalsIgnoreCase(role); }
    public static boolean isOperationsOfficer()  { return "operations_officer".equalsIgnoreCase(role); }
    public static boolean isBillingOfficer()     { return "billing_officer".equalsIgnoreCase(role); }
    public static boolean isFleetManager()       { return "fleet_manager".equalsIgnoreCase(role); }
    public static boolean isCustomerRelations()  { return "customer_relations".equalsIgnoreCase(role); }
    public static boolean isReadOnly()           { return "read_only".equalsIgnoreCase(role); }
}