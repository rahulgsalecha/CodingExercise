import java.util.concurrent.locks.Condition; 
import java.util.concurrent.locks.Lock; 
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList; 
import java.util.Queue; 
import java.util.Random; 

class Solution {
    public static void main(String[] args) {
        final int ProdCount = 10;
        final int ConsCount = 2;

        ProdConsClass pc = new ProdConsClass();
        Producer p = new Producer(pc, ProdCount, "Producer");
        Consumer c1 = new Consumer(pc, ConsCount, "Consumer 1");
        Consumer c2 = new Consumer(pc, ConsCount, "Consumer 2");
        Consumer c3 = new Consumer(pc, ConsCount, "Consumer 3");

        p.start();
        c1.start();
        c2.start();
        c3.start();
    }
}

class ProdConsClass {
    private static final int CAPACITY = 10;
    private final Queue queue = new LinkedList<>();
    private final Random rand = new Random();
    private final Lock mutex = new ReentrantLock();
    private final Condition queueNotFull = mutex.newCondition();
    private final Condition queueNotEmpty = mutex.newCondition();
    
    public void push() throws InterruptedException {
        mutex.lock();
        try { 
            while (queue.size() == CAPACITY) {
                System.out.println(Thread.currentThread().getName() + " : Queue is full, waiting"); 
                queueNotEmpty.await(); 
            } 
            
            Integer number = rand.nextInt(); 
            boolean isAdded = queue.offer(number); 
            if (isAdded) { 
                System.out.printf("%s added %d into queue %n", Thread.currentThread().getName(), number); 
                System.out.println(Thread.currentThread().getName() + " : Notifying that queue is no more empty now"); 
                queueNotFull.signalAll(); 
            } 
        }
        finally { 
            mutex.unlock(); 
        }
    }

    public void remove() throws InterruptedException {
        mutex.lock();
        try {
            while (queue.size() == 0) {
                System.out.println(Thread.currentThread().getName() + " : Queue is empty, waiting");
                queueNotFull.await();
            }
            Integer value = (Integer)queue.poll();
            if (value != null) {
                System.out.printf("%s consumed %d from queue %n", Thread.currentThread().getName(), value);
                System.out.println(Thread.currentThread().getName() + " : Notifying that queue may be empty now");
                queueNotEmpty.signalAll();
            }
        } finally {
            mutex.unlock();
        }
    }
}

class Producer extends Thread {
    ProdConsClass pc;
    int count;

    public Producer(ProdConsClass pc, int count, String name) {
        super(name);
        this.pc = pc;
        this.count = count;
    }

    @Override
    public void run() {
        try {
            for(int i=0;i<count;++i){                
                pc.push();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Consumer extends Thread {
    ProdConsClass pc;
    int count;

    public Consumer(ProdConsClass pc, int count, String name) {
        super(name);
        this.pc = pc;
        this.count = count;
    }

    @Override
    public void run() {
        try {
            for(int i=0; i<count; ++i){
                pc.remove();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


