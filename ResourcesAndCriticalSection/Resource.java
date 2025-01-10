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

        public void  Increase(){  //synchronized 를 쓰면 쓰레드간 상태 동기화가되기때문에 동일한 값보장 가능 
            item++;
        }

        public void Decrease(){
            item--;
        }

        public int getItem(){
            return item;
        }
    }
}
