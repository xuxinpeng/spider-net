package com.boundary.aaron.spider.recruit.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { RecruitApplication.class })
public class RecruitSpiderTest {

	@Autowired
	RecruitSpiderService service;

	@Test
	public void test1() {
		System.out.println(service);
	}

}
