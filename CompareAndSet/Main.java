package CompareAndSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // LockFreeStack 은 락을 사용하지 않고 동시성 해결 compareAndSet 메소드를 통해 현재 읽어오는 노드값이 지금 읽으려고 하는값과 같은지 비교후 시행
        // 성공 여부를 boolean 값으로 반환
        //밑에 if (head.compareAndSet(currentHeadNode, newHeadNode)) 이쪽 부분 로직및 주석 참조
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        Random random = new Random();

        for (int i = 0; i < 100000; i++) {
            stack.push(random.nextInt());
        }

        List<Thread> threads = new ArrayList<>();

        int pushingThreads = 2;
        int poppingThreads = 2;

        for (int i = 0; i < pushingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.push(random.nextInt());
                }
            });

            thread.setDaemon(true);
            threads.add(thread);
        }

        for (int i = 0; i < poppingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.pop();
                }
            });

            thread.setDaemon(true);
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        Thread.sleep(10000);

        System.out.println(String.format("%,d operations were performed in 10 seconds ", stack.getCounter()));
    }

    public static class LockFreeStack<T> {  //락 없이 동작하는 자료구조
        //여러 스레드가 동시에 AtomicReference의 값을 변경하려고 해도 항상 하나의 스레드만 성공적으로 변경할 수 있도록 보장
        private AtomicReference<StackNode<T>> head = new AtomicReference<>(); //AtomicReference: 임의의 객체에 대한 참조를 원자적으로 변경하기 위한 클래스
        private AtomicInteger counter = new AtomicInteger(0);

        public void push(T value) {
            StackNode<T> newHeadNode = new StackNode<>(value);

            while (true) {
                StackNode<T> currentHeadNode = head.get();
                newHeadNode.next = currentHeadNode;
                //현재 값이 첫 번째 인자와 같으면 두 번째 인자로 값을 업데이트하고, 성공 여부를 boolean 값으로 반환
                if (head.compareAndSet(currentHeadNode, newHeadNode)) { //compareAndSet 연산을 통해 원자적으로 노드를 추가하거나 삭제
                    break;
                } else {
                    LockSupport.parkNanos(1); //thread.sleep 보다 더 세밀한 제어 가능
                }
            }
            counter.incrementAndGet();
        }

        public T pop() {
            StackNode<T> currentHeadNode = head.get();
            StackNode<T> newHeadNode;

            while (currentHeadNode != null) {
                newHeadNode = currentHeadNode.next;
                if (head.compareAndSet(currentHeadNode, newHeadNode)) {
                    break;
                } else {
                    LockSupport.parkNanos(1);
                    currentHeadNode = head.get();
                }
            }
            counter.incrementAndGet();
            return currentHeadNode != null ? currentHeadNode.value : null;
        }

        public int getCounter() {
            return counter.get();
        }
    }

    public static class StandardStack<T> {
        private StackNode<T> head;
        private int counter = 0;

        public synchronized void push(T value) {
            StackNode<T> newHead = new StackNode<>(value);
            newHead.next = head;
            head = newHead;
            counter++;
        }

        public synchronized T pop() {
            if (head == null) {
                counter++;
                return null;
            }

            T value = head.value;
            head = head.next;
            counter++;
            return value;
        }

        public int getCounter() {
            return counter;
        }
    }

    private static class StackNode<T> {
        public T value;
        public StackNode<T> next;

        public StackNode(T value) {
            this.value = value;
            this.next = next;
        }
    }
}
