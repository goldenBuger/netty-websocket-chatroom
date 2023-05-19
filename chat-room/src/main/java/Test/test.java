package Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class test {

    public static void main(String[] args) throws InterruptedException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 创建一个Date对象，表示当前时间
        Date now = new Date();
        // 使用SimpleDateFormat对象格式化当前时间
        String currentTime = sdf.format(now);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(11, 20, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        for (int i = 0; i < 10; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("hello,现在时间是" + currentTime + "现在的线程数量是：" + executor.getPoolSize());
                }
            });
        }
    }

}
