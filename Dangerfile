message "call 1"

# GitHub Actions の job のステータスを受け取る
job_status = ENV['JOB_STATUS']

message "call 2"

# 追加・変更していないコードはコメント対象外とするか
github.dismiss_out_of_range_messages({
  error: false, # エラーは追加・変更していないコードでもコメント
  warning: true,
  message: true,
  markdown: true
})

message "call 3"
# Android Lintの結果ファイルの解析とコメント
android_lint.skip_gradle_task = true # すでにある結果ファイルを利用する
android_lint.report_file = "app/build/reports/lint-results-debug.html"
android_lint.filtering = false # エラーは追加・変更したファイルでなくてもコメント
android_lint.lint(inline_mode: true) # コードにインラインでコメントする

message "call 4"

# 最終結果でレポートするワーニング数は Android Lint と ktlint のみの合計としたいのでここで変数に保存
lint_warning_count = status_report[:warnings].count

# Sometimes it's a README fix, or something like that - which isn't relevant for
# including in a project's CHANGELOG for example
declared_trivial = github.pr_title.include? "#trivial"

# Make it more obvious that a PR is a work in progress and shouldn't be merged yet
warn("PR is classed as Work in Progress") if github.pr_title.include? "[WIP]"

# Warn when there is a big PR
warn("Big PR") if git.lines_of_code > 500

# Don't let testing shortcuts get into master by accident
fail("fdescribe left in tests") if `grep -r fdescribe specs/ `.length > 1
fail("fit left in tests") if `grep -r fit specs/ `.length > 1

# 追加で独自のチェックをする場合はこのあたりで実施する
# ...

# Danger でエラーがある場合は既に何かしらコメントされているのでここで終了
return unless status_report[:errors].empty?

# GitHub Actions のワークフローのどこかでエラーがあった場合はその旨をコメントして終了
return markdown ':heavy_exclamation_mark:Pull request check failed.' if job_status != 'success'

# ktlint と Android Lint のワーニング数の合計をレポート
markdown ":heavy_check_mark:Pull request check passed. (But **#{lint_warning_count}** warnings reported by Android Lint and ktlint.)" if lint_warning_count != 0