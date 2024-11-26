package example06;

import java.math.BigInteger;

public class Practice {
    public BigInteger calculateResult(BigInteger base1,
                                      BigInteger power1,
                                      BigInteger base2,
                                      BigInteger power2) {
        BigInteger result;
        PowerCalculatingThread thread1 = new PowerCalculatingThread(base1, power1);
        PowerCalculatingThread thread2 = new PowerCalculatingThread(base2, power2);
        System.out.println(" thread1.start();======================");
        thread1.start();
        System.out.println(" thread2.start();======================");
        thread2.start();

        try {
            System.out.println(" thread1.join();======================");
            thread1.join(2000);
            System.out.println(" thread2.join();======================");
            thread2.join(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        result = thread1.getResult().add(thread2.getResult());
        System.out.println(" result = " + result);
        return result;
    }

    private static class PowerCalculatingThread extends Thread {
        private BigInteger result = BigInteger.ONE;
        private BigInteger base;
        private BigInteger power;

        public PowerCalculatingThread(BigInteger base, BigInteger power) {
            this.base = base;
            this.power = power;
        }

        @Override
        public void run() {
            for(BigInteger i = BigInteger.ZERO;
                i.compareTo(power) !=0;
                i = i.add(BigInteger.ONE)) {
                result = result.multiply(base);
            }
        }

        public BigInteger getResult() {
            return result;
        }
    }
    public static void main(String[] args) {
        Practice practice = new Practice();
        BigInteger base = BigInteger.valueOf(500);
        BigInteger power = BigInteger.valueOf(30);
        BigInteger result = practice.calculateResult(base, power, base, power);
        System.out.println(result);
    }
}