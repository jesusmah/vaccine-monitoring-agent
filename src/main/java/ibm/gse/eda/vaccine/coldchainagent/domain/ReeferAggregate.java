package ibm.gse.eda.vaccine.coldchainagent.domain;

import java.util.LinkedList;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Bean to keep aggregates on the reefer's telemetries, like max temperature and
 * the number of consecutive time temperature measurements which are violating
 * the threshold
 * 
 * This class needs to be serializable as json so arguments are set public
 */
@RegisterForReflection
public class ReeferAggregate {

    public int maxRecordToKeep = 25;
    // max temperature allowed to be still in the cold chain
    public double maxTemperature = 0;
    public double maxTemperatureRegistered = -1000;
    // number of time we accept temperature violation. This is linked to the
    // measurement snapshot
    public int maxViolationAllowed = 4;
    public int violatedTemperatureCount = 0;
    public boolean tooManyViolations = false; // help to verify if previous T was above threshold
    public LinkedList<Double> temperatureList = new LinkedList<Double>();;
    public boolean alreadyReportedColdChainViolation = false;

    public ReeferAggregate(int maxViolationAllowed, double maxTemperature) {
        this.maxViolationAllowed = maxViolationAllowed;
        this.maxTemperature = maxTemperature;
    }

    public ReeferAggregate(int maxViolationAllowed,  double maxTemperature, int maxRecordToKeep) {
        this.maxViolationAllowed = maxViolationAllowed;
        this.maxTemperature = maxTemperature;
        this.maxRecordToKeep = maxRecordToKeep;
    }


    public ReeferAggregate() {
    }

  

	public boolean violateTemperatureThresholdOverTime(int temperatureCount) {
        if (temperatureCount >= maxViolationAllowed) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Update current aggregates with new telemetry temperature.
     * 
     * @param key need to be there to keep reference of the refrigerator ID. It could not
     * be passed in constructor, as the ReeferAggregate is created in the KafkaStream Topology:
     * elemetryGroup.aggregate(
     *      () -> new ReeferAggregate(maxCount,temperatureThreshold),
     * and the lambda as only the value
     * @param telemetryEvent
     * @return
     */
    public ReeferAggregate updateTemperature(final double temperature) {
        // this is because in kafka stream the aggregator lambda does not provide key,
        // value access
        keepLastRecord(temperature);
        // check temperature
        if (temperature >= this.maxTemperature) {
            this.violatedTemperatureCount++;
            this.tooManyViolations = violateTemperatureThresholdOverTime(violatedTemperatureCount);
        } else {
            this.violatedTemperatureCount = 0;
        }
        if (temperature > maxTemperatureRegistered) {
            this.maxTemperatureRegistered = temperature;
        }
        return this;
    }

    public void keepLastRecord(double temperature) {
        if (temperatureList.size() == maxRecordToKeep) {
            temperatureList.remove();
        }
        temperatureList.addLast(temperature);
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public int getViolatedTemperatureCount() {
        return violatedTemperatureCount;
    }

    public void setViolatedTemperatureCount(int violatedTemperatureCount) {
        this.violatedTemperatureCount = violatedTemperatureCount;
    }

    public void setPreviousViolation(boolean previousViolation) {
        this.tooManyViolations = previousViolation;
    }

    public LinkedList<Double> getTemperatureList() {
        return temperatureList;
    }


    public int getMaxViolationAllowed() {
        return maxViolationAllowed;
    }

    public void setMaxViolationAllowed(int maxViolationAllowed) {
        this.maxViolationAllowed = maxViolationAllowed;
    }


    @Override
    public String toString() {
        return "ReeferAggregate [ maxTemperature=" + maxTemperature + ", maxViolationAllowed=" + maxViolationAllowed
                + ", previousViolation=" + tooManyViolations + ", temperatureList="
                + temperatureList + ", violatedTemperatureCount=" + violatedTemperatureCount + " ]";
    }

    public boolean hasTooManyViolations() {
        return this.tooManyViolations;
    }


    public boolean alreadyReportedColdChainViolation(){
        return this.alreadyReportedColdChainViolation;
    }

	public double maxTemperatureRegistered() {
		return maxTemperatureRegistered;
	}

}