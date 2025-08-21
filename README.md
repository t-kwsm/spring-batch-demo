# Spring Batch Demo Application

## 概要

Spring Batchを使用したCSVファイルとデータベース間のデータ処理を行うデモアプリケーションです。  
チャンクモデルとタスクレットモデルの両方のパターンで実装されており、MyBatis3を使用してデータベースアクセスを行います。

## 主な機能

- **CSV→DB インポート**: CSVファイルからデータベースへのデータ登録
- **DB→CSV エクスポート**: データベースからCSVファイルへのデータ出力
- **2つの処理モデル**: チャンクモデルとタスクレットモデルの両方で実装
- **柔軟なCSV形式対応**: OpenCSVを使用した多様なCSV形式のサポート
- **MyBatis3統合**: SQLマッピングフレームワークによる効率的なDB操作

## 技術スタック

- Java 17
- Spring Boot 3.2.0
- Spring Batch
- MyBatis 3.0.3
- H2 Database (インメモリDB)
- OpenCSV 5.9
- Lombok
- Gradle

## プロジェクト構造

```
spring-batch-demo/
├── src/main/java/com/example/batch/
│   ├── config/          # Spring Batch, MyBatis設定
│   ├── dto/             # CSV用DTOクラス
│   ├── entity/          # エンティティクラス
│   ├── job/             # ジョブ設定クラス
│   ├── listener/        # ジョブリスナー
│   ├── mapper/          # MyBatis Mapperインターフェース
│   ├── processor/       # データ処理プロセッサー
│   ├── runner/          # ジョブ実行用ランナー
│   └── tasklet/         # タスクレット実装クラス
├── src/main/resources/
│   ├── mapper/          # MyBatis XMLマッパーファイル
│   ├── data/
│   │   ├── input/       # 入力CSVファイル格納ディレクトリ
│   │   └── output/      # 出力CSVファイル格納ディレクトリ
│   ├── application.yml  # アプリケーション設定
│   ├── schema.sql       # DBスキーマ定義
│   └── data.sql         # 初期データ
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

### ジョブ実行例

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

## CSVファイル形式

### 従業員CSV
```csv
employee_code,first_name,last_name,email,department,position,salary,hire_date,status
EMP001,Taro,Yamada,taro.yamada@example.com,Engineering,Senior Engineer,80000.00,2020-04-01,ACTIVE
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

## ベストプラクティス

1. **チャンクサイズの調整**: データ量に応じて適切なチャンクサイズを設定
2. **エラーハンドリング**: Skip/Retry/Restart機能を活用
3. **パフォーマンスチューニング**: 
   - 適切なフェッチサイズの設定
   - バッチ更新の活用
   - インデックスの適切な配置
4. **ログ監視**: ジョブ実行状況の監視とログ分析
5. **テスト**: 単体テスト、統合テストの実装

## トラブルシューティング

### よくある問題と解決方法

1. **OutOfMemoryError**
   - チャンクサイズを小さくする
   - JVMのヒープサイズを増やす

2. **CSVファイルが見つからない**
   - ファイルパスを確認
   - ファイルの読み取り権限を確認

3. **文字化け**
   - CSVファイルのエンコーディングを確認（UTF-8推奨）

## 参考資料

- [Spring Batch公式ドキュメント](https://docs.spring.io/spring-batch/docs/current/reference/html/)
- [MyBatis公式ドキュメント](https://mybatis.org/mybatis-3/ja/)
- [TERASOLUNA Batch Framework for Java (5.x) Development Guideline](https://terasoluna-batch.github.io/guideline/5.0.0.RELEASE/ja/)

## ライセンス

このプロジェクトはデモ用途のサンプルアプリケーションです。