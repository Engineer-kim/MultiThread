package ResourcesAndCriticalSection;

public class Resource {

    public static void main(String[] args) throws InterruptedException {

        InventoryCounter inventoryCounter = new InventoryCounter();
        IncrementingThread incrementingThread = new IncrementingThread(inventoryCounter);
        DecrementingThread decrementingThread = new DecrementingThread(inventoryCounter);

        incrementingThread.start();
        decrementingThread.start();
        incrementingThread.join(); //종료될 때까지 대기하게 만듬
        decrementingThread.join();

        System.out.println("We currently have " + inventoryCounter.getItem() + " items");
    }

    
    //1번 쓰레드
    public static class DecrementingThread extends Thread {

        private InventoryCounter inventoryCounter;

        public DecrementingThread(InventoryCounter inventoryCounter) {
            this.inventoryCounter = inventoryCounter;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10000; i++) {
                inventoryCounter.Decrease();
            }
        }
    }
    
    //2번 쓰레드
    public static class IncrementingThread extends Thread {

        private InventoryCounter inventoryCounter;

        public IncrementingThread(InventoryCounter inventoryCounter) {
            this.inventoryCounter = inventoryCounter;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10000; i++) {
                inventoryCounter.Increase();
            }
        }
    }


    //사용할 객체 및 증감함수
    private static class InventoryCounter{
        private int item = 0;

        Object lock = new Object();//lock 객체를 획득하여 임계 영역에 있다면, 다른 쓰레드는 lock 획득을 위해 대기


        public void  Increase(){  //synchronized 를 쓰면 쓰레드간 상태 동기화가되기때문에 동일한 값보장 가능
            synchronized (this.lock){
                item++;
            }
        }

        public void Decrease(){
            synchronized (this.lock){  //필요한 부분만 동기화할 수 있어 성능을 향상
                item--;
            }
        }

        public int getItem(){
            synchronized (this.lock) {
                return item;
            }
        }
    }
}
