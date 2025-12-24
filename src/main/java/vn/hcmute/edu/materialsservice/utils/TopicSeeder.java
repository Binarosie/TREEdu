package vn.hcmute.edu.materialsservice.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hcmute.edu.materialsservice.models.Topic;
import vn.hcmute.edu.materialsservice.repository.TopicRepository;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopicSeeder {

    private final TopicRepository topicRepository;

    public void seedTopics() {
        if (topicRepository.count() > 0) {
            log.info("⏭️ Topics already exist, skipping topic seeding");
            return;
        }

        log.info("🌱 Seeding topics...");

        List<Topic> topics = Arrays.asList(
                Topic.builder()
                        .name("Chào hỏi")
                        .description("Các cách chào hỏi và giới thiệu cơ bản trong tiếng Việt")
                        .level("Cơ bản")
                        .sentences(Arrays.asList(
                                "Xin chào, bạn khỏe không?",
                                "Rất vui được gặp bạn.",
                                "Bạn tên là gì?",
                                "Chúc bạn một ngày tốt lành!",
                                "Hẹn gặp lại bạn."))
                        .build(),

                Topic.builder()
                        .name("Hoạt động hàng ngày")
                        .description("Từ vựng về các hoạt động sinh hoạt thường ngày")
                        .level("Cơ bản")
                        .sentences(Arrays.asList(
                                "Tôi thức dậy lúc 7 giờ sáng.",
                                "Tôi đánh răng mỗi buổi sáng.",
                                "Tôi đi làm bằng xe buýt.",
                                "Tôi ăn trưa lúc 12 giờ.",
                                "Tôi đi ngủ lúc 10 giờ tối."))
                        .build(),

                Topic.builder()
                        .name("Ẩm thực")
                        .description("Từ vựng liên quan đến đồ ăn và thức uống")
                        .level("Trung cấp")
                        .sentences(Arrays.asList(
                                "Tôi muốn một tô phở.",
                                "Món này rất ngon.",
                                "Cho tôi xem thực đơn.",
                                "Tôi ăn chay.",
                                "Tính tiền cho tôi."))
                        .build(),

                Topic.builder()
                        .name("Đi lại và Du lịch")
                        .description("Các từ và cụm từ về đi lại, du lịch")
                        .level("Trung cấp")
                        .sentences(Arrays.asList(
                                "Nhà ga xe lửa ở đâu?",
                                "Tôi cần một chiếc taxi đến sân bay.",
                                "Vé bao nhiêu tiền?",
                                "Xe buýt khởi hành lúc mấy giờ?",
                                "Tôi bị mất hành lý."))
                        .build(),

                Topic.builder()
                        .name("Công việc và Văn phòng")
                        .description("Từ vựng chuyên nghiệp trong môi trường làm việc")
                        .level("Nâng cao")
                        .sentences(Arrays.asList(
                                "Chúng ta hãy sắp xếp một cuộc họp.",
                                "Tôi sẽ gửi báo cáo qua email.",
                                "Chúng ta cần cải thiện năng suất.",
                                "Hạn chót của dự án này là khi nào?",
                                "Tôi đánh giá cao sự hợp tác của bạn."))
                        .build());

        topicRepository.saveAll(topics);
        log.info("✅ Seeded {} topics", topics.size());
    }
}
