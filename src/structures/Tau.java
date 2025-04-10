package structures;

public class Tau {
    private int number;

    public Tau(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "Tau(number=" + number + ")";
    }
}