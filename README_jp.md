
# NovelAI_input_support_android

## オーバービュー

このツール群は実際に動くもので Google play に登録されています

## Google play へのリンクは以下になります
https://play.google.com/store/apps/details?id=jp.ne.ruru.park.ando.naiview

## このプライバシーポリシー
- フリーです (GNU LESSER GENERAL PUBLIC LICENSE)
    - このソフトウェアの使用はご自身の責任において使用してください
- Novel AI の REST API を使用しています
    - https://api.novelai.net/docs/
- NOVEL AI に登録した e-メールアドレスとパスワードを使用しています
    - https://novelai.net/
- 登録した e-mail とパスワードは api.novelai.net に暗号化して送信しています
- 画像への読み書きを行っています
- 設定データの読み書きを行っています
- カメラは使用していません
- アプリ上に広告は掲載していません(今後は載せるかも)
- 上記以外の個人情報は使用しておりません

以上、Google play に書けと言われたプライバシーポリシーでした。

## カンタンな使い方

- Novel AI でアカウントを作成します
- トップ画面の「設定」ボタンをクリックして表示される「NAIの設定」から「メール」と「パスワード」を入力します
- 以下のプロンプト用データをダウンロードします
  - https://github.com/Tand0/NovelAI_input_support_android/blob/main/data_jp.json
- トップ画面の「ツリー」をクリックして表示される「ツリー」から「ロード」ボタンを押して data_jp.json を読み込みます
  - グレーアウト(ignore)されている項目は右側のチェックボタンで解除できます
- トップ画面の「画像」をクリックしてデフォルトの画面表示を出します
- タップするとメニューが出てくるので「ツリーの使用」をONに変更します
- 「/ai/generate-image 」 を押してしばらく待ちます
- 気に入ったら「/ai/upscale」を押します
- キャンセルで画面に戻ると左右スクロールで前後の画像が、上下スクロールで画像のリストがでます
- 画像がNovelAIの画像の場合、設定を読み込んでプロンプトに渡します
- Enjoy!

## Reference
- ウィンドウズ用のツールも用意しています
  - https://github.com/Tand0/NovelAI_input_support_tool

