public class PaymentResult {
    private final boolean success;
    private final String message;
    private final String receipt;
    public PaymentResult(boolean success, String message, String receipt) {
        this.success = success; this.message = message; this.receipt = receipt;
    }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getReceipt() { return receipt; }
}
