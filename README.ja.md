# darzana

darzana (ダルシャナ) は、Web APIをデータソースとしたWebアプリケーションを手軽に作る
ためのフレームワークです。

## アーキテクチャ

APIを組み合わせて、Webアプリケーションを作るために、darzana は以下の機能を持ちます。

* ルーティング
* APIコール
** APIの並列実行
** APIレスポンスのキャッシュ
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
      (call-api [ hotpepper/groups ])
      (render "groups/list"))

第一引数はHTTPメソッド名、第二引数はURLになります。すなわちHTTPリクエストがこの
メソッドとURLにマッチすると、以降で定義した処理が実行されます。

## インタフェース仕様

### (call-api [apis])

APIを実行します。実行結果はPageスコープに格納されます。

### (render template-path)

テンプレートを使ってレンダリングします。

### (redirect url)

urlにリダイレクトします。

### (if-success success-expr failure-expr)

この時点でエラースコープが空であれば、success-exprを実行し、そうでなければfailure-exprを実行します。

## 変数のスコープ

darzana で使える変数には以下の種類のスコープを持ちます。

Handlebars からこれらの変数にアクセスする際には、スコープがマージされるのでどの変数が
どのスコープに格納されているかを意識することなくテンプレートを記述できます。

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



