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

                // ── CƠ BẢN ────────────────────────────────────────────────────────────

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
                        .name("Đại từ nhân xưng")
                        .description("Hệ thống đại từ xưng hô trong tiếng Việt theo tuổi tác và mối quan hệ")
                        .level("Cơ bản")
                        .sentences(Arrays.asList(
                                "Tôi là sinh viên.",
                                "Bạn có thể giúp tôi không?",
                                "Anh ấy đang học tiếng Việt.",
                                "Chúng tôi đến từ Nhật Bản.",
                                "Họ là bạn bè của tôi."))
                        .build(),

                Topic.builder()
                        .name("Gia đình")
                        .description("Từ vựng về các thành viên trong gia đình và cách xưng hô")
                        .level("Cơ bản")
                        .sentences(Arrays.asList(
                                "Gia đình tôi có bốn người.",
                                "Bố tôi làm kỹ sư.",
                                "Mẹ tôi nấu ăn rất ngon.",
                                "Em gái tôi đang học lớp mười.",
                                "Ông bà tôi sống ở quê."))
                        .build(),

                Topic.builder()
                        .name("Màu sắc")
                        .description("Tên các màu sắc cơ bản và cách dùng trong miêu tả")
                        .level("Cơ bản")
                        .sentences(Arrays.asList(
                                "Bầu trời màu xanh dương.",
                                "Quả táo này màu đỏ.",
                                "Áo của bạn màu gì?",
                                "Tôi thích màu vàng và màu cam.",
                                "Lá cây mùa thu chuyển sang màu nâu."))
                        .build(),

                Topic.builder()
                        .name("Con số và Thời gian")
                        .description("Cách đọc số, giờ, ngày tháng và các đơn vị thời gian")
                        .level("Cơ bản")
                        .sentences(Arrays.asList(
                                "Bây giờ là mấy giờ?",
                                "Hôm nay là ngày mùng hai tháng chín.",
                                "Tôi thức dậy lúc sáu giờ rưỡi.",
                                "Cuộc họp bắt đầu lúc tám giờ sáng.",
                                "Tháng sau tôi sẽ về quê."))
                        .build(),

                Topic.builder()
                        .name("Hoạt động hàng ngày")
                        .description("Từ vựng về các hoạt động sinh hoạt thường ngày")
                        .level("Cơ bản")
                        .sentences(Arrays.asList(
                                "Tôi thức dậy lúc bảy giờ sáng.",
                                "Tôi đánh răng mỗi buổi sáng.",
                                "Tôi đi làm bằng xe buýt.",
                                "Tôi ăn trưa lúc mười hai giờ.",
                                "Tôi đi ngủ lúc mười giờ tối."))
                        .build(),

                Topic.builder()
                        .name("Đồ vật trong nhà")
                        .description("Tên các đồ vật thông dụng trong nhà và cách sử dụng")
                        .level("Cơ bản")
                        .sentences(Arrays.asList(
                                "Cái bàn này làm bằng gỗ.",
                                "Tivi đặt ở phòng khách.",
                                "Bạn có thể tắt đèn giúp tôi không?",
                                "Tủ lạnh để thức ăn luôn tươi.",
                                "Chìa khóa của tôi để trên ghế."))
                        .build(),

                Topic.builder()
                        .name("Động vật và Thiên nhiên")
                        .description("Tên các loài động vật phổ biến và các yếu tố thiên nhiên")
                        .level("Cơ bản")
                        .sentences(Arrays.asList(
                                "Con chó của tôi rất dễ thương.",
                                "Trời hôm nay nhiều mây.",
                                "Sông Mekong rất dài và rộng.",
                                "Mùa xuân hoa nở rất đẹp.",
                                "Chim én bay về báo hiệu mùa xuân."))
                        .build(),

                // ── TRUNG CẤP ─────────────────────────────────────────────────────────

                Topic.builder()
                        .name("Ẩm thực")
                        .description("Từ vựng liên quan đến đồ ăn, thức uống và nhà hàng")
                        .level("Trung cấp")
                        .sentences(Arrays.asList(
                                "Tôi muốn một tô phở bò tái.",
                                "Món này cay quá, bạn có thể bớt ớt không?",
                                "Cho tôi xem thực đơn.",
                                "Tôi ăn chay nên không ăn thịt.",
                                "Tính tiền cho tôi, cảm ơn."))
                        .build(),

                Topic.builder()
                        .name("Đi lại và Du lịch")
                        .description("Các từ và cụm từ về đi lại, hỏi đường và du lịch")
                        .level("Trung cấp")
                        .sentences(Arrays.asList(
                                "Nhà ga xe lửa ở đâu?",
                                "Tôi cần một chiếc taxi đến sân bay.",
                                "Vé một chiều giá bao nhiêu tiền?",
                                "Xe buýt số mấy đi về trung tâm?",
                                "Tôi bị mất hành lý, tôi phải làm gì?"))
                        .build(),

                Topic.builder()
                        .name("Mua sắm")
                        .description("Từ vựng về mua bán, trả giá và các loại cửa hàng")
                        .level("Trung cấp")
                        .sentences(Arrays.asList(
                                "Cái áo này giá bao nhiêu?",
                                "Bạn có size lớn hơn không?",
                                "Tôi muốn đổi màu khác.",
                                "Ở đây có giảm giá không?",
                                "Tôi trả bằng thẻ được không?"))
                        .build(),

                Topic.builder()
                        .name("Cảm xúc và Tính cách")
                        .description("Từ vựng diễn tả tâm trạng, cảm xúc và đặc điểm tính cách")
                        .level("Trung cấp")
                        .sentences(Arrays.asList(
                                "Hôm nay tôi cảm thấy rất vui.",
                                "Anh ấy là người rất tốt bụng.",
                                "Tôi lo lắng về kỳ thi sắp tới.",
                                "Cô ấy rất kiên nhẫn với học sinh.",
                                "Đừng buồn, mọi chuyện rồi sẽ ổn."))
                        .build(),

                Topic.builder()
                        .name("Sức khỏe và Bệnh viện")
                        .description("Từ vựng về triệu chứng, khám bệnh và chăm sóc sức khỏe")
                        .level("Trung cấp")
                        .sentences(Arrays.asList(
                                "Tôi bị đau đầu từ sáng đến giờ.",
                                "Tôi muốn đặt lịch khám bác sĩ.",
                                "Thuốc này uống mấy viên một ngày?",
                                "Bạn có bị dị ứng thuốc gì không?",
                                "Tôi cần nghỉ ngơi thêm vài ngày."))
                        .build(),

                Topic.builder()
                        .name("Giáo dục và Học tập")
                        .description("Từ vựng trong môi trường học đường và các hoạt động học tập")
                        .level("Trung cấp")
                        .sentences(Arrays.asList(
                                "Thư viện đóng cửa lúc mấy giờ?",
                                "Tôi cần nộp bài tập vào ngày mai.",
                                "Bạn có thể giải thích lại phần này không?",
                                "Kỳ thi học kỳ sẽ diễn ra vào tuần tới.",
                                "Giáo viên dạy rất dễ hiểu."))
                        .build(),

                // ── NÂNG CAO ──────────────────────────────────────────────────────────

                Topic.builder()
                        .name("Công việc và Văn phòng")
                        .description("Từ vựng chuyên nghiệp trong môi trường làm việc và kinh doanh")
                        .level("Nâng cao")
                        .sentences(Arrays.asList(
                                "Chúng ta hãy sắp xếp một cuộc họp vào thứ Hai.",
                                "Tôi sẽ gửi báo cáo qua email trước năm giờ.",
                                "Chúng ta cần cải thiện năng suất làm việc.",
                                "Hạn chót của dự án này là khi nào?",
                                "Tôi đánh giá cao sự hợp tác của cả nhóm."))
                        .build(),

                Topic.builder()
                        .name("Kinh tế và Tài chính")
                        .description("Từ vựng về kinh tế vĩ mô, đầu tư và quản lý tài chính cá nhân")
                        .level("Nâng cao")
                        .sentences(Arrays.asList(
                                "Lạm phát đang ảnh hưởng đến sức mua của người dân.",
                                "Công ty đã đạt doanh thu kỷ lục trong quý này.",
                                "Tôi muốn mở tài khoản tiết kiệm có kỳ hạn.",
                                "Thị trường chứng khoán biến động mạnh tuần qua.",
                                "Cần đa dạng hóa danh mục đầu tư để giảm rủi ro."))
                        .build(),

                Topic.builder()
                        .name("Môi trường và Xã hội")
                        .description("Từ vựng về các vấn đề môi trường, xã hội và phát triển bền vững")
                        .level("Nâng cao")
                        .sentences(Arrays.asList(
                                "Biến đổi khí hậu là thách thức toàn cầu.",
                                "Chúng ta cần giảm thiểu rác thải nhựa.",
                                "Năng lượng tái tạo đang phát triển mạnh.",
                                "Bất bình đẳng xã hội cần được giải quyết.",
                                "Mỗi cá nhân đều có trách nhiệm bảo vệ môi trường."))
                        .build(),

                Topic.builder()
                        .name("Tư duy phản biện và Học thuật")
                        .description("Từ vựng dùng trong thảo luận học thuật, lập luận và nghiên cứu")
                        .level("Nâng cao")
                        .sentences(Arrays.asList(
                                "Lập luận này thiếu bằng chứng thuyết phục.",
                                "Chúng ta cần phân tích vấn đề từ nhiều góc độ.",
                                "Giả thuyết cần được kiểm chứng bằng thực nghiệm.",
                                "Kết luận này mâu thuẫn với dữ liệu đã trình bày.",
                                "Cần phân biệt rõ sự kiện và quan điểm cá nhân."))
                        .build()
        );

        topicRepository.saveAll(topics);
        log.info("✅ Seeded {} topics successfully!", topics.size());
    }
}