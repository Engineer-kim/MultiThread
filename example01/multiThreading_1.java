package example01;

public class multiThreading_1 {


    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(new Runnable(){ //Thread 객체 생성 시 Runnable 객체를 인자로 전달하여 스레드와 작업을 연결
            @Override
            public void run(){
                //Code that will run in a new thread
                System.out.println("처음");
                System.out.println("Current " + Thread.currentThread().getPriority());
                throw new RuntimeException("일부러 오류냄");
            }
        });

        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("A critical error happend in thread"  + t.getName()
                + " the error is" + e.getMessage());
            }
        });


        thread.setName("new Worker Thread");
        thread.setPriority(Thread.MAX_PRIORITY);

        System.out.println("2번째" );
        thread.start();// thread.start()를 호출해야 실제로 스레드가 실행됨
        System.out.println("3번째" );
        Thread.sleep(10000);// 이시간이 지날때까지는 현재 스레드를 스케줄링하지 않는다
    }


}
