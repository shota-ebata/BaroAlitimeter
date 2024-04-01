# GitHub Actions の job のステータスを受け取る
job_status = ENV['JOB_STATUS']

# 追加・変更していないコードはコメント対象外とするか
github.dismiss_out_of_range_messages({
  error: false, # エラーは追加・変更していないコードでもコメント
  warning: false,
  message: false,
  markdown: false
})

# Android Lintの結果ファイルの解析とコメント
Dir.glob("app/build/reports/lint-results-debug.html").each { |report|
    android_lint.skip_gradle_task = true # すでにある結果ファイルを利用する
    android_lint.report_file = report.to_s
    android_lint.filtering = false # エラーは追加・変更したファイルでなくてもコメント
    android_lint.lint(inline_mode: true) # コードにインラインでコメントする
}

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

require_relative "findResourceUsageList"


# strings.xmlのパス
STRINGS_XML_PATH = "app/src/main/res/values/strings.xml"

# Pull Request内のファイル変更を取得
changed_files = git.modified_files + git.added_files

file_name = STRINGS_XML_PATH

# strings.xmlが変更されたかチェックし、コメントを追加
if changed_files.include?(file_name)
    # 変更行の一覧を取得
    diff = git.diff_for_file(file_name)
    # 変更行がある場合にのみコメントを出力
    if diff
#         diff.patch.lines.map do |line|
#             next unless line.match(/^\+{1}[ ].+/)
#         end
        message_text = "Stringリソース使用箇所\n"
        diff.patch.lines.each do |line|
            # 差分から追加行を検索
            if line.match(/^\+{1}[ ].+/)
                line_text = line.sub("+ ", "")
                # Stringリソース名取得
                res_text = line_text.match(/<.+ name=".+">.+<\/.+>/)[0]
                file_list_text = get_string_res_usage_file_list(res_text)
                line_number = -1
                # リソース名を利用している場所を検索する
#                 File.open(file_name, "r") do |file|
#                     line_number = get_line_number(file, line_text)
#                 end
#                 message(message_text_list.join, file: file_name, line: line_number)
                message_text += file_list_text
            end
        end
        message(message_text)
    end
end

# Danger でエラーがある場合は既に何かしらコメントされているのでここで終了
return unless status_report[:errors].empty?

# GitHub Actions のワークフローのどこかでエラーがあった場合はその旨をコメントして終了
return markdown ':heavy_exclamation_mark:Pull request check failed.' if job_status != 'success'

# ktlint と Android Lint のワーニング数の合計をレポート
markdown ":heavy_check_mark:Pull request check passed. (But **#{lint_warning_count}** warnings reported by Android Lint and ktlint.)" if lint_warning_count != 0
