package com.example.jammoney;

import com.example.jammoney.news.crawler.FinanceNewsCrawler;
import com.example.jammoney.news.dto.NewsRequestDto;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class JammoneyApplication {

	public static void main(String[] args) {
		SpringApplication.run(JammoneyApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(FinanceNewsCrawler crawler) {
		return args -> {
			List<NewsRequestDto> newsList = crawler.fetchTodayNews();
			System.out.println("크롤링된 뉴스 개수: " + newsList.size());
			for (NewsRequestDto dto : newsList) {
				System.out.println("제목: " + dto.getTitle());
				System.out.println("출처: " + dto.getSource());
				System.out.println("날짜: " + dto.getPublishDate());
				System.out.println("내용: " + (dto.getContent().length() > 100 ? dto.getContent().substring(0, 100) + "..." : dto.getContent()));
				System.out.println("------");
			}
		};
	}
}
