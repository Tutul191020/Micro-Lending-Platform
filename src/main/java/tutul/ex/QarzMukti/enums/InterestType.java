package tutul.ex.QarzMukti.enums;

/**
 * Enum representing different types of interest calculation methods
 */
public enum InterestType {
    FLAT("Flat Interest Rate - Interest calculated on original principal"),
    REDUCING_BALANCE("Reducing Balance - Interest calculated on outstanding principal"),
    SIMPLE("Simple Interest - Interest = P × R × T"),
    COMPOUND("Compound Interest - Interest compounded periodically");

    private final String description;

    InterestType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
