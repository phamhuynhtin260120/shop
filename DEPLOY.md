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

DB_PASS=$(openssl rand -base64 18 | tr -d '/+=')
gcloud sql users create shop --instance=$INSTANCE --password="$DB_PASS"
```

## 3. Lưu mật khẩu vào Secret Manager

```bash
printf %s "$DB_PASS" | gcloud secrets create shop-db-password --data-file=-

read -s -p "Nhập mật khẩu cho tài khoản admin@patternstore.vn: " ADMIN_PASS; echo
printf %s "$ADMIN_PASS" | gcloud secrets create shop-admin-password --data-file=-
```

## 4. Tạo service account riêng cho app (chỉ có đúng quyền cần dùng)

Không dùng service account mặc định, vì nó dùng chung với các service khác trong project.

```bash
gcloud iam service-accounts create shop-run --display-name="Pattern Store Cloud Run"
SA=shop-run@$PROJECT_ID.iam.gserviceaccount.com
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:$SA --role=roles/cloudsql.client --condition=None
for S in shop-db-password shop-admin-password; do
  gcloud secrets add-iam-policy-binding $S --member=serviceAccount:$SA --role=roles/secretmanager.secretAccessor
done
```

Tạo thêm service account riêng để **build** (Cloud Build đọc mã nguồn, ghi image):

```bash
gcloud iam service-accounts create shop-build --display-name="Pattern Store build"
BUILD_SA=shop-build@$PROJECT_ID.iam.gserviceaccount.com
gcloud projects add-iam-policy-binding $PROJECT_ID \
  --member=serviceAccount:$BUILD_SA --role=roles/run.builder --condition=None
sleep 60   # chờ quyền IAM có hiệu lực
```

## 5. Build và deploy (5–8 phút lần đầu)

```bash
gcloud run deploy shop --source . --region=$REGION --allow-unauthenticated \
  --service-account=$SA \
  --build-service-account=projects/$PROJECT_ID/serviceAccounts/$BUILD_SA \
  --memory=1Gi --cpu=1 --cpu-boost --min-instances=0 --max-instances=1 \
  --add-cloudsql-instances=$PROJECT_ID:$REGION:$INSTANCE \
  --set-env-vars=SPRING_PROFILES_ACTIVE=prod,DB_INSTANCE=$PROJECT_ID:$REGION:$INSTANCE \
  --set-secrets=DB_PASSWORD=shop-db-password:latest,ADMIN_PASSWORD=shop-admin-password:latest
```

Kết thúc, lệnh in ra `Service URL: https://shop-xxxxx.asia-southeast1.run.app`.
Lần đầu mở sẽ chậm khoảng 15–30 giây (container khởi động, tạo bảng và dữ liệu mẫu).

- `--max-instances=1`: phiên đăng nhập lưu trong bộ nhớ, chỉ chạy 1 container để không mất phiên.
- `--min-instances=0`: không có người truy cập thì container tắt, gần như không tốn tiền Cloud Run.

## 6. Bucket Cloud Storage cho ảnh sản phẩm tải lên

Ảnh admin tải lên được lưu trong bucket riêng tư (không mở công khai); ứng dụng tự đọc
và trả ảnh qua đường dẫn `/media/...`. Chỉ service account `shop-run` được ghi/đọc bucket.

```bash
BUCKET=$PROJECT_ID-shop-images
gcloud storage buckets create gs://$BUCKET --location=$REGION \
  --uniform-bucket-level-access --public-access-prevention
gcloud storage buckets add-iam-policy-binding gs://$BUCKET \
  --member=serviceAccount:$SA --role=roles/storage.objectAdmin
gcloud run services update shop --region=$REGION --update-env-vars=GCS_BUCKET=$BUCKET
```

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
