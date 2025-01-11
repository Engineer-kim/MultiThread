package LockingAndDeadLock;

import java.util.Random;

public class Deadlock {
    public static void main(String[] args) {
        Intersection intersection = new Intersection();
        Thread trainAThread = new Thread(new TrainA(intersection));
        Thread trainBThread = new Thread(new TrainB(intersection));

        trainAThread.start();
        trainBThread.start();
    }

    public static class TrainB implements Runnable {
        private Intersection intersection;
        private Random random = new Random();

        public TrainB(Intersection intersection) {
            this.intersection = intersection;
        }

        @Override
        public void run() {
            while (true) {
                long sleepingTime = random.nextInt(5);
                try {
                    Thread.sleep(sleepingTime);
                } catch (InterruptedException e) {
                }

                intersection.takeRoadB();
            }
        }
    }

    public static class TrainA implements Runnable {
        private Intersection intersection;
        private Random random = new Random();

        public TrainA(Intersection intersection) {
            this.intersection = intersection;
        }

        @Override
        public void run() {
            while (true) {
                long sleepingTime = random.nextInt(5);
                try {
                    Thread.sleep(sleepingTime);
                } catch (InterruptedException e) {
                }

                intersection.takeRoadA();
            }
        }
    }

    public static class Intersection {
        private Object roadA = new Object();
        private Object roadB = new Object();

        public void takeRoadA() {
            synchronized (roadA) {
                System.out.println("Road A is locked by thread " + Thread.currentThread().getName());

                synchronized (roadB) {
                    System.out.println("Train is passing through road A");
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        public void takeRoadB() {
            // 주석 친 부분의 코드대로 하면 서로 해제될길 바라기때문에 안됨
            //주석 아래 부분 코드처럼하면 잠구는 순서가 같아지기에 데드락 회피 가능
//            synchronized (roadB) {
//                System.out.println("Road B is locked by thread " + Thread.currentThread().getName());
//
//                synchronized (roadA) {
//                    System.out.println("Train is passing through road B");
//
//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                    }
//                }
//            }
            synchronized (roadA) {  // roadA를 먼저 잠급니다.
                synchronized (roadB) {
                    System.out.println("Road B is locked by thread " + Thread.currentThread().getName());
                    System.out.println("Train is passing through road B");
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
