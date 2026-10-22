package com.learningplatform.backend.config;

import com.learningplatform.backend.entity.QuizQuestion;
import com.learningplatform.backend.repository.QuizQuestionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fills the quiz with starter questions the first time the app runs.
 * Does nothing if questions already exist, so restarts never create duplicates.
 */
@Component
public class QuizSeeder implements ApplicationRunner {

    private final QuizQuestionRepository questionRepository;

    public QuizSeeder(QuizQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (questionRepository.count() > 0) {
            return;
        }

        questionRepository.saveAll(List.of(
            q("What is the official height of Mount Everest, announced in 2020?",
              List.of("8,849 m", "8,611 m", "8,586 m", "8,516 m"), 0,
              "Nepal and China jointly announced 8,848.86 m in 2020 after a new survey."),
            q("Which is the second-highest mountain on Earth?",
              List.of("Kangchenjunga", "K2", "Lhotse", "Makalu"), 1,
              "K2 (8,611 m) in the Karakoram is second, known as the Savage Mountain."),
            q("What does the word \"Himalaya\" mean in Sanskrit?",
              List.of("Land of the gods", "Roof of the world", "Abode of snow", "Mountain of stone"), 2,
              "Hima means snow and alaya means abode or home."),
            q("Roughly when did the Indian plate begin colliding with Asia?",
              List.of("About 5,000 years ago", "About 5 million years ago", "About 50 million years ago", "About 500 million years ago"), 2,
              "The collision began roughly 50 million years ago and is still pushing the mountains up."),
            q("Who made the first confirmed ascent of Mount Everest in 1953?",
              List.of("Edmund Hillary and Tenzing Norgay", "Reinhold Messner and Peter Habeler", "Maurice Herzog and Louis Lachenal", "George Mallory and Andrew Irvine"), 0,
              "Hillary and Tenzing reached the summit on 29 May 1953."),
            q("Who was the first woman to climb Mount Everest?",
              List.of("Bachendri Pal", "Lhakpa Sherpa", "Junko Tabei", "Arunima Sinha"), 2,
              "Junko Tabei of Japan reached the summit on 16 May 1975."),
            q("Who was the first Indian woman to climb Mount Everest?",
              List.of("Arunima Sinha", "Bachendri Pal", "Malavath Purna", "Junko Tabei"), 1,
              "Bachendri Pal reached the summit on 23 May 1984."),
            q("Which was the first 8,000-metre peak ever climbed?",
              List.of("Mount Everest", "K2", "Nanga Parbat", "Annapurna I"), 3,
              "Annapurna I was climbed in 1950, three years before Everest."),
            q("Which river begins at the Gangotri Glacier?",
              List.of("Ganga", "Indus", "Brahmaputra", "Sutlej"), 0,
              "The Ganga starts at Gaumukh, the snout of the Gangotri Glacier."),
            q("Why is the Himalayan region called the \"Third Pole\"?",
              List.of("It is the third-highest range", "It holds the largest store of ice outside the polar regions", "It lies on the third line of latitude", "It is as cold as the poles all year"), 1,
              "Its glaciers hold more ice than anywhere except the Arctic and Antarctica."),
            q("How many mountains in the world are higher than 8,000 m?",
              List.of("8", "10", "14", "20"), 2,
              "There are 14 eight-thousanders, all in the Himalaya and Karakoram."),
            q("Which sacred mountain has never been climbed?",
              List.of("Mount Kailash", "Mount Everest", "Cho Oyu", "Manaslu"), 0,
              "Mount Kailash is holy to Hindus, Buddhists, Jains and followers of Bon, and is left unclimbed out of respect."),
            q("At about what height does the \"death zone\" begin?",
              List.of("5,000 m", "6,500 m", "8,000 m", "9,000 m"), 2,
              "Above about 8,000 m there is roughly a third of the oxygen at sea level."),
            q("Who was the first person to climb all fourteen 8,000-metre peaks?",
              List.of("Nirmal Purja", "Reinhold Messner", "Edmund Hillary", "Kami Rita Sherpa"), 1,
              "Reinhold Messner completed all fourteen in 1986."),
            q("In 2019, Nirmal \"Nims\" Purja climbed all fourteen 8,000-metre peaks in about how long?",
              List.of("1 month", "6 months", "2 years", "8 years"), 1,
              "He did it in 6 months and 6 days; the previous record was nearly eight years."),
            q("What does the name Kangchenjunga usually mean?",
              List.of("Turquoise Goddess", "White Mountain", "Five Treasures of the Snow", "Naked Mountain"), 2,
              "It is named after its five high summits."),
            q("What does Cho Oyu mean in Tibetan?",
              List.of("Turquoise Goddess", "South Peak", "Mountain of the Spirit", "Mother of the World"), 0,
              "Cho Oyu, the sixth-highest mountain, is the Turquoise Goddess."),
            q("Which trek crosses the Thorong La pass (5,416 m)?",
              List.of("Everest Base Camp", "Annapurna Circuit", "Markha Valley", "Hampta Pass"), 1,
              "Thorong La is the high point of the Annapurna Circuit."),
            q("Which of these treks is best during the monsoon, because it lies in a rain shadow?",
              List.of("Everest Base Camp", "Kedarkantha", "Markha Valley", "Annapurna Base Camp"), 2,
              "Ladakh, where Markha Valley lies, stays dry while the southern slopes get heavy rain."),
            q("Which big cat is often called the \"ghost of the mountains\"?",
              List.of("Snow leopard", "Clouded leopard", "Bengal tiger", "Asian lion"), 0,
              "Snow leopards are so elusive in the high Himalaya that they are rarely seen.")
        ));
    }

    private static QuizQuestion q(String text, List<String> options, int correct, String explanation) {
        return new QuizQuestion(text, options, correct, explanation);
    }
}
