package structures;

// Lambda structure
public class Lambda {
    private int number;
    private String boundedVariable;
    private int environment;

    public Lambda(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public String getBoundedVariable() {
        return boundedVariable;
    }

    public void setBoundedVariable(String boundedVariable) {
        this.boundedVariable = boundedVariable;
    }

    public int getEnvironment() {
        return environment;
    }

    public void setEnvironment(int environment) {
        this.environment = environment;
    }

        @Override
    public String toString() {
        return "Lambda(number=" + number + ", boundedVariable=" + boundedVariable + ", environment=" + environment + ")";
    }

}
