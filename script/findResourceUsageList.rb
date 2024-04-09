
def get_line_number_by_file_name_and_search_text(full_file_name, search_text)
    hit_lines = []
    File.open(full_file_name, "r") do |file|
        line_number = 1
        file.each_line do |line|
            if line.include?(search_text)
                hit_lines.append(line_number)
            end
            line_number += 1
        end
    end
    return hit_lines
end

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
    # 特定のテキストを含むファイルの名前を格納する配列を初期化
    files_with_text = []

    # 指定したディレクトリ内のファイルを走査して特定のテキストを含むファイルを検索
    Dir.glob("**/*").each do |file_name|
        next if file_name.include?("build/")
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
def get_additional_row_list(diff_lines)
    additional_row_list = []
    diff_lines.each do |line|
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
def create_string_res_usage_list_message(diff_lines:)
    additional_row_list = get_additional_row_list(diff_lines)
    message_text = ""
    additional_row_list.each do |additional_row_text|
        # リソース名取得
        string_res_name = get_resource_name(additional_row_text)
        # リソース名を出力に加える
        message_text += "- `" + string_res_name + "`\n"
        # Stringリソース使用ファイル一覧を取得
        full_file_name_list = find_string_res_usage_file_name_list(string_res_name)
        # ファイル一覧も出力に加える
        message_text += full_file_name_list.map { |full_file_name| "  - " + full_file_name + " :#{get_line_number_by_file_name_and_search_text(full_file_name, string_res_name)}\n" }.join
    end
    return message_text
end

# ファイル名からリソース名部分を抽出
def get_res_name_by_full_file_name(full_file_name:)
    # xxx.xml, xxx.pngなど
    match = full_file_name.match(/\w+\..+$/)
    # リソース名抽出
    return match[0].sub("/", "").sub(/\..+$/, "")
end

def find_file_name_list(drawable_res_name:)
    res_use_file_name_list1 = find_file_names_include("R.drawable.#{drawable_res_name}")
    res_use_file_name_list2 = find_file_names_include("@drawable/#{drawable_res_name}")
    return res_use_file_name_list1 + res_use_file_name_list2
end

# リソース使用箇所の一覧を表示する
def show_res_usage_message(git)
    # Pull Request内のファイル変更を取得
    changed_files = git.modified_files + git.added_files

    # Stringリソースの変更をチェック
    strings_xml_file_name_list = changed_files.filter_map { |full_file_name| full_file_name if full_file_name.include?("res/values/strings.xml") }
    strings_xml_file_name_list.each do |full_file_name|
        # 変更行の一覧を取得
        diff = git.diff_for_file(full_file_name)
        # 変更行がある場合にのみコメントを出力
        if diff
            message_text = "<b>Stringリソース(#{full_file_name})の影響範囲</b>\n"
            message_text += create_string_res_usage_list_message(diff_lines: diff.patch.lines)
            # danger出力
            message(message_text)
        end
    end
    # Drawableリソースの変更をチェック
    drawable_message_text = "<b>Drawableリソースの影響範囲</b>\n"
    drawable_file_name_list = changed_files.filter_map { |full_file_name| full_file_name if full_file_name.include?("res/drawable") }
    drawable_file_name_list.each do |full_file_name|
        # リソース名抽出
        res_name = get_res_name_by_full_file_name(full_file_name: full_file_name)
        drawable_message_text += "- `#{full_file_name}`\n"
        # リソース使用しているファイル一覧を取得する
        file_list = find_file_name_list(drawable_res_name: res_name)
        file_list.each do |file_name|
            drawable_message_text += "  - #{file_name}  :#{get_line_number_by_file_name_and_search_text(file_name, file_name)}\n"
        end
    end
    # danger出力
    message(drawable_message_text)
end