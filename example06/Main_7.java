package example06;


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//join()은 다른 스레드가 작업을 마칠 때까지 기다리는 "대기실"과 같습니다. 여러 사람이 동시에 작업을 하는데,
// 한 사람이 다른 사람의 작업 결과가 필요하다면 그 사람이 작업을 끝낼 때까지 기다려야 합니다.
// 이때 join()을 사용하여 다른 사람(스레드)이 작업을 마칠 때까지 기다리는 것입니다.
public class Main_7 {
    public static void main(String[] args) throws InterruptedException {
        List<Long> inputNumbers = Arrays.asList(100000000L, 3435L, 35435L, 2324L, 4656L, 23L, 5556L);

        List<FactorialThread> threads = new ArrayList<>();

        for (long inputNumber : inputNumbers) {
            threads.add(new FactorialThread(inputNumber)); //입력 숫자 목록을 반복하면서 각 숫자에 대해 FactorialThread 객체를 생성하고 리스트에 추가
        }

        for (Thread thread : threads) {
            thread.setDaemon(true); //데몬 스레드는 프로그램의 주요 스레드가 종료되면 자동으로 종료됩니다.
            thread.start();
        }

        for (Thread thread : threads) {
            //호출된 스레드의 run() 메소드가 끝날 때까지 현재 스레드는 블록
            thread.join(2000);//각 스레드가 완료될 때까지 기다립니다. 최대 2초 동안 기다리며, 만약 스레드가 아직 작업 중이라면 다음 스레드로 넘어갑니다.
        }

        for (int i = 0; i < inputNumbers.size(); i++) {
            FactorialThread factorialThread = threads.get(i);
            if (factorialThread.isFinished()) {
                System.out.println("Factorial of " + inputNumbers.get(i) + " is " + factorialThread.getResult());
            } else {
                System.out.println("The calculation for " + inputNumbers.get(i) + " is still in progress");
            }
        }
    }

    public static class FactorialThread extends Thread {
        private long inputNumber;
        private BigInteger result = BigInteger.ZERO;
        private boolean isFinished = false;

        public FactorialThread(long inputNumber) {
            this.inputNumber = inputNumber;
        }

        @Override
        public void run() {
            this.result = factorial(inputNumber);
            this.isFinished = true;
        }

        public BigInteger factorial(long n) {
            BigInteger tempResult = BigInteger.ONE;

            for (long i = n; i > 0; i--) {
                tempResult = tempResult.multiply(new BigInteger((Long.toString(i))));
            }
            return tempResult;
        }

        public BigInteger getResult() {
            return result;
        }

        public boolean isFinished() {
            return isFinished;
        }
    }
}
