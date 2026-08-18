package anchor_wfx;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Payment model for ANCHOR ADS — Billings & Documentation module.
 */
public class Payment {

    private static int idCounter = 1;

    private final IntegerProperty           id;
    private final IntegerProperty           invoiceId;
    private final DoubleProperty            amountPaid;
    private final ObjectProperty<LocalDate> paymentDate;
    private final StringProperty            receiptNumber;

    // ── New payment constructor (auto-increment ID) ────────────────────────
    public Payment(int invoiceId, double amountPaid,
                   LocalDate paymentDate, String receiptNumber) {
        this.id            = new SimpleIntegerProperty(idCounter++);
        this.invoiceId     = new SimpleIntegerProperty(invoiceId);
        this.amountPaid    = new SimpleDoubleProperty(amountPaid);
        this.paymentDate   = new SimpleObjectProperty<>(paymentDate);
        this.receiptNumber = new SimpleStringProperty(receiptNumber);
    }

    // ── Edit constructor (preserve existing ID) ────────────────────────────
    public Payment(int id, int invoiceId, double amountPaid,
                   LocalDate paymentDate, String receiptNumber) {
        this.id            = new SimpleIntegerProperty(id);
        this.invoiceId     = new SimpleIntegerProperty(invoiceId);
        this.amountPaid    = new SimpleDoubleProperty(amountPaid);
        this.paymentDate   = new SimpleObjectProperty<>(paymentDate);
        this.receiptNumber = new SimpleStringProperty(receiptNumber);
    }

    public static void resetCounter(int next) { idCounter = next; }

    // ── Getters ────────────────────────────────────────────────────────────
    public int       getId()            { return id.get(); }
    public int       getInvoiceId()     { return invoiceId.get(); }
    public double    getAmountPaid()    { return amountPaid.get(); }
    public LocalDate getPaymentDate()   { return paymentDate.get(); }
    public String    getReceiptNumber() { return receiptNumber.get(); }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setAmountPaid(double v)      { amountPaid.set(v); }
    public void setPaymentDate(LocalDate v)  { paymentDate.set(v); }
    public void setReceiptNumber(String v)   { receiptNumber.set(v); }

    // ── Properties ─────────────────────────────────────────────────────────
    public IntegerProperty           idProperty()            { return id; }
    public IntegerProperty           invoiceIdProperty()     { return invoiceId; }
    public DoubleProperty            amountPaidProperty()    { return amountPaid; }
    public ObjectProperty<LocalDate> paymentDateProperty()   { return paymentDate; }
    public StringProperty            receiptNumberProperty() { return receiptNumber; }
}