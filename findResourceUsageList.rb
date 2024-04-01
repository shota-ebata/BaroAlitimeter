
def get_line_number(file, search_text)
    line_number = 1
    file.each_line do |line|
        if line.include?(search_text)
            return line_number
        end
        line_number += 1
    end
    return -1
end


def find_file_names_include(search_text)
   # 検索するディレクトリを指定
   search_directory = "app/src"

   # 特定のテキストを含むファイルの名前を格納する配列を初期化
   files_with_text = []

   # 指定したディレクトリ内のファイルを走査して特定のテキストを含むファイルを検索
   Dir.glob("#{search_directory}/**/*").each do |file_name|
     next unless File.file?(file_name)

     # ファイルを開いてテキストを検索
     if File.read(file_name).include?(search_text)
       files_with_text << file_name
     end
   end
   return files_with_text
end

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
        diff.patch.lines.each do |line|
            # 差分から追加行を検索
            if line.match(/^\+{1}[ ].+/)
                line_text = line.sub("+ ", "")
                # Stringリソース名取得
                res_text = line_text.match(/<.+ name=".+">.+<\/.+>/)[0]
                string_res_name = res_text.sub(/<.+ name="/, "").sub(/">.+<\/.+>/, "")
                res_use_file_name_list = find_file_names_include("R.string.#{string_res_name}") + find_file_names_include("@string/#{string_res_name}")
                message_text_list = []
                message_text_list << "リソース使用箇所\n"
                message_text_list << res_use_file_name_list.unshift("- ").push("\n")
                line_number = -1
                # リソース名を利用している場所を検索する
                File.open(file_name, "r") do |file|
                    line_number = get_line_number(file, line_text)
                end
                message(message_text_list.join, file: file_name, line: line_number)
            end
        end
    end
end