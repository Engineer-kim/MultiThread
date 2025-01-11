package LockingAndDeadLock;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReEntrantReadAndWrite {
    public static final int HIGHEST_PRICE = 1000;

    public static void main(String[] args) throws InterruptedException {
        // InventoryDatabase 인스턴스 생성
        InventoryDatabase inventoryDatabase = new InventoryDatabase();

        Random random = new Random();
        // 초기 아이템 추가
        for (int i = 0; i < 100000; i++) {
            inventoryDatabase.addItem(random.nextInt(HIGHEST_PRICE));
        }

        // 쓰기 작업을 수행할 쓰레드 생성
        Thread writer = new Thread(() -> {
            while (true) {
                // 랜덤 가격으로 아이템 추가 및 제거
                inventoryDatabase.addItem(random.nextInt(HIGHEST_PRICE));
                inventoryDatabase.removeItem(random.nextInt(HIGHEST_PRICE));
                try {
                    Thread.sleep(10); // 잠시 대기
                } catch (InterruptedException e) {
                    // 예외 처리
                }
            }
        });

        writer.setDaemon(true); // 데몬 쓰레드로 설정(주 쓰레드 죽으면 종료 , 주로 네트워크 감시나 로깅할때)
        writer.start(); // 쓰레드 시작

        int numberOfReaderThreads = 7; // 읽기 쓰레드 수
        List<Thread> readers = new ArrayList<>();

        // 읽기 쓰레드 생성
        for (int readerIndex = 0; readerIndex < numberOfReaderThreads; readerIndex++) {
            Thread reader = new Thread(() -> {
                for (int i = 0; i < 100000; i++) {
                    int upperBoundPrice = random.nextInt(HIGHEST_PRICE);
                    int lowerBoundPrice = upperBoundPrice > 0 ? random.nextInt(upperBoundPrice) : 0;
                    inventoryDatabase.getNumberOfItemsInPriceRange(lowerBoundPrice, upperBoundPrice);
                }
            });

            reader.setDaemon(true); // 데몬 쓰레드로 설정
            readers.add(reader); // 리스트에 추가
        }

        long startReadingTime = System.currentTimeMillis(); // 읽기 시작 시간 기록
        for (Thread reader : readers) {
            reader.start(); // 읽기 쓰레드 시작
        }

        for (Thread reader : readers) {
            reader.join(); // 모든 읽기 쓰레드가 종료될 때까지 대기
        }

        long endReadingTime = System.currentTimeMillis(); // 읽기 종료 시간 기록

        // 읽기 시간 출력
        System.out.println(String.format("Reading took %d ms", endReadingTime - startReadingTime));
    }

    public static class InventoryDatabase {
        // 가격과 아이템 수를 저장하는 TreeMap
        //TreeMap  이진 검색 트리를 사용하여 키-값 쌍을 저장 , 제공된 Comparator에 따라 정렬
        private TreeMap<Integer, Integer> priceToCountMap = new TreeMap<>();
        // 읽기-쓰기 락
        private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        private Lock readLock = reentrantReadWriteLock.readLock(); // 읽기 락
        private Lock writeLock = reentrantReadWriteLock.writeLock(); // 쓰기 락

        // 주어진 가격 범위 내 아이템 수를 반환하는 메서드
        public int getNumberOfItemsInPriceRange(int lowerBound, int upperBound) {
            readLock.lock(); // 읽기 락 획득
            try {
                // 가격 범위의 시작과 끝 키 찾기
                Integer fromKey = priceToCountMap.ceilingKey(lowerBound);
                Integer toKey = priceToCountMap.floorKey(upperBound);

                if (fromKey == null || toKey == null) {
                    return 0; // 범위에 해당하는 아이템이 없으면 0 반환
                }

                // 가격 범위에 해당하는 서브맵 생성
                NavigableMap<Integer, Integer> rangeOfPrices = priceToCountMap.subMap(fromKey, true, toKey, true);
                // NavigableMap 정렬된 키에 대한 다양한 탐색 메서드를 제공합니다. subMap, higherKey, lowerKey, ceilingKey, floorKey

                int sum = 0;
                // 가격 범위 내 아이템 수 합산
                for (int numberOfItemsForPrice : rangeOfPrices.values()) {
                    sum += numberOfItemsForPrice;
                }

                return sum; // 총합 반환
            } finally {
                readLock.unlock(); // 읽기 락 해제
            }
        }

        // 아이템 추가 메서드
        public void addItem(int price) {
            writeLock.lock(); // 쓰기 락 획득
            try {
                Integer numberOfItemsForPrice = priceToCountMap.get(price);
                if (numberOfItemsForPrice == null) {
                    priceToCountMap.put(price, 1); // 새로운 가격이면 1로 초기화
                } else {
                    priceToCountMap.put(price, numberOfItemsForPrice + 1); // 기존 가격이면 수량 증가
                }
            } finally {
                writeLock.unlock(); // 쓰기 락 해제
            }
        }

        // 아이템 제거 메서드
        public void removeItem(int price) {
            writeLock.lock(); // 쓰기 락 획득
            try {
                Integer numberOfItemsForPrice = priceToCountMap.get(price);
                if (numberOfItemsForPrice == null || numberOfItemsForPrice == 1) {
                    priceToCountMap.remove(price); // 아이템 수가 1 이하이면 제거
                } else {
                    priceToCountMap.put(price, numberOfItemsForPrice - 1); // 수량 감소
                }
            } finally {
                writeLock.unlock(); // 쓰기 락 해제
            }
        }
    }
}
