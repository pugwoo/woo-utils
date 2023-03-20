package com.pugwoo.wooutils.collect;

import org.junit.jupiter.api.Test;

import java.util.*;

public class TestSortingUtils {

	public static class StudentSortDO {
		private String name;
		private Integer age;

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Integer getAge() {
			return age;
		}
		public void setAge(Integer age) {
			this.age = age;
		}
	}

	/**测试排序*/
	@Test
	public void testSort() {
		// 创建随机10000条记录
		List<StudentSortDO> list = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			StudentSortDO d = new StudentSortDO();
			d.setName(UUID.randomUUID().toString());
			d.setAge(new Random().nextInt(100));
			list.add(d);
		}

		ListUtils.sortAscNullLast(list, StudentSortDO::getAge, StudentSortDO::getName);

		// 检查数据
		for (int i = 0; i < 10000 - 1; i++) {
			StudentSortDO d1 = list.get(i);
			StudentSortDO d2 = list.get(i + 1);
			if (Objects.equals(d1.getAge(), d2.getAge())) {
				assert d1.getName().compareTo(d2.getName()) <= 0;
			} else {
				assert d1.getAge() < d2.getAge();
			}
		}

		// 逆序排一下
		ListUtils.sortDescNullLast(list, StudentSortDO::getAge, StudentSortDO::getName);

		// 检查数据
		for (int i = 0; i < 10000 - 1; i++) {
			StudentSortDO d1 = list.get(i);
			StudentSortDO d2 = list.get(i + 1);
			if (Objects.equals(d1.getAge(), d2.getAge())) {
				assert d1.getName().compareTo(d2.getName()) >= 0;
			} else {
				assert d1.getAge() > d2.getAge();
			}
		}
	}

    // 原生jdk1.8的排序写法
	public void testJdk() {
		// 先定义一个comparator
		Comparator<Map<String, Object>> mapComparator = Comparator
				.<Map<String, Object>>nullsLast((o1, o2) -> 0)
				.thenComparing(o -> Integer.valueOf(o.get("a").toString()), Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(o -> Integer.valueOf(o.get("a").toString()), Comparator.nullsLast(Comparator.naturalOrder()));

		// 然后就可以用这个去排序了
		// Collections.sort();
	}
}
