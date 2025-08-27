# Spring Batch Demo Application

## 概要

Spring Batch 5を使用したエンタープライズグレードのバッチ処理アプリケーションです。  
高度なエラーハンドリング、並列処理、監視機能を備え、本番環境での使用を想定した堅牢な設計となっています。

## 主な機能

### 基本機能
- **CSV→DB インポート**: CSVファイルからデータベースへのデータ登録
- **DB→CSV エクスポート**: データベースからCSVファイルへのデータ出力
- **2つの処理モデル**: チャンクモデルとタスクレットモデルの両方で実装
- **JasperReports統合**: PDF/Excel形式のレポート生成
- **MyBatis3統合**: SQLマッピングフレームワークによる効率的なDB操作

### エンタープライズ機能

#### 🔒 堅牢性とエラー処理
- **引数妥当性チェック**: ジョブパラメータの厳密な検証とセキュリティ対策
- **入力データバリデーション**: Bean Validationによる自動検証とカスタムルール
- **例外ハンドリング**: スキップ/リトライ可能な例外の自動分類と処理
- **エラーログ記録**: 問題のあるレコードの詳細記録と分析

#### 🔄 リトライとリカバリ（Spring Retry統合）
- **自動リトライ**: 一時的なエラーの自動再試行
- **指数バックオフ**: 段階的な待機時間による負荷軽減
- **カスタムリトライポリシー**: 例外タイプごとの細かな制御

#### 🎯 フロー制御
- **条件分岐フロー**: 実行結果に基づく動的な処理フロー
- **エラー率監視**: スキップ率による自動的なエラー処理への移行
- **データ量適応**: データサイズに応じた処理方式の自動選択

#### ⚡ 高性能並列処理
- **パーティション処理**: データの分割による並列実行
- **マルチスレッド処理**: チャンク単位での同時処理
- **非同期処理**: 独立ステップの並行実行
- **動的スレッドプール管理**: 負荷に応じた自動調整

#### 📊 監視とメトリクス（Micrometer/Prometheus統合）
- **リアルタイムメトリクス**: 処理速度、成功/失敗率の可視化
- **パフォーマンス分析**: スループット、レイテンシの測定
- **Prometheusエクスポート**: 外部監視ツールとの連携
- **Spring Boot Actuator**: ヘルスチェックと運用管理

#### 🔁 再実行とステート管理
- **再起動可能ジョブ**: 中断箇所からの自動再開
- **冪等性保証**: 重複実行の防止とデータ整合性
- **チェックポイント機能**: 段階的な進捗保存
- **ステート永続化**: 実行状態の信頼性の高い管理

## 技術スタック

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Batch 5**
- **Spring Retry** (リトライ機能)
- **MyBatis 3.0.3** (データアクセス)
- **H2 Database** (開発用インメモリDB)
- **Micrometer** (メトリクス収集)
- **Prometheus** (メトリクス可視化)
- **JasperReports 6.21.2** (レポート生成)
- **OpenCSV 5.9** (CSV処理)
- **Jakarta Validation** (データ検証)
- **Lombok**
- **Gradle**

## プロジェクト構造

```
spring-batch-demo/
├── src/main/java/com/example/batch/
│   ├── config/          # 設定クラス
│   │   ├── BatchConfig.java         # バッチ基本設定
│   │   ├── RetryConfig.java         # リトライ設定
│   │   ├── TaskExecutorConfig.java  # スレッドプール設定
│   │   └── MetricsConfig.java       # メトリクス設定
│   ├── decider/         # フロー制御
│   │   └── JobFlowDecider.java      # 条件分岐ロジック
│   ├── dto/             # データ転送オブジェクト
│   │   └── CsvEmployee.java         # バリデーション付きDTO
│   ├── entity/          # エンティティクラス
│   ├── exception/       # カスタム例外
│   │   ├── BusinessException.java
│   │   └── DataValidationException.java
│   ├── job/             # ジョブ設定
│   │   ├── ConditionalFlowJobConfig.java    # 条件分岐ジョブ
│   │   ├── ParallelProcessingJobConfig.java # 並列処理ジョブ
│   │   └── RestartableJobConfig.java        # 再起動可能ジョブ
│   ├── listener/        # リスナー
│   │   ├── SkipListener.java        # スキップ処理監視
│   │   └── RetryListener.java       # リトライ処理監視
│   ├── mapper/          # MyBatis Mapperインターフェース
│   ├── metrics/         # メトリクス収集
│   │   └── BatchMetricsListener.java
│   ├── partitioner/     # パーティション処理
│   │   └── RangePartitioner.java
│   ├── processor/       # データ処理プロセッサー
│   ├── runner/          # ジョブ実行用ランナー
│   ├── tasklet/         # タスクレット実装
│   └── validator/       # バリデーター
│       ├── InputDataValidator.java
│       └── JobParametersValidator.java
├── src/main/resources/
│   ├── mapper/          # MyBatis XMLマッパーファイル
│   ├── data/
│   │   ├── input/       # 入力CSVファイル
│   │   └── output/      # 出力CSVファイル
│   ├── reports/         # JasperReportsテンプレート
│   ├── application.yml  # アプリケーション設定
│   ├── schema.sql       # DBスキーマ定義
│   └── data.sql         # 初期データ
├── src/test/           # テストコード
│   └── java/com/example/batch/
│       ├── validator/   # バリデーターテスト
│       └── job/         # ジョブ統合テスト
└── build.gradle         # Gradle設定ファイル
```

## データモデル

### 1. 従業員 (Employee)
- 従業員情報を管理するテーブル
- フィールド: 従業員コード、氏名、メール、部署、役職、給与、入社日など

### 2. 商品 (Product)
- 商品情報を管理するテーブル
- フィールド: 商品コード、商品名、カテゴリ、価格、在庫数、説明など

### 3. 売上 (Sales)
- 売上情報を管理するテーブル
- フィールド: 取引ID、商品コード、顧客名、数量、単価、合計金額など

## セットアップ

### 前提条件

- Java 17以上
- Gradle 7.6以上

### ビルド方法

```bash
# プロジェクトのビルド
./gradlew build

# テストを含むビルド
./gradlew clean build
```

## 実行方法

### アプリケーション起動（ジョブ一覧表示）

```bash
./gradlew bootRun
```

### 新機能ジョブの実行

#### エンタープライズ機能付きジョブ

```bash
# 再起動可能ジョブ（失敗時に中断箇所から再開可能）
./gradlew bootRun --args="--spring.batch.job.names=restartableJob --inputFile=input/employees.csv --outputFile=output/result.csv"

# 条件分岐フロージョブ（データ量やエラー率に応じて処理を分岐）
./gradlew bootRun --args="--spring.batch.job.names=conditionalFlowJob --inputFile=input/employees.csv --outputFile=output/result.csv"

# 並列処理ジョブ（パーティション分割による高速処理）
./gradlew bootRun --args="--spring.batch.job.names=parallelProcessingJob --inputFile=input/employees.csv --outputFile=output/result.csv"

# マルチスレッドジョブ（チャンク単位での並列実行）
./gradlew bootRun --args="--spring.batch.job.names=multiThreadedJob --inputFile=input/employees.csv --outputFile=output/result.csv"

# 非同期ジョブ（複数ステップの同時実行）
./gradlew bootRun --args="--spring.batch.job.names=asyncJob"

# 冪等性保証ジョブ（重複実行防止）
./gradlew bootRun --args="--spring.batch.job.names=idempotentJob --inputFile=input/employees.csv --outputFile=output/result.csv"

# チェックポイントジョブ（段階的な進捗保存）
./gradlew bootRun --args="--spring.batch.job.names=checkpointJob"
```

### 従来のジョブ実行例

#### 1. CSV→DB インポート（チャンクモデル）

```bash
# 従業員データのインポート
./gradlew bootRun --args="employeeCsvToDbChunkJob input.file.path=src/main/resources/data/input/employees.csv"

# 商品データのインポート
./gradlew bootRun --args="productCsvToDbChunkJob input.file.path=src/main/resources/data/input/products.csv"

# 売上データのインポート
./gradlew bootRun --args="salesCsvToDbChunkJob input.file.path=src/main/resources/data/input/sales.csv"
```

#### 2. CSV→DB インポート（タスクレット）

```bash
# 従業員データのインポート
./gradlew bootRun --args="employeeCsvToDbTaskletJob input.file.path=src/main/resources/data/input/employees.csv"

# 商品データのインポート
./gradlew bootRun --args="productCsvToDbTaskletJob input.file.path=src/main/resources/data/input/products.csv"

# 売上データのインポート
./gradlew bootRun --args="salesCsvToDbTaskletJob input.file.path=src/main/resources/data/input/sales.csv"
```

#### 3. DB→CSV エクスポート（チャンクモデル）

```bash
# 従業員データのエクスポート
./gradlew bootRun --args="employeeDbToCsvChunkJob output.file.path=src/main/resources/data/output/employees_export.csv"

# 商品データのエクスポート
./gradlew bootRun --args="productDbToCsvChunkJob output.file.path=src/main/resources/data/output/products_export.csv"

# 売上データのエクスポート
./gradlew bootRun --args="salesDbToCsvChunkJob output.file.path=src/main/resources/data/output/sales_export.csv"
```

#### 4. DB→CSV エクスポート（タスクレット）

```bash
# 従業員データのエクスポート
./gradlew bootRun --args="employeeDbToCsvTaskletJob output.file.path=src/main/resources/data/output/employees_export.csv"

# 商品データのエクスポート
./gradlew bootRun --args="productDbToCsvTaskletJob output.file.path=src/main/resources/data/output/products_export.csv"

# 売上データのエクスポート
./gradlew bootRun --args="salesDbToCsvTaskletJob output.file.path=src/main/resources/data/output/sales_export.csv"
```

## 利用可能なジョブ一覧

### エンタープライズ機能ジョブ

| ジョブ名 | 処理内容 | 主要機能 |
|---------|----------|----------|
| restartableJob | 再起動可能ジョブ | 中断箇所から再開、エラーハンドリング、バリデーション |
| idempotentJob | 冪等性保証ジョブ | 重複実行防止、UPSERT操作 |
| checkpointJob | チェックポイントジョブ | 段階的進捗保存、ステート管理 |
| parallelProcessingJob | パーティション並列処理 | データ分割、並列実行、高速処理 |
| multiThreadedJob | マルチスレッド処理 | チャンク並列、スロットリング |
| asyncJob | 非同期処理 | ステップ並行実行、独立処理 |
| conditionalFlowJob | 条件分岐フロー | 動的フロー制御、エラー率監視 |

### 基本ジョブ

| ジョブ名 | 処理内容 | モデル |
|---------|----------|--------|
| employeeCsvToDbChunkJob | 従業員CSV→DB | チャンク |
| employeeCsvToDbTaskletJob | 従業員CSV→DB | タスクレット |
| employeeDbToCsvChunkJob | 従業員DB→CSV | チャンク |
| employeeDbToCsvTaskletJob | 従業員DB→CSV | タスクレット |
| productCsvToDbChunkJob | 商品CSV→DB | チャンク |
| productCsvToDbTaskletJob | 商品CSV→DB | タスクレット |
| productDbToCsvChunkJob | 商品DB→CSV | チャンク |
| productDbToCsvTaskletJob | 商品DB→CSV | タスクレット |
| salesCsvToDbChunkJob | 売上CSV→DB | チャンク |
| salesCsvToDbTaskletJob | 売上CSV→DB | タスクレット |
| salesDbToCsvChunkJob | 売上DB→CSV | チャンク |
| salesDbToCsvTaskletJob | 売上DB→CSV | タスクレット |

## チャンクモデル vs タスクレットモデル

### チャンクモデル
- **特徴**: データを指定されたサイズのチャンクに分割して処理
- **利点**: 
  - メモリ効率が良い（大量データ処理に適している）
  - トランザクション制御が細かくできる
  - エラー時の再処理が容易
- **用途**: 大量データの定期バッチ処理

### タスクレットモデル
- **特徴**: 一つのタスクとして一括処理
- **利点**: 
  - シンプルな実装
  - 小〜中規模データの処理に適している
  - 複雑なビジネスロジックの実装が容易
- **用途**: データ量が少ない処理、複雑な処理フロー

## 設定ファイル

### application.yml

主要な設定項目：

```yaml
app:
  batch:
    chunk-size: 100        # チャンクサイズ
    page-size: 100         # ページサイズ
    csv:
      input-path: src/main/resources/data/input/
      output-path: src/main/resources/data/output/
      encoding: UTF-8
```

## H2データベースコンソール

開発環境では、H2データベースのWebコンソールが利用可能です。

- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: (空)

## データバリデーション

### 従業員データ（CsvEmployee）のバリデーションルール

| フィールド | バリデーション | エラーメッセージ |
|-----------|---------------|------------------|
| 従業員コード | 必須、形式: EMP + 6桁数字 | 形式不正（例：EMP000001） |
| 名前 | 必須、最大50文字 | 必須入力/文字数超過 |
| メール | 必須、Email形式 | メールアドレス形式不正 |
| 部署 | 必須、指定値のみ | 営業部/開発部/人事部/経理部/総務部のみ |
| 給与 | 必須、0より大 | 正の数値を入力 |
| 入社日 | 必須、過去日付のみ | 未来の日付は指定不可 |
| ステータス | 指定値のみ | ACTIVE/INACTIVE/SUSPENDEDのみ |

## CSVファイル形式

### 従業員CSV
```csv
employee_code,first_name,last_name,email,department,position,salary,hire_date,status
EMP000001,Taro,Yamada,taro.yamada@example.com,開発部,Senior Engineer,5000000.00,2020-04-01,ACTIVE
```

### 商品CSV
```csv
product_code,product_name,category,price,stock_quantity,description,manufacturer,release_date,is_active
PRD001,Laptop Pro 15,Electronics,1500.00,50,High-performance laptop,TechCorp,2023-01-15,true
```

### 売上CSV
```csv
transaction_id,product_code,customer_name,quantity,unit_price,total_amount,sale_date,payment_method,status
TRX001,PRD001,Customer A,2,1500.00,3000.00,2024-01-15 10:30:00,CREDIT_CARD,COMPLETED
```

## 監視とメトリクス

### Actuatorエンドポイント

```bash
# ヘルスチェック
curl http://localhost:8080/actuator/health

# メトリクス一覧
curl http://localhost:8080/actuator/metrics

# 特定メトリクスの詳細
curl http://localhost:8080/actuator/metrics/batch.job.completed

# Prometheusフォーマット
curl http://localhost:8080/actuator/prometheus
```

### 主要メトリクス

| メトリクス名 | 説明 | 用途 |
|-------------|------|------|
| batch.job.started | 開始されたジョブ数 | ジョブ実行頻度の監視 |
| batch.job.completed | 正常完了したジョブ数 | 成功率の計算 |
| batch.job.failed | 失敗したジョブ数 | エラー率の監視 |
| batch.job.duration | ジョブ実行時間 | パフォーマンス分析 |
| batch.step.throughput | ステップのスループット | 処理速度の測定 |
| batch.item.read | 読み込まれたアイテム数 | 入力データ量の追跡 |
| batch.item.processed | 処理されたアイテム数 | 処理効率の分析 |
| batch.item.written | 書き込まれたアイテム数 | 出力データ量の確認 |
| batch.item.skipped | スキップされたアイテム数 | エラー率の監視 |

## エラー処理設定

### リトライ設定

| 設定項目 | デフォルト値 | 説明 |
|---------|------------|------|
| 最大リトライ回数 | 3回 | 一時的エラーの再試行上限 |
| 初期待機時間 | 1秒 | 最初のリトライまでの待機 |
| バックオフ乗数 | 2倍 | 待機時間の増加率 |
| 最大待機時間 | 10秒 | リトライ間隔の上限 |

### スキップ設定

| 例外タイプ | スキップ可否 | 説明 |
|-----------|------------|------|
| ValidationException | 可 | データ検証エラー |
| IllegalArgumentException | 可 | 不正な引数 |
| TransientDataAccessException | 不可（リトライ） | 一時的なDB接続エラー |
| BusinessException | 条件付き | retryableフラグによる |

### エラーログ

エラーレコードは `output/error_records.log` に以下の形式で記録されます：

```
[タイプ] [タイムスタンプ] 詳細情報
READ_ERROR [2024-01-01 10:00:00] Line: 10, Input: ..., Error: ...
VALIDATION_ERROR [2024-01-01 10:00:01] Item: ..., Error: ...
WRITE_ERROR [2024-01-01 10:00:02] Item: ..., Error: ...
```

## ベストプラクティス

### パフォーマンスチューニング

1. **チャンクサイズ**: 
   - 小規模データ: 50-100
   - 中規模データ: 100-500
   - 大規模データ: 500-1000

2. **スレッドプール設定**:
   - コアプールサイズ: CPU数の2倍
   - 最大プールサイズ: CPU数の4倍
   - キューサイズ: チャンクサイズ × 2

3. **パーティション数**:
   - データ量 / 10000 を目安に設定
   - 最大値はCPU数の2倍程度

### セキュリティ対策

1. **入力検証**: すべての入力データをバリデーション
2. **パストラバーサル防止**: ファイルパスの厳密なチェック
3. **SQLインジェクション対策**: MyBatisのパラメータバインディング使用
4. **機密情報**: ログやエラーファイルに個人情報を出力しない

## トラブルシューティング

### よくある問題と解決方法

1. **OutOfMemoryError**
   ```bash
   # JVMヒープサイズを増やす
   ./gradlew bootRun -Dspring-boot.run.jvmArguments="-Xmx2048m -Xms1024m"
   ```
   - チャンクサイズを小さくする（application.ymlで調整）

2. **ジョブが再起動できない**
   ```sql
   -- H2コンソールで実行
   DELETE FROM BATCH_JOB_EXECUTION WHERE JOB_INSTANCE_ID = ?;
   DELETE FROM BATCH_JOB_INSTANCE WHERE JOB_INSTANCE_ID = ?;
   ```

3. **デッドロックが発生する**
   - パーティション数を減らす
   - トランザクション分離レベルを調整
   - インデックスを最適化

4. **CSVファイルの文字化け**
   - ファイルエンコーディングをUTF-8に統一
   - application.ymlでエンコーディングを指定

5. **バリデーションエラーが多発する**
   - error_records.logを確認
   - スキップ上限値を調整
   - 入力データの品質を改善

## テスト

### 単体テストの実行

```bash
# 全テストの実行
./gradlew test

# 特定のテストクラスの実行
./gradlew test --tests JobParametersValidatorTest
./gradlew test --tests RestartableJobConfigTest
```

### 実装済みテスト

- **JobParametersValidatorTest**: ジョブパラメータのバリデーションテスト
  - 必須パラメータチェック
  - 形式バリデーション
  - セキュリティテスト（パストラバーサル）
  
- **RestartableJobConfigTest**: ジョブ機能の統合テスト
  - ジョブ再起動テスト
  - チェックポイント機能テスト
  - 冪等性保証テスト

## 本番環境への移行

### 推奨事項

1. **データベース**: H2からPostgreSQL/MySQL/Oracleへの移行
2. **メトリクス収集**: PrometheusとGrafanaによる監視ダッシュボード構築
3. **ログ管理**: ELKスタック（Elasticsearch, Logstash, Kibana）の導入
4. **スケジューラー**: Spring Cloud Data Flowまたはジョブスケジューラーの導入
5. **高可用性**: クラスタリング構成、ロードバランサーの設定
6. **セキュリティ**: Spring Security統合、暗号化、監査ログ

## 参考資料

- [Spring Batch 5公式ドキュメント](https://docs.spring.io/spring-batch/docs/5.0.x/reference/html/)
- [Spring Retry公式ドキュメント](https://github.com/spring-projects/spring-retry)
- [Micrometer公式ドキュメント](https://micrometer.io/docs)
- [MyBatis公式ドキュメント](https://mybatis.org/mybatis-3/ja/)
- [TERASOLUNA Batch Framework for Java (5.x) Development Guideline](https://terasoluna-batch.github.io/guideline/5.0.0.RELEASE/ja/)

## コントリビューション

プルリクエストを歓迎します。大きな変更の場合は、まずissueを開いて変更内容を議論してください。

## ライセンス

このプロジェクトはデモ・学習用途のサンプルアプリケーションです。商用利用の際は適切なライセンスを適用してください。