import java.util.UUID;

public class FeeService {
    public PaymentResult pay(String regNo, String amountText) {
        if (regNo == null || regNo.isBlank())
            return new PaymentResult(false, "Enter registration number.", null);
        if (amountText == null || amountText.isBlank())
            return new PaymentResult(false, "Enter an amount.", null);
        final double amount;
        try { amount = Double.parseDouble(amountText.trim()); }
        catch (NumberFormatException e) {
            return new PaymentResult(false, "Enter a valid numeric amount.", null);
        }
        if (!Double.isFinite(amount) || amount <= 0)
            return new PaymentResult(false, "Amount must be greater than zero.", null);

        // Demonstration only. A real payment gateway/database call belongs in a background task.
        String receipt = "FEE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(true, String.format("Payment accepted: Rs %.2f", amount), receipt);
    }
}
