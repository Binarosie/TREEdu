package vn.hcmute.edu.materialsservice.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.materialsservice.models.Answer;
import vn.hcmute.edu.materialsservice.models.Question;
import vn.hcmute.edu.materialsservice.models.Quiz;
import vn.hcmute.edu.materialsservice.repository.QuizRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuizSeeder {

    private final QuizRepository quizRepository;

    public void seedQuizzes() {
        if (quizRepository.count() > 0) {
            log.info("⏭️ Quizzes already exist, skipping quiz seeding");
            return;
        }

        log.info("🌱 Seeding quizzes...");

        // Quiz 1: Ngữ pháp cơ bản
        List<Question> questions1 = new ArrayList<>();
        questions1.add(Question.builder()
                .content("Chọn từ đúng để điền vào chỗ trống: 'Tôi ___ đến trường mỗi ngày.'")
                .options(Arrays.asList(
                        Answer.builder().content("đi").isCorrect(true).build(),
                        Answer.builder().content("đến").isCorrect(false).build(),
                        Answer.builder().content("về").isCorrect(false).build(),
                        Answer.builder().content("ra").isCorrect(false).build()))
                .explanation("Động từ 'đi' được dùng để chỉ hành động di chuyển từ nơi này đến nơi khác.")
                .build());

        questions1.add(Question.builder()
                .content("Từ nào sau đây là danh từ?")
                .options(Arrays.asList(
                        Answer.builder().content("Đẹp").isCorrect(false).build(),
                        Answer.builder().content("Nhà").isCorrect(true).build(),
                        Answer.builder().content("Chạy").isCorrect(false).build(),
                        Answer.builder().content("Nhanh").isCorrect(false).build()))
                .explanation("'Nhà' là danh từ chỉ địa điểm, nơi ở.")
                .build());

        questions1.add(Question.builder()
                .content("Câu nào sau đây viết đúng chính tả?")
                .options(Arrays.asList(
                        Answer.builder().content("Tôi đang học tiếng Việt.").isCorrect(false).build(),
                        Answer.builder().content("Tôi đang học tiếng Việt.").isCorrect(true).build(),
                        Answer.builder().content("Tôi đang học tiếng Việt.").isCorrect(false).build(),
                        Answer.builder().content("Tôi dang học tiếng Việt.").isCorrect(false).build()))
                .explanation("Chính tả đúng là 'Việt' với dấu sắc ở 'ê'.")
                .build());

        Quiz quiz1 = Quiz.builder()
                .title("Kiểm tra Ngữ pháp Cơ bản")
                .level(1)
                .topic("Ngữ pháp")
                .timer(300)
                .questions(questions1)
                .questionCount(questions1.size())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Quiz 2: Từ vựng
        List<Question> questions2 = new ArrayList<>();
        questions2.add(Question.builder()
                .content("Từ 'thư viện' có nghĩa là gì?")
                .options(Arrays.asList(
                        Answer.builder().content("Nơi bán sách").isCorrect(false).build(),
                        Answer.builder().content("Nơi lưu trữ và cho mượn sách").isCorrect(true).build(),
                        Answer.builder().content("Nơi in sách").isCorrect(false).build(),
                        Answer.builder().content("Nơi viết sách").isCorrect(false).build()))
                .explanation("Thư viện là nơi lưu trữ, bảo quản và cho mượn sách, tài liệu.")
                .build());

        questions2.add(Question.builder()
                .content("Chọn từ đồng nghĩa với 'vui vẻ':")
                .options(Arrays.asList(
                        Answer.builder().content("Buồn bã").isCorrect(false).build(),
                        Answer.builder().content("Giận dữ").isCorrect(false).build(),
                        Answer.builder().content("Hạnh phúc").isCorrect(true).build(),
                        Answer.builder().content("Lo lắng").isCorrect(false).build()))
                .explanation("'Hạnh phúc' là từ đồng nghĩa với 'vui vẻ'.")
                .build());

        questions2.add(Question.builder()
                .content("Từ trái nghĩa của 'khó' là gì?")
                .options(Arrays.asList(
                        Answer.builder().content("Phức tạp").isCorrect(false).build(),
                        Answer.builder().content("Dễ").isCorrect(true).build(),
                        Answer.builder().content("Rắc rối").isCorrect(false).build(),
                        Answer.builder().content("Khó khăn").isCorrect(false).build()))
                .explanation("'Dễ' là từ trái nghĩa với 'khó'.")
                .build());

        questions2.add(Question.builder()
                .content("'Khổng lồ' có nghĩa là:")
                .options(Arrays.asList(
                        Answer.builder().content("Rất nhỏ").isCorrect(false).build(),
                        Answer.builder().content("Rất lớn").isCorrect(true).build(),
                        Answer.builder().content("Trung bình").isCorrect(false).build(),
                        Answer.builder().content("Không quan trọng").isCorrect(false).build()))
                .explanation("'Khổng lồ' nghĩa là có kích thước rất to lớn.")
                .build());

        Quiz quiz2 = Quiz.builder()
                .title("Bài Kiểm Tra Từ Vựng")
                .level(2)
                .topic("Từ vựng")
                .timer(420)
                .questions(questions2)
                .questionCount(questions2.size())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Quiz 3: Đọc hiểu
        List<Question> questions3 = new ArrayList<>();
        questions3.add(Question.builder()
                .content(
                        "Đọc đoạn văn: 'Hoa đi chợ mua rau, cá và trái cây. Cô ấy không mua thịt vì đang ăn chay.' Hoa đã mua gì?")
                .options(Arrays.asList(
                        Answer.builder().content("Rau, cá, trái cây, thịt").isCorrect(false).build(),
                        Answer.builder().content("Rau, cá, trái cây").isCorrect(true).build(),
                        Answer.builder().content("Chỉ rau và trái cây").isCorrect(false).build(),
                        Answer.builder().content("Chỉ có thịt").isCorrect(false).build()))
                .explanation("Theo đoạn văn, Hoa mua rau, cá và trái cây. Cô ấy không mua thịt.")
                .build());

        questions3.add(Question.builder()
                .content("Trong câu 'Con mèo ngồi trên chiếu', chủ ngữ là gì?")
                .options(Arrays.asList(
                        Answer.builder().content("ngồi").isCorrect(false).build(),
                        Answer.builder().content("chiếu").isCorrect(false).build(),
                        Answer.builder().content("Con mèo").isCorrect(true).build(),
                        Answer.builder().content("trên").isCorrect(false).build()))
                .explanation("Chủ ngữ là 'Con mèo' - đối tượng thực hiện hành động.")
                .build());

        Quiz quiz3 = Quiz.builder()
                .title("Bài Kiểm Tra Đọc Hiểu")
                .level(2)
                .topic("Đọc hiểu")
                .timer(600)
                .questions(questions3)
                .questionCount(questions3.size())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        quizRepository.saveAll(Arrays.asList(quiz1, quiz2, quiz3));
        log.info("✅ Seeded {} quizzes with total {} questions",
                3,
                questions1.size() + questions2.size() + questions3.size());
    }
}
