package anchor_wfx;

import static anchor_wfx.Shipment.Status.PENDING_DEPARTURE;
import static anchor_wfx.Shipment.Status.values;
import javafx.beans.property.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Invoice model for ANCHOR ADS — Billings & Documentation module.
 */
public class Invoice {

    private static int idCounter = 1;

    // ── Status Enum ────────────────────────────────────────────────────────
    public enum Status {
        UNPAID        ("Unpaid"),
        PARTIALLY_PAID("Partially Paid"),
        PAID          ("Paid");

        private final String display;
        Status(String d) { this.display = d; }
        public String getDisplay() { return display; }

        public static Status fromDisplay(String d) {
            for (Status s : values()) if (s.display.equals(d)) return s;
            return UNPAID;
        }
        
        public static Invoice.Status fromName(String dbValue) {
            if (dbValue == null) {
                return UNPAID;
            }
            for (Invoice.Status s : values()) {
                if (s.name().equalsIgnoreCase(dbValue)) {
                    return s;
                }
            }
            return UNPAID;
        }
    }

    private final IntegerProperty           id;
    private final IntegerProperty           shipmentId;
    private final IntegerProperty           customerId;
    private final IntegerProperty           freightRateId;
    private final DoubleProperty            subtotal;
    private final DoubleProperty            totalAmount;
    private final ObjectProperty<Status>    status;
    private final ObjectProperty<LocalDate> invoiceDate;

    /** Surcharges applied to this invoice — managed in-memory. */
    private final List<InvoiceSurcharge> appliedSurcharges = new ArrayList<>();

    // ── New invoice constructor (auto-increment ID) ────────────────────────
    public Invoice(int shipmentId, int customerId, int freightRateId,
                   double subtotal, double totalAmount,
                   Status status, LocalDate invoiceDate) {
        this.id            = new SimpleIntegerProperty(idCounter++);
        this.shipmentId    = new SimpleIntegerProperty(shipmentId);
        this.customerId    = new SimpleIntegerProperty(customerId);
        this.freightRateId = new SimpleIntegerProperty(freightRateId);
        this.subtotal      = new SimpleDoubleProperty(subtotal);
        this.totalAmount   = new SimpleDoubleProperty(totalAmount);
        this.status        = new SimpleObjectProperty<>(status);
        this.invoiceDate   = new SimpleObjectProperty<>(invoiceDate);
    }

    // ── Edit constructor (preserve existing ID) ────────────────────────────
    public Invoice(int id, int shipmentId, int customerId, int freightRateId,
                   double subtotal, double totalAmount,
                   Status status, LocalDate invoiceDate) {
        this.id            = new SimpleIntegerProperty(id);
        this.shipmentId    = new SimpleIntegerProperty(shipmentId);
        this.customerId    = new SimpleIntegerProperty(customerId);
        this.freightRateId = new SimpleIntegerProperty(freightRateId);
        this.subtotal      = new SimpleDoubleProperty(subtotal);
        this.totalAmount   = new SimpleDoubleProperty(totalAmount);
        this.status        = new SimpleObjectProperty<>(status);
        this.invoiceDate   = new SimpleObjectProperty<>(invoiceDate);
    }

    public static void resetCounter(int next) { idCounter = next; }

    // ── Surcharge helpers ──────────────────────────────────────────────────
    public List<InvoiceSurcharge> getAppliedSurcharges() { return appliedSurcharges; }

    public void addSurcharge(InvoiceSurcharge s) {
        appliedSurcharges.add(s);
        recalculateTotal();
    }

    /** Recomputes totalAmount = subtotal + sum of all applied surcharge amounts. */
    public void recalculateTotal() {
        double surchargeSum = appliedSurcharges.stream()
            .mapToDouble(InvoiceSurcharge::getAppliedAmount).sum();
        totalAmount.set(subtotal.get() + surchargeSum);
    }

    // ── Getters ────────────────────────────────────────────────────────────
    public int       getId()            { return id.get(); }
    public int       getShipmentId()    { return shipmentId.get(); }
    public int       getCustomerId()    { return customerId.get(); }
    public int       getFreightRateId() { return freightRateId.get(); }
    public double    getSubtotal()      { return subtotal.get(); }
    public double    getTotalAmount()   { return totalAmount.get(); }
    public Status    getStatus()        { return status.get(); }
    public String    getStatusDisplay() { return status.get().getDisplay(); }
    public LocalDate getInvoiceDate()   { return invoiceDate.get(); }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setShipmentId(int v)      { shipmentId.set(v); }
    public void setCustomerId(int v)      { customerId.set(v); }
    public void setFreightRateId(int v)   { freightRateId.set(v); }
    public void setSubtotal(double v)     { subtotal.set(v); recalculateTotal(); }
    public void setTotalAmount(double v)  { totalAmount.set(v); }
    public void setStatus(Status v)       { status.set(v); }
    public void setInvoiceDate(LocalDate v){ invoiceDate.set(v); }

    // ── Properties ─────────────────────────────────────────────────────────
    public IntegerProperty           idProperty()            { return id; }
    public IntegerProperty           shipmentIdProperty()    { return shipmentId; }
    public IntegerProperty           customerIdProperty()    { return customerId; }
    public IntegerProperty           freightRateIdProperty() { return freightRateId; }
    public DoubleProperty            subtotalProperty()      { return subtotal; }
    public DoubleProperty            totalAmountProperty()   { return totalAmount; }
    public ObjectProperty<Status>    statusProperty()        { return status; }
    public ObjectProperty<LocalDate> invoiceDateProperty()   { return invoiceDate; }
}