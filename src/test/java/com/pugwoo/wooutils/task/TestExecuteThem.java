package com.pugwoo.wooutils.task;

import com.pugwoo.wooutils.lang.NumberUtils;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class TestExecuteThem {

	@Test
	public void testBasic() {
		// 默认10个线程
		ExecuteThem executeThem = new ExecuteThem();

		// 10个线程，100个任务，每个3秒，那么一共30秒可以执行完
		for(int i = 0; i < 100; i++) {
			final int fi = i;
			executeThem.add(() -> {
				Thread.sleep(3000);
				return fi;
			});
		}

		long start = System.currentTimeMillis();
		List<Object> results = executeThem.waitAllTerminate();
		long end = System.currentTimeMillis();

		long cost = end - start;
		assert cost > 30_000 && cost < 30_100;

		BigDecimal sum = NumberUtils.sum(results);
		assert sum.intValue() == 4950;
	}
	
}
