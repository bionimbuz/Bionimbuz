package br.unb.cic.bionimbus.services.tarifation.Amazon;

/**
 * Classe AmazonVirtualMachine, used to represent a Amazon VM.
 *
 * @author Gabriel Fritz Sluzala
 */
public class AmazonVirtualMachine {

    private String pricing;
    private String region;
    private int id;
    private String os;
    private String model;
    private double upfront;
    private String updated_at;
    private double term;
    private String created_at;
    private boolean latest;
    private double hourly;
    private boolean ebsoptimized;

    /**
     *
     * @param pricing - Pricing Style
     * @param region - Region of VM
     * @param id - ID of VM
     * @param os - OS of VM
     * @param model - model of VM
     * @param upfront - Upfront
     * @param updated_at - Date of info. update
     * @param term - Term
     * @param created_at - Date of info. creation
     * @param latest - Latest
     * @param hourly - Price of VM/hour.
     * @param ebsoptimized - Ebsoptimized
     */
    public AmazonVirtualMachine(String pricing, String region, int id, String os, String model, double upfront, String updated_at, double term, String created_at, boolean latest, double hourly, boolean ebsoptimized) {
        this.pricing = pricing;
        this.region = region;
        this.id = id;
        this.os = os;
        this.model = model;
        this.upfront = upfront;
        this.updated_at = updated_at;
        this.term = term;
        this.created_at = created_at;
        this.latest = latest;
        this.hourly = hourly;
        this.ebsoptimized = ebsoptimized;
    }

    /**
     *
     * @return - the pricing style of VM
     */
    public String getPricing() {
        return pricing;
    }

    /**
     *
     * @return - the region of VM
     */
    public String getRegion() {
        return region;
    }

    /**
     *
     * @return - the ID of VM
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @return - the OS of VM
     */
    public String getOs() {
        return os;
    }

    /**
     *
     * @return - the model of VM
     */
    public String getModel() {
        return model;
    }

    /**
     *
     * @return - the upfront of VM
     */
    public double getUpfront() {
        return upfront;
    }

    /**
     *
     * @return - the date of info. update
     */
    public String getUpdated_at() {
        return updated_at;
    }

    /**
     *
     * @return - the term of VM.
     */
    public double getTerm() {
        return term;
    }

    /**
     *
     * @return - the date of creation of info.
     */
    public String getCreated_at() {
        return created_at;
    }

    /**
     *
     * @return - if the VM is latest
     */
    public boolean isLatest() {
        return latest;
    }

    /**
     *
     * @return - the price of VM/hour
     */
    public double getHourly() {
        return hourly;
    }

    /**
     *
     * @return - if VM is ebsoptimized
     */
    public boolean isEbsoptimized() {
        return ebsoptimized;
    }

    /**
     *
     * @return - hashcode
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.id;
        return hash;
    }

    /**
     *
     * @param obj
     * @return - if obj is equals to VM.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AmazonVirtualMachine other = (AmazonVirtualMachine) obj;
        return this.id == other.id;
    }
}
