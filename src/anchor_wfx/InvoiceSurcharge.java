package anchor_wfx;

/**
 * Represents a surcharge applied to a specific Invoice.
 * Maps to the invoice_surcharge junction table.
 */
public class InvoiceSurcharge {

    private final int    invoiceId;
    private final int    surchargeId;
    private final double appliedAmount;

    public InvoiceSurcharge(int invoiceId, int surchargeId, double appliedAmount) {
        this.invoiceId     = invoiceId;
        this.surchargeId   = surchargeId;
        this.appliedAmount = appliedAmount;
    }

    public int    getInvoiceId()     { return invoiceId; }
    public int    getSurchargeId()   { return surchargeId; }
    public double getAppliedAmount() { return appliedAmount; }
}