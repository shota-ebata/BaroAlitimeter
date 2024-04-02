
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
   search_dir = ""

   # 特定のテキストを含むファイルの名前を格納する配列を初期化
   files_with_text = []

   # 指定したディレクトリ内のファイルを走査して特定のテキストを含むファイルを検索
   Dir.glob("**/*").each do |file_name|
     next unless file_name.match(/.+build.+/)
     next unless File.file?(file_name)

     # ファイルを開いてテキストを検索
     if File.read(file_name).include?(search_text)
       files_with_text << file_name
     end
   end
   return files_with_text
end


def find_string_res_usage_file_list_text(res_text)
    string_res_name = res_text.sub(/<.+ name="/, "").sub(/">.+<\/.+>/, "")
    res_use_file_name_list1 = find_file_names_include("R.string.#{string_res_name}")
    res_use_file_name_list2 = find_file_names_include("@string/#{string_res_name}")

    message_text_list = []
    message_text_list << "- `" + res_text + "`\n"
    message_text_list << res_use_file_name_list1.unshift("  - ").push("\n") if !res_use_file_name_list1.empty?
    message_text_list << res_use_file_name_list2.unshift("  - ").push("\n") if !res_use_file_name_list2.empty?
    return message_text_list.join
end

def find_string_res_usage_file_name_list(string_res_name)
    res_use_file_name_list1 = find_file_names_include("R.string.#{string_res_name}")
    res_use_file_name_list2 = find_file_names_include("@string/#{string_res_name}")
    return res_use_file_name_list1 + res_use_file_name_list2
end

# 差分から追加行だけを抽出
def get_additional_row_list(diff)
    additional_row_list = []
    diff.patch.lines.each do |line|
        # 差分から追加行だけを抽出
        if line.match(/^\+{1}[ ].+/)
            additional_row_list.append(line.sub("+ ", ""))
        end
    end
    return additional_row_list
end

# <xxx name="リソース名">リソース</xxx>形式のテキストからリソース名を抽出
def get_resource_name(text)
    # <xxx name="リソース名">リソース</>形式のテキストだけを抽出する
    match = text.match(/<.+ name=".+">.+<\/.+>/)
    return nil if !match
    res_text = match[0]
    # リソース名取得
    return res_text.sub(/<.+ name="/, "").sub(/">.+<\/.+>/, "")
end

# Stringリソース使用箇所一覧メッセージを作成
def create_string_res_usage_list_message(diff:)
    message_text = "Stringリソース使用箇所\n"
    additional_row_list = get_additional_row_list(diff)
    additional_row_list.each do |additional_row_text|
        # リソース名取得
        string_res_name = get_resource_name(additional_row_text)
        # リソース名を出力に加える
        message_text += "- `" + string_res_name + "`\n"
        # Stringリソース使用ファイル一覧を取得
        file_name_list = find_string_res_usage_file_name_list(string_res_name)
        # ファイル一覧も出力に加える
        message_text += file_name_list.map { |file_name| "  - " + file_name + "\n" }.join
    end
    return message_text
end