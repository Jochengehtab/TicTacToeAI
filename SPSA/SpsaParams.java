package src.SPSA;

public class SpsaParams {
    double a, c;
    int A;
    double alpha, gamma;

    public SpsaParams(double a, double c, int A, double alpha, double gamma) {
        this.a = a;
        this.c = c;
        this.A = A;
        this.alpha = alpha;
        this.gamma = gamma;
    }
}