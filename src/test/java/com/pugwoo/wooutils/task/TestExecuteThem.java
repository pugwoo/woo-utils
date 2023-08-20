package com.pugwoo.wooutils.task;

import com.pugwoo.wooutils.collect.ListUtils;
import com.pugwoo.wooutils.lang.NumberUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class TestExecuteThem {

	@Test
	public void testBasic() {
		// 默认10个线程
		ExecuteThem executeThem = new ExecuteThem();
		List<Future<Integer>> futures = new ArrayList<>();

		// 10个线程，100个任务，每个3秒，那么一共30秒可以执行完
		for(int i = 0; i < 100; i++) {
			final int fi = i;
			futures.add(executeThem.add(() -> {
				Thread.sleep(3000);
				return fi;
			}));
		}

		long start = System.currentTimeMillis();
		executeThem.waitAllTerminate();
		long end = System.currentTimeMillis();

		long cost = end - start;
		assert cost > 30_000 && cost < 30_100;

		BigDecimal sum = NumberUtils.sum(ListUtils.transform(futures, o -> {
			try {
				return o.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}));
		assert sum.intValue() == 4950;
	}

	@Test
	public void testBlock() {
		// 默认10个线程，最长等待队列长度是5
		ExecuteThem executeThem = new ExecuteThem(10, 5);

		// 30个任务，每个3秒
		// 添加了15个之后（10个执行5个等待中）等待3秒
		// 会等待前10个执行完，然后再加10个，再等待3秒
		// 最后再加5个，就完成了，一共等待了6秒
		long start = System.currentTimeMillis();

		for(int i = 0; i < 30; i++) {
			final int fi = i;
			executeThem.add(() -> {
				Thread.sleep(3000);
				return fi;
			});
		}

		long end = System.currentTimeMillis();

		long cost = end - start;
		assert cost > 6000 && cost < 6050;

		// 这个最后最后一批执行，3秒完成
		start = System.currentTimeMillis();
		executeThem.waitAllTerminate();
		end = System.currentTimeMillis();
		cost = end - start;
		assert cost > 3000 && cost < 3020;

	}
	
}
