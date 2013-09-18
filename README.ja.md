# darzana

darzana (ダルシャナ) は、Web APIをデータソースとしたWebアプリケーションを手軽に作る
ためのフレームワークです。

## アーキテクチャ

APIを組み合わせて、Webアプリケーションを作るために、darzana は以下の機能を持ちます。

* ルーティング
* APIコール
  * APIの並列実行
  * APIレスポンスのキャッシュ
  * Content-Typeに応じたリクエスト/レスポンス (フォームURLエンコーディング, JSON, XML)
* テンプレートレンダリング (Handlebars を使ってレンダリングできます)

## Get started

アプリケーションの設定ファイルを書きます。

APIの設定はdefapiマクロを使って宣言的に書きます。

    (defapi gourmet
      (url "http://webservice.recruit.co.jp/hotpepper/gourmet/v1/")
      (query-keys :key :name :middle_area)
      (expire 300))

ルーティングのファイルを書きます。defmarga マクロを使って以下のように書きます。

    (defmarga GET "/groups" 
      (call-api [ app/groups ])
      (render "groups/list"))

第一引数はHTTPメソッド名、第二引数はURLになります。すなわちHTTPリクエストがこの
メソッドとURLにマッチすると、以降で定義した処理が実行されます。
ルーティングファイルはソースパス以外において、load-app-routes で読みこむようにしておくと、
http://[darzana-host]/router/reload でルーティングファイルの更新を再読み込みできるようになります。

Darzanaを使ったアプリケーションは、lein template を使って生成できます。
~/.lein/profiles.clj ファイルに以下のように、プラグイン設定を追加します。

    {:user {:plugins [[darzana/lein-template "0.1.0-SNAPSHOT"]]}}

その後、lein コマンドで

    lein new darzana [Your application]

Ringサーバを起動すると、Twitterのタイムラインを表示するサンプルアプリケーションが開きます。

    lein ring server

http://localhost:3000/admin/ が管理アプリケーションのURLです。ここでルーティングやテンプレートの編集ができます。

## インタフェース仕様

### API定義

#### (url APIのURL)

#### (query-keys & ks)

APIの検索クエリ

### コントローラコンポーネント

#### (call-api [apis])

APIを実行します。実行結果はPageスコープに格納されます。

#### (render template-path)

テンプレートを使ってレンダリングします。

#### (redirect url)

urlにリダイレクトします。

#### (if-success success-expr failure-expr)

この時点でエラースコープが空であれば、success-exprを実行し、そうでなければfailure-exprを実行します。

## 変数のスコープ

darzana で使える変数には以下の種類のスコープを持ちます。

Handlebars からこれらの変数にアクセスする際には、スコープがマージされるのでどの変数が
どのスコープに格納されているかを意識することなくテンプレートを記述できます。

スコープのマージは、

    application -> session -> params -> page -> error

の順に実行されます。すなわち、もし同名のキーが異なるスコープに存在するときは、
より後でマージされる方がセットされることになります。


### Application スコープ

アプリケーションのどこからでも共通に見える変数スコープです。
set-application-scope ファンクションを使って、このスコープを生成することができます。

    (set-application-scope {:api-key "xxxxxx"})

### Params スコープ

HTTPのクエリパラメータが格納されます。

### Page スコープ

APIを実行した結果が格納されます。

### Session スコープ

異なるHTTPリクエストにまたがって有効な変数スコープです。
J2EEのHttpSessionとほぼ同じと考えてください。

### Error スコープ

APIを実行した結果、エラーのレスポンスがあればここに入ります。

## Tutorial

### HandlebarsのHelperを追加したい

darzana.core/handlebars にHandlebarsエンジンのインスタンスが格納されています。
reify を使ってHelperクラスを実装し、darzana/handlebars に registerHelper しておくと、
テンプレートからこのHelperが利用できるようになります。

    (.registerHelper darzana.core/handlebars "hello"
      (reify com.github.jknack.handlebars/Helper
        (apply [this context options]
          (com.github.jknack.handlebars/Handlebars$SafeString.
            (str "Hello", options)))))



