package samples;

public class HelloWorld {

    public int a, b, c, max;

    public HelloWorld(int i, int j, int k){
        a = i;
        b = j;
        c = k;
    }

    public int max() {
        if (a > b) {
            max = a;
        } else {
            max = b;
        }
        if (c > max) {
            max = c;
        }
        int two = a + b + c - max;
        if(two > max) {
            max = two;
        }
        return max;
    }

    private void minusOne() {
        a = a - 1;
        b = b - 1;
        c = c - 1;
    }

    public int maxAfterMinus() {
        minusOne();
        max();
        return max;
    }

    public int max(int a, int b, int c) {
        if(a > b) {
            if(a > c) {
                return a;
            }
            return c;
        }
        if(b > c) {
            return b;
        }
        return c;
    }
}
