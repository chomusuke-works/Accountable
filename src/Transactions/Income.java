package Transactions;

public class Income extends Transaction {

    /**
     * Used when the total of a wallet is computed.
     * @return the amount of the transaction
     */
    @Override
    public float add() {
        return this.getAmount();
    }
}
