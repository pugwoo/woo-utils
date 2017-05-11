package com.pugwoo.wooutils.task;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class TestExecuteThem {

	public static void main(String[] args) {
		ExecuteThem executeThem = new ExecuteThem();
		
		for(int i = 0; i < 100; i++) {
			final int fi = i;
			executeThem.add(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Thread.sleep(3000);
					return fi;
				}
			});
		}
		
		System.out.println("waiting:" + new Date());
		List<Object> results = executeThem.waitAllTerminate();
		System.out.println("end:" + new Date());
		for(Object result : results) {
		    System.out.println(result);
		}
	}
	
}
