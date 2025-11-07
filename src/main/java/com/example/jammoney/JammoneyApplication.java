package com.example.jammoney;

import com.example.jammoney.news.crawler.FinanceNewsCrawler;
import com.example.jammoney.news.dto.NewsRequestDto;
import com.example.jammoney.news.service.NewsService;
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
	public CommandLineRunner run(FinanceNewsCrawler crawler, NewsService newsService) {
		return args -> {
			List<NewsRequestDto> newsList = crawler.fetchTodayNews();
			System.out.println("크롤링된 뉴스 개수: " + newsList.size());
			for (NewsRequestDto dto : newsList) {
				System.out.println("제목: " + dto.getTitle());
				System.out.println("출처: " + dto.getSource());
				System.out.println("날짜: " + dto.getPublishDate());

				// 본문 일부 출력
				String snippet = dto.getContent().length() > 100
						? dto.getContent().substring(0, 100) + "..."
						: dto.getContent();
				System.out.println("내용: " + snippet);

				// 요약 기능 제거됨
				System.out.println("------");
			}
      newsService.saveNewsList(newsList);
		};
	}
}
