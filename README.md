
## 1. Branch Naming Rules
Khi phát triển một chức năng mới, bắt buộc tạo branch theo cú pháp: feature(service name)-feature name

**Ví dụ:**
feat(auth)-authen
## 2. Quy trình tạo Pull Request (PR)
### 2.1. Tạo branch chức năng
Luôn tạo branch từ nhánh `dev` đã được cập nhật mới nhất:

git checkout dev

git pull origin dev

git checkout -b feat(service)-featureName

### 2.2. Commit và push code
git add .

git commit -m "feat: mô tả chức năng"

git push -u origin feat(service)-featureName

### 2.3. Tạo Pull Request vào nhánh dev
Base branch: dev

Compare branch: feat(service)-featureName

Nội dung PR yêu cầu:

Mô tả chức năng

Thay đổi API / Model / DB (nếu có)

Lý do thay đổi

Hình minh họa hoặc ví dụ request/response (tùy chọn)

### 2.4. Review và Approve
PR chỉ được merge khi đã được reviewer approve.

Nếu reviewer yêu cầu chỉnh sửa, tiếp tục commit và push lên cùng branch.

### 2.5. Merge vào dev
Sau khi được approve, tiến hành merge theo workflow của team

## 3. Cập nhật và làm việc với các chức năng tiếp theo
### 3.1. Cập nhật nhánh dev sau khi merge
git checkout dev

git pull origin dev

### 3.2. Tạo branch chức năng mới từ dev

git checkout -b feat(service)-newFeature

### 3.3. Đồng bộ với nhánh dev khi có thay đổi
Trong quá trình làm việc, nếu nhánh dev được cập nhật, cần merge vào branch hiện tại:

git checkout dev

git pull origin dev

git checkout feat(service)-currentFeature

git merge dev