package structures;

public class Delta {
    private int number;

    public Delta(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "Delta(number=" + number + ")";
    }
}