# Deploy Pattern Store lên Google Cloud (Cloud Run + Cloud SQL)

Kiến trúc khi deploy:

```
Trình duyệt ──HTTPS──> Cloud Run (container Spring Boot, profile "prod")
                            │  Cloud SQL Java Connector (không mở IP công khai)
                            ▼
                       Cloud SQL for PostgreSQL 16 (shop-db)
Mật khẩu DB và mật khẩu admin nằm trong Secret Manager, không nằm trong mã nguồn.
```

Chạy tất cả lệnh dưới đây trong **Cloud Shell** (biểu tượng `>_` trên console.cloud.google.com),
không cần cài gì lên máy.

## 0. Lấy mã nguồn và đặt biến

```bash
git clone https://github.com/phamhuynhtin260120/shop.git
cd shop
gcloud config set project <PROJECT_ID>          # thay bằng ID project của bạn
PROJECT_ID=$(gcloud config get-value project)
REGION=asia-southeast1                          # Singapore, gần Việt Nam nhất
INSTANCE=shop-db
```

## 1. Bật các API cần dùng

```bash
gcloud services enable run.googleapis.com sqladmin.googleapis.com \
  cloudbuild.googleapis.com artifactregistry.googleapis.com secretmanager.googleapis.com
```

## 2. Tạo Cloud SQL PostgreSQL (mất 5–10 phút)

```bash
gcloud sql instances create $INSTANCE \
  --database-version=POSTGRES_16 --edition=ENTERPRISE --tier=db-f1-micro \
  --region=$REGION --storage-size=10 --no-backup

gcloud sql databases create shopdb --instance=$INSTANCE

DB_PASS=$(openssl rand -base64 18)
gcloud sql users create shop --instance=$INSTANCE --password="$DB_PASS"
```

## 3. Lưu mật khẩu vào Secret Manager

```bash
printf %s "$DB_PASS" | gcloud secrets create shop-db-password --data-file=-

read -s -p "Nhập mật khẩu cho tài khoản admin@patternstore.vn: " ADMIN_PASS; echo
printf %s "$ADMIN_PASS" | gcloud secrets create shop-admin-password --data-file=-
```

## 4. Cấp quyền cho service account mặc định

```bash
PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format='value(projectNumber)')
SA=$PROJECT_NUMBER-compute@developer.gserviceaccount.com
for ROLE in roles/cloudsql.client roles/secretmanager.secretAccessor roles/cloudbuild.builds.builder; do
  gcloud projects add-iam-policy-binding $PROJECT_ID --member=serviceAccount:$SA --role=$ROLE --condition=None
done
```

## 5. Build và deploy (5–8 phút lần đầu)

```bash
gcloud run deploy shop --source . --region=$REGION --allow-unauthenticated \
  --memory=1Gi --cpu=1 --cpu-boost --min-instances=0 --max-instances=1 \
  --add-cloudsql-instances=$PROJECT_ID:$REGION:$INSTANCE \
  --set-env-vars=SPRING_PROFILES_ACTIVE=prod,DB_INSTANCE=$PROJECT_ID:$REGION:$INSTANCE \
  --set-secrets=DB_PASSWORD=shop-db-password:latest,ADMIN_PASSWORD=shop-admin-password:latest
```

Kết thúc, lệnh in ra `Service URL: https://shop-xxxxx.asia-southeast1.run.app`.
Lần đầu mở sẽ chậm khoảng 15–30 giây (container khởi động, tạo bảng và dữ liệu mẫu).

- `--max-instances=1`: phiên đăng nhập lưu trong bộ nhớ, chỉ chạy 1 container để không mất phiên.
- `--min-instances=0`: không có người truy cập thì container tắt, gần như không tốn tiền Cloud Run.

## Cập nhật phiên bản mới

```bash
cd ~/shop && git pull
gcloud run deploy shop --source . --region=asia-southeast1
```

## Xem log khi có lỗi

```bash
gcloud run services logs read shop --region=asia-southeast1 --limit=100
```

## Chi phí và dọn dẹp

- Cloud SQL `db-f1-micro` tốn khoảng 9–10 USD/tháng khi đang chạy. Cloud Run gần như miễn phí với lượng truy cập nhỏ.
- Tạm dừng database sau buổi chấm (dữ liệu vẫn giữ):
  `gcloud sql instances patch shop-db --activation-policy=NEVER`
- Bật lại: `gcloud sql instances patch shop-db --activation-policy=ALWAYS`
- Xóa hẳn: `gcloud run services delete shop --region=asia-southeast1` và `gcloud sql instances delete shop-db`
